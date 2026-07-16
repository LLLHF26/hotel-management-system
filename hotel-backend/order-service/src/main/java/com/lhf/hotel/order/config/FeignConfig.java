package com.lhf.hotel.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            String userId = attrs.getRequest().getHeader("X-UserId");
            String username = attrs.getRequest().getHeader("X-Username");
            String role = attrs.getRequest().getHeader("X-Role");
            if (userId != null) template.header("X-UserId", userId);
            if (username != null) template.header("X-Username", username);
            if (role != null) template.header("X-Role", role);
        };
    }
}
