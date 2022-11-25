package group.xuxiake.admin.service;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.admin.dashboard.StatisticsDataQueryParam;

/**
 * @Author xuxiake
 * @Date 17:12 2022/11/16
 * @Description
 */
public interface DashBoardService {
    Result getCardData();

    Result getStatisticsData(StatisticsDataQueryParam param);
}
