package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.UserAdmin;

public interface UserAdminMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserAdmin record);

    int insertSelective(UserAdmin record);

    UserAdmin selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserAdmin record);

    int updateByPrimaryKey(UserAdmin record);

    UserAdmin selectByUsername(String username);
}