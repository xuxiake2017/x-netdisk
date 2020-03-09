package group.xuxiake.web.exception;

import org.apache.shiro.authc.AccountException;

/**
 * Author by xuxiake, Date on 2019/4/17.
 * PS: Not easy to write code, please indicate.
 * Description：用户重复登录异常
 */
public class UserRepeatLoginException extends AccountException {

    public UserRepeatLoginException() {
        super();
    }

    public UserRepeatLoginException(String message) {
        super(message);
    }

    public UserRepeatLoginException(Throwable cause) {
        super(cause);
    }

    public UserRepeatLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
