package group.xuxiake.web.shiro;

/**
 * 登录类型
 */
public enum LoginType {
    PASSWORD("password"), // 密码登录
    NO_PASSWORD("NO_PASSWORD"); // 免密登录

    private String code;

    LoginType(String code) {
        this.code = code;
    }
}
