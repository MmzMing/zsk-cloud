package com.zsk.document;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 文档管理微服务
 * 
 * @author wuhuaming
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.zsk")
@MapperScan("com.zsk.document.mapper")
public class ZskDocumentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZskDocumentApplication.class, args);
    }
}
