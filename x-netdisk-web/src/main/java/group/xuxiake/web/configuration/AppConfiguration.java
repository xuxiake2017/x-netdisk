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
    @Value("${application.route.sendMsgToUserPath}")
    private String sendMsgToUserPath;
    @Value("${application.route.quartz.delFilePath}")
    private String delFilePath;
    @Value("${application.route.quartz.delJobPath}")
    private String delJobPath;

    @Value("${application.captcha.prefix}")
    private String captchaPrefix;
    @Value("${application.captcha.timeout}")
    private Long captchaTimeout;

    @Value("${jodconverter.local.officeHome}")
    private String officeHome;
    @Value("${jodconverter.local.portNumbers}")
    private String portNumbers;

    @Resource
    private CustomConfiguration customConfiguration;
}
