package com.zsk.gateway;

import com.zsk.common.core.config.properties.IgnoreWhiteProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 * 
 * @author wuhuaming
 * @date 2024-02-13
 * @version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(IgnoreWhiteProperties.class)
public class ZskGatewayApplication {
    /**
     * 项目启动入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ZskGatewayApplication.class, args);
    }
}
