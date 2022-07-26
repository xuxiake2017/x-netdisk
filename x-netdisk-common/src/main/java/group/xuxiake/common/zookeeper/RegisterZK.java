package group.xuxiake.common.zookeeper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
     * @param args hostname socketIp socketPort httpPort
     */
    public void register(String ...args) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(zkRoot + "/ip-" + args[0]);
        for (int i = 1; i < args.length; i++) {
            stringBuffer.append(":" + args[i]);
        }
        String path = stringBuffer.toString();
        new Thread(() -> {
            //创建父节点
            zkRegistryUtils.createRootNode(zkRoot);
            zkRegistryUtils.createNode(path);
            log.info("注册 zookeeper 成功，msg=[{}]", path);
        }).start();
    }
}
