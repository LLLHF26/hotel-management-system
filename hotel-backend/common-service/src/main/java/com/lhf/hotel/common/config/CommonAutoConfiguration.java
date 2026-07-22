package com.lhf.hotel.common.config;

import com.lhf.hotel.common.util.OssUtil;
import com.lhf.hotel.common.util.RedisUtil;
import com.lhf.hotel.common.util.SchedulerLock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(OssProperties.class)
public class CommonAutoConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public RedisUtil redisUtil(StringRedisTemplate stringRedisTemplate) {
        return new RedisUtil(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public SchedulerLock schedulerLock(StringRedisTemplate stringRedisTemplate) {
        return new SchedulerLock(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(prefix = "alioss", name = "endpoint")
    public OssUtil ossUtil(OssProperties ossProperties) {
        return new OssUtil(ossProperties);
    }
}
