package com.zsk.auth;

import com.zsk.common.security.annotation.EnableZskFeignClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 认证授权中心启动类
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableZskFeignClients
public class ZskAuthApplication {
    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(ZskAuthApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        if (!StringUtils.hasText(path)) {
            path = "";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Application Auth is running successfully!\n\t" +
                "Nacos: \t\thttp://192.168.101.129:8848/nacos\n\t" +
                "Knife4j: \thttp://" + ip + ":" + port + path + "/doc.html\n\t" +
                "1Panel: \thttp://192.168.101.129:29585/d66a3eae39\n" +
                "----------------------------------------------------------");
    }
}
