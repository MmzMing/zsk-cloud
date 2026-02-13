package com.zsk.common.datasource.domain;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 *
 * @author zsk
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 500, message = "每页大小不能超过500")
    private Long pageSize = 10L;

    /**
     * 排序字段
     */
    private String orderByColumn;

    /**
     * 排序方向（asc/desc）
     */
    private String isAsc = "asc";

    /**
     * 构建 MyBatis Plus 分页对象
     */
    public <T> Page<T> build() {
        Page<T> page = new Page<>(pageNum, pageSize);
        if (orderByColumn != null && !orderByColumn.isEmpty()) {
            OrderItem item = "asc".equalsIgnoreCase(isAsc) ? OrderItem.asc(orderByColumn) : OrderItem.desc(orderByColumn);
            page.addOrder(item);
        }
        return page;
    }

    /**
     * 计算偏移量
     */
    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取排序SQL
     */
    public String getOrderBy() {
        if (orderByColumn == null || orderByColumn.isEmpty()) {
            return "";
        }
        return orderByColumn + " " + isAsc;
    }
}
