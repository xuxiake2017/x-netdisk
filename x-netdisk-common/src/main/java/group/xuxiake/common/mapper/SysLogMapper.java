package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.SysLog;
import group.xuxiake.common.entity.param.SysLogQueryParams;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysLog record);

    int insertSelective(SysLog record);

    SysLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysLog record);

    int updateByPrimaryKey(SysLog record);

    List<SysLog> getSysLog(@Param("params") SysLogQueryParams params);
}