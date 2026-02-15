package com.zsk.system.controller;

import com.zsk.common.core.domain.R;
import com.zsk.system.config.ResumeProperties;
import com.zsk.system.domain.vo.BasicInfoVo;
import com.zsk.system.domain.vo.ResumeModuleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 简历 控制器
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Tag(name = "简历")
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeProperties resumeProperties;

    /**
     * 获取简历详情
     *
     * @return 简历模块列表
     */
    @Operation(summary = "获取简历详情")
    @GetMapping("/detail")
    public R<List<ResumeModuleVo>> getDetail() {
        List<ResumeModuleVo> result = new ArrayList<>();
        List<ResumeProperties.ResumeModule> modules = resumeProperties.getModules();

        if (modules != null) {
            for (ResumeProperties.ResumeModule module : modules) {
                ResumeModuleVo vo = new ResumeModuleVo();
                vo.setId(module.getId());
                vo.setType(module.getType());
                vo.setTitle(module.getTitle());
                vo.setIcon(module.getIcon());
                vo.setIsDeletable(module.getIsDeletable());
                vo.setIsVisible(module.getIsVisible());
                vo.setContent(module.getContent());

                if (module.getData() != null) {
                    ResumeProperties.BasicInfo info = module.getData();
                    BasicInfoVo basicInfo = new BasicInfoVo(
                        info.getName(),
                        info.getJobIntention(),
                        info.getAge(),
                        info.getGender(),
                        info.getCity(),
                        info.getPhone(),
                        info.getEmail(),
                        info.getGithub(),
                        info.getSummary(),
                        info.getAvatar(),
                        info.getExperience(),
                        info.getSalary(),
                        info.getPolitics(),
                        info.getStatus()
                    );
                    vo.setData(basicInfo);
                }

                result.add(vo);
            }
        }

        return R.ok(result);
    }

    /**
     * 保存简历
     *
     * @param modules 简历模块列表
     * @return 操作结果
     */
    @Operation(summary = "保存简历")
    @PostMapping("/save")
    public R<Void> save(@RequestBody List<ResumeModuleVo> modules) {
        /** 暂时只返回成功，后续可实现持久化 */
        return R.ok();
    }
}
