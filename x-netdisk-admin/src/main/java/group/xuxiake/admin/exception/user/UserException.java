package group.xuxiake.admin.exception.user;

import group.xuxiake.admin.exception.base.BaseException;

/**
 * 用户信息异常类
 * 
 * @author ruoyi
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String message)
    {
        super("user", message);
    }
}
