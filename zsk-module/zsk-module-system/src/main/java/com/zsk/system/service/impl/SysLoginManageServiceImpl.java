package com.zsk.system.service.impl;

import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.redis.service.RedisService;
import com.zsk.system.domain.SysUser;
import com.zsk.system.domain.dto.SysForceLogoutDTO;
import com.zsk.system.domain.vo.SysOnlineUserVO;
import com.zsk.system.service.ISysLoginManageService;
import com.zsk.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录管理 服务层实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginManageServiceImpl implements ISysLoginManageService {

    private final RedisService redisService;
    private final ISysUserService userService;

    /**
     * 查询在线用户列表
     *
     * @param userName 用户名（可选，用于模糊查询）
     * @return 在线用户列表
     */
    @Override
    public List<SysOnlineUserVO> listOnlineUsers(String userName) {
        // 获取 Redis 中所有登录 token 的 Key
        Collection<String> keys = redisService.keys(CacheConstants.CACHE_LOGIN_TOKEN + "*");
        List<SysOnlineUserVO> onlineUsers = new ArrayList<>();

        // 如果没有在线用户，直接返回空列表
        if (keys == null || keys.isEmpty()) {
            return onlineUsers;
        }

        // 获取所有 Token 对应的用户 ID 映射
        Map<String, Long> tokenUserMap = getTokenUserMap(keys);
        if (tokenUserMap.isEmpty()) {
            return onlineUsers;
        }

        // 批量查询用户信息，提升性能
        List<Long> userIds = new ArrayList<>(tokenUserMap.values());
        List<SysUser> users = userService.listByIds(userIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));

        // 遍历所有在线 Token，构建在线用户列表
        for (Map.Entry<String, Long> entry : tokenUserMap.entrySet()) {
            String sessionId = extractSessionId(entry.getKey());
            Long userId = entry.getValue();
            SysUser user = userMap.get(userId);

            // 如果用户信息不存在，跳过该 Token
            if (user == null) {
                continue;
            }

            // 如果指定了用户名，进行模糊匹配过滤
            if (StringUtils.hasText(userName) && !user.getUserName().contains(userName)) {
                continue;
            }

            // 构建在线用户视图对象并添加到列表
            SysOnlineUserVO vo = buildOnlineUserVO(sessionId, user);
            onlineUsers.add(vo);
        }

        return onlineUsers;
    }

    /**
     * 强制下线用户
     *
     * @param dto 强制下线请求
     * @return 是否成功
     */
    @Override
    public boolean forceLogout(SysForceLogoutDTO dto) {
        List<String> sessionIds = dto.getSessionIds();
        if (sessionIds == null || sessionIds.isEmpty()) {
            return false;
        }

        // 遍历会话列表，逐个删除 Redis 中的 Token
        for (String sessionId : sessionIds) {
            String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + sessionId;
            boolean deleted = redisService.deleteObject(tokenKey);
            if (!deleted) {
                log.warn("删除会话失败，sessionId: {}", sessionId);
            }
        }

        return true;
    }

    /**
     * 刷新会话过期时间
     *
     * @param sessionId 会话编号
     * @return 是否成功
     */
    @Override
    public boolean refreshSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return false;
        }

        // 检查 Token 是否存在
        String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + sessionId;
        Object userId = redisService.getCacheObject(tokenKey);
        if (userId == null) {
            log.warn("会话不存在，sessionId: {}", sessionId);
            return false;
        }

        // 刷新 Token 过期时间
        return redisService.expire(tokenKey, SecurityConstants.TOKEN_EXPIRE);
    }

    /**
     * 获取Token与用户ID的映射关系
     *
     * @param keys Redis键集合
     * @return Token与用户ID的映射
     */
    private Map<String, Long> getTokenUserMap(Collection<String> keys) {
        return keys.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> {
                            Object userId = redisService.getCacheObject(key);
                            return userId != null ? Long.valueOf(userId.toString()) : null;
                        }
                ));
    }

    /**
     * 从Redis键中提取会话ID
     *
     * @param key Redis键
     * @return 会话ID
     */
    private String extractSessionId(String key) {
        return key.replace(CacheConstants.CACHE_LOGIN_TOKEN, "");
    }

    /**
     * 构建在线用户视图对象
     *
     * @param sessionId 会话ID
     * @param user 用户信息
     * @return 在线用户视图对象
     */
    private SysOnlineUserVO buildOnlineUserVO(String sessionId, SysUser user) {
        SysOnlineUserVO vo = new SysOnlineUserVO();
        vo.setSessionId(sessionId);
        vo.setUserId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        vo.setIpaddr(user.getLoginIp());
        vo.setLoginTime(user.getLoginDate());
        vo.setExpireTime(LocalDateTime.now().plusMinutes(SecurityConstants.TOKEN_EXPIRE));
        return vo;
    }
}
