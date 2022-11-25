package group.xuxiake.common.entity.admin.dashboard;

import lombok.Data;

import java.util.List;

/**
 * @Author xuxiake
 * @Date 18:28 2022/11/17
 * @Description
 */
@Data
public class PVData {
    private Integer todayPVNum;
    private Integer currentMonthPVNum;
    private List<StatisticsDataItem> statisticsData;

    public PVData(Integer todayPVNum, Integer currentMonthPVNum, List<StatisticsDataItem> statisticsData) {
        this.todayPVNum = todayPVNum;
        this.currentMonthPVNum = currentMonthPVNum;
        this.statisticsData = statisticsData;
    }

    public PVData() {
    }
}
