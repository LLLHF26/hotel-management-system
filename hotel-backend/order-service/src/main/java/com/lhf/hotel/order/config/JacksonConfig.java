package com.lhf.hotel.order.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 将 Long / long 类型在 JSON 序列化时输出为字符串，
 * 避免前端 JavaScript 在解析 19 位雪花 ID（超过 2^53 安全整数范围）时发生精度丢失。
 * 这样订单 / 房间 / 会员等雪花 ID 以字符串形式传给前端，URL 拼接无精度损失，
 * 后端 @PathVariable Long 又能从字符串精确还原。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonLongToStringCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(long.class, ToStringSerializer.instance);
    }
}
