package com.zsk.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.constant.SecurityConstants;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.common.redis.service.RedisService;
import com.zsk.system.domain.SysUser;
import com.zsk.system.domain.dto.SysForceLogoutDTO;
import com.zsk.system.domain.dto.SysOnlineUserQuery;
import com.zsk.system.domain.vo.SysOnlineUserVO;
import com.zsk.system.service.ISysLoginManageService;
import com.zsk.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 登录管理 服务层实现 (v2)
 * <p>
 * 核心逻辑：
 * <ol>
 *   <li>扫描 Redis Key {@code zsk:login:token:*} 提取所有 userId（在线用户集合）</li>
 *   <li>按 userId 维度合并多设备会话（一用户一行）</li>
 *   <li>从 sys_user 批量补充用户基础信息（账号/昵称/头像/IP/登录时间）</li>
 *   <li>设备数 = Token Set 大小；过期时间 = key TTL；在线时长 = now - loginDate</li>
 *   <li>支持按用户名/昵称/IP 模糊过滤 + 内存分页</li>
 * </ol>
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginManageServiceImpl implements ISysLoginManageService {

    private final RedisService redisService;
    private final ISysUserService userService;

    @Override
    public PageResult<SysOnlineUserVO> listOnlineUsers(SysOnlineUserQuery query) {
        // 1. 从 Redis 获取所有在线用户 token key
        Collection<String> keys = redisService.keys(CacheConstants.CACHE_LOGIN_TOKEN + "*");
        if (CollUtil.isEmpty(keys)) {
            return PageResult.empty();
        }

        // 2. 提取 userId（去重）
        List<Long> userIds = keys.stream()
                .map(this::extractUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return PageResult.empty();
        }

        // 3. 批量查询用户基础信息
        List<SysUser> users = userService.listByIds(userIds);
        if (CollUtil.isEmpty(users)) {
            return PageResult.empty();
        }
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 4. 组装 VO（每个 userId 一行）
        List<SysOnlineUserVO> all = new ArrayList<>(userIds.size());
        for (Long userId : userIds) {
            SysUser user = userMap.get(userId);
            if (user == null) {
                continue;
            }
            // 4.1 模糊过滤
            if (!matchFilter(user, query)) {
                continue;
            }
            all.add(buildVO(userId, user));
        }

        // 5. 排序：按最近登录时间倒序
        all.sort(Comparator.comparing(
                SysOnlineUserVO::getLoginTime,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        // 6. 内存分页（在线用户量级一般 < 万级，可接受）
        long total = all.size();
        long pageNum = query.getPageNum();
        long pageSize = query.getPageSize();
        int from = (int) Math.min((pageNum - 1) * pageSize, total);
        int to = (int) Math.min(from + pageSize, total);
        List<SysOnlineUserVO> records = all.subList(from, to);

        return PageResult.of(records, total, pageNum, pageSize);
    }

    @Override
    public boolean forceLogout(SysForceLogoutDTO dto) {
        if (dto == null || CollUtil.isEmpty(dto.getUserIds())) {
            return false;
        }
        for (Long userId : dto.getUserIds()) {
            if (userId == null) {
                continue;
            }
            String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + userId;
            String rolesKey = CacheConstants.CACHE_LOGIN_ROLES + userId;
            String permsKey = CacheConstants.CACHE_LOGIN_PERMISSIONS + userId;
            redisService.deleteObject(tokenKey);
            redisService.deleteObject(rolesKey);
            redisService.deleteObject(permsKey);
            log.info("强制下线用户 userId={}, 已清理 token/roles/permissions 缓存", userId);
        }
        return true;
    }

    // ====================== 私有方法 ======================

    /**
     * 从 Redis Key 提取 userId
     */
    private Long extractUserId(String key) {
        String userIdStr = key.replace(CacheConstants.CACHE_LOGIN_TOKEN, "");
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            log.warn("无效的用户ID格式: {}", userIdStr);
            return null;
        }
    }

    /**
     * 模糊匹配过滤
     */
    private boolean matchFilter(SysUser user, SysOnlineUserQuery query) {
        if (StrUtil.isNotBlank(query.getUserName())
                && (user.getUserName() == null || !user.getUserName().contains(query.getUserName()))) {
            return false;
        }
        if (StrUtil.isNotBlank(query.getNickName())
                && (user.getNickName() == null || !user.getNickName().contains(query.getNickName()))) {
            return false;
        }
        if (StrUtil.isNotBlank(query.getIpaddr())
                && (user.getLoginIp() == null || !user.getLoginIp().contains(query.getIpaddr()))) {
            return false;
        }
        return true;
    }

    /**
     * 构建在线用户 VO
     */
    private SysOnlineUserVO buildVO(Long userId, SysUser user) {
        SysOnlineUserVO vo = new SysOnlineUserVO();
        vo.setUserId(userId);
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setIpaddr(user.getLoginIp());
        vo.setLoginTime(user.getLoginDate());

        // 设备数 = Token Set 大小
        String tokenKey = CacheConstants.CACHE_LOGIN_TOKEN + userId;
        Long deviceCount = redisService.getSetSize(tokenKey);
        vo.setDeviceCount(deviceCount == null ? 0 : deviceCount.intValue());

        // 过期时间：Redis TTL（秒），fallback 至 TOKEN_EXPIRE
        long ttl = redisService.getExpire(tokenKey);
        if (ttl > 0) {
            vo.setExpireTime(LocalDateTime.now().plusSeconds(ttl));
        } else {
            vo.setExpireTime(LocalDateTime.now().plusMinutes(SecurityConstants.TOKEN_EXPIRE));
        }

        // 在线时长（秒）：now - loginDate
        if (user.getLoginDate() != null) {
            long seconds = Duration.between(user.getLoginDate(), LocalDateTime.now()).getSeconds();
            vo.setOnlineDuration(Math.max(seconds, 0L));
        } else {
            vo.setOnlineDuration(0L);
        }
        return vo;
    }
}
