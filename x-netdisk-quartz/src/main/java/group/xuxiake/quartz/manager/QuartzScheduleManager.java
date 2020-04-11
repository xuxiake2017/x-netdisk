package group.xuxiake.quartz.manager;

import group.xuxiake.common.entity.FileRecycle;

/**
 * Author by xuxiake, Date on 2020/3/30 12:59.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public interface QuartzScheduleManager {

    /**
     * 创建删除回收站文件job
     * @param recycle
     */
    void createDelRecycleFleJob(FileRecycle recycle);

    /**
     * 删除删除回收站文件job
     * @param recycle
     */
    void delDelRecycleFleJob(FileRecycle recycle);
}
