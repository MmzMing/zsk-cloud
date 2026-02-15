package com.zsk.system.service.impl;

import com.zsk.system.config.AboutProperties;
import com.zsk.system.domain.vo.FaqCategoryVo;
import com.zsk.system.domain.vo.FaqItemVo;
import com.zsk.system.domain.vo.TechStackVo;
import com.zsk.system.service.IAboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * About页面 服务实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements IAboutService {

    private final AboutProperties aboutProperties;

    @Override
    public List<TechStackVo> getTechStack() {
        List<TechStackVo> result = new ArrayList<>();
        List<AboutProperties.TechStackItem> items = aboutProperties.getTechStack();

        if (items != null) {
            for (AboutProperties.TechStackItem item : items) {
                result.add(new TechStackVo(item.getId(), item.getName(), item.getDescription()));
            }
        }

        return result;
    }

    @Override
    public List<FaqCategoryVo> getFaq() {
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

        return result;
    }
}
