package com.zsk.common.xxljob.register;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zsk.common.xxljob.annotation.XxlJobAutoRegister;
import com.zsk.common.xxljob.config.XxlJobProperties;
import com.zsk.common.xxljob.service.XxlJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * XXL-Job任务自动注册器
 * 扫描所有带有@XxlJobAutoRegister注解的方法，自动注册到XXL-Job控制台
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
public class XxlJobRegister {

    private static final Logger log = LoggerFactory.getLogger(XxlJobRegister.class);

    /**
     * XXL-Job配置属性
     */
    private final XxlJobProperties properties;

    /**
     * XXL-Job服务
     */
    private final XxlJobService xxlJobService;

    /**
     * Spring应用上下文
     */
    private final ApplicationContext applicationContext;

    /**
     * 执行器ID缓存
     */
    private volatile Integer executorId;

    /**
     * 构造函数
     *
     * @param properties         XXL-Job配置属性
     * @param xxlJobService      XXL-Job服务
     * @param applicationContext Spring应用上下文
     */
    public XxlJobRegister(XxlJobProperties properties, XxlJobService xxlJobService, ApplicationContext applicationContext) {
        this.properties = properties;
        this.xxlJobService = xxlJobService;
        this.applicationContext = applicationContext;
    }

    /**
     * 执行任务注册
     * <p>扫描所有Spring Bean，查找带有@XxlJobAutoRegister注解的方法并注册到XXL-Job控制台</p>
     *
     * @return 注册成功的任务数量
     */
    public int registerJobs() {
        if (!properties.isEnabled()) {
            log.info("XXL-Job自动注册已禁用");
            return 0;
        }

        if (!xxlJobService.login()) {
            log.error("XXL-Job登录失败，无法自动注册任务");
            return 0;
        }

        executorId = getOrCreateExecutorId();
        if (executorId == null) {
            log.error("获取执行器ID失败，无法自动注册任务");
            return 0;
        }

        AtomicInteger count = new AtomicInteger(0);
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            Class<?> targetClass = AopUtils.getTargetClass(bean);

            Map<Method, XxlJobAutoRegister> annotatedMethods = MethodIntrospector.selectMethods(
                    targetClass,
                    (MethodIntrospector.MetadataLookup<XxlJobAutoRegister>) method ->
                            AnnotatedElementUtils.findMergedAnnotation(method, XxlJobAutoRegister.class)
            );

            for (Map.Entry<Method, XxlJobAutoRegister> methodEntry : annotatedMethods.entrySet()) {
                Method method = methodEntry.getKey();
                XxlJobAutoRegister annotation = methodEntry.getValue();

                XxlJob xxlJob = AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class);
                if (xxlJob == null) {
                    log.warn("方法 {} 缺少@XxlJob注解，跳过注册", method.getName());
                    continue;
                }

                String jobHandler = xxlJob.value();
                if (registerJob(jobHandler, annotation)) {
                    count.incrementAndGet();
                }
            }
        }

        log.info("XXL-Job任务自动注册完成，成功注册 {} 个任务", count.get());
        return count.get();
    }

    /**
     * 获取或创建执行器ID
     * <p>先查询执行器是否存在，不存在则自动注册</p>
     *
     * @return 执行器ID
     */
    private Integer getOrCreateExecutorId() {
        String appname = properties.getExecutor().getAppname();
        if (appname == null || appname.isEmpty()) {
            log.error("执行器appname未配置");
            return null;
        }

        Integer id = xxlJobService.getExecutorIdByAppname(appname);
        if (id != null) {
            log.info("执行器已存在: {} (ID: {})", appname, id);
            return id;
        }

        log.info("执行器不存在，开始注册: {}", appname);
        return xxlJobService.registerExecutor(
                appname,
                properties.getExecutor().getTitle(),
                properties.getExecutor().getAddress()
        );
    }

    /**
     * 注册单个任务
     *
     * @param jobHandler 任务Handler名称
     * @param annotation 注解信息
     * @return 是否注册成功
     */
    private boolean registerJob(String jobHandler, XxlJobAutoRegister annotation) {
        String jobName = annotation.name().isEmpty() ? jobHandler : annotation.name();

        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("executorHandler", jobHandler);
        jobInfo.put("jobDesc", annotation.description().isEmpty() ? jobName : annotation.description());
        jobInfo.put("cron", annotation.cron());
        jobInfo.put("routeStrategy", annotation.routeStrategy());
        jobInfo.put("blockStrategy", annotation.blockStrategy());
        jobInfo.put("timeout", String.valueOf(annotation.timeout()));
        jobInfo.put("failRetryCount", String.valueOf(annotation.failRetryCount()));
        jobInfo.put("author", annotation.author());
        jobInfo.put("alarmEmail", annotation.alarmEmail());
        jobInfo.put("executorParam", annotation.executorParam());
        jobInfo.put("shardingParam", annotation.shardingParam());
        jobInfo.put("status", String.valueOf(annotation.status()));

        if (annotation.childJobId() > 0) {
            jobInfo.put("childJobId", String.valueOf(annotation.childJobId()));
        }

        Integer existingJobId = xxlJobService.getJobIdByName(executorId, jobHandler);
        if (existingJobId != null) {
            jobInfo.put("jobGroup", String.valueOf(executorId));
            log.info("任务已存在，更新配置: {} (ID: {})", jobHandler, existingJobId);
            return xxlJobService.updateJob(existingJobId, jobInfo);
        }

        log.info("任务不存在，开始注册: {}", jobHandler);
        Integer jobId = xxlJobService.registerJob(executorId, jobInfo);
        return jobId != null;
    }

    /**
     * 获取执行器ID
     *
     * @return 执行器ID
     */
    public Integer getExecutorId() {
        return executorId;
    }
}
