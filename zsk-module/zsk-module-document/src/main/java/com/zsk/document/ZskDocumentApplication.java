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
        System.out.println("（づ￣3￣）づ╭❤～ 文档管理服务启动成功 \n");
    }
}
