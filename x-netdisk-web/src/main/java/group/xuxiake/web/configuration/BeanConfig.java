package group.xuxiake.web.configuration;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.common.zookeeper.CuratorClientFactory;
import group.xuxiake.common.zookeeper.SubscribeZK;
import group.xuxiake.common.zookeeper.ZkCacheManager;
import group.xuxiake.common.zookeeper.balancer.Balancer;
import org.apache.curator.framework.CuratorFramework;
import org.jodconverter.spring.JodConverterBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

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
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        return new RestTemplate(requestFactory);
    }

    @Bean("mediaTaskExecutor")
    public ThreadPoolTaskExecutor mediaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(appConfiguration.getMediaCorePoolSize());
        executor.setMaxPoolSize(appConfiguration.getMediaMaxPoolSize());
        executor.setQueueCapacity(appConfiguration.getMediaQueueCapacity());
        executor.setThreadNamePrefix("media-convert-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("emailTaskExecutor")
    public ThreadPoolTaskExecutor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-send-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        return CuratorClientFactory.create(appConfiguration.getZkServer(),
                appConfiguration.getSessionTimeout(), appConfiguration.getConnectTimeout());
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

    @Bean(destroyMethod = "close")
    public SubscribeZK subscribeZK() {
        SubscribeZK subscribeZK = new SubscribeZK();
        subscribeZK.setCuratorFramework(this.curatorFramework());
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

    /**
     * JodConverter doc、xls、ppt、txt转pdf
     * @return
     */
    @Bean
    public JodConverterBean springJodConverter() {

        final JodConverterBean jodConverterBean = new JodConverterBean();
        jodConverterBean.setPortNumbers(appConfiguration.getPortNumbers());
        jodConverterBean.setOfficeHome(appConfiguration.getOfficeHome());
        return jodConverterBean;
    }
}
