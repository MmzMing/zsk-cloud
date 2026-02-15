package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocVideoDetail;
import com.zsk.document.mapper.DocVideoDetailMapper;
import com.zsk.document.service.IDocVideoDetailService;
import org.springframework.stereotype.Service;

/**
 * 视频详情Service业务层处理
 *
 * @author wuhuaming
 * @date 2026-02-14
 */
@Service
public class DocVideoDetailServiceImpl extends ServiceImpl<DocVideoDetailMapper, DocVideoDetail> implements IDocVideoDetailService {
}
