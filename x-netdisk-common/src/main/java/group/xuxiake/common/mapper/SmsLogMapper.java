package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.SmsLog;
import group.xuxiake.common.entity.admin.dashboard.StatisticsDataItem;
import group.xuxiake.common.entity.param.SmsLogQueryParams;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SmsLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SmsLog record);

    int insertSelective(SmsLog record);

    SmsLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SmsLog record);

    int updateByPrimaryKey(SmsLog record);

    SmsLog selectByBizId(String bizId);

    List<SmsLog> getSmsLog(@Param("params") SmsLogQueryParams params);

    /**
     * 获取时间内发送统计数据
     * @param startTime
     * @param endTime
     * @param groupType month day hour
     * @return
     */
    List<StatisticsDataItem> getTimePeriodStatisticsData(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("groupType") String groupType);

    /**
     * 获取时间段内短信发送数量
     * @param startTime
     * @param endTime
     * @return
     */
    Integer getTimePeriodSendNum(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}