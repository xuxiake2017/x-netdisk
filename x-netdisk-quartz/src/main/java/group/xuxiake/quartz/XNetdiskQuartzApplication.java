package group.xuxiake.quartz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("group.xuxiake.common.mapper")
@EnableScheduling
@SpringBootApplication
public class XNetdiskQuartzApplication {

    public static void main(String[] args) {
        SpringApplication.run(XNetdiskQuartzApplication.class, args);
    }

}
