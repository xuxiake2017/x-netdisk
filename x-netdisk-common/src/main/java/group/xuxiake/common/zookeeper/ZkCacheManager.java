package group.xuxiake.common.zookeeper;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author by xuxiake, Date on 2020/3/3 22:56.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class ZkCacheManager {

    private Map<String, String> cache = new ConcurrentHashMap<>();

    public void addCache(String key) {
        cache.put(key, key);
    }


    /**
     * 更新所有缓存/先删除 再新增
     *
     * @param currentChilds
     */
    public void updateCache(List<String> currentChilds) {
        cache.clear();
        for (String currentChild : currentChilds) {
            String key = currentChild.split("-")[1];
            addCache(key);
        }
    }
}
