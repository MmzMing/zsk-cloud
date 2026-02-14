package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocProcess;
import com.zsk.document.mapper.DocProcessMapper;
import com.zsk.document.service.IDocProcessService;
import org.springframework.stereotype.Service;

/**
 * 文档处理Service业务层处理
 * 
 * @author wuhuaming
 * @date 2026-02-14
 * @version 1.0
 */
@Service
public class DocProcessServiceImpl extends ServiceImpl<DocProcessMapper, DocProcess> implements IDocProcessService {
}
