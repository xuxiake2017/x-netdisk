package group.xuxiake.common.entity.show;

import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.WechatUser;
import lombok.Data;

@Data
public class WeChatUserWidthUserInfo extends WechatUser {
    private User user;
}
