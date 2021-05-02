package group.xuxiake.common.entity.wechat;

import lombok.Data;

@Data
public class LoginAndRegisterParam {

    private WechatUserInfo wechatUserInfo;

    private String code;

    private String phone;

    private String uuid;

    private String smsCaptcha;
}
