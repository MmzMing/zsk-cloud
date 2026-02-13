package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysRole;

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
}
