package group.xuxiake.common.enums;

/**
 * 日志类型：1登录 2登出 3注册
 */
public enum LogType {
    LOGIN(1),
    LOGOUT(2),
    REGISTER(3);

    private int value;

    LogType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
