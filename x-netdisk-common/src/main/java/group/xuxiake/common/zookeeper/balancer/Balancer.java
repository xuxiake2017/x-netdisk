package group.xuxiake.common.zookeeper.balancer;

import java.util.List;

/**
 * Author by xuxiake, Date on 2020/3/3 23:04.
 * PS: Not easy to write code, please indicate.
 * Description：负载均衡算法
 */
public interface Balancer {

    /**
     * 返回Server的IP地址
     * @param serverList 可用的serverList
     * @param key userId
     * @return
     */
    String select(List<String> serverList, String key);
}
