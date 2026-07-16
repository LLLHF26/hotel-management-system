package com.lhf.hotel.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.lhf.hotel.order", "com.lhf.hotel.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.lhf.hotel.common.feign", "com.lhf.hotel.order.feign"})
@EnableScheduling
@MapperScan("com.lhf.hotel.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
