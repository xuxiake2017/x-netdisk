package group.xuxiake.common.zookeeper;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author by xuxiake, Date on 2020/3/13 16:40.
 * PS: Not easy to write code, please indicate.
 * Description：zookeeper 订阅
 */
@Slf4j
@Data
public class SubscribeZK {

    private ZkClient zkClient;
    private ZkCacheManager zkCacheManager;
    private List<String> zkRoots;

    /**
     * 订阅事件
     *
     */
    public void subscribeEvent() {

        if (zkRoots != null && zkRoots.size() > 0) {
            for (String zkRoot : zkRoots) {
                new Thread(() -> zkClient.subscribeChildChanges(zkRoot, (parentPath, currentChilds) -> {
                    log.info("清除/更新本地缓存 parentPath=【{}】,currentChilds=【{}】", parentPath, currentChilds.toString());

                    // 更新所有缓存/先删除 再新增
                    zkCacheManager.updateCache(zkRoot, currentChilds) ;
                })).start();
            }
        }
    }

    /**
     * 获取所有注册节点
     * @return
     */
    public List<String> getAllNode(String zkRoot){
        List<String> children = zkClient.getChildren(zkRoot);
        log.info("查询所有节点成功=【{}】", JSON.toJSONString(children));
        return children;
    }

    /**
     * 获取所有的服务列表
     *
     * @return
     */
    public List<String> getAll(String zkRoot) {

        Map<String, List<String>> cache = zkCacheManager.getCache();
        if (cache.size() == 0) {
            List<String> allNode = this.getAllNode(zkRoot);
            List<String> serverList = new ArrayList<>();
            for (String node : allNode) {
                String server = node.split("-")[1];
                serverList.add(server);
            }
            cache.put(zkRoot, serverList);
        }
        return cache.get(zkRoot);

    }
}
