package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.SysLog;
import group.xuxiake.common.entity.admin.dashboard.StatisticsDataItem;
import group.xuxiake.common.entity.param.SysLogQueryParams;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SysLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysLog record);

    int insertSelective(SysLog record);

    SysLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysLog record);

    int updateByPrimaryKey(SysLog record);

    List<SysLog> getSysLog(@Param("params") SysLogQueryParams params);

    /**
     * 获取时间内统计数据
     * @param startTime
     * @param endTime
     * @param groupType month day hour
     * @return
     */
    List<StatisticsDataItem> getTimePeriodStatisticsData(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("groupType") String groupType);

    /**
     * 获取时间段内访问量
     * @param startTime
     * @param endTime
     * @return
     */
    Integer getTimePeriodPVNum(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}