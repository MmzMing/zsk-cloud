package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.dto.SysForceLogoutDTO;
import com.zsk.system.domain.vo.SysOnlineUserVO;
import com.zsk.system.service.ISysLoginManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 登录管理 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "登录管理")
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class SysLoginManageController {

    private final ISysLoginManageService loginManageService;

    /**
     * 查询在线用户列表
     *
     * @param userName 用户名（可选，用于模糊查询）
     * @return 在线用户列表
     */
    @Operation(summary = "查询在线用户列表")
    @GetMapping("/online/list")
    public R<List<SysOnlineUserVO>> listOnlineUsers(
            @Parameter(description = "用户名") @RequestParam(required = false) String userName) {
        return R.ok(loginManageService.listOnlineUsers(userName));
    }

    /**
     * 强制下线用户
     *
     * @param dto 强制下线请求
     * @return 是否成功
     */
    @Operation(summary = "强制下线用户")
    @PostMapping("/online/forceLogout")
    public R<Void> forceLogout(@Valid @RequestBody SysForceLogoutDTO dto) {
        return loginManageService.forceLogout(dto) ? R.ok() : R.fail("强制下线失败");
    }

    /**
     * 刷新会话过期时间
     *
     * @param sessionId 会话编号
     * @return 是否成功
     */
    @Operation(summary = "刷新会话过期时间")
    @PutMapping("/online/refresh/{sessionId}")
    public R<Void> refreshSession(
            @Parameter(description = "会话编号", required = true) @PathVariable String sessionId) {
        return loginManageService.refreshSession(sessionId) ? R.ok() : R.fail("刷新会话失败");
    }
}
