package com.zsk.common.sentinel.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.zsk.common.core.domain.R;
import com.zsk.common.core.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sentinel 限流熔断异常处理器
 *
 * @author wuhuaming
 */
@Slf4j
@RestControllerAdvice
@Order(1)
public class SentinelBlockHandler {

    @ExceptionHandler(BlockException.class)
    public R<?> handleBlockException(BlockException e) {
        log.warn("Sentinel 限流: {}", e.getMessage());
        if (e instanceof FlowException) {
            return R.fail(ResultCode.RATE_LIMIT_ERROR.getCode(), "请求过于频繁，请稍后再试");
        }
        return R.fail("系统繁忙，请稍后再试");
    }
}
