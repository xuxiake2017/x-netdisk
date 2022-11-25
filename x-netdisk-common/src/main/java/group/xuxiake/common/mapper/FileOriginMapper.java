package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.FileOrigin;
import group.xuxiake.common.entity.admin.dashboard.StatisticsDataItem;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface FileOriginMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FileOrigin record);

    int insertSelective(FileOrigin record);

    FileOrigin selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FileOrigin record);

    int updateByPrimaryKey(FileOrigin record);

    /**
     * 根据MD5值查找文件
     * @param md5Hex
     * @return
     */
    FileOrigin findFileByMd5Hex(String md5Hex);

    /**
     * 根据userFileId查找
     * @param userFileId
     * @return
     */
    FileOrigin findByUserFileId(Integer userFileId);

    /**
     * 获取时间内上传数据统计
     * @param startTime
     * @param endTime
     * @param groupType month day hour
     * @return
     */
    List<StatisticsDataItem> getTimePeriodStatisticsData(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("groupType") String groupType);

    /**
     * 获取时间段内上传数据量
     * @param startTime
     * @param endTime
     * @return
     */
    Integer getTimePeriodUploadSize(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}