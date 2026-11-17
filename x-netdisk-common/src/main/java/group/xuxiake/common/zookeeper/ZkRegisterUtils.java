package group.xuxiake.common.zookeeper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Author by xuxiake, Date on 2020/3/1 20:05.
 * PS: Not easy to write code, please indicate.
 * Description： zookeeper 注册工具
 */
@Slf4j
@Data
public class ZkRegisterUtils {

    private CuratorFramework curatorFramework;
    private int connectTimeout;
    private final Map<String, PersistentNode> registeredNodes = new ConcurrentHashMap<>();
    /**
     * 创建父级节点
     */
    public void createRootNode(String path) {
        try {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
        } catch (KeeperException.NodeExistsException ignored) {
            // 多个服务同时启动时，根节点可能已经由其他服务创建。
        } catch (Exception e) {
            throw new IllegalStateException("创建 ZooKeeper 根节点失败: " + path, e);
        }
    }

    /**
     * 写入指定节点 临时目录
     *
     * @param path
     */
    public synchronized void createNode(String path) {
        if (registeredNodes.containsKey(path)) {
            return;
        }

        PersistentNode node = new PersistentNode(
                curatorFramework, CreateMode.EPHEMERAL, false, path, new byte[0]);
        node.start();
        try {
            if (!node.waitForInitialCreate(connectTimeout, TimeUnit.MILLISECONDS)) {
                CloseableUtils.closeQuietly(node);
                throw new IllegalStateException("等待 ZooKeeper 注册节点创建超时: " + path);
            }
            registeredNodes.put(path, node);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CloseableUtils.closeQuietly(node);
            throw new IllegalStateException("等待 ZooKeeper 注册节点时线程被中断: " + path, e);
        } catch (Exception e) {
            CloseableUtils.closeQuietly(node);
            throw new IllegalStateException("创建 ZooKeeper 注册节点失败: " + path, e);
        }
    }

    public void close() {
        registeredNodes.values().forEach(CloseableUtils::closeQuietly);
        registeredNodes.clear();
    }
}
