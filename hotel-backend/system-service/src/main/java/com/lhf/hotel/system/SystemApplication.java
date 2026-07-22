package com.lhf.hotel.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * system-service 启动类。
 * 职责：酒店级配置、系统参数、字典等基础配置信息（跨业务域共享）。
 */
@SpringBootApplication(scanBasePackages = {"com.lhf.hotel.system", "com.lhf.hotel.common"})
@EnableDiscoveryClient
@MapperScan("com.lhf.hotel.system.mapper")
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
