package group.xuxiake.admin.exception.base;

/**
 * 基础异常
 * 
 * @author ruoyi
 */
public class BaseException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    private String module;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public BaseException(String module, String message) {
        super(message);
        this.module = module;
    }
}
