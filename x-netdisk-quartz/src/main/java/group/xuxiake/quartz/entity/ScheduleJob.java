package group.xuxiake.quartz.entity;

import lombok.Data;
import org.quartz.Job;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Author by xuxiake, Date on 2019/12/13.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class ScheduleJob implements Serializable {

    private Long id;

    private Class<? extends Job> clazz;

    private String cronExpression;

    private String jobName;

    private String jobGroup;

    private String triggerName;

    private String triggerGroup;

    private Boolean pause;

    private Boolean enable;

    private String description;

    private Date createTime;

    private Date lastUpdateTime;

    private Map<String, Object> jobDataParam;

    public Boolean isPause() {
        return this.pause;
    }
    public Boolean isEnable() {
        return this.enable;
    }
}
