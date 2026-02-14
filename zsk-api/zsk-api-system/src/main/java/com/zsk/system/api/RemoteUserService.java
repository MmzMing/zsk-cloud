package com.zsk.system.api;

import com.zsk.common.core.constant.CommonConstants;
import com.zsk.common.core.constant.ServiceNameConstants;
import com.zsk.common.core.domain.R;
import com.zsk.system.api.domain.SysUserApi;
import com.zsk.system.api.factory.RemoteUserFallbackFactory;
import com.zsk.system.api.model.LoginUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务
 *
 * @author wuhuaming
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteUserFallbackFactory.class, url = "http://127.0.0.1:20010")
public interface RemoteUserService {
    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @return 结果
     */
    @GetMapping("/system/user/info/{username}")
    public R<SysUserApi> getUserInfo(@PathVariable String username);

    /**
     * 通过用户名查询登录用户信息
     *
     * @param username 用户名
     * @param source   请求来源
     * @return 结果
     */
    @GetMapping("/system/user/info/{username}")
    public R<LoginUser> getUserInfo(@PathVariable String username, @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 通过邮箱查询登录用户信息
     *
     * @param email  邮箱
     * @param source 请求来源
     * @return 结果
     */
    @GetMapping("/system/user/info/email/{email}")
    public R<LoginUser> getUserInfoByEmail(@PathVariable("email") String email, @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 通过第三方ID查询登录用户信息
     *
     * @param loginType    第三方登录类型
     * @param thirdPartyId 第三方ID
     * @param source       请求来源
     * @return 结果
     */
    @GetMapping("/system/user/info/thirdparty/{loginType}/{thirdPartyId}")
    public R<LoginUser> getUserByThirdPartyId(@PathVariable("loginType") String loginType, @PathVariable("thirdPartyId") String thirdPartyId, @RequestHeader(CommonConstants.REQUEST_SOURCE_HEADER) String source);

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 结果
     */
    @PostMapping("/system/user")
    public R<Boolean> createUser(@RequestBody SysUserApi user);
}
