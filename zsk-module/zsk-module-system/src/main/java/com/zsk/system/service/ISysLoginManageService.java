package com.zsk.system.service;

import com.zsk.system.domain.dto.SysForceLogoutDTO;
import com.zsk.system.domain.vo.SysOnlineUserVO;

import java.util.List;

/**
 * 登录管理 服务层
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface ISysLoginManageService {

    /**
     * 查询在线用户列表
     *
     * @param userName 用户名（可选，用于模糊查询）
     * @return 在线用户列表
     */
    List<SysOnlineUserVO> listOnlineUsers(String userName);

    /**
     * 强制下线用户
     *
     * @param dto 强制下线请求
     * @return 是否成功
     */
    boolean forceLogout(SysForceLogoutDTO dto);

    /**
     * 刷新会话过期时间
     *
     * @param sessionId 会话编号
     * @return 是否成功
     */
    boolean refreshSession(String sessionId);
}
