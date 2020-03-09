package group.xuxiake.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("group.xuxiake.common.mapper")
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class XNetdiskWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(XNetdiskWebApplication.class, args);
    }

}
