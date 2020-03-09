package group.xuxiake.common.zookeeper.balancer;

import java.util.List;
import java.util.Random;

/**
 * Author by xuxiake, Date on 2020/3/3 23:05.
 * PS: Not easy to write code, please indicate.
 * Description：随机算法
 */
public class RandomBalancer implements Balancer {

    @Override
    public String select(List<String> serverList, String key) {
        if(serverList == null){
            return null;
        }
        int size = serverList.size();
        if(size == 0){
            return null;
        }
        Random r = new Random();
        int index = r.nextInt(size);
        return serverList.get(index);
    }
}
