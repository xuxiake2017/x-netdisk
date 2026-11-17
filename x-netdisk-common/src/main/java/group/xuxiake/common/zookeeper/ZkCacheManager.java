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
        List<String> serverList = new ArrayList<>();
        for (String currentChild : currentChilds) {
            if (currentChild.startsWith("ip-") && currentChild.length() > 3) {
                serverList.add(currentChild.substring(3));
            }
        }
        cache.put(zkRoot, serverList);
    }
}
