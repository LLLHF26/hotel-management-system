package com.lhf.hotel.gateway.handler;

import com.alibaba.fastjson2.JSON;
import com.lhf.hotel.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

@Order(-1)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getURI().getPath();
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof ConnectException) {
                log.warn("下游服务不可用 — path={} msg={}", path, cause.getMessage());
                return writeResponse(exchange, HttpStatus.SERVICE_UNAVAILABLE, 503, "服务暂时不可用，请稍后重试");
            }
            cause = cause.getCause();
        }

        log.error("网关异常 — path={}", path, ex);
        return writeResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, 500, "服务器内部错误");
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, int code, String msg) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = JSON.toJSONString(Result.fail(code, msg)).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
