package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysRole;

import java.util.List;
import java.util.Set;

/**
 * 角色管理 服务层
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface ISysRoleService extends IService<SysRole> {
    /**
     * 根据用户ID查询角色权限
     *
     * @param userId 用户ID
     * @return 角色权限列表
     */
    Set<String> selectRolePermissionByUserId(Long userId);

    /**
     * 新增角色
     *
     * @param role 角色信息
     * @return 结果
     */
    boolean insertRole(SysRole role);

    /**
     * 修改角色
     *
     * @param role 角色信息
     * @return 结果
     */
    boolean updateRole(SysRole role);

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    boolean deleteRoleByIds(List<Long> roleIds);

    /**
     * 批量复制角色
     *
     * @param roleIds 角色ID列表
     * @return 结果
     */
    boolean copyRoles(List<Long> roleIds);

    /**
     * 查询角色关联的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 绑定角色权限（追加菜单）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 结果
     */
    boolean bindRoleMenus(Long roleId, List<Long> menuIds);

    /**
     * 解绑角色权限（移除菜单）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 结果
     */
    boolean unbindRoleMenus(Long roleId, List<Long> menuIds);

    /**
     * 更新角色权限（全量替换菜单）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 结果
     */
    boolean updateRoleMenus(Long roleId, List<Long> menuIds);

    /**
     * 查询角色关联的用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByRoleId(Long roleId);

    /**
     * 绑定角色用户（追加用户）
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 结果
     */
    boolean bindRoleUsers(Long roleId, List<Long> userIds);

    /**
     * 解绑角色用户（移除用户）
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 结果
     */
    boolean unbindRoleUsers(Long roleId, List<Long> userIds);

    /**
     * 更新角色用户（全量替换用户）
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 结果
     */
    boolean updateRoleUsers(Long roleId, List<Long> userIds);
}
