package group.xuxiake.web.configuration;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.common.zookeeper.SubscribeZK;
import group.xuxiake.common.zookeeper.ZkCacheManager;
import group.xuxiake.common.zookeeper.balancer.Balancer;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Author by xuxiake, Date on 2020/3/2 10:05.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Configuration
public class BeanConfig {

    @Resource
    private AppConfiguration appConfiguration;

    /**
     * 加载自定义的yml文件
     * @return
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-custom.yml"));
        configurer.setProperties(yaml.getObject());
        return configurer;
    }

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
    public ZkCacheManager zkCacheManager() {
        ZkCacheManager zkCacheManager = new ZkCacheManager();
        return zkCacheManager;
    }

    @Bean
    public SubscribeZK subscribeZK() {
        SubscribeZK subscribeZK = new SubscribeZK();
        subscribeZK.setZkClient(this.zkClient());
        // 订阅routeRoot
        List<String> roots = new ArrayList<>();
        roots.add(appConfiguration.getRouteRoot());
        subscribeZK.setZkRoots(roots);
        subscribeZK.setZkCacheManager(this.zkCacheManager());
        return subscribeZK;
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
    public RedisUtils redisUtils(RedisTemplate redisTemplate) {
        RedisUtils redisUtils = new RedisUtils();
        redisUtils.setRedisTemplate(redisTemplate);
        return redisUtils;
    }
}
