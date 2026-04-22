package io.github.xiaocan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
@MapperScan("io.github.xiaocan.mapper")
public class XiaocanServer {
    public static void main(String[] args) {
        SpringApplication.run(XiaocanServer.class, args);
    }
}