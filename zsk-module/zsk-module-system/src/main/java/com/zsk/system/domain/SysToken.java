package com.zsk.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统令牌对象 sys_token
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_token")
@Schema(description = "系统令牌对象")
public class SysToken extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 令牌名称 */
    @Schema(description = "令牌名称")
    private String tokenName;

    /** 令牌值 */
    @Schema(description = "令牌值")
    private String tokenValue;

    /** 令牌类型（api-接口令牌 personal-个人令牌 internal-内部令牌） */
    @Schema(description = "令牌类型（api-接口令牌 personal-个人令牌 internal-内部令牌）")
    private String tokenType;

    /** 绑定用户ID */
    @Schema(description = "绑定用户ID")
    private Long boundUserId;

    /** 绑定用户名 */
    @Schema(description = "绑定用户名")
    private String boundUserName;

    /** 过期时间 */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /** 最后使用时间 */
    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedTime;

    /** 状态（active-有效 expired-已过期 revoked-已吊销） */
    @Schema(description = "状态（active-有效 expired-已过期 revoked-已吊销）")
    private String status;
}
