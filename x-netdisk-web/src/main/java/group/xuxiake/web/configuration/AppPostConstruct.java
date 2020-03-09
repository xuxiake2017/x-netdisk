package group.xuxiake.web.configuration;

import group.xuxiake.common.zookeeper.SubscribeZK;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class AppPostConstruct {

    @Resource
    private SubscribeZK subscribeZK;
    @PostConstruct
    public void start() {
        // 订阅事件（route server变动）
        subscribeZK.subscribeEvent();
    }
}
