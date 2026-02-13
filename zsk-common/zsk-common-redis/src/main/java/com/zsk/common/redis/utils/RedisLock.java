package com.zsk.common.redis.utils;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2024-01-15
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RedisLock {

    /**
     * 锁的Key
     */
    String key();

    /**
     * 等待时间
     */
    long waitTime() default 30000;

    /**
     * 租赁时间（自动释放时间）
     */
    long leaseTime() default 60000;

    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

    /**
     * 提示消息
     */
    String message() default "系统繁忙，请稍后再试";
}
