package com.zsk.document.controller;

import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.vo.DocHomeUserStatsVo;
import com.zsk.document.domain.vo.DocHomeUserWorksVo;
import com.zsk.document.service.IDocHomeUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "前台用户作品主页")
@RestController
@RequestMapping("/docHomeUser")
@RequiredArgsConstructor
public class DocHomeUserController {

    private final IDocHomeUserService docHomeUserService;

    @Operation(summary = "获取用户作品列表")
    @GetMapping("/{userId}/works")
    public R<PageResult<DocHomeUserWorksVo>> getUserWorks(
            @PathVariable("userId") Long userId,
            PageQuery pageQuery,
            @RequestParam(value = "type", required = false) String type) {
        log.info("获取用户作品列表请求, userId={}, type={}", userId, type);

        PageResult<DocHomeUserWorksVo> result = docHomeUserService.getUserWorks(userId, type, pageQuery);

        log.info("获取用户作品列表成功, userId={}, total={}", userId, result.getTotal());
        return R.ok(result);
    }

    @Operation(summary = "获取用户作品统计（点赞数、浏览数、收藏数）")
    @GetMapping("/{userId}/stats")
    public R<DocHomeUserStatsVo> getUserStats(@PathVariable("userId") Long userId) {
        log.info("获取用户作品统计请求, userId={}", userId);

        DocHomeUserStatsVo stats = docHomeUserService.getUserStats(userId);

        log.info("获取用户作品统计成功, userId={}", userId);
        return R.ok(stats);
    }
}
