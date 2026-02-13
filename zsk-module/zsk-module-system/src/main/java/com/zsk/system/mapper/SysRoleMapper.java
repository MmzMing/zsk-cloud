package com.zsk.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsk.system.domain.SysRole;

import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 角色表 数据层
 * 
 * @author zsk
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据用户ID查询角色权限
     * 
     * @param userId 用户ID
     * @return 角色权限列表
     */
    @Select("select distinct r.role_key from sys_role r " +
            "left join sys_user_role ur on ur.role_id = r.id " +
            "where ur.user_id = #{userId} and r.del_flag = 0 and r.status = 0")
    List<String> selectRolePermissionByUserId(Long userId);
}
