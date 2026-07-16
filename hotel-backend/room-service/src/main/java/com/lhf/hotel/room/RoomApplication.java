package com.lhf.hotel.room;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.lhf.hotel.room", "com.lhf.hotel.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.lhf.hotel.common.feign")
@EnableScheduling
@MapperScan("com.lhf.hotel.room.mapper")
public class RoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomApplication.class, args);
    }
}
