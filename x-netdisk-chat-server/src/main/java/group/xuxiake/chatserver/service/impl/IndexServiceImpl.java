package group.xuxiake.chatserver.service.impl;

import group.xuxiake.chatserver.service.IndexService;
import group.xuxiake.chatserver.socketio.ChatSocketIOHandler;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;
import org.springframework.stereotype.Service;

/**
 * Author by xuxiake, Date on 2020/3/7 21:40.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Service("indexService")
public class IndexServiceImpl implements IndexService {

    /**
     * 给好友发送消息
     * @param param
     * @return
     */
    @Override
    public Result sendMsg(RouteOfSendMsgPojo param) {

        Integer userId = param.getUserId();
        ChatSocketIOHandler.sendMessage(userId, param.getChatMessageBase());
        return Result.SUCCESS;
    }
}
