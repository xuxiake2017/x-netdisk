package group.xuxiake.common.zookeeper;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author by xuxiake, Date on 2020/3/13 16:41.
 * PS: Not easy to write code, please indicate.
 * Description：保存服务器列表
 */
@Data
public class ZkCacheManager {

    private Map<String, List<String>> cache = new ConcurrentHashMap<>();

    /**
     * 更新所有缓存/先删除 再新增
     *
     * @param currentChilds
     */
    public void updateCache(String zkRoot, List<String> currentChilds) {
        cache.remove(zkRoot);
        List<String> serverList = new ArrayList<>();
        for (String currentChild : currentChilds) {
            String server = currentChild.split("-")[1];
            serverList.add(server);
        }
        cache.put(zkRoot, serverList);
    }
}
