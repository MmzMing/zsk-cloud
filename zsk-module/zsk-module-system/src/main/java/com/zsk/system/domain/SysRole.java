package com.zsk.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zsk.common.datasource.domain.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 角色对象 sys_role
 *
 * @author wuhuaming
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色对象")
public class SysRole extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色权限字符串
     */
    @Schema(description = "角色权限字符串")
    private String roleKey;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    private Integer roleSort;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示
     */
    @Schema(description = "菜单树选择项是否关联显示")
    private Boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示
     */
    @Schema(description = "部门树选择项是否关联显示")
    private Boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    @Schema(description = "角色状态（0正常 1停用）")
    private String status;

    /**
     * 菜单组
     */
    @TableField(exist = false)
    @Schema(description = "菜单组")
    private Long[] menuIds;
}
