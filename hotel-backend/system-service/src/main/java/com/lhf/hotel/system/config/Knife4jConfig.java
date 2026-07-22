package com.lhf.hotel.system.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI systemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("system-service")
                        .description("酒店级配置、系统参数、字典的接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("hotel")))
                .externalDocs(new ExternalDocumentation()
                        .description("完整文档")
                        .url("https://doc.hotel.com"));
    }
}
