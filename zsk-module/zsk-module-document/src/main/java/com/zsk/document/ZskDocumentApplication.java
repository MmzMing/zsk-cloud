package com.zsk.document;

import com.zsk.common.security.annotation.EnableZskFeignClients;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 文档管理微服务
 *
 * @author wuhuaming
 */
@Slf4j
@EnableZskFeignClients
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.zsk")
@MapperScan("com.zsk.document.mapper")
public class ZskDocumentApplication {
    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(ZskDocumentApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        if (!StringUtils.hasText(path)) {
            path = "";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Application System is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "nacos: \t\thttp://192.168.101.129:8088/nacos\n\t" +
                "Knife4j: \thttp://" + ip + ":" + port + path + "/doc.html\n" +
                "----------------------------------------------------------");
    }
}
