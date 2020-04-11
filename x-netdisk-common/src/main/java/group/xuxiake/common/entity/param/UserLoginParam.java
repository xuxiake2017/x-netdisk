package group.xuxiake.common.entity.param;

import lombok.Data;

/**
 * Author by xuxiake, Date on 2020/4/6 18:16.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class UserLoginParam {

    private String imgCode;
    private Boolean remindPwd;
    private String loginInfo;
    private String password;
}
