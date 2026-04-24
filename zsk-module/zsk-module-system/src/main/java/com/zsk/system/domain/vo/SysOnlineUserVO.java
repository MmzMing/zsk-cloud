package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线用户信息 视图对象
 * <p>
 * 一个用户对应一行（合并多设备会话），通过 Redis 中的
 * {@code zsk:login:token:{userId}} Set 是否存在/非空判断在线状态。
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-22
 */
@Data
@Schema(description = "在线用户信息")
public class SysOnlineUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号")
    private String userName;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickName;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    private String email;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Schema(description = "帐号状态（0正常 1停用）")
    private String status;

    /**
     * 最后登录IP地址
     */
    @Schema(description = "最后登录IP地址")
    private String ipaddr;

    /**
     * 登录地点
     */
    @Schema(description = "登录地点")
    private String loginLocation;

    /**
     * 最近登录时间
     */
    @Schema(description = "最近登录时间")
    private LocalDateTime loginTime;

    /**
     * Token过期时间
     */
    @Schema(description = "Token过期时间")
    private LocalDateTime expireTime;

    /**
     * 在线时长（秒，从最近登录时间计算）
     */
    @Schema(description = "在线时长（秒）")
    private Long onlineDuration;

    /**
     * 当前在线设备数（同一用户多端登录数）
     */
    @Schema(description = "在线设备数")
    private Integer deviceCount;
}
