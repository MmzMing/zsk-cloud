package com.zsk.common.sentinel.config;

import com.zsk.common.sentinel.handler.SentinelBlockHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Sentinel 配置
 *
 * @author wuhuaming
 */
@AutoConfiguration
public class SentinelConfig {

    @Bean
    public SentinelBlockHandler sentinelBlockHandler() {
        return new SentinelBlockHandler();
    }
}
