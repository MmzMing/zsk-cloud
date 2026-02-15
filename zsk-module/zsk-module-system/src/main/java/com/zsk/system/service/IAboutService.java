package com.zsk.system.service;

import com.zsk.system.domain.vo.FaqCategoryVo;
import com.zsk.system.domain.vo.TechStackVo;

import java.util.List;

/**
 * About页面 服务接口
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
public interface IAboutService {

    /**
     * 获取技术栈列表
     *
     * @return 技术栈列表
     */
    List<TechStackVo> getTechStack();

    /**
     * 获取FAQ列表
     *
     * @return FAQ分类列表
     */
    List<FaqCategoryVo> getFaq();
}
