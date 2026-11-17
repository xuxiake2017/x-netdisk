package group.xuxiake.common.zookeeper;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 Curator 订阅 ZooKeeper 子节点变化。
 */
@Slf4j
@Data
public class SubscribeZK {

    private CuratorFramework curatorFramework;
    private ZkCacheManager zkCacheManager;
    private List<String> zkRoots;
    private final Map<String, PathChildrenCache> pathCaches = new ConcurrentHashMap<>();

    public synchronized void subscribeEvent() {
        if (zkRoots == null || zkRoots.isEmpty()) {
            return;
        }

        for (String zkRoot : zkRoots) {
            if (pathCaches.containsKey(zkRoot)) {
                continue;
            }
            ensureRootExists(zkRoot);
            PathChildrenCache pathCache = new PathChildrenCache(curatorFramework, zkRoot, false);
            pathCache.getListenable().addListener((client, event) -> {
                switch (event.getType()) {
                    case CHILD_ADDED:
                    case CHILD_UPDATED:
                    case CHILD_REMOVED:
                    case CONNECTION_RECONNECTED:
                        updateCache(zkRoot, pathCache);
                        break;
                    default:
                        break;
                }
            });
            try {
                pathCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
                pathCaches.put(zkRoot, pathCache);
                updateCache(zkRoot, pathCache);
            } catch (Exception e) {
                CloseableUtils.closeQuietly(pathCache);
                throw new IllegalStateException("订阅 ZooKeeper 节点失败: " + zkRoot, e);
            }
        }
    }

    public List<String> getAllNode(String zkRoot) {
        ensureRootExists(zkRoot);
        try {
            List<String> children = curatorFramework.getChildren().forPath(zkRoot);
            log.info("查询所有节点成功，root=[{}], children=[{}]", zkRoot, JSON.toJSONString(children));
            return children;
        } catch (Exception e) {
            throw new IllegalStateException("查询 ZooKeeper 节点失败: " + zkRoot, e);
        }
    }

    public List<String> getAll(String zkRoot) {
        Map<String, List<String>> cache = zkCacheManager.getCache();
        if (!cache.containsKey(zkRoot)) {
            zkCacheManager.updateCache(zkRoot, getAllNode(zkRoot));
        }
        return cache.get(zkRoot);
    }

    public void close() {
        pathCaches.values().forEach(CloseableUtils::closeQuietly);
        pathCaches.clear();
    }

    private void updateCache(String zkRoot, PathChildrenCache pathCache) {
        List<String> currentChildren = new ArrayList<>();
        for (ChildData childData : pathCache.getCurrentData()) {
            currentChildren.add(ZKPaths.getNodeFromPath(childData.getPath()));
        }
        log.info("更新本地服务缓存，root=[{}], children=[{}]", zkRoot, JSON.toJSONString(currentChildren));
        zkCacheManager.updateCache(zkRoot, currentChildren);
    }

    private void ensureRootExists(String zkRoot) {
        try {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkRoot);
        } catch (KeeperException.NodeExistsException ignored) {
            // 根节点已存在。
        } catch (Exception e) {
            throw new IllegalStateException("创建 ZooKeeper 订阅根节点失败: " + zkRoot, e);
        }
    }
}
