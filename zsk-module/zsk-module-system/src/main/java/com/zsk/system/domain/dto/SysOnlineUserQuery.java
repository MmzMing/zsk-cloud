package com.zsk.system.domain.dto;

import com.zsk.common.datasource.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 在线用户查询条件
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-04-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "在线用户查询条件")
public class SysOnlineUserQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户账号（模糊查询）
     */
    @Schema(description = "用户账号（模糊查询）")
    private String userName;

    /**
     * 用户昵称（模糊查询）
     */
    @Schema(description = "用户昵称（模糊查询）")
    private String nickName;

    /**
     * 登录IP（模糊查询）
     */
    @Schema(description = "登录IP（模糊查询）")
    private String ipaddr;
}
