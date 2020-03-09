package group.xuxiake.chatserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("group.xuxiake.common.mapper")
@SpringBootApplication
public class XNetdiskChatServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XNetdiskChatServerApplication.class, args);
    }

}
