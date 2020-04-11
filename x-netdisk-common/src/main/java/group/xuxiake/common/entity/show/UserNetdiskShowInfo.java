package group.xuxiake.common.entity.show;

import group.xuxiake.common.entity.SysMessage;

import java.util.List;

/**
 * Author by xuxiake, Date on 2019/1/3.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public class UserNetdiskShowInfo {

    // id
    private Integer id;
    // 头像
    private String avatar;
    // 姓名
    private String name;
    // 总内存
    private Long totalMemory;
    // 已用内存
    private Long usedMemory ;
    // 可用内存
    private Long availableMemory  ;
    // token
    private String token;
    // 消息
    private List<SysMessage> messages;
    // 好友列表
    private List<UserFriendListShow> friendList;

    public List<UserFriendListShow> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<UserFriendListShow> friendList) {
        this.friendList = friendList;
    }

    public List<SysMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SysMessage> messages) {
        this.messages = messages;
    }

    public Long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(Long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public Long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public Long getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(Long availableMemory) {
        this.availableMemory = availableMemory;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
