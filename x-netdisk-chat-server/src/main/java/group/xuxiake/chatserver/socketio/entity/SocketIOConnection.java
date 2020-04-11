package group.xuxiake.chatserver.socketio.entity;

import com.corundumstudio.socketio.SocketIOClient;
import group.xuxiake.common.entity.User;
import lombok.Data;

/**
 * Author by xuxiake, Date on 2020/3/4 22:11.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class SocketIOConnection {

    private String token;
    private User user;
    private SocketIOClient socketIOClient;
}
