package com.zsk.gateway.handler;

import com.zsk.common.core.domain.R;
import com.zsk.common.core.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一异常处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Order(-1)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    /**
     * 异常处理逻辑实现
     *
     * @param exchange 服务网络交换器
     * @param ex       异常对象
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        String msg;
        HttpStatusCode statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof NotFoundException) {
            msg = "服务未找到";
            statusCode = HttpStatus.NOT_FOUND;
        } else if (ex instanceof NoResourceFoundException) {
            msg = "资源未找到";
            statusCode = HttpStatus.NOT_FOUND;
        } else if (ex instanceof ResponseStatusException responseStatusException) {
            msg = responseStatusException.getReason();
            if (msg == null) {
                msg = responseStatusException.getMessage();
            }
            statusCode = responseStatusException.getStatusCode();
        } else {
            msg = "内部服务器错误";
        }

        log.error("[网关异常处理]请求路径:{}, 状态码:{}, 异常信息:{}", exchange.getRequest().getPath(), statusCode, ex.getMessage());

        response.setStatusCode(statusCode);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        final String finalMsg = msg;
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            return bufferFactory.wrap(JsonUtil.toJsonString(R.fail(finalMsg)).getBytes());
        }));
    }
}
