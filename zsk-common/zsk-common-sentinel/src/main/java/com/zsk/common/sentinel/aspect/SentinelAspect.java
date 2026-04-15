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

    /**
     * 使用 Sentinel 进行限流保护
     * 当 Redis 不可用或未配置业务 Key 时，降级为 Sentinel 原生限流
     *
     * @param point 切面连接点，用于执行目标方法
     * @param rateLimit 限流注解对象，包含资源名、限流规则等信息
     * @return 目标方法的执行结果
     * @throws Throwable 方法执行异常或限流异常（RateLimitException）
     */
    private Object handleSentinelRateLimit(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 获取资源名称（优先使用注解配置，否则使用方法签名）
        String resourceName = getResourceName(point, rateLimit.resource());
        Entry entry = null;
        try {
            // 进入 Sentinel 资源保护，监控入口流量
            entry = SphU.entry(resourceName, EntryType.IN, 1);
            // 执行目标方法
            return point.proceed();
        } catch (BlockException e) {
            // 捕获 Sentinel 限流异常，抛出限流业务异常
            log.warn("Sentinel 限流：资源={}, 规则={}", resourceName, rateLimit);
            throw new RateLimitException(rateLimit.message());
        } finally {
            // 退出 Sentinel 资源，释放监控上下文
            if (entry != null) {
                entry.exit(1, EntryType.IN);
            }
        }
    }

    /**
     * 使用 Redis 进行限流保护
     * 基于 Redis INCR 原子操作实现分布式限流，支持动态业务 Key（如邮箱、用户名等）
     *
     * @param point 切面连接点，用于执行目标方法
     * @param rateLimit 限流注解对象，包含资源名、业务 Key、限流阈值等信息
     * @return 目标方法的执行结果
     * @throws Throwable 方法执行异常或限流异常（RateLimitException）
     */
    private Object handleRedisRateLimit(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 获取资源名称和业务 Key
        String resourceName = getResourceName(point, rateLimit.resource());
        String businessKey = parseSpel(rateLimit.key(), point);

        // 构造 Redis Key：rate_limit:资源名：业务 Key
        String redisKey = "rate_limit:" + resourceName + ":" + businessKey;
        // 获取限流阈值和时间窗口（转换为秒）
        long limit = (long) rateLimit.count();
        long time = rateLimit.timeUnit().toSeconds(1);

        // Redis 原子自增，记录请求次数
        Long count = redisService.increment(redisKey, 1);
        // 如果是第一次请求，设置过期时间
        if (count != null && count == 1) {
            redisService.expire(redisKey, time, TimeUnit.SECONDS);
        }

        // 超过限流阈值，抛出限流异常
        if (count != null && count > limit) {
             throw new RateLimitException(rateLimit.message());
        }

        // 未触发限流，继续执行目标方法
        return point.proceed();
    }


    /**
     * 解析 SpEL 表达式，提取动态业务 Key
     * 将方法参数绑定到 Spring EL 上下文，支持如 "#user.id" 等表达式语法
     *
     * @param key SpEL 表达式字符串（例如："#registerBody.email"）
     * @param point 切面连接点，用于获取方法签名和参数信息
     * @return 解析后的业务 Key 值；如果解析失败返回 "default"
     */
    private String parseSpel(String key, ProceedingJoinPoint point) {
        // 获取方法签名和方法对象
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        // 创建 Spring EL 评估上下文
        EvaluationContext context = new StandardEvaluationContext();
        
        // 获取方法参数名称和实际参数值
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();
        
        // 将参数名和参数值绑定到 EL 上下文，支持表达式引用
        if (paramNames != null) {
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        
        try {
            // 解析 SpEL 表达式并获取结果
            return parser.parseExpression(key).getValue(context, String.class);
        } catch (Exception e) {
            // 捕获表达式解析异常，记录错误日志并返回默认值
            log.error("SpEL 表达式解析失败：{}", key, e);
            return "default";
        }
    }

    /**
     * 熔断降级处理切面
     * 拦截标注了 {@link CircuitBreaker} 注解的方法，使用 Sentinel 进行熔断保护
     * 当触发熔断规则时，抛出服务不可用异常
     *
     * @param point 切面连接点，用于执行目标方法
     * @param circuitBreaker 熔断注解对象，包含资源配置信息
     * @return 目标方法的执行结果
     * @throws Throwable 方法执行异常或熔断异常
     */
    @Around("@annotation(circuitBreaker)")
    public Object aroundCircuitBreaker(ProceedingJoinPoint point, CircuitBreaker circuitBreaker) throws Throwable {
        // 获取资源名称（优先使用注解配置，否则使用方法签名）
        String resourceName = getResourceName(point, circuitBreaker.resource());
        Entry entry = null;
        try {
            // 进入 Sentinel 资源保护，监控入口流量
            entry = SphU.entry(resourceName, EntryType.IN, 1);
            // 执行目标方法
            return point.proceed();
        } catch (BlockException e) {
            // 捕获 Sentinel 限流/熔断异常，抛出业务异常提示
            log.warn("Sentinel 熔断：资源={}, 规则={}", resourceName, circuitBreaker);
            throw new BaseException(ResultCode.CIRCUIT_BREAKER_ERROR.getCode(), "服务暂时不可用，请稍后再试");
        } finally {
            // 退出 Sentinel 资源，释放监控上下文
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
