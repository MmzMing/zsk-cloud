package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.dto.SysForceLogoutDTO;
import com.zsk.system.domain.dto.SysOnlineUserQuery;
import com.zsk.system.domain.vo.SysOnlineUserVO;
import com.zsk.system.service.ISysLoginManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 登录管理 控制器 (v2)
 * <p>
 * 在线用户：用户维度展示（一用户一行），按 Redis Token Set 判断在线状态。
 *
 * @author wuhuaming
 * @date 2026-04-22
 * @version 2.0
 */
@Tag(name = "登录管理")
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class SysLoginManageController {

    private final ISysLoginManageService loginManageService;

    /**
     * 分页查询在线用户列表
     */
    @Operation(summary = "分页查询在线用户列表")
    @GetMapping("/online/page")
    public R<PageResult<SysOnlineUserVO>> listOnlineUsers(@Valid SysOnlineUserQuery query) {
        return R.ok(loginManageService.listOnlineUsers(query));
    }

    /**
     * 强制下线（按用户ID批量）
     */
    @Operation(summary = "强制下线（按用户ID批量）")
    @PostMapping("/online/forceLogout")
    public R<Void> forceLogout(@Valid @RequestBody SysForceLogoutDTO dto) {
        return loginManageService.forceLogout(dto) ? R.ok() : R.fail("强制下线失败");
    }
}
