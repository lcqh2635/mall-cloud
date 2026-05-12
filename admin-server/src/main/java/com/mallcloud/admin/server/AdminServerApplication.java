package com.mallcloud.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class AdminServerApplication {
    static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}

// SpringApplication 相关教程
// https://docs.springjava.cn/spring-boot/reference/features/spring-application.html