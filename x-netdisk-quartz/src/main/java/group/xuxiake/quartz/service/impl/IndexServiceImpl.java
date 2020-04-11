package group.xuxiake.quartz.service.impl;

import group.xuxiake.common.entity.FileRecycle;
import group.xuxiake.common.entity.Result;
import group.xuxiake.quartz.manager.QuartzScheduleManager;
import group.xuxiake.quartz.service.IndexService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2020/3/30 10:15.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Service("indexService")
public class IndexServiceImpl implements IndexService {

    @Resource
    private QuartzScheduleManager quartzScheduleManager;

    /**
     * 删除文件（创建定时清理过期文件的任务）
     * @param recycle
     * @return
     */
    @Override
    public Result delFile(FileRecycle recycle) {

        quartzScheduleManager.createDelRecycleFleJob(recycle);
        return Result.SUCCESS;
    }

    /**
     * 删除定时清理过期文件的任务
     * @param recycle
     * @return
     */
    @Override
    public Result delJob(FileRecycle recycle) {

        quartzScheduleManager.delDelRecycleFleJob(recycle);
        return Result.SUCCESS;
    }
}
