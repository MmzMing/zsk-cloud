package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.SysMenu;
import com.zsk.system.domain.vo.MenuTreeVo;
import com.zsk.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService menuService;

    /**
     * 查询菜单列表
     *
     * @param menu 查询条件
     * @return 菜单列表
     */
    @Operation(summary = "查询菜单列表")
    @GetMapping("/list")
    public R<List<SysMenu>> list(SysMenu menu) {
        return R.ok(menuService.list());
    }

    /**
     * 根据用户ID查询菜单树列表
     *
     * @param userId 用户ID
     * @return 菜单树列表
     */
    @Operation(summary = "根据用户ID查询菜单树列表")
    @GetMapping("/user/{userId}")
    public R<List<SysMenu>> userMenu(@PathVariable Long userId) {
        return R.ok(menuService.selectMenuTreeByUserId(userId));
    }

    /**
     * 获取当前用户的菜单树（含排序）
     * <p>
     * 根据当前登录用户ID查询其有权限访问的菜单，并构建树形结构
     *
     * @return 菜单树列表
     */
    @Operation(summary = "获取当前用户菜单树", description = "根据当前登录用户ID查询其有权限访问的菜单，并构建树形结构，包含完整的菜单信息和排序")
    @GetMapping("/tree")
    public R<List<MenuTreeVo>> getUserMenuTree() {
        return R.ok(menuService.getUserMenuTree());
    }

    /**
     * 获取菜单详细信息
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @Operation(summary = "获取菜单详细信息")
    @GetMapping("/{id}")
    public R<SysMenu> getInfo(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    /**
     * 新增菜单
     *
     * @param menu 菜单信息
     * @return 是否成功
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        return menuService.save(menu) ? R.ok() : R.fail();
    }

    /**
     * 修改菜单
     *
     * @param menu 菜单信息
     * @return 是否成功
     */
    @Operation(summary = "修改菜单")
    @PutMapping
    public R<Void> edit(@RequestBody SysMenu menu) {
        return menuService.updateById(menu) ? R.ok() : R.fail();
    }

    /**
     * 批量更新菜单（用于拖拽排序等场景）
     *
     * @param menuList 菜单列表
     * @return 是否成功
     */
    @Operation(summary = "批量更新菜单")
    @PutMapping("/batch")
    public R<Void> batchUpdate(@RequestBody List<SysMenu> menuList) {
        return menuService.updateBatchById(menuList) ? R.ok() : R.fail();
    }

    /**
     * 删除菜单（支持批量删除）
     * <p>
     * 删除逻辑：
     * 1. 递归查找所有子菜单一并删除
     * 2. 自动解除与角色的关联关系（sys_role_menu）
     * 3. 物理删除菜单记录
     *
     * @param ids 菜单ID列表（多个ID用逗号分隔）
     * @return 是否成功
     */
    @Operation(summary = "删除菜单", description = "删除菜单及其子菜单，并自动解除角色关联")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return menuService.deleteMenuByIds(ids) ? R.ok() : R.fail();
    }
}