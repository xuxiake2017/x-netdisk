package group.xuxiake.admin.security.handle;

import group.xuxiake.admin.exception.user.UserPasswordNotMatchException;
import group.xuxiake.admin.exception.user.UserPasswordRetryLimitExceedException;
import group.xuxiake.admin.security.context.AuthenticationContextHolder;
import group.xuxiake.admin.util.CacheConstants;
import group.xuxiake.admin.util.SecurityUtils;
import group.xuxiake.common.entity.UserAdmin;
import group.xuxiake.common.util.RedisUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 登录密码方法
 * 
 * @author ruoyi
 */
@Component
public class SysPasswordService
{
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RedisTemplate redisTemplate;

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private Long lockTime;

    /**
     * 登录账户密码错误次数缓存键名
     * 
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    public void validate(UserAdmin user)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        Integer retryCount = (Integer) redisUtils.get(getCacheKey(username));

        if (retryCount == null)
        {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
//                    MessageUtils.message("user.password.retry.limit.exceed", maxRetryCount, lockTime)));
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }

        if (!matches(user, password))
        {
            retryCount = retryCount + 1;
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
//                    MessageUtils.message("user.password.retry.limit.count", retryCount)));
            redisUtils.set(getCacheKey(username), retryCount, lockTime);
            throw new UserPasswordNotMatchException();
        }
        else
        {
            clearLoginRecordCache(username);
        }
    }

    public boolean matches(UserAdmin user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    public void clearLoginRecordCache(String loginName)
    {
        if (redisTemplate.hasKey(getCacheKey(loginName)))
        {
            redisUtils.del(getCacheKey(loginName));
        }
    }
}
