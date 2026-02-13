package com.zsk.auth;

import com.zsk.common.security.annotation.EnableZskFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证授权中心启动类
 * 
 * @author wuhuaming
 * @date 2024-01-15
 * @version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZskFeignClients
public class ZskAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZskAuthApplication.class, args);
    }
}
