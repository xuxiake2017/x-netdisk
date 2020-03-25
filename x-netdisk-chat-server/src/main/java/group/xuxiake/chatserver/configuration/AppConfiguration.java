package group.xuxiake.chatserver.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    @Value("${application.zookeeper.IBLA}")
    private String IBLA;

    @Value("${server.port}")
    private String httpPort;
    // socket-io
    @Value("${application.socketIo.port}")
    private Integer socketIoPort;
    @Value("${application.socketIo.ip}")
    private String socketIoIp;
    @Value("${application.socketIo.maxFramePayloadLength}")
    private Integer maxFramePayloadLength;
    @Value("${application.socketIo.bossCount}")
    private Integer bossCount;
    @Value("${application.socketIo.workCount}")
    private Integer workCount;
    @Value("${application.socketIo.allowCustomRequests}")
    private Boolean allowCustomRequests;
    @Value("${application.socketIo.upgradeTimeout}")
    private Integer upgradeTimeout;
    @Value("${application.socketIo.pingTimeout}")
    private Integer pingTimeout;
    @Value("${application.socketIo.pingInterval}")
    private Integer pingInterval;

    @Value("${application.path.saveRoute}")
    private String saveRoutePath;
    @Value("${application.path.delRoute}")
    private String delRoutePath;
    @Value("${application.path.sendMsgToUser}")
    private String sendMsgToUserPath;
}
