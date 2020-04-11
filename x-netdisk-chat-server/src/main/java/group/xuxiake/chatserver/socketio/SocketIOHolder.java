package group.xuxiake.chatserver.socketio;

import group.xuxiake.chatserver.socketio.entity.SocketIOConnection;
import group.xuxiake.common.entity.User;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author by xuxiake, Date on 2020/3/4 22:13.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public class SocketIOHolder {

    // 用来存已连接的客户端，key为client token
    private static Map<String, SocketIOConnection> clientMap = new ConcurrentHashMap<>();

    public static void remove(String token) {
        clientMap.remove(token);
    }

    public static void put(String token, SocketIOConnection socketIOConnection) {
        clientMap.put(token, socketIOConnection);
    }

    public static User getUser(String token) {
        SocketIOConnection socketIOConnection = clientMap.get(token);
        if (socketIOConnection != null) {
            return socketIOConnection.getUser();
        }
        return null;
    }

    public static SocketIOConnection getSocketIOConnection(Integer userId) {

        Set<Map.Entry<String, SocketIOConnection>> entries = clientMap.entrySet();
        for (Map.Entry<String, SocketIOConnection> entry : entries) {
            SocketIOConnection socketIOConnection = entry.getValue();
            if (socketIOConnection.getUser().getId().intValue() == userId.intValue()) {
                return socketIOConnection;
            }
        }
        return null;
    }
}
