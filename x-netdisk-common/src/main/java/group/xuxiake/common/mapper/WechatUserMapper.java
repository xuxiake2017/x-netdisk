package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.WechatUser;

public interface WechatUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(WechatUser record);

    int insertSelective(WechatUser record);

    WechatUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WechatUser record);

    int updateByPrimaryKey(WechatUser record);

    /**
     * 根据openid查找微信用户
     * @param openid
     * @return
     */
    WechatUser findByOpenid(String openid);

    /**
     * 根据系统userId查找微信用户
     * @param userId
     * @return
     */
    WechatUser findByUserId(Integer userId);
}