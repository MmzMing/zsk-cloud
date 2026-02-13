package com.zsk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsk.system.domain.SysUser;

/**
 * 用户管理 服务层
 * 
 * @author zsk
 */
public interface ISysUserService extends IService<SysUser> {
    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectUserByUserName(String username);

    /**
     * 通过邮箱查询用户信息
     *
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser selectUserByEmail(String email);

    /**
     * 通过第三方ID查询用户信息
     *
     * @param loginType 第三方登录类型
     * @param thirdPartyId 第三方ID
     * @return 用户信息
     */
    SysUser selectUserByThirdPartyId(String loginType, String thirdPartyId);
}
