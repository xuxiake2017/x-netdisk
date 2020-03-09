package group.xuxiake.common.zookeeper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Author by xuxiake, Date on 2020/3/1 20:07.
 * PS: Not easy to write code, please indicate.
 * Description： zookeeper 注册
 */
@Slf4j
@Data
public class RegisterZK {

    private ZkRegisterUtils zkRegistryUtils;
    private String zkRoot;

    /**
     * zk注册
     * @param socketPort
     * @param httpPort
     */
    public void register(String socketPort, String httpPort) {

        try {
            StringBuffer stringBuffer = new StringBuffer();
            //获得本机IP
            String addr = InetAddress.getLocalHost().getHostAddress();
            stringBuffer.append(zkRoot + "/ip-" + addr);
            if (!StringUtils.isAnyEmpty(socketPort)) {
                stringBuffer.append(":" + socketPort);
            }
            if (!StringUtils.isAnyEmpty(httpPort)) {
                stringBuffer.append(":" + httpPort);
            }
            String path = stringBuffer.toString();
            new Thread(() -> {
                //创建父节点
                zkRegistryUtils.createRootNode(zkRoot);
                zkRegistryUtils.createNode(path);
                log.info("注册 zookeeper 成功，msg=[{}]", path);
            }).start();
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }


    }
}
