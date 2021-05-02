package group.xuxiake.web.shiro;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

public class AutoLoginCredentialsMatcher extends HashedCredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        AutoLoginToken autoLoginToken = (AutoLoginToken) token;
        if (autoLoginToken.getLoginType().equals(LoginType.NO_PASSWORD)) { // 免密登录直接通过
            return true;
        } else {
            return super.doCredentialsMatch(token, info);
        }
    }
}
