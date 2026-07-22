package com.lhf.hotel.room.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI roomOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("room-service")
                        .description("房间管理、房型管理、打扫调度、维修记录的接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("hotel")))
                .externalDocs(new ExternalDocumentation()
                        .description("完整文档")
                        .url("https://doc.hotel.com"));
    }
}
