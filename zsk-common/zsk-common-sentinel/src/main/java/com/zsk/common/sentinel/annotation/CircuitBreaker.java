package com.zsk.common.sentinel.annotation;

import java.lang.annotation.*;

/**
 * 熔断降级注解
 *
 * @author wuhuaming
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CircuitBreaker {
    /**
     * 资源名称
     */
    String resource() default "";

    /**
     * 熔断策略（0-慢调用比例，1-异常比例，2-异常数）
     */
    int strategy() default 0;

    /**
     * 慢调用比例阈值
     */
    double slowRatioThreshold() default 0.5;

    /**
     * 熔断时长（秒）
     */
    int timeout() default 10;

    /**
     * 最小请求数
     */
    int minRequestAmount() default 5;

    /**
     * 统计时长（秒）
     */
    int statIntervalMs() default 10000;

    /**
     * 异常比例阈值
     */
    double exceptionRatioThreshold() default 0.5;

    /**
     * 异常数阈值
     */
    int exceptionCountThreshold() default 5;
}
