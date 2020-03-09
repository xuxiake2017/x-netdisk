package group.xuxiake.web.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2020/3/2 10:05.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Component
@Data
public class AppConfiguration {

    @Value("${spring.shiro.sessionManager.globalSessionTimeout}")
    private Integer globalSessionTimeout;
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private Integer redisPort;
    @Value("${spring.mqtt.url}")
    private String mqttUrl;
    @Value("${spring.mqtt.client.inboundClientId}")
    private String inboundClientId;
    @Value("${spring.mqtt.client.outboundClientId}")
    private String outboundClientId;
    @Value("${spring.mqtt.topic.inboundTopic}")
    private String inboundTopic;
    @Value("${spring.mqtt.topic.outboundTopic}")
    private String outboundTopic;
    @Value("${spring.mqtt.completionTimeout}")
    private Integer completionTimeout;
    @Value("${fdfs.fdfs-nginx-server}")
    private String fdfsNginxServer;

    @Value("${application.zookeeper.zkServer}")
    private String zkServer;
    @Value("${application.zookeeper.connectTimeout}")
    private Integer connectTimeout;
    @Value("${application.zookeeper.routeRoot}")
    private String routeRoot;
    @Value("${application.zookeeper.IBLA}")
    private String IBLA;

    @Value("${application.route.getChatServerPath}")
    private String getChatServerPath;
    @Value("${application.route.findRouteByUserPath}")
    private String findRouteByUserPath;

    @Value("${application.captcha.prefix}")
    private String captchaPrefix;
    @Value("${application.captcha.timeout}")
    private Long captchaTimeout;

    @Resource
    private CustomConfiguration customConfiguration;
}
