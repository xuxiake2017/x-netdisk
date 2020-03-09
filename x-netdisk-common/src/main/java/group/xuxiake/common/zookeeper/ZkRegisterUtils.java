package group.xuxiake.common.zookeeper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

/**
 * Author by xuxiake, Date on 2020/3/1 20:05.
 * PS: Not easy to write code, please indicate.
 * Description： zookeeper 注册工具
 */
@Slf4j
@Data
public class ZkRegisterUtils {

    private ZkClient zkClient;
    /**
     * 创建父级节点
     */
    public void createRootNode(String path){
        boolean exists = zkClient.exists(path);
        if (exists){
            return;
        }

        //创建 root
        zkClient.createPersistent(path) ;
    }

    /**
     * 写入指定节点 临时目录
     *
     * @param path
     */
    public void createNode(String path) {
        zkClient.createEphemeral(path);
    }
}
