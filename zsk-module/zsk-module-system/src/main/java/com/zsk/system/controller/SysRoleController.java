package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.SysRole;
import com.zsk.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理 控制器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService roleService;

    /**
     * 查询角色列表
     *
     * @param role 查询条件
     * @return 角色列表
     */
    @Operation(summary = "查询角色列表")
    @GetMapping("/list")
    public R<List<SysRole>> list(SysRole role) {
        return R.ok(roleService.list());
    }

    /**
     * 获取角色详细信息
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Operation(summary = "获取角色详细信息")
    @GetMapping("/{id}")
    public R<SysRole> getInfo(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    /**
     * 新增角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    @Operation(summary = "新增角色")
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        return roleService.insertRole(role) ? R.ok() : R.fail();
    }

    /**
     * 修改角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    @Operation(summary = "修改角色")
    @PutMapping
    public R<Void> edit(@RequestBody SysRole role) {
        return roleService.updateRole(role) ? R.ok() : R.fail();
    }

    /**
     * 删除角色（支持批量删除）
     *
     * @param ids 角色ID列表
     * @return 是否成功
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return roleService.deleteRoleByIds(ids) ? R.ok() : R.fail();
    }

    /**
     * 批量复制角色
     *
     * @param body 请求体（包含ids字段）
     * @return 是否成功
     */
    @Operation(summary = "批量复制角色")
    @PostMapping("/copy")
    public R<Void> batchCopy(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        return roleService.copyRoles(ids) ? R.ok() : R.fail();
    }

    /**
     * 查看角色权限
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Operation(summary = "查看角色权限")
    @GetMapping("/{roleId}/menus")
    public R<List<Long>> listRoleMenus(@PathVariable Long roleId) {
        return R.ok(roleService.selectMenuIdsByRoleId(roleId));
    }

    /**
     * 绑定角色权限（追加菜单，已存在的不会重复绑定）
     *
     * @param roleId 角色ID
     * @param body   请求体（包含menuIds字段）
     * @return 是否成功
     */
    @Operation(summary = "绑定角色权限")
    @PostMapping("/{roleId}/menus")
    public R<Void> bindRoleMenus(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        List<Long> menuIds = body.get("menuIds");
        return roleService.bindRoleMenus(roleId, menuIds) ? R.ok() : R.fail();
    }

    /**
     * 解绑角色权限（移除指定菜单）
     *
     * @param roleId 角色ID
     * @param body   请求体（包含menuIds字段）
     * @return 是否成功
     */
    @Operation(summary = "解绑角色权限")
    @DeleteMapping("/{roleId}/menus")
    public R<Void> unbindRoleMenus(@PathVariable Long roleId, @RequestBody(required = false) Map<String, List<Long>> body) {
        List<Long> menuIds = body != null ? body.get("menuIds") : null;
        return roleService.unbindRoleMenus(roleId, menuIds) ? R.ok() : R.fail();
    }

    /**
     * 更新角色权限（全量替换菜单）
     *
     * @param roleId 角色ID
     * @param body   请求体（包含menuIds字段）
     * @return 是否成功
     */
    @Operation(summary = "更新角色权限")
    @PutMapping("/{roleId}/menus")
    public R<Void> updateRoleMenus(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        List<Long> menuIds = body.get("menuIds");
        return roleService.updateRoleMenus(roleId, menuIds) ? R.ok() : R.fail();
    }

    /**
     * 查看角色用户
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Operation(summary = "查看角色用户")
    @GetMapping("/{roleId}/users")
    public R<List<Long>> listRoleUsers(@PathVariable Long roleId) {
        return R.ok(roleService.selectUserIdsByRoleId(roleId));
    }

    /**
     * 绑定角色用户（追加用户，已存在的不会重复绑定）
     *
     * @param roleId 角色ID
     * @param body   请求体（包含userIds字段）
     * @return 是否成功
     */
    @Operation(summary = "绑定角色用户")
    @PostMapping("/{roleId}/users")
    public R<Void> bindRoleUsers(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        List<Long> userIds = body.get("userIds");
        return roleService.bindRoleUsers(roleId, userIds) ? R.ok() : R.fail();
    }

    /**
     * 解绑角色用户（移除指定用户）
     *
     * @param roleId 角色ID
     * @param body   请求体（包含userIds字段）
     * @return 是否成功
     */
    @Operation(summary = "解绑角色用户")
    @DeleteMapping("/{roleId}/users")
    public R<Void> unbindRoleUsers(@PathVariable Long roleId, @RequestBody(required = false) Map<String, List<Long>> body) {
        List<Long> userIds = body != null ? body.get("userIds") : null;
        return roleService.unbindRoleUsers(roleId, userIds) ? R.ok() : R.fail();
    }

    /**
     * 更新角色用户（全量替换用户）
     *
     * @param roleId 角色ID
     * @param body   请求体（包含userIds字段）
     * @return 是否成功
     */
    @Operation(summary = "更新角色用户")
    @PutMapping("/{roleId}/users")
    public R<Void> updateRoleUsers(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        List<Long> userIds = body.get("userIds");
        return roleService.updateRoleUsers(roleId, userIds) ? R.ok() : R.fail();
    }
}
