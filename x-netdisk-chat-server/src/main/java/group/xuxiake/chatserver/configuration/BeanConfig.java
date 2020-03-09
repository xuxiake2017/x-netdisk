package group.xuxiake.chatserver.configuration;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.common.zookeeper.RegisterZK;
import group.xuxiake.common.zookeeper.SubscribeZK;
import group.xuxiake.common.zookeeper.ZkCacheManager;
import group.xuxiake.common.zookeeper.ZkRegisterUtils;
import group.xuxiake.common.zookeeper.balancer.Balancer;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2020/3/1 21:39.
 * PS: Not easy to write code, please indicate.
 * Description：配置bean
 */
@Configuration
public class BeanConfig {

    @Resource
    private AppConfiguration appConfiguration;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ZkClient zkClient() {
        ZkClient zkClient = new ZkClient(appConfiguration.getZkServer(), appConfiguration.getConnectTimeout());
        return zkClient;
    }

    @Bean
    public SocketIOServer socketIOServer() {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setSocketConfig(socketConfig);
        config.setPort(appConfiguration.getSocketIoPort());
        config.setBossThreads(appConfiguration.getBossCount());
        config.setWorkerThreads(appConfiguration.getWorkCount());
        config.setAllowCustomRequests(appConfiguration.getAllowCustomRequests());
        config.setUpgradeTimeout(appConfiguration.getUpgradeTimeout());
        config.setPingTimeout(appConfiguration.getPingTimeout());
        config.setPingInterval(appConfiguration.getPingInterval());
        return new SocketIOServer(config);
    }

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericFastJsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public Balancer balancer() throws Exception {
        String ibla = appConfiguration.getIBLA();
        Balancer balancer = (Balancer) Class.forName(ibla).newInstance();
        return balancer;
    }
    @Bean
    public RedisUtils redisUtils(RedisTemplate redisTemplate) {
        RedisUtils redisUtils = new RedisUtils();
        redisUtils.setRedisTemplate(redisTemplate);
        return redisUtils;
    }

    @Bean
    public ZkRegisterUtils zkRegisterUtils() {
        ZkRegisterUtils zkRegisterUtils = new ZkRegisterUtils();
        zkRegisterUtils.setZkClient(this.zkClient());
        return zkRegisterUtils;
    }

    @Bean
    public RegisterZK registerZK() {
        RegisterZK registerZK = new RegisterZK();
        // 注册chatRoot
        registerZK.setZkRoot(appConfiguration.getChatRoot());
        registerZK.setZkRegistryUtils(this.zkRegisterUtils());
        return registerZK;
    }

    @Bean
    public ZkCacheManager zkCacheManager() {
        ZkCacheManager zkCacheManager = new ZkCacheManager();
        return zkCacheManager;
    }

    @Bean
    public SubscribeZK subscribeZK() {
        SubscribeZK subscribeZK = new SubscribeZK();
        subscribeZK.setZkClient(this.zkClient());
        // 订阅routeRoot
        subscribeZK.setZkRoot(appConfiguration.getRouteRoot());
        subscribeZK.setZkCacheManager(this.zkCacheManager());
        return subscribeZK;
    }
}
