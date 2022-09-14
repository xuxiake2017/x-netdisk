package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.SmsLog;

public interface SmsLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SmsLog record);

    int insertSelective(SmsLog record);

    SmsLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SmsLog record);

    int updateByPrimaryKey(SmsLog record);
}