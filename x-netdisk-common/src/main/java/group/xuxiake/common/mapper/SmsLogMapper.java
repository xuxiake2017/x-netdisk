package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.SmsLog;
import group.xuxiake.common.entity.param.SmsLogQueryParams;
import org.apache.ibatis.annotations.Param;

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
}