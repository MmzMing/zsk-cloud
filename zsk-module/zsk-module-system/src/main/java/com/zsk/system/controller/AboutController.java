package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.domain.vo.FaqCategoryVo;
import com.zsk.system.domain.vo.TechStackVo;
import com.zsk.system.service.IAboutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * About页面 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "About页面")
@RestController
@RequestMapping("/about")
@RequiredArgsConstructor
public class AboutController {

    private final IAboutService aboutService;

    /**
     * 获取技术栈列表
     *
     * @return 技术栈列表
     */
    @Operation(summary = "获取技术栈列表")
    @GetMapping("/skill")
    public R<List<TechStackVo>> getTechStack() {
        return R.ok(aboutService.getTechStack());
    }

    /**
     * 获取FAQ列表
     *
     * @return FAQ分类列表
     */
    @Operation(summary = "获取FAQ列表")
    @GetMapping("/faq")
    public R<List<FaqCategoryVo>> getFaq() {
        return R.ok(aboutService.getFaq());
    }
}
