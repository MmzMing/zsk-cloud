package com.zsk.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色和菜单关联 sys_role_menu
 *
 * @author wuhuaming
 */
@Data
@TableName("sys_role_menu")
@Schema(description = "角色和菜单关联对象")
public class SysRoleMenu {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Long roleId;

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private Long menuId;
}
