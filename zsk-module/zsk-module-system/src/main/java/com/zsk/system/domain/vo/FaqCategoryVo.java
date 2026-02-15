package com.zsk.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * FAQ分类 视图对象
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FAQ分类")
public class FaqCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分类名称 */
    @Schema(description = "分类名称")
    private String title;

    /** 该分类下的FAQ列表 */
    @Schema(description = "该分类下的FAQ列表")
    private List<FaqItemVo> items;
}
