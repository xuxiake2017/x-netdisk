package group.xuxiake.common.entity.chat;

import lombok.Data;

import java.util.Date;

/**
 * @author: xuxiake
 * @create: 2019-05-12 00:03
 * @description:
 **/
@Data
public class ChatMessageBase<T> {

    // 消息类型 FRIEND：好友；GROUP：群；FRIEND_APPLY_FOR：添加好友；SYSTEM：系统消息
    private String type;
    private T content;
    private Date createTime;
}
