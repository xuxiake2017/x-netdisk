package group.xuxiake.common.entity.param;

import lombok.Data;

@Data
public class WechatSendSMSCaptchaParam extends WechatEncryptionParam {
    private String phone;
}
