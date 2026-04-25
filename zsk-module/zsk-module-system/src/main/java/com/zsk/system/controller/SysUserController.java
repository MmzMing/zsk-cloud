package com.zsk.system.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.context.SecurityContext;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.api.RemoteDocFilesService;
import com.zsk.document.api.domain.DocFilesApi;
import com.zsk.system.api.domain.SysUserApi;
import com.zsk.system.api.model.LoginUser;
import com.zsk.system.domain.SysUser;
import com.zsk.system.service.ISysMenuService;
import com.zsk.system.service.ISysRoleService;
import com.zsk.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户管理 控制器
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysMenuService menuService;
    private final RemoteDocFilesService remoteDocFilesService;

    /**
     * 获取当前请求的用户信息（从本地线程中获取）
     *
     * @return 当前登录用户信息
     */
    @Operation(summary = "获取当前请求的用户信息")
    @GetMapping("/current")
    public R<LoginUser> getCurrentUser() {
        LoginUser loginUser = new LoginUser();
        SysUserApi sysUserApi = new SysUserApi();

        SysUser sysUserDtl = userService.selectUserById(SecurityContext.getUserId());
        BeanUtil.copyProperties(sysUserDtl, sysUserApi);

        //组装
        loginUser.setSysUser(sysUserApi);
        loginUser.setRoles(SecurityContext.getRoles());
        loginUser.setPermissions(SecurityContext.getPermissions());
        return R.ok(loginUser);
    }

    /**
     * 获取用户详细信息（通过用户名）
     *
     * @param username 用户名
     * @param source   请求来源
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
     * @param email  邮箱
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
     * @param loginType    登录类型
     * @param thirdPartyId 第三方ID
     * @param source       请求来源
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
    private static SysUserApi getSysUserApi(SysUser sysUser) {
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
     * @param user     查询条件
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Operation(summary = "查询用户列表")
    @GetMapping("/list")
    public R<PageResult<SysUser>> list(SysUser user,
                                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return R.ok(userService.selectUserPage(user, pageNum, pageSize));
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
     * 根据用户ID列表查询用户列表
     *
     * @param ids 用户ID列表
     * @return 用户列表
     */
    @Operation(summary = "根据用户ID列表查询用户列表")
    @GetMapping("/list/ids")
    public R<List<SysUser>> listByIds(@RequestParam List<Long> ids) {
        return R.ok(userService.listByIds(ids));
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
     * @param id   用户ID
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
     * @param source  请求来源
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
        user.setLoginIp(userApi.getLoginIp());
        user.setLoginDate(userApi.getLoginDate());

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

    /**
     * 更新用户信息（支持头像文件上传）
     *
     * <p>业务逻辑：
     * <ul>
     *   <li>文件校验：文件大小不超过2MB，仅支持图片类型</li>
     *   <li>空文件处理：前端不传文件或传空文件时不调用文件服务</li>
     *   <li>旧头像清理：新头像与原头像不一致时，删除旧头像文件并将avatarId置空</li>
     * </ul>
     *
     * @param user 用户信息，必须包含用户ID
     * @param file 头像文件（可选），支持 jpg、png、gif 等图片格式，大小不超过2MB
     * @return 更新结果，成功返回 R.ok()，失败返回 R.fail() 并携带错误信息
     */
    @Operation(summary = "更新用户信息（支持头像文件上传）")
    @PostMapping("/update/infoFile")
    public R<Void> updateSystemUserInfo(@RequestPart("user") SysUser user,
                                        @RequestPart(value = "file", required = false) MultipartFile file) {
        // 参数校验：用户ID不能为空
        Long userId = user.getId();
        if (userId == null) {
            return R.fail("用户ID不能为空");
        }

        // 权限校验：只能修改自己的个人信息
        Long currentUserId = SecurityContext.getUserId();
        if (!userId.equals(currentUserId)) {
            return R.fail("无权修改他人用户信息");
        }

        // 查询现有用户信息
        SysUser existingUser = userService.selectUserById(userId);
        if (existingUser == null) {
            return R.fail("用户不存在");
        }

        // 保存原始头像信息，用于后续对比
        String originalAvatar = existingUser.getAvatar();
        Long originalAvatarId = existingUser.getAvatarId();

        // 判断是否上传了新文件
        if (file != null && !file.isEmpty()) {
            // 文件大小校验：不超过2MB
            long maxSize = 2 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return R.fail("文件大小不能超过2MB");
            }

            // 文件类型校验：仅支持图片
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return R.fail("仅支持图片文件");
            }

            // 调用文件服务上传头像
            R<DocFilesApi> uploadResult = remoteDocFilesService.upload(file, CommonConstants.REQUEST_SOURCE_INNER);
            if (!uploadResult.isSuccess()) {
                return R.fail("头像上传失败: " + uploadResult.getMsg());
            }

            // 更新用户头像信息
            DocFilesApi docFilesApi = uploadResult.getData();
            user.setAvatar(docFilesApi.getUrl());
            user.setAvatarId(Long.parseLong(docFilesApi.getFileId()));

            // 新头像与原头像不一致时，删除旧头像文件
            if (StrUtil.isNotEmpty(originalAvatar) && !originalAvatar.equals(docFilesApi.getUrl()) && originalAvatarId != null) {
                remoteDocFilesService.remove(String.valueOf(originalAvatarId), CommonConstants.REQUEST_SOURCE_INNER);
                existingUser.setAvatarId(null);
            }
        } else if (StrUtil.isNotEmpty(user.getAvatar()) && !user.getAvatar().equals(originalAvatar) && originalAvatarId != null) {
            // 未上传文件但通过参数传入了新头像URL，且与原头像不一致时，删除旧头像文件
            remoteDocFilesService.remove(String.valueOf(originalAvatarId), CommonConstants.REQUEST_SOURCE_INNER);
            existingUser.setAvatarId(null);
        }

        // 更新用户信息到数据库
        return userService.updateUser(user) ? R.ok() : R.fail();
    }

    /**
     * 查询用户关联的角色列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Operation(summary = "查询用户关联的角色列表")
    @GetMapping("/{userId}/roles")
    public R<List<Long>> getUserRoles(@PathVariable Long userId) {
        return R.ok(userService.selectRoleIdsByUserId(userId));
    }

    /**
     * 绑定用户角色（追加角色）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    @Operation(summary = "绑定用户角色（追加角色）")
    @PostMapping("/{userId}/roles")
    public R<Void> bindUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        return userService.bindUserRoles(userId, roleIds) ? R.ok() : R.fail();
    }

    /**
     * 解绑用户角色（移除角色）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    @Operation(summary = "解绑用户角色（移除角色）")
    @DeleteMapping("/{userId}/roles")
    public R<Void> unbindUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        return userService.unbindUserRoles(userId, roleIds) ? R.ok() : R.fail();
    }

    /**
     * 更新用户角色（全量替换角色）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    @Operation(summary = "更新用户角色（全量替换角色）")
    @PutMapping("/{userId}/roles")
    public R<Void> updateUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        return userService.updateUserRoles(userId, roleIds) ? R.ok() : R.fail();
    }

}
