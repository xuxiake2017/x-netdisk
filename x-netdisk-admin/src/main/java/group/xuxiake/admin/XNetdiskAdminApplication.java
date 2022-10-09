package group.xuxiake.admin;

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
public class XNetdiskAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(XNetdiskAdminApplication.class, args);
    }

}
