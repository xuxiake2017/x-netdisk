package group.xuxiake.quartz.service.impl;

import com.github.tobato.fastdfs.service.FastFileStorageClient;
import group.xuxiake.common.entity.FileRecycle;
import group.xuxiake.common.mapper.FileMediaMapper;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.FileShareMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.RecyclePurgeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class RecyclePurgeService {

    @Resource
    private FileRecycleMapper recycleMapper;
    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private FileShareMapper fileShareMapper;
    @Resource
    private FileMediaMapper fileMediaMapper;
    @Resource
    private FileOriginMapper fileOriginMapper;
    @Resource
    private FastFileStorageClient storageClient;

    @Transactional
    public void purge(Integer recycleId) {
        FileRecycle recycle = recycleMapper.selectByPrimaryKeyForUpdate(recycleId);
        if (recycle == null
                || !Integer.valueOf(NetdiskConstant.RECYCLE_STATUS_FILE_DEL).equals(recycle.getRecycleStatus())) {
            return;
        }
        List<String> storagePaths = RecyclePurgeHelper.purge(recycle.getRecycleId(), recycle.getFileId(),
                userFileMapper, fileShareMapper, fileMediaMapper, fileOriginMapper, recycleMapper);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String storagePath : storagePaths) {
                    try {
                        storageClient.deleteFile(storagePath);
                    } catch (RuntimeException e) {
                        log.error("删除 FastDFS 文件失败: {}", storagePath, e);
                    }
                }
            }
        });
    }
}
