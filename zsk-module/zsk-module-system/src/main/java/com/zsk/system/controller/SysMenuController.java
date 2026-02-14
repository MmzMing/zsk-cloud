package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.SysMenu;
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
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService menuService;

    /**
     * 查询菜单列表
     */
    @Operation(summary = "查询菜单列表")
    @GetMapping("/list")
    public R<List<SysMenu>> list(SysMenu menu) {
        return R.ok(menuService.list());
    }

    /**
     * 根据用户ID查询菜单树列表
     */
    @Operation(summary = "根据用户ID查询菜单树列表")
    @GetMapping("/user/{userId}")
    public R<List<SysMenu>> userMenu(@PathVariable Long userId) {
        return R.ok(menuService.selectMenuTreeByUserId(userId));
    }

    /**
     * 获取菜单详细信息
     */
    @Operation(summary = "获取菜单详细信息")
    @GetMapping("/{id}")
    public R<SysMenu> getInfo(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    /**
     * 新增菜单
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        return menuService.save(menu) ? R.ok() : R.fail();
    }

    /**
     * 修改菜单
     */
    @Operation(summary = "修改菜单")
    @PutMapping
    public R<Void> edit(@RequestBody SysMenu menu) {
        return menuService.updateById(menu) ? R.ok() : R.fail();
    }

    /**
     * 删除菜单
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        return menuService.removeById(id) ? R.ok() : R.fail();
    }
}
