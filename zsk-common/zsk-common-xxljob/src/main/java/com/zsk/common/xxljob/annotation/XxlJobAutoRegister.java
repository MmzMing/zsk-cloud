package com.zsk.common.xxljob.annotation;

import java.lang.annotation.*;

/**
 * XXL-Job自动注册注解
 * 标注在方法上，应用启动时自动向XXL-Job控制台注册任务
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XxlJobAutoRegister {

    /**
     * 任务名称（默认取方法名）
     *
     * @return 任务名称
     */
    String name() default "";

    /**
     * 任务描述
     *
     * @return 任务描述信息
     */
    String description() default "";

    /**
     * Cron表达式（必填）
     *
     * @return Cron表达式
     */
    String cron();

    /**
     * 路由策略
     * <p>可选值：</p>
     * <ul>
     *   <li>FIRST - 第一个</li>
     *   <li>LAST - 最后一个</li>
     *   <li>ROUND - 轮询</li>
     *   <li>RANDOM - 随机</li>
     *   <li>CONSISTENT_HASH - 一致性哈希</li>
     *   <li>LEAST_FREQUENTLY_USED - 最不经常使用</li>
     *   <li>LEAST_RECENTLY_USED - 最近最久未使用</li>
     *   <li>FAILOVER - 故障转移</li>
     *   <li>BUSYOVER - 忙碌转移</li>
     *   <li>SHARDING_BROADCAST - 分片广播</li>
     * </ul>
     *
     * @return 路由策略
     */
    String routeStrategy() default "FIRST";

    /**
     * 执行器任务分片参数
     *
     * @return 分片参数
     */
    String shardingParam() default "";

    /**
     * 子任务ID
     *
     * @return 子任务ID，0表示无子任务
     */
    int childJobId() default 0;

    /**
     * 阻塞处理策略
     * <p>可选值：</p>
     * <ul>
     *   <li>SERIALIZATION_EXECUTION - 单机串行</li>
     *   <li>DISCARD_LATER - 丢弃后续调度</li>
     *   <li>COVER_EARLY - 覆盖之前调度</li>
     * </ul>
     *
     * @return 阻塞处理策略
     */
    String blockStrategy() default "SERIALIZATION_EXECUTION";

    /**
     * 任务超时时间（秒）
     *
     * @return 超时时间，0表示不超时
     */
    int timeout() default 0;

    /**
     * 任务失败重试次数
     *
     * @return 重试次数
     */
    int failRetryCount() default 0;

    /**
     * 负责人
     *
     * @return 负责人名称
     */
    String author() default "admin";

    /**
     * 报警邮件
     *
     * @return 报警邮件地址
     */
    String alarmEmail() default "";

    /**
     * 执行参数
     *
     * @return 任务执行参数
     */
    String executorParam() default "";

    /**
     * 任务状态
     *
     * @return 状态值（0-正常，1-停止）
     */
    int status() default 0;
}
