package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.config.ToolboxProperties;
import com.zsk.system.domain.vo.ToolboxDetailVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具箱 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "工具箱")
@RestController
@RequestMapping("/toolbox")
@RequiredArgsConstructor
public class ToolboxController {

    private final ToolboxProperties toolboxProperties;

    /**
     * 获取工具详情
     *
     * @param id 工具ID
     * @return 工具详情
     */
    @Operation(summary = "获取工具详情")
    @GetMapping("/{id}")
    public R<ToolboxDetailVo> getDetail(@PathVariable("id") String id) {
        List<ToolboxProperties.ToolItem> tools = toolboxProperties.getTools();

        if (tools != null) {
            for (ToolboxProperties.ToolItem tool : tools) {
                if (id.equals(tool.getId())) {
                    ToolboxDetailVo vo = buildToolboxDetailVo(tool);
                    return R.ok(vo);
                }
            }
        }

        return R.fail("工具不存在");
    }

    /**
     * 获取工具列表
     *
     * @return 工具列表
     */
    @Operation(summary = "获取工具列表")
    @GetMapping("/list")
    public R<List<ToolboxDetailVo>> getList() {
        List<ToolboxDetailVo> result = new ArrayList<>();
        List<ToolboxProperties.ToolItem> tools = toolboxProperties.getTools();

        if (tools != null) {
            for (ToolboxProperties.ToolItem tool : tools) {
                result.add(buildToolboxDetailVo(tool));
            }
        }

        return R.ok(result);
    }

    /**
     * 构建工具详情VO
     */
    private ToolboxDetailVo buildToolboxDetailVo(ToolboxProperties.ToolItem tool) {
        ToolboxDetailVo vo = new ToolboxDetailVo();
        vo.setId(tool.getId());
        vo.setTitle(tool.getTitle());
        vo.setDescription(tool.getDescription());
        vo.setLogo(tool.getLogo());
        vo.setTags(tool.getTags() != null ? tool.getTags() : new ArrayList<>());
        vo.setUrl(tool.getUrl());
        vo.setImages(tool.getImages() != null ? tool.getImages() : new ArrayList<>());
        vo.setFeatures(tool.getFeatures() != null ? tool.getFeatures() : new ArrayList<>());
        vo.setRelatedTools(new ArrayList<>());
        vo.setCreateAt(tool.getCreateAt());

        /** 统计数据 */
        if (tool.getStats() != null) {
            ToolboxDetailVo.StatsInfoVo stats = new ToolboxDetailVo.StatsInfoVo();
            stats.setViews(tool.getStats().getViews());
            stats.setLikes(tool.getStats().getLikes());
            stats.setUsage(tool.getStats().getUsage());
            vo.setStats(stats);
        } else {
            ToolboxDetailVo.StatsInfoVo stats = new ToolboxDetailVo.StatsInfoVo();
            stats.setViews(0L);
            stats.setLikes(0L);
            stats.setUsage(0L);
            vo.setStats(stats);
        }

        /** 作者信息 */
        if (tool.getAuthor() != null) {
            ToolboxDetailVo.AuthorInfoVo author = new ToolboxDetailVo.AuthorInfoVo();
            author.setName(tool.getAuthor().getName());
            author.setAvatar(tool.getAuthor().getAvatar());
            vo.setAuthor(author);
        }

        return vo;
    }
}
