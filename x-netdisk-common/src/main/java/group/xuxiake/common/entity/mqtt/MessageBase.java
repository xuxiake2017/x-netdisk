package group.xuxiake.common.entity.mqtt;

/**
 * Author by xuxiake, Date on 2019/6/30.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public class MessageBase {

    private String type;
    private String status;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
