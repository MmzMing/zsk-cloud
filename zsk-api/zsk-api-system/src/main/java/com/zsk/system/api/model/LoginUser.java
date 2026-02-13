package com.zsk.system.api.model;

import com.zsk.system.api.domain.SysUserApi;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 用户登录信息
 *
 * @author zsk
 */
@Data
public class LoginUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户信息
     */
    private SysUserApi sysUser;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色列表
     */
    private Set<String> roles;
}
