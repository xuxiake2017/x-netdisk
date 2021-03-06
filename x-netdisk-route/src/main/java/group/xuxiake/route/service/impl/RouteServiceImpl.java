package group.xuxiake.route.service.impl;

import group.xuxiake.common.entity.FileRecycle;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.RouteShowSimple;
import group.xuxiake.common.entity.route.RouteOfSaveRoutePojo;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.common.zookeeper.SubscribeZK;
import group.xuxiake.common.zookeeper.balancer.Balancer;
import group.xuxiake.route.config.AppConfiguration;
import group.xuxiake.route.service.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author by xuxiake, Date on 2020/3/5 23:58.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Slf4j
@Service("routeService")
public class RouteServiceImpl implements RouteService {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AppConfiguration appConfiguration;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private SubscribeZK subscribeZK;
    @Resource
    private Balancer balancer;

    /**
     * 保存路由
     * @param param
     * @return
     */
    @Override
    public Result saveRoute(RouteOfSaveRoutePojo param) {

        log.info(param.toString());
        String key = appConfiguration.getChatRoutePrefix() + param.getUserId();
        redisUtils.set(key, param,null);
        return Result.SUCCESS;
    }

    /**
     * 删除路由
     * @param userId
     * @return
     */
    @Override
    public Result delRoute(Integer userId) {

        log.info(userId.toString());
        String key = appConfiguration.getChatRoutePrefix() + userId.toString();
        redisUtils.del(key);
        return Result.SUCCESS;
    }

    /**
     * 给用户发送消息
     * @param param
     * @return
     */
    @Override
    public Result sendMsgToUser(RouteOfSendMsgPojo param) {

        Integer userId = param.getUserId();
        RouteOfSaveRoutePojo routeOfSaveRoutePojo = (RouteOfSaveRoutePojo) this.findRouteByUser(userId).getData();
        if (routeOfSaveRoutePojo != null) {
            String requestUrl = "http://" + routeOfSaveRoutePojo.getIp() + ":" + routeOfSaveRoutePojo.getHttpPort() + appConfiguration.getChatSendMsgPath();
            ResponseEntity<Result> responseEntity = restTemplate.postForEntity(requestUrl, param, Result.class);
            return responseEntity.getBody();
        } else {
            return Result.SUCCESS;
        }
    }

    /**
     * 根据userId查找路由
     * @param userId
     * @return
     */
    @Override
    public Result findRouteByUser(Integer userId) {

        Result result = new Result();
        String key = appConfiguration.getChatRoutePrefix() + userId.toString();
        RouteOfSaveRoutePojo routeOfSaveRoutePojo = (RouteOfSaveRoutePojo) redisUtils.get(key);
        result.setData(routeOfSaveRoutePojo);
        return result;
    }

    /**
     * 获取可用的chat server
     * @param sessionId
     * @return
     */
    @Override
    public Result getChatServer(String sessionId) {

        List<String> serverList = subscribeZK.getAll(appConfiguration.getChatRoot());
        if (serverList.size() == 0) {
            Result result = new Result();
            result.setCode(NetdiskErrMsgConstant.UN_AVAILABLE_ROUTE_SERVER);
            return result;
        }
        // 192.168.0.104:192.168.0.104:9527:8081 httpIP:socketIp:socket端口:HTTP端口
        String server = balancer.select(serverList, sessionId);
        String[] meta = server.split(":");
        Map<String, String> data = new HashMap<>();
        data.put("ip", meta[1]);
        data.put("port", meta[2]);
        Result result = new Result();
        result.setData(data);
        return result;

    }

    /**
     * 获取 quartz server
     * @param key
     * @return
     */
    @Override
    public RouteShowSimple getQuartzServer(String key) {

        List<String> serverList = subscribeZK.getAll(appConfiguration.getQuartzRoot());
        if (serverList.size() == 0) {
            return null;
        }
        // 192.168.0.104:8081 httpIP:HTTP端口
        String server = balancer.select(serverList, key);
        String[] meta = server.split(":");
        RouteShowSimple route = new RouteShowSimple();
        route.setIp(meta[0]);
        route.setPort(meta[1]);
        return route;
    }

    @Override
    public Result delFile(FileRecycle recycle) {

        RouteShowSimple routeServer = this.getQuartzServer(recycle.getRecycleId().toString());
        String requestUrl = "http://" + routeServer.getIp() + ":" + routeServer.getPort() + appConfiguration.getDelFilePath();
        ResponseEntity<Result> responseEntity = restTemplate.postForEntity(requestUrl, recycle, Result.class);
        return responseEntity.getBody();
    }

    @Override
    public Result delJob(FileRecycle recycle) {

        RouteShowSimple routeServer = this.getQuartzServer(recycle.getRecycleId().toString());
        String requestUrl = "http://" + routeServer.getIp() + ":" + routeServer.getPort() + appConfiguration.getDelJobPath();
        ResponseEntity<Result> responseEntity = restTemplate.postForEntity(requestUrl, recycle, Result.class);
        return responseEntity.getBody();
    }
}
