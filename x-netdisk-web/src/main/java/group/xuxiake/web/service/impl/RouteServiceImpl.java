package group.xuxiake.web.service.impl;

import group.xuxiake.common.entity.RouteShowSimple;
import group.xuxiake.common.zookeeper.SubscribeZK;
import group.xuxiake.common.zookeeper.balancer.Balancer;
import group.xuxiake.web.service.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author by xuxiake, Date on 2020/3/5 23:58.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Slf4j
@Service("routeService")
public class RouteServiceImpl implements RouteService {

    @Resource
    private SubscribeZK subscribeZK;
    @Resource
    private Balancer balancer;

    /**
     * 获取route server
     * @return
     */
    @Override
    public RouteShowSimple getRouteServer(String key) {


        List<String> serverList = subscribeZK.getAll();
        if (serverList.size() == 0) {
            return null;
        }
        // 192.168.0.104:9527:8081 IP:HTTP端口
        String server = balancer.select(serverList, key);
        String[] meta = server.split(":");
        String ip = meta[0];
        String port = meta[1];
        RouteShowSimple routeShowSimple = new RouteShowSimple();
        routeShowSimple.setIp(ip);
        routeShowSimple.setPort(port);
        return routeShowSimple;
    }
}
