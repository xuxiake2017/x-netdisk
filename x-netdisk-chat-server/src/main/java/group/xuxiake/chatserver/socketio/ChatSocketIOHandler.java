package group.xuxiake.chatserver.socketio;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.vdurmont.emoji.EmojiParser;
import group.xuxiake.chatserver.configuration.AppConfiguration;
import group.xuxiake.chatserver.socketio.entity.SocketIOConnection;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.UserFriendMessage;
import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.entity.chat.ChatMessageBase;
import group.xuxiake.common.entity.chat.ChatMessageType;
import group.xuxiake.common.entity.route.RouteOfSaveRoutePojo;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;
import group.xuxiake.common.entity.show.UserFriendMessageShow;
import group.xuxiake.common.mapper.UserFriendMessageMapper;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.common.zookeeper.SubscribeZK;
import group.xuxiake.common.zookeeper.balancer.Balancer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author by xuxiake, Date on 2020/3/4 20:57.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Slf4j
@Component
public class ChatSocketIOHandler {

    //推送的事件
    public final static String PUSH_EVENT = "on_chat_message";
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserFriendMessageMapper userFriendMessageMapper;
    @Resource
    private SocketIOServer socketIOServer;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private AppConfiguration appConfiguration;
    @Resource
    private Balancer balancer;
    @Resource
    private SubscribeZK subscribeZK;

    public void start() {

        // 监听客户端连接
        socketIOServer.addConnectListener(client -> {

            try {
                log.info("------- 客户端连接 -------");
                String token = getParamsByClient(client);
                if (StringUtils.isEmpty(token)) {
                    // token为空
                    Result result = new Result();
                    result.setCode(NetdiskErrMsgConstant.GET_CHAT_SERVER_AND_TOKEN_IS_NULL);
                    client.sendEvent(PUSH_EVENT, result);
                    client.disconnect();
                    return;
                }
                UserNetdisk user = (UserNetdisk) redisUtils.get(token);
                if (user == null) {
                    // 用户未登陆或登陆过期
                    client.sendEvent(PUSH_EVENT, Result.IS_NOT_AUTH);
                    client.disconnect();
                    return;
                }
                SocketIOConnection socketIOConnection = new SocketIOConnection();
                socketIOConnection.setSocketIOClient(client);
                socketIOConnection.setToken(token);
                socketIOConnection.setUser(user);
                SocketIOHolder.put(token, socketIOConnection);

                // 保存路由
                RouteOfSaveRoutePojo routeOfSaveRoutePojo = new RouteOfSaveRoutePojo();
                routeOfSaveRoutePojo.setIp(InetAddress.getLocalHost().getHostAddress());
                routeOfSaveRoutePojo.setUserId(user.getId());
                routeOfSaveRoutePojo.setHttpPort(appConfiguration.getHttpPort());

                Map<String, String> routeServer = this.getRouteServer(user.getId().toString());
                if (routeServer == null) {
                    log.error("无可用route server");
                    return;
                }
                String requestUrl = "http://" + routeServer.get("ip") + ":" + routeServer.get("port") + appConfiguration.getSaveRoutePath();

                ResponseEntity<Result> resultResponseEntity = restTemplate.postForEntity(requestUrl, routeOfSaveRoutePojo, Result.class);
                log.info("resultResponseEntity: {}", resultResponseEntity);
                log.info("------- {} 上线 -------", user.getUsername());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

        // 监听客户端断开连接
        socketIOServer.addDisconnectListener(client -> {
            log.info("------- 客户端断开连接 -------");
            String token = getParamsByClient(client);
            if (StringUtils.isEmpty(token)) {
                return;
            }
            UserNetdisk user = SocketIOHolder.getUser(token);
            if (user != null) {
                log.info("------- {} 下线 -------", user.getUsername());
            }
            SocketIOHolder.remove(token);
            // 删除路由
            Map<String, String> routeServer = this.getRouteServer(user.getId().toString());
            if (routeServer == null) {
                log.error("无可用route server");
                return;
            }
            String requestUrl = "http://" + routeServer.get("ip") + ":" + routeServer.get("port") + appConfiguration.getDelRoutePath();
            ResponseEntity<Result> responseEntity = restTemplate.getForEntity(requestUrl + "?userId=" + user.getId(), Result.class);
            log.info("statusCode: {}", responseEntity);

        });

        // 处理自定义的事件，与连接监听类似
        socketIOServer.addEventListener(PUSH_EVENT, String.class, (client, data, ackSender) -> {
            log.info("收到消息：{}", data);
            JSONObject jsonObject = JSONObject.parseObject(data, JSONObject.class);
            String type = jsonObject.getString("type");
            Date createTime = jsonObject.getDate("createTime");
            if (type.equals(ChatMessageType.FRIEND.toString())) {

                UserFriendMessageShow userFriendMessageShow = JSONObject.parseObject(jsonObject.getJSONObject("content").toJSONString(), UserFriendMessageShow.class);
                ChatMessageBase<UserFriendMessageShow> chatMessageBase = new ChatMessageBase<>();
                chatMessageBase.setType(type);
                chatMessageBase.setCreateTime(createTime);
                chatMessageBase.setContent(userFriendMessageShow);
                messageHandler(chatMessageBase);
            }


        });
        socketIOServer.start();
    }

    /**
     * 处理消息
     * @param messageBase
     */
    public void messageHandler(ChatMessageBase<UserFriendMessageShow> messageBase) {
        switch (messageBase.getType()) {
            case "FRIEND":
                UserFriendMessageShow userFriendMessage = userFriendMessage = messageBase.getContent();
                Integer to = userFriendMessage.getTo();
                if (to != null) {
                    RouteOfSendMsgPojo routeOfSendMsgPojo = new RouteOfSendMsgPojo();
                    routeOfSendMsgPojo.setUserId(to);
                    routeOfSendMsgPojo.setChatMessageBase(messageBase);

                    Map<String, String> routeServer = this.getRouteServer(to.toString());
                    if (routeServer == null) {
                        log.error("无可用route server");
                        return;
                    }
                    String requestUrl = "http://" + routeServer.get("ip") + ":" + routeServer.get("port") + appConfiguration.getSendMsgToUserPath();
                    restTemplate.postForEntity(requestUrl, routeOfSendMsgPojo, Result.class);
                }
                // 将emoji转码能到数据库存储
                if (!StringUtils.isAnyEmpty(userFriendMessage.getContent())) {
                    userFriendMessage.setContent(EmojiParser.parseToAliases(userFriendMessage.getContent()));
                }
                // 保存聊天记录
                UserFriendMessage userFriendMessage_ = new UserFriendMessage();
                userFriendMessage_.setContent(userFriendMessage.getContent());
                userFriendMessage_.setFrom(userFriendMessage.getFrom());
                userFriendMessage_.setTo(userFriendMessage.getTo());
                userFriendMessage_.setFileId(userFriendMessage.getFileId());
                userFriendMessageMapper.insertSelective(userFriendMessage_);
                break;
            case "GROUP":
                break;
            case "FRIEND_APPLY_FOR":
                break;
            case "SYSTEM":
                break;
        }
    }

    /**
     * 发送消息
     * @param userId userId (要接收消息的用户id)
     * @param messageBase
     */
    public static void sendMessage(Integer userId, ChatMessageBase messageBase) {
        SocketIOConnection socketIOConnection = SocketIOHolder.getSocketIOConnection(userId);
        if (socketIOConnection != null) {
            SocketIOClient socketIOClient = socketIOConnection.getSocketIOClient();
            socketIOClient.sendEvent(PUSH_EVENT, messageBase);
        }
    }

    public void stop() {

        if (socketIOServer != null) {
            socketIOServer.stop();
            socketIOServer = null;
        }
    }

    /**
     * 获取client连接中的参数
     * @param client
     * @return
     */
    public String getParamsByClient(SocketIOClient client) {
        // 从请求的连接中拿出参数(token)
        Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
        List<String> list = params.get("X-Token");
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 获取route server
     * @param key userId
     * @return
     */
    public Map<String, String> getRouteServer(String key) {

        List<String> serverList = subscribeZK.getAll();
        if (serverList.size() == 0) {
            return null;
        }
        // 192.168.0.104:9527:8081 IP:HTTP端口
        String server = balancer.select(serverList, key);
        String[] meta = server.split(":");
        Map<String, String> data = new HashMap<>();
        data.put("ip", meta[0]);
        data.put("port", meta[1]);
        return data;
    }
}
