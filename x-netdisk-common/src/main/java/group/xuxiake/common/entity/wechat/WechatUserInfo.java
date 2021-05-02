package group.xuxiake.common.entity.wechat;

import lombok.Data;

/**
 * 微信用户信息
 */
@Data
public class WechatUserInfo {
    private String avatarUrl;
    private String city;
    private String country;
    private Integer gender;
    private String language;
    private String nickName;
    private String province;
}
