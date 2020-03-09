package group.xuxiake.route.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
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
 * Author by xuxiake, Date on 2020/3/2 10:05.
 * PS: Not easy to write code, please indicate.
 * Description：
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
    public Balancer balancer() throws Exception {
        String ibla = appConfiguration.getIBLA();
        Balancer balancer = (Balancer) Class.forName(ibla).newInstance();
        return balancer;
    }

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        // 指定key value的序列化方式
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericFastJsonRedisSerializer());
        return redisTemplate;
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
        // 注册routeRoot
        registerZK.setZkRoot(appConfiguration.getRouteRoot());
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
        // 订阅chatRoot
        subscribeZK.setZkRoot(appConfiguration.getChatRoot());
        subscribeZK.setZkCacheManager(this.zkCacheManager());
        return subscribeZK;
    }
}
