package com.zsk.common.xxljob.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.zsk.common.xxljob.register.XxlJobRegister;
import com.zsk.common.xxljob.service.XxlJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * XXL-Job自动配置
 * <p>自动配置XXL-Job执行器和任务自动注册功能</p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@AutoConfiguration
@EnableConfigurationProperties(XxlJobProperties.class)
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XxlJobAutoConfiguration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(XxlJobAutoConfiguration.class);

    /**
     * XXL-Job配置属性
     */
    private final XxlJobProperties properties;

    /**
     * Spring应用上下文
     */
    private final ApplicationContext applicationContext;

    /**
     * 构造函数
     *
     * @param properties         XXL-Job配置属性
     * @param applicationContext Spring应用上下文
     */
    public XxlJobAutoConfiguration(XxlJobProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    /**
     * 创建XXL-Job执行器
     *
     * @return XXL-Job执行器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(properties.getAdmin().getAddresses());
        executor.setAppname(properties.getExecutor().getAppname());
        executor.setAddress(properties.getExecutor().getAddress());
        executor.setIp(properties.getExecutor().getIp());
        executor.setPort(properties.getExecutor().getPort());
        executor.setAccessToken(properties.getExecutor().getAccessToken());
        executor.setLogPath(properties.getExecutor().getLogPath());
        executor.setLogRetentionDays(properties.getExecutor().getLogRetentionDays());
        return executor;
    }

    /**
     * 创建XXL-Job服务
     *
     * @return XXL-Job服务实例
     */
    @Bean
    @ConditionalOnMissingBean
    public XxlJobService xxlJobService() {
        return new XxlJobService(properties);
    }

    /**
     * 创建任务注册器
     *
     * @param xxlJobService      XXL-Job服务
     * @param applicationContext Spring应用上下文
     * @return 任务注册器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public XxlJobRegister xxlJobRegister(XxlJobService xxlJobService, ApplicationContext applicationContext) {
        return new XxlJobRegister(properties, xxlJobService, applicationContext);
    }

    /**
     * 应用启动后自动注册任务
     *
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        if (properties.isEnabled()) {
            log.info("开始执行XXL-Job任务自动注册...");
            XxlJobRegister xxlJobRegister = applicationContext.getBean(XxlJobRegister.class);
            xxlJobRegister.registerJobs();
        }
    }
}
