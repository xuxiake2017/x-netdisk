package group.xuxiake.common.entity.admin.dashboard;

import lombok.Data;

import java.util.List;

/**
 * @Author xuxiake
 * @Date 20:22 2022/11/23
 * @Description
 */
@Data
public class UploadData {
    private Integer todayUploadNum;
    private Integer currentMonthUploadNum;
    private List<StatisticsDataItem> statisticsData;

    public UploadData(Integer todayUploadNum, Integer currentMonthUploadNum, List<StatisticsDataItem> statisticsData) {
        this.todayUploadNum = todayUploadNum;
        this.currentMonthUploadNum = currentMonthUploadNum;
        this.statisticsData = statisticsData;
    }

    public UploadData() {
    }
}
