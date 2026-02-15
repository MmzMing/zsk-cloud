package com.zsk.auth.domain;

import lombok.Data;

/**
 * 验证码校验请求对象
 * 
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
public class CaptchaCheckRequest {
    /** 验证码UUID */
    private String uuid;
    
    /** 验证码内容（X坐标移动距离） */
    private String code;
}
