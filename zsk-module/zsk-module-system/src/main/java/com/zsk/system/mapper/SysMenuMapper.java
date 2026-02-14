package com.zsk.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.system.domain.SysMenu;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单权限表 数据层
 *
 * @author wuhuaming
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    /**
     * 根据用户ID查询菜单权限
     *
     * @param userId 用户ID
     * @return 菜单权限列表
     */
    @Select("select distinct m.perms from sys_menu m " +
            "left join sys_role_menu rm on m.id = rm.menu_id " +
            "left join sys_user_role ur on rm.role_id = ur.role_id " +
            "left join sys_role r on r.id = ur.role_id " +
            "where ur.user_id = #{userId} and m.status = 0 and r.status = 0 and r.deleted = 0")
    List<String> selectMenuPermissionByUserId(Long userId);

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("select distinct m.id, m.parent_id, m.menu_name, m.path, m.component, m.query, m.visible, m.status, " +
            "ifnull(m.perms,'') as perms, m.is_frame, m.is_cache, m.menu_type, m.icon, m.order_num, m.create_time " +
            "from sys_menu m " +
            "left join sys_role_menu rm on m.id = rm.menu_id " +
            "left join sys_user_role ur on rm.role_id = ur.role_id " +
            "left join sys_role r on r.id = ur.role_id " +
            "where ur.user_id = #{userId} and m.menu_type in ('M', 'C') and m.status = 0 and r.status = 0 and r.deleted = 0 " +
            "order by m.parent_id, m.order_num")
    List<SysMenu> selectMenuTreeByUserId(Long userId);
}
