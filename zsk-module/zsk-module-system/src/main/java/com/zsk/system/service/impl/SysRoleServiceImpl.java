package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysMenu;
import com.zsk.system.domain.SysRole;
import com.zsk.system.domain.SysRoleMenu;
import com.zsk.system.domain.SysUserRole;
import com.zsk.system.mapper.SysRoleMapper;
import com.zsk.system.mapper.SysRoleMenuMapper;
import com.zsk.system.mapper.SysUserRoleMapper;
import com.zsk.system.service.ISysMenuService;
import com.zsk.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色管理 服务层实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final ISysMenuService menuService;

    /**
     * 根据用户ID查询角色权限
     *
     * @param userId 用户ID
     * @return 角色权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        log.info("根据用户ID查询角色权限, userId={}", userId);
        return new HashSet<>(baseMapper.selectRolePermissionByUserId(userId));
    }

    /**
     * 新增角色
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRole(SysRole role) {
        log.info("新增角色, roleName={}", role.getRoleName());
        if (role.getRoleKey() == null || role.getRoleKey().isEmpty()) {
            role.setRoleKey("role_" + System.currentTimeMillis());
        }
        if (role.getRoleSort() == null) {
            Long maxSort = baseMapper.selectMaxRoleSort();
            role.setRoleSort(maxSort == null ? 1 : maxSort.intValue() + 1);
        }
        boolean result = save(role);
        insertRoleMenu(role);
        log.info("新增角色完成, roleName={}, result={}", role.getRoleName(), result);
        return result;
    }

    /**
     * 修改角色
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        log.info("修改角色, roleId={}, roleName={}", role.getId(), role.getRoleName());
        boolean result = updateById(role);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getId()));
        insertRoleMenu(role);
        log.info("修改角色完成, roleId={}, result={}", role.getId(), result);
        return result;
    }

    /**
     * 批量删除角色信息
     * <p>
     * 执行流程：
     * 1. 删除角色与菜单的关联关系（sys_role_menu）
     * 2. 删除角色与用户的关联关系（sys_user_role）
     * 3. 物理删除角色记录
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleByIds(List<Long> roleIds) {
        log.info("批量删除角色, roleIds={}", roleIds);
        /**
         * 1. 删除角色与菜单的关联关系
         */
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));

        /**
         * 2. 删除角色与用户的关联关系
         */
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, roleIds));

        /**
         * 3. 物理删除角色记录
         */
        boolean result = removeBatchByIds(roleIds);
        log.info("批量删除角色完成, roleIds={}, result={}", roleIds, result);
        return result;
    }

    /**
     * 批量复制角色
     *
     * @param roleIds 角色ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyRoles(List<Long> roleIds) {
        log.info("批量复制角色, roleIds={}", roleIds);
        for (Long roleId : roleIds) {
            SysRole original = getById(roleId);
            if (original != null) {
                SysRole copy = new SysRole();
                copy.setRoleName(original.getRoleName() + "_副本");
                copy.setRoleKey(original.getRoleKey() + "_copy");
                copy.setRoleSort(original.getRoleSort());
                copy.setDataScope(original.getDataScope());
                copy.setMenuCheckStrictly(original.getMenuCheckStrictly());
                copy.setDeptCheckStrictly(original.getDeptCheckStrictly());
                copy.setStatus(original.getStatus());
                save(copy);

                List<SysRoleMenu> menuList = roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
                for (SysRoleMenu rm : menuList) {
                    SysRoleMenu newRm = new SysRoleMenu();
                    newRm.setRoleId(copy.getId());
                    newRm.setMenuId(rm.getMenuId());
                    roleMenuMapper.insert(newRm);
                }
                log.info("复制角色完成, originalId={}, newId={}, newName={}", roleId, copy.getId(), copy.getRoleName());
            }
        }
        log.info("批量复制角色完成, 数量={}", roleIds.size());
        return true;
    }

    /**
     * 自动补充所有父级菜单ID
     *
     * @param menuIds 原始菜单ID列表
     * @return 包含父级菜单的完整菜单ID列表
     */
    private List<Long> collectAllParentMenuIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return menuIds;
        }
        Set<Long> resultSet = new HashSet<>(menuIds);
        for (Long menuId : menuIds) {
            collectParentMenuIds(menuId, resultSet);
        }
        return new ArrayList<>(resultSet);
    }

    /**
     * 递归收集父级菜单ID
     *
     * @param menuId    当前菜单ID
     * @param resultSet 收集结果的集合
     */
    private void collectParentMenuIds(Long menuId, Set<Long> resultSet) {
        SysMenu menu = menuService.getById(menuId);
        if (menu == null || menu.getParentId() == null || menu.getParentId() == 0L) {
            return;
        }
        // 添加父级菜单ID
        if (resultSet.add(menu.getParentId())) {
            // 继续向上收集祖先菜单
            collectParentMenuIds(menu.getParentId(), resultSet);
        }
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    public void insertRoleMenu(SysRole role) {
        Long[] menuIds = role.getMenuIds();
        if (menuIds != null) {
            List<Long> menuIdList = new ArrayList<>();
            for (Long menuId : menuIds) {
                menuIdList.add(menuId);
            }
            // 自动补充父级菜单
            List<Long> allMenuIds = collectAllParentMenuIds(menuIdList);
            log.info("新增角色菜单关系, roleId={}, 原始menuIds={}, 补充后menuIds={}", role.getId(), menuIdList, allMenuIds);
            for (Long menuId : allMenuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }

    /**
     * 查询角色关联的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        log.info("查询角色关联的菜单ID列表, roleId={}", roleId);
        List<SysRoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        List<Long> result = list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        log.info("查询角色关联的菜单ID列表完成, roleId={}, 数量={}", roleId, result.size());
        return result;
    }

    /**
     * 绑定角色权限（追加菜单，已存在的不会重复绑定）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindRoleMenus(Long roleId, List<Long> menuIds) {
        log.info("绑定角色权限, roleId={}, menuIds={}", roleId, menuIds);
        List<Long> existMenuIds = selectMenuIdsByRoleId(roleId);
        Set<Long> existSet = new HashSet<>(existMenuIds);
        int addCount = 0;
        // 自动补充父级菜单
        List<Long> allMenuIds = collectAllParentMenuIds(menuIds);
        for (Long menuId : allMenuIds) {
            if (!existSet.contains(menuId)) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
                addCount++;
            }
        }
        log.info("绑定角色权限完成, roleId={}, 新增数量={}", roleId, addCount);
        return true;
    }

    /**
     * 解绑角色权限（移除指定菜单）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindRoleMenus(Long roleId, List<Long> menuIds) {
        log.info("解绑角色权限, roleId={}, menuIds={}", roleId, menuIds);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .in(SysRoleMenu::getMenuId, menuIds));
        log.info("解绑角色权限完成, roleId={}", roleId);
        return true;
    }

    /**
     * 更新角色权限（全量替换菜单，先删除原有权限再绑定新权限）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoleMenus(Long roleId, List<Long> menuIds) {
        log.info("更新角色权限, roleId={}, menuIds={}", roleId, menuIds);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        // 自动补充父级菜单
        List<Long> allMenuIds = collectAllParentMenuIds(menuIds);
        for (Long menuId : allMenuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
        log.info("更新角色权限完成, roleId={}, 数量={}", roleId, allMenuIds.size());
        return true;
    }

    /**
     * 查询角色关联的用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Override
    public List<Long> selectUserIdsByRoleId(Long roleId) {
        log.info("查询角色关联的用户ID列表, roleId={}", roleId);
        List<SysUserRole> list = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        List<Long> result = list.stream().map(SysUserRole::getUserId).collect(Collectors.toList());
        log.info("查询角色关联的用户ID列表完成, roleId={}, 数量={}", roleId, result.size());
        return result;
    }

    /**
     * 绑定角色用户（追加用户，已存在的不会重复绑定）
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindRoleUsers(Long roleId, List<Long> userIds) {
        log.info("绑定角色用户, roleId={}, userIds={}", roleId, userIds);
        List<Long> existUserIds = selectUserIdsByRoleId(roleId);
        Set<Long> existSet = new HashSet<>(existUserIds);
        int addCount = 0;
        for (Long userId : userIds) {
            if (!existSet.contains(userId)) {
                SysUserRole ur = new SysUserRole();
                ur.setRoleId(roleId);
                ur.setUserId(userId);
                userRoleMapper.insert(ur);
                addCount++;
            }
        }
        log.info("绑定角色用户完成, roleId={}, 新增数量={}", roleId, addCount);
        return true;
    }

    /**
     * 解绑角色用户（移除指定用户）
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindRoleUsers(Long roleId, List<Long> userIds) {
        log.info("解绑角色用户, roleId={}, userIds={}", roleId, userIds);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, userIds));
        log.info("解绑角色用户完成, roleId={}", roleId);
        return true;
    }

    /**
     * 更新角色用户（全量替换用户，先删除原有用户再绑定新用户）
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoleUsers(Long roleId, List<Long> userIds) {
        log.info("更新角色用户, roleId={}, userIds={}", roleId, userIds);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        if (userIds != null && !userIds.isEmpty()) {
            for (Long userId : userIds) {
                SysUserRole ur = new SysUserRole();
                ur.setRoleId(roleId);
                ur.setUserId(userId);
                userRoleMapper.insert(ur);
            }
        }
        log.info("更新角色用户完成, roleId={}, 数量={}", roleId, userIds != null ? userIds.size() : 0);
        return true;
    }
}
