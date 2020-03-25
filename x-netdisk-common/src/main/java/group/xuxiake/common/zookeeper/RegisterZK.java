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
     * @param args socketIp socketPort httpPort
     */
    public void register(String ...args) {

        try {
            StringBuffer stringBuffer = new StringBuffer();
            //获得本机IP
            String addr = InetAddress.getLocalHost().getHostAddress();
            stringBuffer.append(zkRoot + "/ip-" + addr);
            for (int i = 0; i < args.length; i++) {
                if (i == 0) {
                    if ("LOCAL".equalsIgnoreCase(args[0])) {
                        stringBuffer.append(":" + addr);
                    } else {
                        stringBuffer.append(":" + args[i]);
                    }
                }
                if (i > 0) {
                    stringBuffer.append(":" + args[i]);
                }
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
