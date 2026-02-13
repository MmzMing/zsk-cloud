package com.zsk.common.sentinel.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解
 *
 * @author wuhuaming
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RateLimit {
    /**
     * 资源名称
     */
    String resource() default "";

    /**
     * 限流阈值
     */
    double count() default 5;

    /**
     * 限流时间窗口
     */
    int grade() default 1;

    /**
     * 限流时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 限流策略（0-直接拒绝，1-Warm Up，2-匀速排队）
     */
    int limitStrategy() default 0;

    /**
     * 流量控制效果（0-快速失败，1-Warm Up，2-排队等待，3-慢启动预热）
     */
    int controlBehavior() default 0;
}
