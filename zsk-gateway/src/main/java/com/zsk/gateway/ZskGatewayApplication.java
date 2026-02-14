package com.zsk.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@SpringBootApplication
@EnableDiscoveryClient
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
