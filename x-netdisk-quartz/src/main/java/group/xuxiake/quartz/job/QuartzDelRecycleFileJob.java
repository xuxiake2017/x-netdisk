package group.xuxiake.quartz.job;

import com.google.gson.Gson;
import group.xuxiake.common.entity.FileRecycle;
import group.xuxiake.quartz.service.impl.RecyclePurgeService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * Author by xuxiake, Date on 2019/12/13.
 * PS: Not easy to write code, please indicate.
 * Description：删除回收站文件job
 */
@Slf4j
public class QuartzDelRecycleFileJob extends QuartzJobBean implements Serializable {

    @Resource
    private RecyclePurgeService recyclePurgeService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {

        log.info("QuartzDelFileJob executeInternal");
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        String data = (String) jobDetail.getJobDataMap().get("data");
        FileRecycle recycle = new Gson().fromJson(data, FileRecycle.class);
        recyclePurgeService.purge(recycle.getRecycleId());
    }
}
