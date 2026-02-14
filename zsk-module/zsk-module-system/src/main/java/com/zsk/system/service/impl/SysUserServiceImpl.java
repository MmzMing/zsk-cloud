package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysUser;
import com.zsk.system.domain.SysUserRole;
import com.zsk.system.mapper.SysUserMapper;
import com.zsk.system.mapper.SysUserRoleMapper;
import com.zsk.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理 服务层实现
 *
 * @author wuhuaming
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper userRoleMapper;

    @Override
    public SysUser selectUserByUserName(String username) {
        return lambdaQuery().eq(SysUser::getUserName, username).one();
    }

    @Override
    public SysUser selectUserByEmail(String email) {
        return lambdaQuery().eq(SysUser::getEmail, email).one();
    }

    @Override
    public SysUser selectUserByThirdPartyId(String loginType, String thirdPartyId) {
        // 假设第三方登录ID存储在某个字段，或者根据用户名规则匹配
        // 这里简单实现为匹配用户名 loginType + "_" + thirdPartyId
        String username = loginType + "_" + thirdPartyId;
        return selectUserByUserName(username);
    }

    /**
     * 通过用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId) {
        SysUser user = getById(userId);
        if (user != null) {
            List<SysUserRole> list = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
            if (list != null && !list.isEmpty()) {
                user.setRoleIds(list.stream().map(SysUserRole::getRoleId).toArray(Long[]::new));
            }
        }
        return user;
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(SysUser user) {
        // 新增用户信息
        boolean rows = save(user);
        // 新增用户岗位关联
        insertUserRole(user);
        return rows;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        Long userId = user.getId();
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 新增用户与角色管理
        insertUserRole(user);
        return updateById(user);
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserByIds(List<Long> userIds) {
        for (Long userId : userIds) {
            // 删除用户与角色关联
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        }
        return removeBatchByIds(userIds);
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user) {
        Long[] roles = user.getRoleIds();
        if (roles != null && roles.length > 0) {
            // 新增用户与角色管理
            for (Long roleId : roles) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }
}
