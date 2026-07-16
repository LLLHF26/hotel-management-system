package com.lhf.hotel.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 路由配置（Java DSL 方式，与 application.yml 二选一）
 *
 * 此处仅作为代码式路由的参考框架，默认使用 YAML 配置。
 */
@Configuration
public class RouteConfig {

    // @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/auth/**", "/api/user/**", "/api/customer/**")
                        .uri("lb://user-service"))
                .route("room-service", r -> r
                        .path("/api/room/**")
                        .uri("lb://room-service"))
                .route("order-service", r -> r
                        .path("/api/order/**")
                        .uri("lb://order-service"))
                .route("finance-service", r -> r
                        .path("/api/finance/**")
                        .uri("lb://finance-service"))
                .build();
    }
}
