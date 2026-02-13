package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysRole;
import com.zsk.system.mapper.SysRoleMapper;
import com.zsk.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色管理 服务层实现
 * 
 * @author zsk
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        return new HashSet<>(baseMapper.selectRolePermissionByUserId(userId));
    }
}
