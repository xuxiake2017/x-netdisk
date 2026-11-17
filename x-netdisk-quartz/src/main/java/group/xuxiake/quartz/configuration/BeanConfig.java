package group.xuxiake.quartz.configuration;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.common.zookeeper.CuratorClientFactory;
import group.xuxiake.common.zookeeper.RegisterZK;
import group.xuxiake.common.zookeeper.ZkRegisterUtils;
import group.xuxiake.common.zookeeper.balancer.Balancer;
import org.apache.curator.framework.CuratorFramework;
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

    @Bean(destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        return CuratorClientFactory.create(appConfiguration.getZkServer(),
                appConfiguration.getSessionTimeout(), appConfiguration.getConnectTimeout());
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

    @Bean
    public Balancer balancer() throws Exception {
        String ibla = appConfiguration.getIBLA();
        Balancer balancer = (Balancer) Class.forName(ibla).newInstance();
        return balancer;
    }


    @Bean
    public ZkRegisterUtils zkRegisterUtils() {
        ZkRegisterUtils zkRegisterUtils = new ZkRegisterUtils();
        zkRegisterUtils.setCuratorFramework(this.curatorFramework());
        zkRegisterUtils.setConnectTimeout(appConfiguration.getConnectTimeout());
        return zkRegisterUtils;
    }

    @Bean(destroyMethod = "close")
    public RegisterZK registerZK() {
        RegisterZK registerZK = new RegisterZK();
        // 注册quartzRoot
        registerZK.setZkRoot(appConfiguration.getQuartzRoot());
        registerZK.setZkRegistryUtils(this.zkRegisterUtils());
        return registerZK;
    }
}
