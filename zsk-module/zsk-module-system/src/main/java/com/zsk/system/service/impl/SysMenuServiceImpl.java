package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysMenu;
import com.zsk.system.domain.SysRoleMenu;
import com.zsk.system.mapper.SysMenuMapper;
import com.zsk.system.mapper.SysRoleMenuMapper;
import com.zsk.system.service.ISysMenuService;
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
 * 菜单管理 服务层实现
 *
 * @author wuhuaming
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

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

    /**
     * 批量删除菜单
     * <p>
     * 执行流程：
     * 1. 递归收集所有待删除菜单ID（包含子菜单）
     * 2. 删除角色与菜单的关联关系（sys_role_menu）
     * 3. 物理删除菜单记录
     *
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenuByIds(List<Long> menuIds) {
        /**
         * 1. 递归收集所有待删除的菜单ID（包含子菜单）
         * 避免删除父菜单后子菜单成为孤儿数据
         */
        Set<Long> allMenuIds = new HashSet<>(menuIds);
        for (Long menuId : menuIds) {
            collectChildMenuIds(menuId, allMenuIds);
        }
        List<Long> allMenuIdList = new ArrayList<>(allMenuIds);

        log.info("删除菜单，原始ID: {}，包含子菜单后ID: {}", menuIds, allMenuIdList);

        /**
         * 2. 删除角色与菜单的关联关系
         * 查询哪些角色绑定了这些菜单，记录日志后删除关联
         */
        List<SysRoleMenu> roleMenuList = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getMenuId, allMenuIdList));
        if (!roleMenuList.isEmpty()) {
            Set<Long> affectedRoleIds = roleMenuList.stream()
                    .map(SysRoleMenu::getRoleId)
                    .collect(Collectors.toSet());
            log.info("以下角色与待删除菜单存在关联，将自动解除关联，角色ID: {}", affectedRoleIds);

            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                    .in(SysRoleMenu::getMenuId, allMenuIdList));
        }

        /**
         * 3. 物理删除菜单记录
         */
        return removeByIds(allMenuIdList);
    }

    /**
     * 递归收集子菜单ID
     *
     * @param parentId   父菜单ID
     * @param resultSet  收集结果的集合
     */
    private void collectChildMenuIds(Long parentId, Set<Long> resultSet) {
        List<SysMenu> children = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, parentId));
        for (SysMenu child : children) {
            resultSet.add(child.getId());
            collectChildMenuIds(child.getId(), resultSet);
        }
    }
}
