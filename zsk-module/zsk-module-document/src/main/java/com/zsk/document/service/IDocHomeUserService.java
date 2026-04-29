package com.zsk.document.service;

import com.zsk.common.datasource.domain.PageQuery;
import com.zsk.common.datasource.domain.PageResult;
import com.zsk.document.domain.vo.DocHomeUserStatsVo;
import com.zsk.document.domain.vo.DocHomeUserWorksVo;

public interface IDocHomeUserService {

    PageResult<DocHomeUserWorksVo> getUserWorks(Long userId, String type, PageQuery pageQuery);

    DocHomeUserStatsVo getUserStats(Long userId);
}
