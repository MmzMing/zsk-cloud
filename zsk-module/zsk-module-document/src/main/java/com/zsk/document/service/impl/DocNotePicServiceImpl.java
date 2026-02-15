package com.zsk.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsk.document.domain.DocNotePic;
import com.zsk.document.mapper.DocNotePicMapper;
import com.zsk.document.service.IDocNotePicService;
import org.springframework.stereotype.Service;

/**
 * 笔记图片Service业务层处理
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Service
public class DocNotePicServiceImpl extends ServiceImpl<DocNotePicMapper, DocNotePic> implements IDocNotePicService {
}
