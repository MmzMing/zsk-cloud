package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.SysToken;
import com.zsk.system.service.ISysTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统令牌 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "系统令牌")
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class SysTokenController {

    private final ISysTokenService tokenService;

    /**
     * 查询令牌列表
     *
     * @return 令牌列表
     */
    @Operation(summary = "查询令牌列表")
    @GetMapping("/list")
    public R<List<SysToken>> list() {
        return R.ok(tokenService.list());
    }

    /**
     * 获取令牌详细信息
     *
     * @param id 令牌ID
     * @return 令牌详情
     */
    @Operation(summary = "获取令牌详细信息")
    @GetMapping("/{id}")
    public R<SysToken> getInfo(@PathVariable Long id) {
        return R.ok(tokenService.getById(id));
    }

    /**
     * 新增令牌
     *
     * @param token 令牌信息
     * @return 生成的令牌值
     */
    @Operation(summary = "新增令牌")
    @PostMapping
    public R<String> add(@RequestBody SysToken token) {
        String tokenValue = tokenService.createToken(token);
        return R.ok(tokenValue);
    }

    /**
     * 修改令牌
     *
     * @param token 令牌信息
     * @return 是否成功
     */
    @Operation(summary = "修改令牌")
    @PutMapping
    public R<Void> edit(@RequestBody SysToken token) {
        return tokenService.updateById(token) ? R.ok() : R.fail();
    }

    /**
     * 吊销令牌
     *
     * @param id 令牌ID
     * @return 是否成功
     */
    @Operation(summary = "吊销令牌")
    @PutMapping("/revoke/{id}")
    public R<Void> revoke(@PathVariable Long id) {
        return tokenService.revokeToken(id) ? R.ok() : R.fail();
    }

    /**
     * 批量吊销令牌
     *
     * @param ids 令牌ID列表
     * @return 是否成功
     */
    @Operation(summary = "批量吊销令牌")
    @PutMapping("/revokeBatch")
    public R<Void> revokeBatch(@RequestBody List<Long> ids) {
        return tokenService.batchRevokeToken(ids) ? R.ok() : R.fail();
    }

    /**
     * 删除令牌
     *
     * @param ids 令牌ID列表
     * @return 是否成功
     */
    @Operation(summary = "删除令牌")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return tokenService.removeByIds(ids) ? R.ok() : R.fail();
    }
}
