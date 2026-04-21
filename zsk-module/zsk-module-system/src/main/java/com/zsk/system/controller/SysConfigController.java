package com.zsk.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsk.common.core.domain.R;
import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.system.domain.SysConfig;
import com.zsk.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数管理 控制器
 *
 * @author wuhuaming
 */
@Tag(name = "参数管理")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final ISysConfigService configService;

    /**
     * 查询参数列表
     */
    @Operation(summary = "查询参数列表")
    @GetMapping("/list")
    public R<List<SysConfig>> list(SysConfig config) {
        return R.ok(configService.list());
    }

    /**
     * 分页查询参数列表
     *
     * @param pageQuery 分页参数
     * @param config    查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询参数列表")
    @GetMapping("/page")
    public R<PageResult<SysConfig>> page(PageQuery pageQuery, SysConfig config) {
        LambdaQueryWrapper<SysConfig> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.hasText(config.getConfigName()), SysConfig::getConfigName, config.getConfigName());
        lqw.eq(StringUtils.hasText(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey());
        lqw.eq(config.getConfigType() != null, SysConfig::getConfigType, config.getConfigType());

        Page<SysConfig> page = configService.page(pageQuery.build(), lqw);
        return R.ok(PageResult.build(page));
    }

    /**
     * 获取参数详细信息
     */
    @Operation(summary = "获取参数详细信息")
    @GetMapping("/{id}")
    public R<SysConfig> getInfo(@PathVariable Long id) {
        return R.ok(configService.getById(id));
    }

    /**
     * 新增参数
     */
    @Operation(summary = "新增参数")
    @PostMapping
    public R<Void> add(@RequestBody SysConfig config) {
        return configService.save(config) ? R.ok() : R.fail();
    }

    /**
     * 修改参数
     */
    @Operation(summary = "修改参数")
    @PutMapping
    public R<Void> edit(@RequestBody SysConfig config) {
        return configService.updateById(config) ? R.ok() : R.fail();
    }

    /**
     * 删除参数
     */
    @Operation(summary = "删除参数")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return configService.removeByIds(ids) ? R.ok() : R.fail();
    }
}
