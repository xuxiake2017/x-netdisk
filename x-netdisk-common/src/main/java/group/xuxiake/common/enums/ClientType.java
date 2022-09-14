package group.xuxiake.common.enums;

/**
 * 客户端类型：1小程序 2web 3H5 4未知
 */
public enum ClientType {
    UNSET(-1),
    MINI_APP(1),
    WEB(2),
    H5(3),
    OTHER(4);

    private int value;

    ClientType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
