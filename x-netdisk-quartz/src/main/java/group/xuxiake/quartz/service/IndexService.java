package group.xuxiake.quartz.service;

import group.xuxiake.common.entity.Recycle;
import group.xuxiake.common.entity.Result;

/**
 * Author by xuxiake, Date on 2020/3/30 10:15.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public interface IndexService {

    /**
     * 删除文件（创建定时清理过期文件的任务）
     * @param recycle
     * @return
     */
    Result delFile(Recycle recycle);

    /**
     * 删除定时清理过期文件的任务
     * @param recycle
     * @return
     */
    Result delJob(Recycle recycle);
}
