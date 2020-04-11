package group.xuxiake.quartz.manager;

import com.google.gson.Gson;
import group.xuxiake.common.entity.FileRecycle;
import group.xuxiake.quartz.entity.ScheduleJob;
import group.xuxiake.quartz.job.QuartzDelRecycleFileJob;
import group.xuxiake.quartz.util.ScheduleUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2020/3/30 12:59.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Slf4j
@Component("quartzScheduleManager")
public class QuartzScheduleManagerImpl implements QuartzScheduleManager {

    public static final String TRIGGER_OF_DEL_RECYCLE_FILE = "trigger_del_recycle_file_";
    public static final String JOB_OF_DEL_RECYCLE_FILE = "job_del_recycle_file_";
    public static final String GROUP_OF_DEL_RECYCLE_FILE = "group_del_recycle_file";

    @Resource
    private ScheduleUtil scheduleUtil;
    @Resource
    private Scheduler scheduler;

    /**
     * 创建删除回收站文件job
     * @param recycle
     */
    @Override
    public void createDelRecycleFleJob(FileRecycle recycle) {

        try {
            String data = new Gson().toJson(recycle);
            JobDetail jobDetail = JobBuilder
                    .newJob(QuartzDelRecycleFileJob.class)
                    .withIdentity(JOB_OF_DEL_RECYCLE_FILE + recycle.getRecycleId(), GROUP_OF_DEL_RECYCLE_FILE)
                    .build();
            jobDetail.getJobDataMap().put("data", data);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TRIGGER_OF_DEL_RECYCLE_FILE + recycle.getRecycleId(), GROUP_OF_DEL_RECYCLE_FILE)
                    .startAt(recycle.getOverTime()).build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 删除 删除回收站文件job
     * @param recycle
     */
    @Override
    public void delDelRecycleFleJob(FileRecycle recycle) {

        ScheduleJob scheduleJob = new ScheduleJob();
        scheduleJob.setJobName(JOB_OF_DEL_RECYCLE_FILE + recycle.getRecycleId());
        scheduleJob.setJobGroup(GROUP_OF_DEL_RECYCLE_FILE);
        scheduleUtil.deleteJob(scheduleJob);
    }
}
