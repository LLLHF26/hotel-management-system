package com.lhf.hotel.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * JWT 鉴权全局过滤器 — 对所有请求校验 Token（白名单除外）
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);

    /** Token 请求头 */
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /** 白名单路径（GET 请求可以匿名访问） */
    private static final Set<String> WHITE_LIST = Set.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/customer/register",
            "/api/room/type/",
            "/api/room/list",
            "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. 白名单放行
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        // 2. 提取 Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return response401(exchange, "未登录，请先登录");
        }
        String token = authHeader.substring(BEARER_PREFIX.length());

        // 3. 校验 Token
        if (!JwtUtil.validate(token)) {
            return response401(exchange, "Token 无效或已过期");
        }

        // 4. 将 userId / role 写入请求头，传递给下游服务
        exchange = exchange.mutate()
                .request(r -> r.header("X-UserId", String.valueOf(JwtUtil.getUserId(token)))
                               .header("X-Username", JwtUtil.getUsername(token))
                               .header("X-Role", JwtUtil.getUserRole(token)))
                .build();

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级，早于其他过滤器
    }

    /** 返回 401 JSON 响应 */
    private Mono<Void> response401(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.fail(401, msg);
        byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }
}
