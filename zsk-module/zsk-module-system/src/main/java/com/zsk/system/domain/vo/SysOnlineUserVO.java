package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线用户信息 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@Schema(description = "在线用户信息")
public class SysOnlineUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话编号（Redis中的uuid） */
    @Schema(description = "会话编号")
    private String sessionId;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 用户名 */
    @Schema(description = "用户名")
    private String userName;

    /** 用户昵称 */
    @Schema(description = "用户昵称")
    private String nickName;

    /** 部门名称 */
    @Schema(description = "部门名称")
    private String deptName;

    /** 登录IP地址 */
    @Schema(description = "登录IP地址")
    private String ipaddr;

    /** 登录地点 */
    @Schema(description = "登录地点")
    private String loginLocation;

    /** 浏览器类型 */
    @Schema(description = "浏览器类型")
    private String browser;

    /** 操作系统 */
    @Schema(description = "操作系统")
    private String os;

    /** 登录时间 */
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    /** Token过期时间 */
    @Schema(description = "Token过期时间")
    private LocalDateTime expireTime;
}
