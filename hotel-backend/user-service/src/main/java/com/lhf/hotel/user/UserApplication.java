package com.lhf.hotel.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.lhf.hotel.user", "com.lhf.hotel.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.lhf.hotel.common.feign")
@MapperScan("com.lhf.hotel.user.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
