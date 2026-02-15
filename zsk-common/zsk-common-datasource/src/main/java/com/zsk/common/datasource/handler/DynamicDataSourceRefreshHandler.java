package com.zsk.common.datasource.handler;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

/**
 * 动态数据源刷新处理器
 * <p>
 * 主要功能：
 * 1. 应用启动完成后自动加载配置文件中定义的动态数据源
 * 2. 监听环境配置变更（如 Nacos 配置刷新），实现数据源的热加载
 * 3. 动态向 {@link DynamicRoutingDataSource} 中注入新的数据源实例
 * </p>
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@Component
public class DynamicDataSourceRefreshHandler {

    /**
     * 核心动态路由数据源（主数据源代理）
     */
    private final DataSource dataSource;
    /**
     * 数据源创建器，用于将配置转换为实际的 DataSource 实例
     */
    private final DefaultDataSourceCreator dataSourceCreator;
    /**
     * 动态数据源配置属性，对应 spring.datasource.dynamic
     */
    private final DynamicDataSourceProperties properties;

    /**
     * 构造方法
     * 使用 @Lazy 避免在数据源完全初始化前产生循环依赖
     *
     * @param dataSource        动态路由数据源
     * @param dataSourceCreator 数据源创建器
     * @param properties        数据源配置
     */
    public DynamicDataSourceRefreshHandler(@Lazy DataSource dataSource,
                                           @Lazy DefaultDataSourceCreator dataSourceCreator,
                                           DynamicDataSourceProperties properties) {
        this.dataSource = dataSource;
        this.dataSourceCreator = dataSourceCreator;
        this.properties = properties;
    }

    /**
     * 监听应用就绪事件
     * 当 Spring Boot 应用启动完成并准备好接收请求时，触发初始数据源加载
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.debug("应用就绪，执行初始动态数据源加载...");
        refreshDataSources();
    }

    /**
     * 监听配置变更事件
     * 当使用 Nacos、Apollo 等配置中心且配置发生变更时，Spring Cloud 会发布 EnvironmentChangeEvent
     *
     * @param event 环境变更事件，包含变更的配置键集合
     */
    @EventListener(EnvironmentChangeEvent.class)
    public void onEnvironmentChange(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        for (String key : keys) {
            // 仅当动态数据源相关的配置项发生变化时才执行刷新
            if (key.contains("spring.datasource.dynamic")) {
                log.info("检测到动态数据源配置变更(Key: {})，开始刷新...", key);
                refreshDataSources();
                break;
            }
        }
    }

    /**
     * 执行数据源刷新逻辑
     * <p>
     * 该方法会遍历配置中的所有数据源定义，如果发现新的数据源配置，则创建并添加到路由中。
     * 目前仅支持“新增”刷新，若需支持“删除”或“更新”现有数据源，需额外实现对比逻辑。
     * </p>
     */
    public void refreshDataSources() {
        log.info("开始同步动态数据源配置...");
        if (!(dataSource instanceof DynamicRoutingDataSource ds)) {
            log.warn("当前数据源不是 DynamicRoutingDataSource 类型，无法执行动态刷新");
            return;
        }

        // 获取当前配置文件中定义的所有数据源
        Map<String, DataSourceProperty> datasourceProperties = properties.getDatasource();

        datasourceProperties.forEach((name, property) -> {
            // 如果路由中尚不存在该名称的数据源，则进行创建和添加
            if (!ds.getDataSources().containsKey(name)) {
                log.info("发现新数据源配置 [{}]，正在初始化并添加...", name);
                try {
                    DataSource newDs = dataSourceCreator.createDataSource(property);
                    ds.addDataSource(name, newDs);
                    log.info("数据源 [{}] 添加成功", name);
                } catch (Exception e) {
                    log.error("数据源 [{}] 初始化失败: {}", name, e.getMessage(), e);
                }
            } else {
                // 对于已存在的数据源，日志输出调试信息
                log.debug("数据源 [{}] 已在运行中，跳过重复添加", name);
            }
        });
        log.info("动态数据源同步任务完成");
    }
}
