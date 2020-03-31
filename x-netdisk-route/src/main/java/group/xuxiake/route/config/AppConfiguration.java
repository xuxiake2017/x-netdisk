package group.xuxiake.route.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Author by xuxiake, Date on 2020/3/9 12:31.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
@Component
public class AppConfiguration {

    @Value("${application.zookeeper.zkServer}")
    private String zkServer;
    @Value("${application.zookeeper.connectTimeout}")
    private Integer connectTimeout;
    @Value("${application.zookeeper.chatRoot}")
    private String chatRoot;
    @Value("${application.zookeeper.routeRoot}")
    private String routeRoot;
    @Value("${application.zookeeper.quartzRoot}")
    private String quartzRoot;
    @Value("${application.zookeeper.IBLA}")
    private String IBLA;
    @Value("${application.route.chat.chatRoutePrefix}")
    private String chatRoutePrefix;
    @Value("${application.route.chat.sendMsgPath}")
    private String chatSendMsgPath;
    @Value("${application.route.quartz.delFilePath}")
    private String delFilePath;
    @Value("${application.route.quartz.delJobPath}")
    private String delJobPath;

    @Value("${server.port}")
    private String httpPort;
}
