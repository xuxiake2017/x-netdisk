package group.xuxiake.common.entity.admin.dashboard;

import lombok.Data;

import java.util.List;

/**
 * @Author xuxiake
 * @Date 17:45 2022/11/24
 * @Description
 */
@Data
public class StatisticsData {
    private List<StatisticsDataItem> userStatisticsData;
    private List<StatisticsDataItem> uploadStatisticsData;
    private List<StatisticsDataItem> uploadSizeStatisticsData;
    private List<StatisticsDataItem> smsStatisticsData;

    public StatisticsData(List<StatisticsDataItem> userStatisticsData, List<StatisticsDataItem> uploadStatisticsData, List<StatisticsDataItem> uploadSizeStatisticsData, List<StatisticsDataItem> smsStatisticsData) {
        this.userStatisticsData = userStatisticsData;
        this.uploadStatisticsData = uploadStatisticsData;
        this.uploadSizeStatisticsData = uploadSizeStatisticsData;
        this.smsStatisticsData = smsStatisticsData;
    }

    public StatisticsData() {
    }
}
