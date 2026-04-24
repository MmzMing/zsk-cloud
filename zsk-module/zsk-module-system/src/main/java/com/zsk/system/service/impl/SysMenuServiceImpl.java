package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.core.context.SecurityContext;
import com.zsk.system.domain.SysMenu;
import com.zsk.system.domain.SysRoleMenu;
import com.zsk.system.domain.vo.MenuTreeVo;
import com.zsk.system.mapper.SysMenuMapper;
import com.zsk.system.mapper.SysRoleMenuMapper;
import com.zsk.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单管理 服务层实现
 *
 * @author wuhuaming
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 根据用户ID查询菜单权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermissionByUserId(Long userId) {
        return new HashSet<>(baseMapper.selectMenuPermissionByUserId(userId));
    }

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        return baseMapper.selectMenuTreeByUserId(userId);
    }

    /**
     * 批量删除菜单
     * <p>
     * 执行流程：
     * 1. 递归收集所有待删除菜单ID（包含子菜单）
     * 2. 删除角色与菜单的关联关系（sys_role_menu）
     * 3. 物理删除菜单记录
     *
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenuByIds(List<Long> menuIds) {
        Set<Long> allMenuIds = new HashSet<>(menuIds);
        for (Long menuId : menuIds) {
            collectChildMenuIds(menuId, allMenuIds);
        }
        List<Long> allMenuIdList = new ArrayList<>(allMenuIds);

        log.info("删除菜单，原始ID: {}，包含子菜单后ID: {}", menuIds, allMenuIdList);

        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getMenuId, allMenuIdList));

        return removeByIds(allMenuIdList);
    }

    /**
     * 获取当前用户的菜单树（含排序）
     * <p>
     * 根据当前登录用户ID查询其有权限访问的菜单，并构建树形结构
     *
     * @return 菜单树列表
     */
    @Override
    public List<MenuTreeVo> getUserMenuTree() {
        Long userId = SecurityContext.getUserId();
        if (userId == null) {
            log.warn("获取用户菜单树失败：用户未登录");
            return Collections.emptyList();
        }

        List<SysMenu> menus = selectMenuTreeByUserId(userId);
        return buildMenuTree(menus);
    }

    /**
     * 递归收集子菜单ID
     *
     * @param parentId  父菜单ID
     * @param resultSet 收集结果的集合
     */
    private void collectChildMenuIds(Long parentId, Set<Long> resultSet) {
        List<SysMenu> children = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, parentId));
        for (SysMenu child : children) {
            resultSet.add(child.getId());
            collectChildMenuIds(child.getId(), resultSet);
        }
    }

    /**
     * 构建菜单树结构
     *
     * @param menus 菜单列表
     * @return 菜单树列表
     */
    private List<MenuTreeVo> buildMenuTree(List<SysMenu> menus) {
        // 按父ID分组
        Map<Long, List<SysMenu>> groupedMenus = menus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        // 获取顶级菜单（parentId = 0）
        List<SysMenu> topMenus = groupedMenus.getOrDefault(0L, Collections.emptyList());

        // 处理排序号重复问题，重新分配序号
        List<SysMenu> sortedTopMenus = resolveDuplicateOrderNum(topMenus);

        // 递归构建树
        return sortedTopMenus.stream()
                .map(menu -> buildTreeNode(menu, groupedMenus))
                .collect(Collectors.toList());
    }

    /**
     * 构建单个菜单树节点
     *
     * @param menu         当前菜单
     * @param groupedMenus 按父ID分组的菜单映射
     * @return 菜单树节点
     */
    private MenuTreeVo buildTreeNode(SysMenu menu, Map<Long, List<SysMenu>> groupedMenus) {
        MenuTreeVo node = MenuTreeVo.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuName(menu.getMenuName())
                .path(menu.getPath())
                .component(menu.getComponent())
                .query(menu.getQuery())
                .isFrame(menu.getIsFrame())
                .isCache(menu.getIsCache())
                .menuType(menu.getMenuType())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .perms(menu.getPerms())
                .icon(menu.getIcon())
                .orderNum(menu.getOrderNum())
                .build();

        // 递归构建子菜单
        List<SysMenu> children = groupedMenus.getOrDefault(menu.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            // 处理排序号重复问题
            List<SysMenu> sortedChildren = resolveDuplicateOrderNum(children);
            List<MenuTreeVo> childNodes = sortedChildren.stream()
                    .map(child -> buildTreeNode(child, groupedMenus))
                    .collect(Collectors.toList());
            node.setChildren(childNodes);
        }

        return node;
    }

    /**
     * 解决同一层级菜单排序号重复问题（优化版）
     * <p>
     * 算法：排序后单次遍历，维护期望序号expectedOrder，
     * 遇到重复或乱序时直接分配期望序号，无需额外检测遍历
     *
     * @param menus 同一层级的菜单列表
     * @return 排序后的菜单列表（排序号已去重）
     */
    private List<SysMenu> resolveDuplicateOrderNum(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 按orderNum排序，null视为0
        List<SysMenu> sortedMenus = menus.stream()
                .sorted(Comparator.comparingInt(m -> m.getOrderNum() != null ? m.getOrderNum() : 0))
                .collect(Collectors.toList());

        int expectedOrder = 1;
        boolean hasDuplicate = false;

        // 单次遍历：检测重复并重新分配序号
        for (SysMenu menu : sortedMenus) {
            Integer currentOrder = menu.getOrderNum();

            // 如果当前序号小于期望序号，说明有重复或乱序
            if (currentOrder == null || currentOrder < expectedOrder) {
                menu.setOrderNum(expectedOrder);
                hasDuplicate = true;
            } else {
                // 当前序号正常，更新期望序号
                expectedOrder = currentOrder + 1;
            }
        }

        if (hasDuplicate) {
            log.debug("检测到菜单排序号重复，已重新分配序号: {}",
                    sortedMenus.stream().map(m -> m.getMenuName() + ":" + m.getOrderNum())
                            .collect(Collectors.toList()));
        }

        return sortedMenus;
    }
}
