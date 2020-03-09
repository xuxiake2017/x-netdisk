package group.xuxiake.chatserver.configuration;

import group.xuxiake.chatserver.socketio.ChatSocketIOHandler;
import group.xuxiake.common.zookeeper.RegisterZK;
import group.xuxiake.common.zookeeper.SubscribeZK;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
public class AppPostConstruct {

    @Resource
    private RegisterZK registerZK;
    @Resource
    private SubscribeZK subscribeZK;
    @Resource
    private AppConfiguration appConfiguration;
    @Resource
    private ChatSocketIOHandler chatSocketIOHandler;
    @PostConstruct
    public void start() {
        chatSocketIOHandler.start();
        // 注册到zookeeper
        registerZK.register(appConfiguration.getSocketIoPort().toString(), appConfiguration.getHttpPort());
        // 订阅事件（route server变动）
        subscribeZK.subscribeEvent();
    }

    @PreDestroy
    public void destory() {
        chatSocketIOHandler.stop();
    }
}
