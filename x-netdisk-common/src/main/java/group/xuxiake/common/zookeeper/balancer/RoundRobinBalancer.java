package group.xuxiake.common.zookeeper.balancer;

import java.util.List;

/**
 * Author by xuxiake, Date on 2020/3/3 23:07.
 * PS: Not easy to write code, please indicate.
 * Description：轮询算法
 */
public class RoundRobinBalancer implements Balancer {

    private static Integer pos = 0;
    @Override
    public String select(List<String> serverList, String key) {

        String server = null;
        synchronized (pos)
        {
            if (pos > serverList.size())
                pos = 0;
            server = serverList.get(pos);
            pos ++;
        }

        return server;
    }
}
