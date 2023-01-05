package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.admin.dashboard.StatisticsDataItem;
import group.xuxiake.common.entity.param.UserListQueryParams;
import group.xuxiake.common.entity.show.UserShowSimple;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User findByLoginInfo(User user);

    /**
     * 搜索好友
     * @param key 关键词
     * @param userId 当前登录用户id
     * @return
     */
    List<UserShowSimple> searchFriend(@Param("key") String key, @Param("userId") Integer userId);

    /**
     * 根据openid查找用户
     * @param openid
     * @return
     */
    User findByOpenid(String openid);

    /**
     * 获取用户列表
     * @param params
     * @return
     */
    List<User> getUserList(@Param("params") UserListQueryParams params);

    /**
     * 获取时间内新增用户数据统计
     * @param startTime
     * @param endTime
     * @param groupType month day hour
     * @return
     */
    List<StatisticsDataItem> getTimePeriodStatisticsData(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("groupType") String groupType);

    /**
     * 获取时间段用户数量
     * @param startTime
     * @param endTime
     * @return
     */
    Integer getTimePeriodUserNum(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}