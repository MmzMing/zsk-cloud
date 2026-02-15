package com.zsk.common.oss.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel工具类
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
public class ExcelUtil {

    /**
     * 导出Excel
     *
     * @param response  响应对象
     * @param data      数据列表
     * @param clazz     类对象
     * @param sheetName Sheet名称
     * @param fileName  文件名
     */
    @SneakyThrows
    public static <T> void export(HttpServletResponse response, List<T> data, Class<T> clazz, String sheetName, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和前端交互时前端需要URLDecoder.decode
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(data);
    }
}
