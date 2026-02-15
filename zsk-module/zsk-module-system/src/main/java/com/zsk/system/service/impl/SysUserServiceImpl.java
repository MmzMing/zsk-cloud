package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.security.utils.SecurityUtils;
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
 * @date 2026-02-15
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper userRoleMapper;

    /** 默认密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public SysUser selectUserByUserName(String username) {
        return lambdaQuery().eq(SysUser::getUserName, username).one();
    }

    /**
     * 通过邮箱查询用户信息
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Override
    public SysUser selectUserByEmail(String email) {
        return lambdaQuery().eq(SysUser::getEmail, email).one();
    }

    /**
     * 通过第三方ID查询用户信息
     *
     * @param loginType 登录类型
     * @param thirdPartyId 第三方ID
     * @return 用户信息
     */
    @Override
    public SysUser selectUserByThirdPartyId(String loginType, String thirdPartyId) {
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
        boolean rows = save(user);
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
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
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
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        }
        return removeBatchByIds(userIds);
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public boolean resetPassword(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(SecurityUtils.encryptPassword(DEFAULT_PASSWORD));
        return updateById(user);
    }

    /**
     * 批量重置用户密码
     *
     * @param userIds 用户ID列表
     * @return 结果
     */
    @Override
    public boolean batchResetPassword(List<Long> userIds) {
        for (Long userId : userIds) {
            resetPassword(userId);
        }
        return true;
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user) {
        Long[] roles = user.getRoleIds();
        if (roles != null && roles.length > 0) {
            for (Long roleId : roles) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }
}
