package com.zsk.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 日志过滤器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Component
public class LogFilter implements GlobalFilter, Ordered {

    /**
     * 执行过滤逻辑
     *
     * @param exchange 服务网络交换器
     * @param chain    过滤器链
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getId();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String remoteAddr = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        log.info("请求开始 - RequestId: {}, Method: {}, Path: {}, RemoteAddr: {}",
                requestId, method, path, remoteAddr);

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).doFinally(signalType -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;

            log.info("请求结束 - RequestId: {}, Method: {}, Path: {}, StatusCode: {}, Duration: {}ms",
                    requestId, method, path, statusCode, duration);
        });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
