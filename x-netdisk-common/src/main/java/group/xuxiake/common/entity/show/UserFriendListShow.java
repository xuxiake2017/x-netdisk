package group.xuxiake.common.entity.show;

import lombok.Data;

/**
 * @author: xuxiake
 * @create: 2019-05-01 10:39
 * @description: 展示好友列表
 **/
@Data
public class UserFriendListShow {

    // 用户id
    private Integer userId;
    // 好友id
    private Integer friendId;
    // 好友用户名
    private String username;
    // 好友真实名
    private String realName;
    // 好友头像
    private String avatar;
    // 好友个性签名
    private String signature;
}
