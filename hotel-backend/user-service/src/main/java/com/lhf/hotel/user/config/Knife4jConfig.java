package com.lhf.hotel.user.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI userOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("user-service")
                        .description("员工管理、会员管理、认证鉴权的接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("hotel")))
                .externalDocs(new ExternalDocumentation()
                        .description("完整文档")
                        .url("https://doc.hotel.com"));
    }
}
