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
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;

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
        // 新增角色信息
        boolean result = save(role);
        // 新增角色菜单信息
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
        // 修改角色信息
        boolean result = updateById(role);
        // 删除角色与菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getId()));
        // 新增角色菜单信息
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
     * 新增角色菜单信息
     */
    public void insertRoleMenu(SysRole role) {
        Long[] menuIds = role.getMenuIds();
        if (menuIds != null) {
            // 新增用户与角色管理
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
