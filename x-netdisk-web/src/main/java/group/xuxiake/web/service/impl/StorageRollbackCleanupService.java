package group.xuxiake.web.service.impl;

import group.xuxiake.common.mapper.StorageDeleteTaskMapper;
import group.xuxiake.web.util.FastDFSClientWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 在业务事务回滚后清理已经上传到 FastDFS 的文件。
 */
@Slf4j
@Service
public class StorageRollbackCleanupService {

    @Resource
    private FastDFSClientWrapper fastDFSClientWrapper;
    @Resource
    private StorageDeleteTaskMapper storageDeleteTaskMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteOrQueue(String storagePath) {
        try {
            fastDFSClientWrapper.deleteFile(storagePath);
        } catch (RuntimeException e) {
            storageDeleteTaskMapper.insertIgnore(storagePath);
            log.error("事务回滚后删除 FastDFS 文件失败，已加入重试队列: {}", storagePath, e);
        }
    }
}
