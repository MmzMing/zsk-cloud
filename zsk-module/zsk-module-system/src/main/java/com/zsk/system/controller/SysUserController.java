package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.api.domain.SysUserApi;
import com.zsk.system.domain.SysUser;
import com.zsk.system.service.ISysUserService;
import com.zsk.system.service.ISysRoleService;
import com.zsk.system.service.ISysMenuService;
import com.zsk.system.api.model.LoginUser;
import com.zsk.common.core.constant.CommonConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 用户管理 控制器
 * 
 * @author zsk
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

    private static @NonNull SysUserApi getSysUserApi(SysUser sysUser) {
        SysUserApi apiUser = new SysUserApi();
        // 简单拷贝常用属性
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
     */
    @Operation(summary = "查询用户列表")
    @GetMapping("/list")
    public R<List<SysUser>> list(SysUser user) {
        return R.ok(userService.list());
    }

    /**
     * 获取用户详细信息
     */
    @Operation(summary = "获取用户详细信息")
    @GetMapping("/{id}")
    public R<SysUser> getInfo(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户")
    @PostMapping
    public R<Boolean> add(@RequestBody SysUser user) {
        return R.ok(userService.save(user));
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改用户")
    @PutMapping
    public R<Void> edit(@RequestBody SysUser user) {
        return userService.updateById(user) ? R.ok() : R.fail();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return userService.removeByIds(ids) ? R.ok() : R.fail();
    }
}
