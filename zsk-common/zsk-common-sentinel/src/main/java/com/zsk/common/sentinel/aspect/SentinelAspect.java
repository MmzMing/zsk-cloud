package com.zsk.common.sentinel.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zsk.common.core.enums.ResultCode;
import com.zsk.common.core.exception.BaseException;
import com.zsk.common.core.exception.RateLimitException;
import com.zsk.common.sentinel.annotation.CircuitBreaker;
import com.zsk.common.sentinel.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Sentinel 限流熔断切面
 *
 * @author wuhuaming
 */
@Slf4j
@Aspect
@Component
@Order(2)
public class SentinelAspect {

    @Around("@annotation(rateLimit)")
    public Object aroundRateLimit(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String resourceName = getResourceName(point, rateLimit.resource());
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, EntryType.IN, 1);
            return point.proceed();
        } catch (BlockException e) {
            log.warn("Sentinel 限流: 资源={}, 规则={}", resourceName, rateLimit);
            throw new RateLimitException("请求过于频繁，请稍后再试");
        } finally {
            if (entry != null) {
                entry.exit(1, EntryType.IN);
            }
        }
    }

    @Around("@annotation(circuitBreaker)")
    public Object aroundCircuitBreaker(ProceedingJoinPoint point, CircuitBreaker circuitBreaker) throws Throwable {
        String resourceName = getResourceName(point, circuitBreaker.resource());
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, EntryType.IN, 1);
            return point.proceed();
        } catch (BlockException e) {
            log.warn("Sentinel 熔断: 资源={}, 规则={}", resourceName, circuitBreaker);
            throw new BaseException(ResultCode.CIRCUIT_BREAKER_ERROR.getCode(), "服务暂时不可用，请稍后再试");
        } finally {
            if (entry != null) {
                entry.exit(1, EntryType.IN);
            }
        }
    }

    private String getResourceName(ProceedingJoinPoint point, String annotationResource) {
        if (annotationResource != null && !annotationResource.isEmpty()) {
            return annotationResource;
        }
        return point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();
    }
}
