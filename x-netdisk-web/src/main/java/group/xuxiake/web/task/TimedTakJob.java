package group.xuxiake.web.task;

import group.xuxiake.web.handler.RecycleHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2019/1/18.
 * PS: Not easy to write code, please indicate.
 * Description：spring定时任务
 */
@Slf4j
@Component
public class TimedTakJob {

    @Resource
    private RecycleHandler recycleHandler;
    /**
     * 清理回收站的定时任务
     */
    public void recycleClearTimedTask() {
        log.debug("清理回收站的定时任务执行了");
        recycleHandler.init();
        recycleHandler.framework();
    }
}
