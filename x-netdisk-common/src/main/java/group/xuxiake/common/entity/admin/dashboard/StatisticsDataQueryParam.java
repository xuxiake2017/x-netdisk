package group.xuxiake.common.entity.admin.dashboard;

import lombok.Data;

import java.util.Date;

/**
 * @Author xuxiake
 * @Date 15:59 2022/11/24
 * @Description
 */
@Data
public class StatisticsDataQueryParam {
    private Date startTime;
    private Date endTime;
}
