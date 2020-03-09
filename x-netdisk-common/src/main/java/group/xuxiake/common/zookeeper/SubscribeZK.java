package group.xuxiake.common.zookeeper;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author by xuxiake, Date on 2020/3/1 20:07.
 * PS: Not easy to write code, please indicate.
 * Description： zookeeper 注册
 */
@Slf4j
@Data
public class SubscribeZK {

    private ZkClient zkClient;
    private ZkCacheManager zkCacheManager;
    private String zkRoot;

    /**
     * 订阅事件
     *
     */
    public void subscribeEvent() {

        new Thread(() -> zkClient.subscribeChildChanges(zkRoot, (parentPath, currentChilds) -> {
            log.info("清除/更新本地缓存 parentPath=【{}】,currentChilds=【{}】", parentPath, currentChilds.toString());

            // 更新所有缓存/先删除 再新增
            zkCacheManager.updateCache(currentChilds) ;
        })).start();
    }

    /**
     * 获取所有注册节点
     * @return
     */
    public List<String> getAllNode(){
        List<String> children = zkClient.getChildren(zkRoot);
        log.info("查询所有节点成功=【{}】", JSON.toJSONString(children));
        return children;
    }

    /**
     * 获取所有的服务列表
     *
     * @return
     */
    public List<String> getAll() {

        List<String> list = new ArrayList<>();

        Map<String, String> cache = zkCacheManager.getCache();
        if (cache.size() == 0) {
            List<String> allNode = this.getAllNode();
            for (String node : allNode) {
                String key = node.split("-")[1];
                zkCacheManager.addCache(key);
            }
        }
        for (Map.Entry<String, String> entry : cache.entrySet()) {
            list.add(entry.getKey());
        }
        return list;

    }
}
