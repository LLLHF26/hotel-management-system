package com.lhf.hotel.finance.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI financeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("finance-service")
                        .description("营收统计、财务报表、数据分析、退款管理的接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("hotel")))
                .externalDocs(new ExternalDocumentation()
                        .description("完整文档")
                        .url("https://doc.hotel.com"));
    }
}
