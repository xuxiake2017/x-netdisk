package group.xuxiake.web.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

public class AutoLoginToken extends UsernamePasswordToken {

    private LoginType loginType;

    public LoginType getLoginType() {
        return loginType;
    }

    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }

    public AutoLoginToken() {
    }

    public AutoLoginToken(String username, String password, LoginType loginType) {
        super(username, password);
        this.loginType = loginType;
    }
}
