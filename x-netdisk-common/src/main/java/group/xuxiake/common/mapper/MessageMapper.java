package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.Message;

import java.util.List;

public interface MessageMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Message record);

    int insertSelective(Message record);

    Message selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);

    /**
     * 查找单个用户的所有消息
     * @param userId
     * @return
     */
    List<Message> findAll(Integer userId);
}