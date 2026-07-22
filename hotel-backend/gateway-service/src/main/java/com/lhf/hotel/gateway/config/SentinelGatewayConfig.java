package com.lhf.hotel.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson2.JSON;
import com.lhf.hotel.common.result.Result;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

/**
 * Sentinel 限流降级配置 — 触发限流时返回统一 JSON + 初始化限流规则
 */
@Configuration
public class SentinelGatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(SentinelGatewayConfig.class);

    @PostConstruct
    public void init() {
        initBlockHandler();
        initGatewayRules();
    }

    private void initBlockHandler() {
        BlockRequestHandler handler = (ServerWebExchange exchange, Throwable t) -> {
            Result<Void> result = Result.fail(429, "请求过于频繁，请稍后重试");
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(JSON.toJSONString(result));
        };
        GatewayCallbackManager.setBlockHandler(handler);
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("order-service")
                .setCount(100).setIntervalSec(1));
        rules.add(new GatewayFlowRule("user-service")
                .setCount(200).setIntervalSec(1));
        rules.add(new GatewayFlowRule("room-service")
                .setCount(200).setIntervalSec(1));
        rules.add(new GatewayFlowRule("finance-service")
                .setCount(200).setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
        log.info("Sentinel 网关限流规则已加载: {} 条", rules.size());
    }
}
