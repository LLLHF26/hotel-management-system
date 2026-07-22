package com.lhf.hotel.order.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI orderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("order-service")
                        .description("订单管理、支付、退款、额外消费的接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("hotel")))
                .externalDocs(new ExternalDocumentation()
                        .description("完整文档")
                        .url("https://doc.hotel.com"));
    }
}
