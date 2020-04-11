package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.show.UserShowSimple;
import org.apache.ibatis.annotations.Param;

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
}