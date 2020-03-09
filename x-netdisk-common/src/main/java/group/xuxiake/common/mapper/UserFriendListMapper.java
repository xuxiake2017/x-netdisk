package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.UserFriendList;
import group.xuxiake.common.entity.show.UserFriendListShow;

import java.util.List;

public interface UserFriendListMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserFriendList record);

    int insertSelective(UserFriendList record);

    UserFriendList selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserFriendList record);

    int updateByPrimaryKey(UserFriendList record);

    /**
     * 获取好友列表
     * @param userId 用户id
     * @return
     */
    List<UserFriendListShow> getFriendList(Integer userId);
}