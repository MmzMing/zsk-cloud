package com.zsk.common.datasource.handler;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

/**
 * 动态数据源刷新处理器
 *
 * @author zsk
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicDataSourceRefreshHandler {

    private final DataSource dataSource;
    private final DataSourceCreator dataSourceCreator;
    private final DynamicDataSourceProperties properties;

    /**
     * 应用启动完成后，加载额外的数据源
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshDataSources();
    }

    /**
     * 监听配置变更事件（如 Nacos 配置刷新）
     */
    @EventListener(EnvironmentChangeEvent.class)
    public void onEnvironmentChange(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        for (String key : keys) {
            if (key.contains("spring.datasource.dynamic")) {
                log.info("检测到动态数据源配置变更，开始刷新...");
                refreshDataSources();
                break;
            }
        }
    }

    /**
     * 刷新数据源
     */
    public void refreshDataSources() {
        log.info("开始刷新动态数据源...");
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        Map<String, DataSourceProperty> datasourceProperties = properties.getDatasource();

        datasourceProperties.forEach((name, property) -> {
            if (!ds.getDataSources().containsKey(name)) {
                log.info("添加新数据源: {}", name);
                DataSource newDs = dataSourceCreator.createDataSource(property);
                ds.addDataSource(name, newDs);
            } else {
                // 如果已存在，可以选择是否更新（这里简单处理为仅添加新数据源）
                // 若要实现完全同步，还需要对比配置并移除已不存在的数据源
                log.debug("数据源 {} 已存在，跳过", name);
            }
        });
        log.info("动态数据源刷新完成");
    }
}
