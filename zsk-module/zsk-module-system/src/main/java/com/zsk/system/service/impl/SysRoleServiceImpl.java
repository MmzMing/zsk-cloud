package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysRole;
import com.zsk.system.domain.SysRoleMenu;
import com.zsk.system.mapper.SysRoleMapper;
import com.zsk.system.mapper.SysRoleMenuMapper;
import com.zsk.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleByIds(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        }
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
}
