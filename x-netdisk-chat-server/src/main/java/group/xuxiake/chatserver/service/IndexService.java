package group.xuxiake.chatserver.service;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;

/**
 * Author by xuxiake, Date on 2020/3/7 21:40.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public interface IndexService {

    /**
     * 给好友发送消息
     * @param param
     * @return
     */
    Result sendMsg(RouteOfSendMsgPojo param);
}
