package com.zjx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@ServletComponentScan
@MapperScan("com.zjx.mapper")
@EnableScheduling
@SpringBootApplication
public class SpringbootWordmapApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootWordmapApplication.class, args);
        System.out.println("欢迎光临*****************************************************");
    }

}
