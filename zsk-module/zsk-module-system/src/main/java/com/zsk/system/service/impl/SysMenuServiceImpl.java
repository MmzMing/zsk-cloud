package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysMenu;
import com.zsk.system.mapper.SysMenuMapper;
import com.zsk.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 菜单管理 服务层实现
 * 
 * @author zsk
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {
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
}
