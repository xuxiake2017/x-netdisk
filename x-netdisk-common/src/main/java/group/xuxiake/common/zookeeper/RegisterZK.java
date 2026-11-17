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

        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(zkRoot).append("/ip-").append(args[0]);
        for (int i = 1; i < args.length; i++) {
            stringBuffer.append(":").append(args[i]);
        }
        String path = stringBuffer.toString();
        // 同步完成首次注册；失败时让应用启动失败，由容器重启策略再次拉起，避免服务未注册却仍在运行。
        zkRegistryUtils.createRootNode(zkRoot);
        zkRegistryUtils.createNode(path);
        log.info("注册 ZooKeeper 成功，path=[{}]", path);
    }

    public void close() {
        zkRegistryUtils.close();
    }
}
