package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.entity.show.UserShowSimple;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserNetdiskMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(UserNetdisk record);

	int insertSelective(UserNetdisk record);

	UserNetdisk selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(UserNetdisk record);

	int updateByPrimaryKey(UserNetdisk record);

	UserNetdisk findByLoginInfo(UserNetdisk userNetdisk);

	/**
	 * 搜索好友
	 * @param key 关键词
	 * @param userId 当前登录用户id
	 * @return
	 */
	List<UserShowSimple> searchFriend(@Param("key") String key, @Param("userId") Integer userId);
}