package group.xuxiake.quartz.util;

import group.xuxiake.common.exception.QuartzCustomException;
import group.xuxiake.quartz.entity.ScheduleJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * Author by xuxiake, Date on 2019/12/13.
 * PS: Not easy to write code, please indicate.
 * Description：quartz scheduleUtil
 */
@Slf4j
@Component("scheduleUtil")
public class ScheduleUtil {

    @Resource
    private Scheduler scheduler;

    /**
     * 获取 Trigger Key
     *
     * @param scheduleJob
     * @return
     */
    public TriggerKey getTriggerKey(ScheduleJob scheduleJob) {
        return TriggerKey.triggerKey(scheduleJob.getTriggerName(), scheduleJob.getTriggerGroup());
    }

    /**
     * 获取 Job Key
     *
     * @param scheduleJob
     * @return
     */
    public JobKey getJobKey(ScheduleJob scheduleJob) {
        return JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
    }

    /**
     * 获取 Cron Trigger
     *
     * @param scheduleJob
     * @return
     */
    public CronTrigger getCronTrigger(ScheduleJob scheduleJob) {
        try {
            return (CronTrigger) scheduler.getTrigger(getTriggerKey(scheduleJob));
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 设置job自定义参数
     * @param job
     * @param scheduleJob
     */
    private void putDataMap(JobDetail job, ScheduleJob scheduleJob) {

        // 将scheduler instanceName存入jobDataMap，方便业务job中进行数据库操作
        JobDataMap jobDataMap = job.getJobDataMap();

        Map<String, Object> jobDataParam = scheduleJob.getJobDataParam();
        if (jobDataParam == null || jobDataParam.isEmpty()) {
            return;
        }
        Set<Map.Entry<String, Object>> entries = jobDataParam.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            jobDataMap.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 创建任务
     *
     * @param scheduleJob
     */
    public void createScheduleJob(ScheduleJob scheduleJob) {

        validateCronExpression(scheduleJob);

        try {
            JobDetail jobDetail = JobBuilder.newJob(scheduleJob.getClazz())
                    .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                    .withDescription(scheduleJob.getDescription())
                    .build();

            this.putDataMap(jobDetail, scheduleJob);

            // 任务错过不做处理
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();

            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(scheduleJob.getTriggerName(), scheduleJob.getTriggerGroup())
                    .withDescription(scheduleJob.getDescription())
                    .withSchedule(scheduleBuilder)
                    .build();

            scheduler.scheduleJob(jobDetail, cronTrigger);

            log.info("Create schedule job {}-{} success", scheduleJob.getJobGroup(), scheduleJob.getJobName());
//            if (scheduleJob.isPause()) {
//                pauseJob(scheduleJob);
//            }

        } catch (Exception e) {
            log.error("Execute schedule job failed");
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 更新任务
     *
     * @param scheduleJob
     */
    public void updateScheduleJob(ScheduleJob scheduleJob) {

        validateCronExpression(scheduleJob);

        try {

            TriggerKey triggerKey = getTriggerKey(scheduleJob);

            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionFireAndProceed();

            CronTrigger cronTrigger = getCronTrigger(scheduleJob);

            cronTrigger = cronTrigger.getTriggerBuilder()
                    .withIdentity(triggerKey)
                    .withDescription(scheduleJob.getDescription())
                    .withSchedule(cronScheduleBuilder).build();

            scheduler.rescheduleJob(triggerKey, cronTrigger);

            log.info("Update schedule job {}-{} success", scheduleJob.getJobGroup(), scheduleJob.getJobName());

//            if (scheduleJob.isPause()) {
//                pauseJob(scheduleJob);
//            }
        } catch (SchedulerException e) {
            log.error("Update schedule job failed");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 执行任务
     *
     * @param scheduleJob
     */
    public void run(ScheduleJob scheduleJob, Boolean flag) {
        try {
            scheduler.triggerJob(getJobKey(scheduleJob));
            if (flag != null) {
                SchedulerContext context = scheduler.getContext();
                context.put(scheduleJob.getJobName(), flag);
            }
            log.info("Run schedule job {}-{} success", scheduleJob.getJobGroup(), scheduleJob.getJobName());
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            log.error("Run schedule job failed");
        }
    }

    /**
     * 暂停任务
     *
     * @param scheduleJob
     */
    public void pauseJob(ScheduleJob scheduleJob) {
        try {
            scheduler.pauseJob(getJobKey(scheduleJob));
            log.info("Pause schedule job {}-{} success", scheduleJob.getJobGroup(), scheduleJob.getJobName());
        } catch (SchedulerException e) {
            e.printStackTrace();
            log.error("Pause schedule job failed");
        }
    }

    /**
     * 继续执行任务
     *
     * @param scheduleJob
     */
    public void resumeJob(ScheduleJob scheduleJob) {
        try {
            scheduler.resumeJob(getJobKey(scheduleJob));
            log.info("Resume schedule job {}-{} success", scheduleJob.getJobGroup(), scheduleJob.getJobName());
        } catch (SchedulerException e) {
            log.error("Resume schedule job failed");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 删除任务
     *
     * @param scheduleJob
     */
    public void deleteJob(ScheduleJob scheduleJob) {
        try {
            scheduler.deleteJob(getJobKey(scheduleJob));
            log.info("Delete schedule job {}-{} success", scheduleJob.getJobGroup(), scheduleJob.getJobName());
        } catch (SchedulerException e) {
            log.error("Delete schedule job failed");
            log.error(e.getMessage(), e);
        }
    }
    /**
     * 校验Cron表达式
     */
    public void validateCronExpression(ScheduleJob scheduleJob) {
        if (!CronExpression.isValidExpression(scheduleJob.getCronExpression())) {
            throw new QuartzCustomException(String.format("Job %s expression %s is not correct!", scheduleJob.getClazz(), scheduleJob.getCronExpression()));
        }
    }
}
