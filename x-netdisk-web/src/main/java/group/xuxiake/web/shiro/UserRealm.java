package group.xuxiake.web.shiro;

import group.xuxiake.common.entity.User;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.web.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import javax.annotation.Resource;

public class UserRealm extends AuthorizingRealm {

	@Resource
	private UserService userService;
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
			userByOpenid.setPassword(null);
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



		String encodedPassword = user.getPassword();
		user.setPassword(null);
		return new SimpleAuthenticationInfo(user, encodedPassword, realmName);
	}

}
