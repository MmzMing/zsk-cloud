package com.zsk.common.sentinel.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zsk.common.core.enums.ResultCode;
import com.zsk.common.core.exception.BaseException;
import com.zsk.common.core.exception.RateLimitException;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import com.zsk.common.sentinel.annotation.CircuitBreaker;
import com.zsk.common.sentinel.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

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

    @Autowired(required = false)
    private RedisService redisService;

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(rateLimit)")
    public Object aroundRateLimit(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.key();
        // 如果配置了key且Redis服务可用，使用Redis限流
        if (StringUtils.isNotBlank(key) && redisService != null) {
            return handleRedisRateLimit(point, rateLimit);
        }
        // 否则使用Sentinel限流
        return handleSentinelRateLimit(point, rateLimit);
    }

    private Object handleSentinelRateLimit(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String resourceName = getResourceName(point, rateLimit.resource());
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, EntryType.IN, 1);
            return point.proceed();
        } catch (BlockException e) {
            log.warn("Sentinel 限流: 资源={}, 规则={}", resourceName, rateLimit);
            throw new RateLimitException(rateLimit.message());
        } finally {
            if (entry != null) {
                entry.exit(1, EntryType.IN);
            }
        }
    }

    private Object handleRedisRateLimit(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String resourceName = getResourceName(point, rateLimit.resource());
        String businessKey = parseSpel(rateLimit.key(), point);
        
        String redisKey = "rate_limit:" + resourceName + ":" + businessKey;
        long limit = (long) rateLimit.count();
        long time = rateLimit.timeUnit().toSeconds(1); // 默认1个单位时间
        
        Long count = redisService.increment(redisKey, 1);
        if (count != null && count == 1) {
            redisService.expire(redisKey, time, TimeUnit.SECONDS);
        }
        
        if (count != null && count > limit) {
             throw new RateLimitException(rateLimit.message());
        }
        
        return point.proceed();
    }

    private String parseSpel(String key, ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        EvaluationContext context = new StandardEvaluationContext();
        
        // 获取参数名和参数值
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();
        
        if (paramNames != null) {
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        
        try {
            return parser.parseExpression(key).getValue(context, String.class);
        } catch (Exception e) {
            log.error("SpEL表达式解析失败: {}", key, e);
            return "default";
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
