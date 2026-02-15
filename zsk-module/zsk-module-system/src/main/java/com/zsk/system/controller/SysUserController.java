package com.zsk.system.controller;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.domain.R;
import com.zsk.system.api.domain.SysUserApi;
import com.zsk.system.api.model.LoginUser;
import com.zsk.system.domain.SysUser;
import com.zsk.system.service.ISysMenuService;
import com.zsk.system.service.ISysRoleService;
import com.zsk.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户管理 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysMenuService menuService;

    /**
     * 获取用户详细信息（通过用户名）
     *
     * @param username 用户名
     * @param source 请求来源
     * @return 用户信息
     */
    @Operation(summary = "获取用户详细信息（通过用户名）")
    @GetMapping("/info/{username}")
    public R<?> getInfoByUsername(@PathVariable String username, @RequestHeader(value = CommonConstants.REQUEST_SOURCE_HEADER, required = false) String source) {
        SysUser sysUser = userService.selectUserByUserName(username);
        if (sysUser == null) {
            return R.fail("用户不存在");
        }

        if (CommonConstants.REQUEST_SOURCE_INNER.equals(source)) {
            return R.ok(createLoginUser(sysUser));
        }

        return R.ok(sysUser);
    }

    /**
     * 获取用户详细信息（通过邮箱）
     *
     * @param email 邮箱
     * @param source 请求来源
     * @return 用户信息
     */
    @Operation(summary = "获取用户详细信息（通过邮箱）")
    @GetMapping("/info/email/{email}")
    public R<?> getInfoByEmail(@PathVariable String email, @RequestHeader(value = CommonConstants.REQUEST_SOURCE_HEADER, required = false) String source) {
        SysUser sysUser = userService.selectUserByEmail(email);
        if (sysUser == null) {
            return R.fail("用户不存在");
        }

        if (CommonConstants.REQUEST_SOURCE_INNER.equals(source)) {
            return R.ok(createLoginUser(sysUser));
        }

        return R.ok(sysUser);
    }

    /**
     * 获取用户详细信息（通过第三方ID）
     *
     * @param loginType 登录类型
     * @param thirdPartyId 第三方ID
     * @param source 请求来源
     * @return 用户信息
     */
    @Operation(summary = "获取用户详细信息（通过第三方ID）")
    @GetMapping("/info/thirdparty/{loginType}/{thirdPartyId}")
    public R<?> getInfoByThirdPartyId(@PathVariable String loginType, @PathVariable String thirdPartyId, @RequestHeader(value = CommonConstants.REQUEST_SOURCE_HEADER, required = false) String source) {
        SysUser sysUser = userService.selectUserByThirdPartyId(loginType, thirdPartyId);
        if (sysUser == null) {
            return R.fail("用户不存在");
        }

        if (CommonConstants.REQUEST_SOURCE_INNER.equals(source)) {
            return R.ok(createLoginUser(sysUser));
        }

        return R.ok(sysUser);
    }

    /**
     * 创建登录用户对象
     *
     * @param sysUser 系统用户
     * @return 登录用户
     */
    private LoginUser createLoginUser(SysUser sysUser) {
        LoginUser loginUser = new LoginUser();
        SysUserApi apiUser = getSysUserApi(sysUser);

        loginUser.setSysUser(apiUser);

        // 角色权限标识
        Set<String> roles = roleService.selectRolePermissionByUserId(sysUser.getId());
        loginUser.setRoles(roles);

        // 菜单权限标识
        Set<String> permissions = menuService.selectMenuPermissionByUserId(sysUser.getId());
        loginUser.setPermissions(permissions);

        return loginUser;
    }

    /**
     * 转换为API用户对象
     *
     * @param sysUser 系统用户
     * @return API用户
     */
    private static @NonNull SysUserApi getSysUserApi(SysUser sysUser) {
        SysUserApi apiUser = new SysUserApi();
        apiUser.setId(sysUser.getId());
        apiUser.setUserName(sysUser.getUserName());
        apiUser.setNickName(sysUser.getNickName());
        apiUser.setEmail(sysUser.getEmail());
        apiUser.setPhonenumber(sysUser.getPhonenumber());
        apiUser.setSex(sysUser.getSex());
        apiUser.setAvatar(sysUser.getAvatar());
        apiUser.setPassword(sysUser.getPassword());
        apiUser.setStatus(sysUser.getStatus());
        apiUser.setDeleted(sysUser.getDeleted());
        return apiUser;
    }

    /**
     * 查询用户列表
     *
     * @param user 查询条件
     * @return 用户列表
     */
    @Operation(summary = "查询用户列表")
    @GetMapping("/list")
    public R<List<SysUser>> list(SysUser user) {
        return R.ok(userService.list());
    }

    /**
     * 获取用户详细信息
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Operation(summary = "获取用户详细信息")
    @GetMapping("/{id}")
    public R<SysUser> getInfo(@PathVariable Long id) {
        return R.ok(userService.selectUserById(id));
    }

    /**
     * 新增用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    @Operation(summary = "新增用户")
    @PostMapping
    public R<Boolean> add(@RequestBody SysUser user) {
        return R.ok(userService.insertUser(user));
    }

    /**
     * 修改用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    @Operation(summary = "修改用户")
    @PutMapping
    public R<Void> edit(@RequestBody SysUser user) {
        return userService.updateUser(user) ? R.ok() : R.fail();
    }

    /**
     * 切换用户状态
     *
     * @param id 用户ID
     * @param body 请求体（包含status字段）
     * @return 是否成功
     */
    @Operation(summary = "切换用户状态")
    @PutMapping("/{id}/status")
    public R<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        return userService.updateUser(user) ? R.ok() : R.fail();
    }

    /**
     * 内部接口：更新用户信息（供其他服务调用）
     *
     * @param userApi 用户API对象
     * @param source 请求来源
     * @return 是否成功
     */
    @Operation(summary = "内部接口：更新用户信息")
    @PutMapping("/inner")
    public R<Boolean> updateUserInfo(@RequestBody SysUserApi userApi, @RequestHeader(value = CommonConstants.REQUEST_SOURCE_HEADER, required = false) String source) {
        if (!CommonConstants.REQUEST_SOURCE_INNER.equals(source)) {
            return R.fail("无权限访问");
        }

        SysUser user = new SysUser();
        user.setId(userApi.getId());
        user.setPassword(userApi.getPassword());
        user.setNickName(userApi.getNickName());
        user.setEmail(userApi.getEmail());
        user.setPhonenumber(userApi.getPhonenumber());
        user.setSex(userApi.getSex());
        user.setAvatar(userApi.getAvatar());
        user.setStatus(userApi.getStatus());

        return R.ok(userService.updateUser(user));
    }

    /**
     * 删除用户（支持批量删除）
     *
     * @param ids 用户ID列表
     * @return 是否成功
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return userService.deleteUserByIds(ids) ? R.ok() : R.fail();
    }

    /**
     * 重置密码
     *
     * @param id 用户ID
     * @return 是否成功
     */
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id) {
        return userService.resetPassword(id) ? R.ok() : R.fail();
    }

    /**
     * 批量重置密码
     *
     * @param ids 用户ID列表
     * @return 是否成功
     */
    @Operation(summary = "批量重置密码")
    @PutMapping("/{ids}/reset-password")
    public R<Void> batchResetPassword(@PathVariable List<Long> ids) {
        return userService.batchResetPassword(ids) ? R.ok() : R.fail();
    }
}
