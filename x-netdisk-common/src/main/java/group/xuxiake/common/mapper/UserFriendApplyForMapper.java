package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.UserFriendApplyFor;
import group.xuxiake.common.entity.show.UserFriendApplyForShowList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserFriendApplyForMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(UserFriendApplyFor record);

    int insertSelective(UserFriendApplyFor record);

    UserFriendApplyFor selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserFriendApplyFor record);

    int updateByPrimaryKey(UserFriendApplyFor record);

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
    void friendApplyForOption(@Param("applicant") Integer applicant, @Param("respondent") Integer respondent, @Param("option") Integer option);
}