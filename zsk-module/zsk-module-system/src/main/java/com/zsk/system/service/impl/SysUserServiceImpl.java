package com.zsk.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.system.domain.SysUser;
import com.zsk.system.mapper.SysUserMapper;
import com.zsk.system.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * 用户管理 服务层实现
 * 
 * @author zsk
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
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
}
