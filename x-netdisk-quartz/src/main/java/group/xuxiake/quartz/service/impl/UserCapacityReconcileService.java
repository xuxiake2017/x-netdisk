package group.xuxiake.quartz.service.impl;

import group.xuxiake.common.entity.User;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserCapacityReconcileService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserFileMapper userFileMapper;

    @Transactional
    public void reconcile(Integer userId) {
        User user = userMapper.selectByPrimaryKeyForUpdate(userId);
        if (user == null) {
            return;
        }
        int brokenFiles = userFileMapper.countNormalFilesWithoutOrigin(userId);
        if (brokenFiles > 0) {
            log.error("用户容量对账跳过，存在缺失存储信息的正常文件: userId={}, count={}", userId, brokenFiles);
            return;
        }
        Long value = userFileMapper.sumNormalFileSizeByUserId(userId);
        long actualUsedMemory = value == null ? 0L : value;
        if (user.getUsedMemory() == null || user.getUsedMemory() != actualUsedMemory) {
            Long recorded = user.getUsedMemory();
            User update = new User();
            update.setId(userId);
            update.setUsedMemory(actualUsedMemory);
            userMapper.updateByPrimaryKeySelective(update);
            log.warn("已校准用户容量: userId={}, recorded={}, actual={}", userId, recorded, actualUsedMemory);
        }
    }
}
