package com.zsk.system.service.impl;

import com.zsk.system.config.AboutProperties;
import com.zsk.system.domain.vo.FaqCategoryVo;
import com.zsk.system.domain.vo.FaqItemVo;
import com.zsk.system.domain.vo.TechStackVo;
import com.zsk.system.service.IAboutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * About页面 服务实现
 * <p>
 * 提供关于页面的技术栈和FAQ信息查询服务
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements IAboutService {

    /** About页面配置属性 */
    private final AboutProperties aboutProperties;

    /**
     * 获取技术栈列表
     * <p>
     * 从配置中读取技术栈信息并转换为视图对象
     *
     * @return 技术栈列表
     */
    @Override
    public List<TechStackVo> getTechStack() {
        log.info("获取技术栈信息");
        List<TechStackVo> result = new ArrayList<>();
        List<AboutProperties.TechStackItem> items = aboutProperties.getTechStack();

        if (items != null) {
            for (AboutProperties.TechStackItem item : items) {
                result.add(new TechStackVo(item.getId(), item.getName(), item.getDescription()));
            }
        }

        log.info("获取技术栈信息完成, 数量={}", result.size());
        return result;
    }

    /**
     * 获取FAQ列表
     * <p>
     * 从配置中读取FAQ分类及问答信息并转换为视图对象
     *
     * @return FAQ分类列表
     */
    @Override
    public List<FaqCategoryVo> getFaq() {
        log.info("获取FAQ信息");
        List<FaqCategoryVo> result = new ArrayList<>();
        List<AboutProperties.FaqCategory> categories = aboutProperties.getFaq();

        if (categories != null) {
            for (AboutProperties.FaqCategory category : categories) {
                List<FaqItemVo> items = new ArrayList<>();
                if (category.getItems() != null) {
                    for (AboutProperties.FaqItem item : category.getItems()) {
                        items.add(new FaqItemVo(item.getId(), item.getQuestion(), item.getAnswer()));
                    }
                }
                result.add(new FaqCategoryVo(category.getTitle(), items));
            }
        }

        log.info("获取FAQ信息完成, 分类数量={}", result.size());
        return result;
    }
}
