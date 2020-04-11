package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.SysMessage;

import java.util.List;

public interface SysMessageMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysMessage record);

    int insertSelective(SysMessage record);

    SysMessage selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysMessage record);

    int updateByPrimaryKey(SysMessage record);

    /**
     * 查找单个用户的所有消息
     * @param userId
     * @return
     */
    List<SysMessage> findAll(Integer userId);
}