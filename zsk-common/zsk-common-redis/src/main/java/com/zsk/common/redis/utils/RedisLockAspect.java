package com.zsk.common.redis.utils;

import com.zsk.common.core.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 分布式锁切面
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Aspect
@Component
public class RedisLockAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        String key = redisLock.key();
        RLock lock = redissonClient.getLock(key);

        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(redisLock.waitTime(), redisLock.leaseTime(), redisLock.unit());
            if (isLocked) {
                return joinPoint.proceed();
            } else {
                throw new BusinessException(redisLock.message());
            }
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
