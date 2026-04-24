package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.security.utils.SecurityUtils;
import com.zsk.system.domain.SysUser;
import com.zsk.system.domain.SysUserRole;
import com.zsk.system.mapper.SysUserMapper;
import com.zsk.system.mapper.SysUserRoleMapper;
import com.zsk.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理 服务层实现
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper userRoleMapper;

    /**
     * 默认密码
     */
    private static final String DEFAULT_PASSWORD = "123456";

    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public SysUser selectUserByUserName(String username) {
        log.info("通过用户名查询用户信息, username={}", username);
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
        log.info("通过邮箱查询用户信息, email={}", email);
        return lambdaQuery().eq(SysUser::getEmail, email).one();
    }

    /**
     * 通过第三方ID查询用户信息
     *
     * @param loginType    登录类型
     * @param thirdPartyId 第三方ID
     * @return 用户信息
     */
    @Override
    public SysUser selectUserByThirdPartyId(String loginType, String thirdPartyId) {
        log.info("通过第三方ID查询用户信息, loginType={}, thirdPartyId={}", loginType, thirdPartyId);
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
        log.info("通过用户ID查询用户信息, userId={}", userId);
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
        log.info("新增保存用户信息, userName={}", user.getUserName());
        boolean rows = save(user);
        insertUserRole(user);
        log.info("新增保存用户信息完成, userName={}, result={}", user.getUserName(), rows);
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
        log.info("修改保存用户信息, userId={}, userName={}", user.getId(), user.getUserName());
        Long userId = user.getId();
        if (user.getRoleIds() != null && user.getRoleIds().length > 0) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
            insertUserRole(user);
        }
        boolean result = updateById(user);
        log.info("修改保存用户信息完成, userId={}, result={}", userId, result);
        return result;
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
        log.info("批量删除用户信息, userIds={}", userIds);
        for (Long userId : userIds) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        }
        boolean result = removeBatchByIds(userIds);
        log.info("批量删除用户信息完成, userIds={}, result={}", userIds, result);
        return result;
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public boolean resetPassword(Long userId) {
        log.info("重置用户密码, userId={}", userId);
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(SecurityUtils.encryptPassword(DEFAULT_PASSWORD));
        boolean result = updateById(user);
        log.info("重置用户密码完成, userId={}, result={}", userId, result);
        return result;
    }

    /**
     * 批量重置用户密码
     *
     * @param userIds 用户ID列表
     * @return 结果
     */
    @Override
    public boolean batchResetPassword(List<Long> userIds) {
        log.info("批量重置用户密码, userIds={}", userIds);
        for (Long userId : userIds) {
            resetPassword(userId);
        }
        log.info("批量重置用户密码完成, 数量={}", userIds.size());
        return true;
    }

    /**
     * 根据条件查询用户列表
     *
     * @param user 查询条件
     * @return 用户列表
     */
    @Override
    public List<SysUser> selectUserList(SysUser user) {
        log.info("根据条件查询用户列表, status={}, userName={}", user.getStatus(), user.getUserName());
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (user.getStatus() != null) {
            queryWrapper.eq(SysUser::getStatus, user.getStatus());
        }
        if (user.getUserName() != null) {
            queryWrapper.like(SysUser::getUserName, user.getUserName());
        }
        if (user.getPhonenumber() != null) {
            queryWrapper.like(SysUser::getPhonenumber, user.getPhonenumber());
        }
        queryWrapper.eq(SysUser::getDeleted, 0);
        List<SysUser> result = list(queryWrapper);
        log.info("根据条件查询用户列表完成, 数量={}", result.size());
        return result;
    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param user     查询条件
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<SysUser> selectUserPage(SysUser user, Integer pageNum, Integer pageSize) {
        log.info("根据条件分页查询用户列表, pageNum={}, pageSize={}, userName={}", pageNum, pageSize, user.getUserName());
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (user.getStatus() != null) {
            queryWrapper.eq(SysUser::getStatus, user.getStatus());
        }
        if (user.getUserName() != null) {
            queryWrapper.like(SysUser::getUserName, user.getUserName());
        }
        if (user.getPhonenumber() != null) {
            queryWrapper.like(SysUser::getPhonenumber, user.getPhonenumber());
        }
        queryWrapper.eq(SysUser::getDeleted, 0);
        Page<SysUser> result = page(page, queryWrapper);
        log.info("根据条件分页查询用户列表完成, 总数={}", result.getTotal());
        return PageResult.build(result);
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user) {
        Long[] roles = user.getRoleIds();
        if (roles != null && roles.length > 0) {
            log.info("新增用户角色信息, userId={}, roleIds={}", user.getId(), roles);
            for (Long roleId : roles) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    /**
     * 查询用户关联的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        log.info("查询用户关联的角色ID列表, userId={}", userId);
        List<SysUserRole> list = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (list != null && !list.isEmpty()) {
            List<Long> result = list.stream().map(SysUserRole::getRoleId).toList();
            log.info("查询用户关联的角色ID列表完成, userId={}, 数量={}", userId, result.size());
            return result;
        }
        return List.of();
    }

    /**
     * 绑定用户角色（追加角色）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindUserRoles(Long userId, List<Long> roleIds) {
        log.info("绑定用户角色, userId={}, roleIds={}", userId, roleIds);
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        List<Long> existingRoleIds = selectRoleIdsByUserId(userId);
        int addCount = 0;
        for (Long roleId : roleIds) {
            if (!existingRoleIds.contains(roleId)) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
                addCount++;
            }
        }
        log.info("绑定用户角色完成, userId={}, 新增数量={}", userId, addCount);
        return true;
    }

    /**
     * 解绑用户角色（移除角色）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindUserRoles(Long userId, List<Long> roleIds) {
        log.info("解绑用户角色, userId={}, roleIds={}", userId, roleIds);
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .in(SysUserRole::getRoleId, roleIds));
        log.info("解绑用户角色完成, userId={}", userId);
        return true;
    }

    /**
     * 更新用户角色（全量替换角色）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRoles(Long userId, List<Long> roleIds) {
        log.info("更新用户角色, userId={}, roleIds={}", userId, roleIds);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        log.info("更新用户角色完成, userId={}, 数量={}", userId, roleIds != null ? roleIds.size() : 0);
        return true;
    }
}
