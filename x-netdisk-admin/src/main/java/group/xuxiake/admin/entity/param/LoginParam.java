package group.xuxiake.admin.entity.param;

import lombok.Data;

@Data
public class LoginParam {
    private String username;
    private String password;
    private String captcha;
    private String uuid;
}
