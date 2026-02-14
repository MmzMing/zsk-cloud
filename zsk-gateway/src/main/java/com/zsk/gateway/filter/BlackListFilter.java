package com.zsk.gateway.filter;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 黑名单过滤器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlackListFilter implements GlobalFilter, Ordered {

    private final RedisService redisService;

    /**
     * 执行过滤逻辑
     *
     * @param exchange 服务网络交换器
     * @param chain    过滤器链
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String remoteAddr = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        Set<String> blackList = redisService.getCacheSet(CacheConstants.GATEWAY_BLACKLIST_KEY);
        if (blackList != null && blackList.contains(remoteAddr)) {
            log.warn("黑名单IP拦截: {}", remoteAddr);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
