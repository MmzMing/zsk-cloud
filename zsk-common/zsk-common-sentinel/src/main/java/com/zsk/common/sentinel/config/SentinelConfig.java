package com.zsk.common.sentinel.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.enums.ResultCode;
import com.zsk.common.sentinel.handler.SentinelBlockHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Sentinel 配置
 *
 * @author wuhuaming
 */
@AutoConfiguration
public class SentinelConfig {

    @Bean
    public SentinelBlockHandler sentinelBlockHandler() {
        return new SentinelBlockHandler();
    }

    @Bean
    public BlockRequestHandler blockRequestHandler() {
        return new BlockRequestHandler();
    }

    /**
     * Gateway 熔断降级处理器
     */
    public static class BlockRequestHandler implements com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler {
        @Override
        public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
            if (t instanceof BlockException) {
                return ServerResponse.status(429)
                        .bodyValue(R.fail(ResultCode.RATE_LIMIT_ERROR.getCode(), "请求过于频繁，请稍后再试"));
            }
            if (t instanceof DegradeException) {
                return ServerResponse.status(503)
                        .bodyValue(R.fail(ResultCode.DEGRADE_ERROR.getCode(), "服务暂时不可用，请稍后再试"));
            }
            return ServerResponse.status(500)
                    .bodyValue(R.fail("系统繁忙，请稍后再试"));
        }
    }
}
