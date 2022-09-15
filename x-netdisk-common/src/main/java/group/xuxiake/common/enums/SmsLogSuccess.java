package group.xuxiake.common.enums;

/**
 * 是否发送成功 1成功 0失败
 */
public enum SmsLogSuccess {
    SUCCESS(1),
    FAILED(0);

    private int value;

    SmsLogSuccess(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
