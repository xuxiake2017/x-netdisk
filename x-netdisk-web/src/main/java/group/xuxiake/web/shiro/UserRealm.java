package group.xuxiake.web.shiro;

import group.xuxiake.common.entity.User;
import group.xuxiake.web.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.ByteSource;
import org.crazycake.shiro.RedisSessionDAO;

import javax.annotation.Resource;
import java.util.Collection;

public class UserRealm extends AuthorizingRealm {

	@Resource
	private UserService userService;
	@Resource
	private RedisSessionDAO redisSessionDAO;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
		return null;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		//Principal 当事人
		//Credentials 认证信息
		
		//获取从前端传过来的登录信息
		String loginInfo = (String) token.getPrincipal();
		//realm名字
		String realmName = getName();
		//这里做登陆信息验证。。。
		User user = null;
		if (loginInfo.contains("@")) {
			user = userService.findByEmail(loginInfo);
		} else {
			user = userService.findByName(loginInfo);
			if (user == null) {
				user = userService.findByPhone(loginInfo);
			}
		}
		if (user == null) {
			throw new UnknownAccountException();
		}

		// 不允许用户重复登录
		Collection<Session> sessions = redisSessionDAO.getActiveSessions();
		for (Session session: sessions) {
			SimplePrincipalCollection simplePrincipalCollection = (SimplePrincipalCollection) session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
			if (simplePrincipalCollection != null) {
				User primaryPrincipal = (User) simplePrincipalCollection.getPrimaryPrincipal();
				System.out.println(primaryPrincipal);
				if (primaryPrincipal.getId().intValue() == user.getId().intValue()) {
					// 删除session，即将其踢出系统
					redisSessionDAO.delete(session);
				}
			}
		}

		//盐
		ByteSource credentialsSalt =  ByteSource.Util.bytes(user.getUsername());
		SimpleAuthenticationInfo simpleAuthenticationInfo = 
				new SimpleAuthenticationInfo(user, user.getPassword(), credentialsSalt, realmName);
		return simpleAuthenticationInfo;
	}

}
