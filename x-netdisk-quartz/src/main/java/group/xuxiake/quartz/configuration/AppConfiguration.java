package group.xuxiake.quartz.configuration;

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
    @Value("${application.zookeeper.quartzRoot}")
    private String quartzRoot;
    @Value("${application.zookeeper.IBLA}")
    private String IBLA;
    @Value("${application.zookeeper.hostname}")
    private String hostname;

    @Value("${server.port}")
    private String httpPort;
}
