package group.xuxiake.admin.exception.user;

import group.xuxiake.admin.util.StrFormatter;

/**
 * 用户错误最大次数异常类
 * 
 * @author ruoyi
 */
public class UserPasswordRetryLimitExceedException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserPasswordRetryLimitExceedException(int retryLimitCount, Long lockTime)
    {
        super(StrFormatter.format("密码输入错误{}次，帐户锁定{}分钟", retryLimitCount, lockTime / ( 60 * 1000)));
    }
}
