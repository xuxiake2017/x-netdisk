package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.UserFriendRequest;
import group.xuxiake.common.entity.show.UserFriendApplyForShowList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserFriendRequestMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserFriendRequest record);

    int insertSelective(UserFriendRequest record);

    UserFriendRequest selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserFriendRequest record);

    int updateByPrimaryKey(UserFriendRequest record);

    /**
     * 查找所有好友请求
     * @param userId
     * @return
     */
    List<UserFriendApplyForShowList> findAll(Integer userId);

    /**
     * 好友申请操作
     * @param applicant 申请者
     * @param respondent 被申请者
     * @param option 操作 1：同意；2：拒绝
     */
    void friendRequestOption(@Param("applicant") Integer applicant, @Param("respondent") Integer respondent, @Param("option") Integer option);
}