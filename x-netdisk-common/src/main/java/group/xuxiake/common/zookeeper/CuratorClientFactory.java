package group.xuxiake.common.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 创建并启动 Curator 客户端。
 */
@Slf4j
public final class CuratorClientFactory {

    private static final int RETRY_BASE_SLEEP_MS = 1000;
    private static final int RETRY_MAX_COUNT = 10;
    private static final int RETRY_MAX_SLEEP_MS = 10000;

    private CuratorClientFactory() {
    }

    public static CuratorFramework create(String zkServer, int sessionTimeout, int connectTimeout) {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkServer)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectTimeout)
                .retryPolicy(new ExponentialBackoffRetry(
                        RETRY_BASE_SLEEP_MS, RETRY_MAX_COUNT, RETRY_MAX_SLEEP_MS))
                .build();
        client.getConnectionStateListenable().addListener((curatorFramework, state) ->
                log.info("ZooKeeper 连接状态变化，state=[{}]", state));
        client.start();
        return client;
    }
}
