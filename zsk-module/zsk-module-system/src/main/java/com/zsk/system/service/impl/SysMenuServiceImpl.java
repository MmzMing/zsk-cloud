package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysMenu;
import com.zsk.system.mapper.SysMenuMapper;
import com.zsk.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 菜单管理 服务层实现
 *
 * @author wuhuaming
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

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        if (userId != null && userId == 1L) {
            return this.list(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getMenuType, "M", "C")
                    .eq(SysMenu::getStatus, "0")
                    .orderByAsc(SysMenu::getParentId)
                    .orderByAsc(SysMenu::getOrderNum));
        }
        return baseMapper.selectMenuTreeByUserId(userId);
    }
}
