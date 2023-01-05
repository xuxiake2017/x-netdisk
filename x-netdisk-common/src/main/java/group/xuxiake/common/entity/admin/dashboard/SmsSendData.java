package group.xuxiake.common.entity.admin.dashboard;

import lombok.Data;

import java.util.List;

/**
 * @Author xuxiake
 * @Date 15:03 2022/11/24
 * @Description
 */
@Data
public class SmsSendData {

    private Integer todaySendNum;
    private Integer currentMonthSendNum;
    private List<StatisticsDataItem> statisticsData;

    public SmsSendData(Integer todaySendNum, Integer currentMonthSendNum, List<StatisticsDataItem> statisticsData) {
        this.todaySendNum = todaySendNum;
        this.currentMonthSendNum = currentMonthSendNum;
        this.statisticsData = statisticsData;
    }

    public SmsSendData() {
    }
}
