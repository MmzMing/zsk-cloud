package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysRole;
import com.zsk.system.domain.SysRoleMenu;
import com.zsk.system.domain.SysUserRole;
import com.zsk.system.mapper.SysRoleMapper;
import com.zsk.system.mapper.SysRoleMenuMapper;
import com.zsk.system.mapper.SysUserRoleMapper;
import com.zsk.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色管理 服务层实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 根据用户ID查询角色权限
     *
     * @param userId 用户ID
     * @return 角色权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
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
        if (role.getRoleKey() == null || role.getRoleKey().isEmpty()) {
            role.setRoleKey("role_" + System.currentTimeMillis());
        }
        if (role.getRoleSort() == null) {
            Long maxSort = baseMapper.selectMaxRoleSort();
            role.setRoleSort(maxSort == null ? 1 : maxSort.intValue() + 1);
        }
        boolean result = save(role);
        insertRoleMenu(role);
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
        boolean result = updateById(role);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getId()));
        insertRoleMenu(role);
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
        return removeBatchByIds(roleIds);
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
            }
        }
        return true;
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    public void insertRoleMenu(SysRole role) {
        Long[] menuIds = role.getMenuIds();
        if (menuIds != null) {
            for (Long menuId : menuIds) {
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
        List<SysRoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        return list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
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
        List<Long> existMenuIds = selectMenuIdsByRoleId(roleId);
        Set<Long> existSet = new HashSet<>(existMenuIds);
        for (Long menuId : menuIds) {
            if (!existSet.contains(menuId)) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
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
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .in(SysRoleMenu::getMenuId, menuIds));
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
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
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
        List<SysUserRole> list = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        return list.stream().map(SysUserRole::getUserId).collect(Collectors.toList());
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
        List<Long> existUserIds = selectUserIdsByRoleId(roleId);
        Set<Long> existSet = new HashSet<>(existUserIds);
        for (Long userId : userIds) {
            if (!existSet.contains(userId)) {
                SysUserRole ur = new SysUserRole();
                ur.setRoleId(roleId);
                ur.setUserId(userId);
                userRoleMapper.insert(ur);
            }
        }
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
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, userIds));
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
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        for (Long userId : userIds) {
            SysUserRole ur = new SysUserRole();
            ur.setRoleId(roleId);
            ur.setUserId(userId);
            userRoleMapper.insert(ur);
        }
        return true;
    }
}
