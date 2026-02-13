package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysRole;

import java.util.List;
import java.util.Set;

/**
 * 角色管理 服务层
 *
 * @author zsk
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
}
