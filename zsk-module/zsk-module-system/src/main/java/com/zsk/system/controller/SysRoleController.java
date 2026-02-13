package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.SysRole;
import com.zsk.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理 控制器
 * 
 * @author zsk
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService roleService;

    /**
     * 查询角色列表
     */
    @Operation(summary = "查询角色列表")
    @GetMapping("/list")
    public R<List<SysRole>> list(SysRole role) {
        return R.ok(roleService.list());
    }

    /**
     * 获取角色详细信息
     */
    @Operation(summary = "获取角色详细信息")
    @GetMapping("/{id}")
    public R<SysRole> getInfo(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    /**
     * 新增角色
     */
    @Operation(summary = "新增角色")
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        return roleService.save(role) ? R.ok() : R.fail();
    }

    /**
     * 修改角色
     */
    @Operation(summary = "修改角色")
    @PutMapping
    public R<Void> edit(@RequestBody SysRole role) {
        return roleService.updateById(role) ? R.ok() : R.fail();
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return roleService.removeByIds(ids) ? R.ok() : R.fail();
    }
}
