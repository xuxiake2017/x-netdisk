package group.xuxiake.quartz.configuration;

import group.xuxiake.common.zookeeper.RegisterZK;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
public class AppPostConstruct {

    @Resource
    private RegisterZK registerZK;
    @Resource
    private AppConfiguration appConfiguration;
    @PostConstruct
    public void start() {
        // 注册到zookeeper
        registerZK.register(appConfiguration.getHttpPort());
    }

    @PreDestroy
    public void destory() {}
}
