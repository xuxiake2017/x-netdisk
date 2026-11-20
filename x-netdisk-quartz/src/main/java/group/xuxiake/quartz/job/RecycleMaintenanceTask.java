package group.xuxiake.quartz.job;

import com.github.tobato.fastdfs.service.FastFileStorageClient;
import group.xuxiake.common.entity.StorageDeleteTask;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.StorageDeleteTaskMapper;
import group.xuxiake.quartz.service.impl.RecyclePurgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 补偿未成功创建 Quartz Job 的过期回收站记录，并重试物理文件删除。
 */
@Slf4j
@Component
public class RecycleMaintenanceTask {

    private static final int BATCH_SIZE = 100;
    private static final long MAX_RETRY_DELAY = 60L * 60L * 1000L;

    @Resource
    private FileRecycleMapper fileRecycleMapper;
    @Resource
    private RecyclePurgeService recyclePurgeService;
    @Resource
    private StorageDeleteTaskMapper storageDeleteTaskMapper;
    @Resource
    private FastFileStorageClient storageClient;

    @Scheduled(fixedDelayString = "${application.recycle.reconcileInterval:60000}")
    public void purgeExpiredRecycleRecords() {
        List<Integer> recycleIds = fileRecycleMapper.findExpiredActiveIds(new Date(), BATCH_SIZE);
        for (Integer recycleId : recycleIds) {
            try {
                recyclePurgeService.purge(recycleId);
            } catch (RuntimeException e) {
                log.error("补偿清理回收站记录失败: {}", recycleId, e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${application.recycle.storageDeleteRetryInterval:60000}")
    public void retryStorageDeletes() {
        List<StorageDeleteTask> tasks = storageDeleteTaskMapper.findDueTasks(new Date(), BATCH_SIZE);
        for (StorageDeleteTask task : tasks) {
            Date now = new Date();
            if (storageDeleteTaskMapper.claim(task.getId(), now,
                    new Date(now.getTime() + 5L * 60L * 1000L)) != 1) {
                continue;
            }
            try {
                storageClient.deleteFile(task.getStoragePath());
                storageDeleteTaskMapper.deleteByPrimaryKey(task.getId());
            } catch (RuntimeException e) {
                int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
                long delay = Math.min(MAX_RETRY_DELAY, 60000L << Math.min(retryCount, 6));
                storageDeleteTaskMapper.markRetry(task.getId(), new Date(System.currentTimeMillis() + delay),
                        abbreviate(e.getMessage()));
                log.error("重试删除 FastDFS 文件失败: {}", task.getStoragePath(), e);
            }
        }
    }

    private String abbreviate(String message) {
        if (message == null) {
            return "未知错误";
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
