package group.xuxiake.common.entity.param;

/**
 * 仅允许修改非敏感个人资料，避免直接绑定 User 造成越权字段写入。
 */
public class UserProfileUpdateParam {

    private Integer sex;
    private String realName;
    private String signature;
    private String nickName;

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
