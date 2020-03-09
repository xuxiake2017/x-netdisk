package group.xuxiake.common.entity.route;

import group.xuxiake.common.entity.chat.ChatMessageBase;
import lombok.Data;

/**
 * Author by xuxiake, Date on 2020/3/6 21:52.
 * PS: Not easy to write code, please indicate.
 * Description：路由发送消息pojo
 */
@Data
public class RouteOfSendMsgPojo {

    private Integer userId;
    private ChatMessageBase chatMessageBase;
}
