package com.zsk.common.oss.config;

import com.zsk.common.oss.core.DynamicOssTemplate;
import com.zsk.common.oss.core.OssTemplate;
import com.zsk.common.oss.properties.OssProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS自动配置类
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(value = "zsk.oss.enabled", havingValue = "true")
public class OssConfig {

    /**
     * 创建动态OSS模板
     *
     * @param properties 配置属性
     * @return DynamicOssTemplate
     */
    @Bean
    public DynamicOssTemplate dynamicOssTemplate(OssProperties properties) {
        return new DynamicOssTemplate(properties);
    }

    /**
     * 将动态OSS模板暴露为OssTemplate接口
     *
     * @param dynamicOssTemplate 动态模板
     * @return OssTemplate
     */
    @Bean
    public OssTemplate ossTemplate(DynamicOssTemplate dynamicOssTemplate) {
        return dynamicOssTemplate;
    }
}
