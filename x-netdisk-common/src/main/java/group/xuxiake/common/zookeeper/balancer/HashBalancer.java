package group.xuxiake.common.zookeeper.balancer;

import java.util.List;

/**
 * Author by xuxiake, Date on 2020/3/4 9:50.
 * PS: Not easy to write code, please indicate.
 * Description：hash算法
 */
public class HashBalancer implements Balancer{
    @Override
    public String select(List<String> serverList, String key) {

        int hashCode = key.hashCode();
        int serverListSize = serverList.size();
        int serverPos = hashCode % serverListSize;

        return serverList.get(serverPos);
    }
}
