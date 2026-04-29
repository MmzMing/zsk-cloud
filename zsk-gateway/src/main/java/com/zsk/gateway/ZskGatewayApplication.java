package com.zsk.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 网关服务启动类
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class ZskGatewayApplication {
    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(ZskGatewayApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        log.info("\n----------------------------------------------------------\n\t" +
                "Application Gateway is running successfully!\n\t" +
                "Nacos: \t\thttp://192.168.101.129:8848/nacos\n\t" +
                "Gateway: \thttp://" + ip + ":" + port + "/\n\t" +
                "1Panel: \thttp://192.168.101.129:29585/d66a3eae39\n" +
                "----------------------------------------------------------");
    }
}
