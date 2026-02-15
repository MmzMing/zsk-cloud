package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.SysMenu;
import com.zsk.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限管理 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final ISysMenuService menuService;

    /**
     * 获取权限列表（按模块分组）
     *
     * @return 权限分组列表
     */
    @Operation(summary = "获取权限列表")
    @GetMapping("/list")
    public R<List<Map<String, Object>>> list() {
        /** 查询所有菜单 */
        List<SysMenu> menuList = menuService.list();

        /** 按父菜单分组 */
        Map<Long, List<SysMenu>> groupedMenus = menuList.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        /** 构建权限分组列表 */
        List<Map<String, Object>> result = new ArrayList<>();

        /** 获取顶级菜单作为模块 */
        List<SysMenu> topMenus = groupedMenus.getOrDefault(0L, new ArrayList<>());
        for (SysMenu topMenu : topMenus) {
            Map<String, Object> group = new HashMap<>();
            group.put("id", String.valueOf(topMenu.getId()));
            group.put("label", topMenu.getMenuName());

            /** 获取子菜单作为权限项 */
            List<Map<String, Object>> items = new ArrayList<>();
            List<SysMenu> children = groupedMenus.getOrDefault(topMenu.getId(), new ArrayList<>());
            for (SysMenu child : children) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", String.valueOf(child.getId()));
                item.put("key", child.getPerms() != null ? child.getPerms() : "");
                item.put("name", child.getMenuName());
                item.put("module", topMenu.getMenuName());
                item.put("description", child.getRemark() != null ? child.getRemark() : "");
                item.put("type", "menu".equals(child.getMenuType()) ? "menu" : "action");
                item.put("createdAt", child.getCreateTime() != null ? child.getCreateTime().toString() : "");
                items.add(item);
            }
            group.put("items", items);
            result.add(group);
        }

        return R.ok(result);
    }
}
