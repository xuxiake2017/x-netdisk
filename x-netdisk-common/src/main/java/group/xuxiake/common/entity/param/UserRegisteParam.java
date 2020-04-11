package group.xuxiake.common.entity.param;

import lombok.Data;

/**
 * Author by xuxiake, Date on 2020/4/6 23:25.
 * PS: Not easy to write code, please indicate.
 * Description： 
 */
@Data
public class UserRegisteParam {

    private String username;

    private String password;

    private String imgCode;

    private String verifyInfo;

    private String smsCode;
}
