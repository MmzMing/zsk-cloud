package com.zsk.common.redis.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2024-01-15
 */
@Service
public class LockService {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 执行带锁的任务
     *
     * @param lockKey  锁的Key
     * @param supplier 任务逻辑
     * @param <T>      返回类型
     * @return 任务结果
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, -1, -1, TimeUnit.SECONDS, supplier);
    }

    /**
     * 执行带锁的任务
     *
     * @param lockKey   锁的Key
     * @param waitTime  等待锁的时间
     * @param leaseTime 锁持有的时间
     * @param unit      时间单位
     * @param supplier  任务逻辑
     * @param <T>       返回类型
     * @return 任务结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked;
            if (waitTime == -1) {
                lock.lock(leaseTime, unit);
                isLocked = true;
            } else {
                isLocked = lock.tryLock(waitTime, leaseTime, unit);
            }

            if (isLocked) {
                return supplier.get();
            } else {
                throw new RuntimeException("Failed to acquire lock for key: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
