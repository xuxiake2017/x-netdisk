package group.xuxiake.common.entity.chat;

/**
 * ChatMessage消息类型
 */
public enum ChatMessageType {

    // 好友
    FRIEND("FRIEND"),
    // 群
    GROUP("GROUP"),
    // 添加好友
    FRIEND_APPLY_FOR("FRIEND_APPLY_FOR"),
    // 系统消息
    SYSTEM("SYSTEM");

    private String value;

    ChatMessageType() {
    }

    ChatMessageType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
