package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysMenu;
import com.zsk.system.domain.vo.MenuTreeVo;

import java.util.List;
import java.util.Set;

/**
 * 菜单管理 服务层
 *
 * @author wuhuaming
 */
public interface ISysMenuService extends IService<SysMenu> {
    /**
     * 根据用户ID查询菜单权限
     *
     * @param userId 用户ID
     * @return 菜单权限列表
     */
    Set<String> selectMenuPermissionByUserId(Long userId);

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 批量删除菜单
     * <p>
     * 删除逻辑：
     * 1. 递归收集所有待删除菜单ID（包含子菜单）
     * 2. 删除角色与菜单的关联关系（sys_role_menu）
     * 3. 物理删除菜单记录
     *
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean deleteMenuByIds(List<Long> menuIds);

    /**
     * 获取当前用户的菜单树（含排序）
     * <p>
     * 根据当前登录用户ID查询其有权限访问的菜单，并构建树形结构
     *
     * @return 菜单树列表
     */
    List<MenuTreeVo> getUserMenuTree();
}