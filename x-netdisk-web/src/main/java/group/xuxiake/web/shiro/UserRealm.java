package group.xuxiake.web.shiro;

import group.xuxiake.common.entity.User;
import group.xuxiake.common.mapper.UserMapper;
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
	@Resource
	private UserMapper userMapper;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
		return null;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

		AutoLoginToken autoLoginToken = (AutoLoginToken) token;
		//Principal 当事人
		//Credentials 认证信息
		
		//获取从前端传过来的登录信息
		String loginInfo = (String) autoLoginToken.getPrincipal();
		//realm名字
		String realmName = getName();

		if (autoLoginToken.getLoginType().equals(LoginType.NO_PASSWORD)) {
			// 如果是免密登录传过来的是openid
			User userByOpenid = userMapper.findByOpenid(loginInfo);
			if (userByOpenid == null) {
				throw new UnknownAccountException("未注册");
			}
			this.keepOneLogin(userByOpenid);
			return new SimpleAuthenticationInfo(userByOpenid, null, null, realmName);
		}

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



		//盐
		ByteSource credentialsSalt =  ByteSource.Util.bytes(user.getUsername());
		SimpleAuthenticationInfo simpleAuthenticationInfo = 
				new SimpleAuthenticationInfo(user, user.getPassword(), credentialsSalt, realmName);
		this.keepOneLogin(user);
		return simpleAuthenticationInfo;
	}

	private void keepOneLogin(User user) {
		// 不允许用户重复登录
		Collection<Session> sessions = redisSessionDAO.getActiveSessions();
		for (Session session: sessions) {
			SimplePrincipalCollection simplePrincipalCollection = (SimplePrincipalCollection) session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
			if (simplePrincipalCollection != null) {
				User primaryPrincipal = (User) simplePrincipalCollection.getPrimaryPrincipal();
				if (primaryPrincipal.getId().intValue() == user.getId().intValue()) {
					// 删除session，即将其踢出系统
					redisSessionDAO.delete(session);
				}
			}
		}
	}

}
