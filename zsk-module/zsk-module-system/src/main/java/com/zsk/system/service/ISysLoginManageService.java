package com.zsk.system.service;

import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.dto.SysForceLogoutDTO;
import com.zsk.system.domain.dto.SysOnlineUserQuery;
import com.zsk.system.domain.vo.SysOnlineUserVO;

/**
 * 登录管理 服务层
 * <p>
 * v2: 在线用户改为「用户维度」展示（一用户一行），通过 Redis Token Set
 * 判断在线状态；强制下线按 userId 批量执行。
 *
 * @author wuhuaming
 * @date 2026-04-22
 * @version 2.0
 */
public interface ISysLoginManageService {

    /**
     * 分页查询在线用户列表（用户维度）
     *
     * @param query 查询条件
     * @return 在线用户分页结果
     */
    PageResult<SysOnlineUserVO> listOnlineUsers(SysOnlineUserQuery query);

    /**
     * 强制下线（按用户ID批量）
     *
     * @param dto 强制下线请求
     * @return 是否成功
     */
    boolean forceLogout(SysForceLogoutDTO dto);
}
