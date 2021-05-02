package group.xuxiake.common.entity.param;

import lombok.Data;

@Data
public class WechatEncryptionParam {
    private String _appid;
    private String _sign;
    private String _timestamp;
}
