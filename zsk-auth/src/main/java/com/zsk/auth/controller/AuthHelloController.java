package com.zsk.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证测试 控制器
 *
 * @author MzgMing
 * @version 1.0
 * @date 2026-02-13
 */
@RestController
@RequestMapping("/test")
public class AuthHelloController {


    /**
     * 测试接口
     *
     * @return 配置内容
     */
    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 测试加认证的接口
     *
     * @return 配置内容
     */
    @GetMapping("/helloAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public String helloAuth() {
        return "hello world";
    }
}
