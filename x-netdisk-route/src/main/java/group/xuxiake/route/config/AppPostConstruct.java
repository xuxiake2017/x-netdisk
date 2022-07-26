package group.xuxiake.route.config;

import group.xuxiake.common.zookeeper.RegisterZK;
import group.xuxiake.common.zookeeper.SubscribeZK;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class AppPostConstruct {

    @Resource
    private RegisterZK registerZK;
    @Resource
    private SubscribeZK subscribeZK;
    @Resource
    private AppConfiguration appConfiguration;
    @PostConstruct
    public void start() {
        // 注册到zookeeper
        registerZK.register(appConfiguration.getHostname(), appConfiguration.getHttpPort());
        // 订阅事件（chat server变动）
        subscribeZK.subscribeEvent();
    }
}
