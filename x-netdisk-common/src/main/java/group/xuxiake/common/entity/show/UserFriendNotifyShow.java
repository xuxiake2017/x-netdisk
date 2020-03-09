package group.xuxiake.common.entity.show;

import lombok.Data;

/**
 * Author by xuxiake, Date on 2019/5/5.
 * PS: Not easy to write code, please indicate.
 * Description：用户好友通知
 */
@Data
public class UserFriendNotifyShow {

    // 通知类型 GROUP：群；FRIEND_APPLY_FOR：好友验证
    private String type;
    private Object content;
}
