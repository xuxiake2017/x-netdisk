package group.xuxiake.common.exception;

/**
 * Author by xuxiake, Date on 2019/12/13.
 * PS: Not easy to write code, please indicate.
 * Description：Quartz自定义异常
 */
public class QuartzCustomException extends RuntimeException {

    public QuartzCustomException() {
        super();
    }
    public QuartzCustomException(String message) {
        super(message);
    }
    public QuartzCustomException(Throwable cause) {
        super(cause);
    }
    public QuartzCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
