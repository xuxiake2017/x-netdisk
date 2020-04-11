package group.xuxiake.web.shiro;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.RouteShowSimple;
import group.xuxiake.common.entity.User;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.exception.UserRepeatLoginException;
import group.xuxiake.web.service.RouteService;
import group.xuxiake.web.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

public class UserRealm extends AuthorizingRealm {

	@Resource
	private UserService userService;
	@Resource
	private AppConfiguration appConfiguration;
	@Resource
	private RestTemplate restTemplate;
	@Resource
	private RouteService routeService;
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
		if(loginInfo.contains("@")){
			user = userService.findByEmail(loginInfo);
		}else {
			user = userService.findByName(loginInfo);
			if (user == null) {
				user = userService.findByPhone(loginInfo);
			}
		}
		if (user == null) {
			throw new UnknownAccountException();
		}
        RouteShowSimple route = routeService.getRouteServer(user.getId().toString());
        String url = "http://" + route.getIp() + ":" + route.getPort() + appConfiguration.getFindRouteByUserPath() + "?userId=" + user.getId();
        ResponseEntity<Result> responseEntity = restTemplate.getForEntity(url, Result.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody().getData() != null) {
			// 用户重复登录
			throw new UserRepeatLoginException();
		}
		//盐
		ByteSource credentialsSalt =  ByteSource.Util.bytes(user.getUsername());
		SimpleAuthenticationInfo simpleAuthenticationInfo = 
				new SimpleAuthenticationInfo(user, user.getPassword(), credentialsSalt, realmName);
		return simpleAuthenticationInfo;
	}

}
