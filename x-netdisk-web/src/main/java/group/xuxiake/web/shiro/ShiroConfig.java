package group.xuxiake.web.shiro;

import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.filter.ShiroLoginFilter;
import group.xuxiake.web.filter.VerifyUserFilter;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author by xuxiake, Date on 2020/3/2 10:14.
 * PS: Not easy to write code, please indicate.
 * Description：shiro配置
 */
@Configuration
public class ShiroConfig {

    @Resource
    private AppConfiguration appConfiguration;

    @Bean
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        String host = appConfiguration.getRedisHost() + ":" + appConfiguration.getRedisPort();
        redisManager.setHost(host);
        return redisManager;
    }

    @Bean
    public RedisSessionDAO redisSessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(this.redisManager());
        return redisSessionDAO;
    }

    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(this.redisManager());
        return redisCacheManager;
    }

    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        // 加密的算法
        hashedCredentialsMatcher.setHashAlgorithmName("MD5");
        // 加密的次数
        hashedCredentialsMatcher.setHashIterations(1024);
        return hashedCredentialsMatcher;
    }

    //注入自定义的realm，告诉shiro如何获取用户信息来做登录或权限控制
    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
        userRealm.setCredentialsMatcher(this.hashedCredentialsMatcher());
        return userRealm;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        /**
         * setUsePrefix(false)用于解决一个奇怪的bug。在引入spring aop的情况下。
         * 在@Controller注解的类的方法中加入@RequiresRole注解，会导致该方法无法映射请求，导致返回404。
         * 加入这项配置能解决这个bug
         */
        creator.setUsePrefix(true);
        return creator;
    }

    @Bean("authenticator")
    public Authenticator authenticator() {
        Authenticator authenticator = new ModularRealmAuthenticator();
        return authenticator;
    }

    @Bean("authorizer")
    public Authorizer authorizer() {
        Authorizer authorizer = new ModularRealmAuthorizer();
        return authorizer;
    }

    @Bean("sessionManager")
    public SessionManager sessionManager() {
        SessionConfig sessionConfig = new SessionConfig();
        sessionConfig.setSessionDAO(this.redisSessionDAO());
        sessionConfig.setSessionValidationSchedulerEnabled(true);
        // 设置session超时时间半天，默认半小时
        sessionConfig.setGlobalSessionTimeout(appConfiguration.getGlobalSessionTimeout());
        return sessionConfig;
    }

    /**
     * 注入 securityManager
     * @return
     */
    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置realm.
        securityManager.setRealm(userRealm());
        securityManager.setSessionManager(sessionManager());
        securityManager.setCacheManager(cacheManager());
        return securityManager;
    }

    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager());

        // 自定义拦截器（实现未登录返回json）,自定义拦截器需要手动new，不能交给spring管理，否则会报错
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("ShiroLoginFilter", new ShiroLoginFilter());
        filterMap.put("VerifyUserFilter", new VerifyUserFilter());
        shiroFilterFactoryBean.setFilters(filterMap);

        // 设置拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/user/logout", "anon");
        filterChainDefinitionMap.put("/user/login", "anon");
        filterChainDefinitionMap.put("/user/getInfo", "anon");
        filterChainDefinitionMap.put("/user/checkUserName", "anon");
        filterChainDefinitionMap.put("/user/checkPhone", "anon");
        filterChainDefinitionMap.put("/user/checkEmail", "anon");
        filterChainDefinitionMap.put("/user/checkImgCode", "anon");
        filterChainDefinitionMap.put("/user/register", "anon");
        filterChainDefinitionMap.put("/user/registerApp", "anon");
        filterChainDefinitionMap.put("/verify/**", "anon");
        filterChainDefinitionMap.put("/user/createImg/**", "anon");
        filterChainDefinitionMap.put("/share/**", "anon");
        filterChainDefinitionMap.put("/friendMessage/getServer", "anon");
        filterChainDefinitionMap.put("/share/shareFile", "VerifyUserFilter");
        filterChainDefinitionMap.put("/share/findAll", "VerifyUserFilter");
        filterChainDefinitionMap.put("/share/delete/*", "VerifyUserFilter");
        filterChainDefinitionMap.put("/file/**", "VerifyUserFilter");
        filterChainDefinitionMap.put("/img/**", "VerifyUserFilter");
        filterChainDefinitionMap.put("/dir/**", "VerifyUserFilter");
        filterChainDefinitionMap.put("/recycle/**", "VerifyUserFilter");
        filterChainDefinitionMap.put("/**", "ShiroLoginFilter");
//        filterChainDefinitionMap.put("/**", "anon");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }
}
