package group.xuxiake.web.shiro;

import group.xuxiake.common.entity.User;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.web.util.PasswordEncoder;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.crazycake.shiro.RedisSessionDAO;

import javax.annotation.Resource;
import java.util.Collection;

public class AutoLoginCredentialsMatcher implements CredentialsMatcher {

	@Resource
	private UserMapper userMapper;
	@Resource
	private RedisSessionDAO redisSessionDAO;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        AutoLoginToken autoLoginToken = (AutoLoginToken) token;
        if (autoLoginToken.getLoginType().equals(LoginType.NO_PASSWORD)) { // 免密登录直接通过
			keepOneLogin((User) info.getPrincipals().getPrimaryPrincipal());
            return true;
        }
		User user = (User) info.getPrincipals().getPrimaryPrincipal();
		String storedPassword = String.valueOf(info.getCredentials());
		Object credentials = token.getCredentials();
		String inputPassword = credentials instanceof char[]
				? new String((char[]) credentials) : String.valueOf(credentials);
		boolean matched = PasswordEncoder.isPasswordValid(inputPassword, storedPassword, user.getUsername());
		if (matched && PasswordEncoder.needsUpgrade(storedPassword)) {
			User passwordUpdate = new User();
			passwordUpdate.setId(user.getId());
			passwordUpdate.setPassword(PasswordEncoder.encode(inputPassword, user.getUsername()));
			userMapper.updateByPrimaryKeySelective(passwordUpdate);
		}
		if (matched) {
			keepOneLogin(user);
		}
		return matched;
    }

	private void keepOneLogin(User user) {
		Collection<Session> sessions = redisSessionDAO.getActiveSessions();
		for (Session session : sessions) {
			SimplePrincipalCollection principals = (SimplePrincipalCollection)
					session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
			if (principals == null) {
				continue;
			}
			User principal = (User) principals.getPrimaryPrincipal();
			if (principal != null && principal.getId().equals(user.getId())) {
				redisSessionDAO.delete(session);
			}
		}
	}
}
