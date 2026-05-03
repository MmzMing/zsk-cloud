package com.zsk.common.core.utils;

import cn.hutool.http.HtmlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * XSS过滤工具类
 *
 * @author wuhuaming
 */
public class XssUtil {
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SRC_PATTERN = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern SRC_PATTERN2 = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern CLOSING_SCRIPT_PATTERN = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPENING_SCRIPT_PATTERN = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EVAL_PATTERN = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 清洗字符串，防止XSS
     *
     * @param value 待清洗的字符串
     * @return 清洗后的字符串
     */
    public static String clean(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }

        // 1. 使用 Hutool 进行 HTML 转义，防止 HTML 注入
        // 注意：如果业务允许 HTML 标签，可以使用 HtmlUtil.filter(value)
        // 这里采用比较严格的过滤策略，结合正则清理常见攻击向量

        // 先进行正则替换，移除危险标签
        value = SCRIPT_PATTERN.matcher(value).replaceAll("");
        value = SRC_PATTERN.matcher(value).replaceAll("");
        value = SRC_PATTERN2.matcher(value).replaceAll("");
        value = CLOSING_SCRIPT_PATTERN.matcher(value).replaceAll("");
        value = OPENING_SCRIPT_PATTERN.matcher(value).replaceAll("");
        value = EVAL_PATTERN.matcher(value).replaceAll("");
        value = EXPRESSION_PATTERN.matcher(value).replaceAll("");
        value = JAVASCRIPT_PATTERN.matcher(value).replaceAll("");
        value = VBSCRIPT_PATTERN.matcher(value).replaceAll("");
        value = ONLOAD_PATTERN.matcher(value).replaceAll("");

        // 2. 再次使用 Hutool 进行过滤，移除所有 HTML 标签，仅保留文本
        // 如果允许部分 HTML，可以使用 cleanHtmlTag 并指定保留标签
        value = HtmlUtil.cleanHtmlTag(value);

        // 3. 转义特殊字符（可选，视情况而定，如果前端会再次转义，这里可能造成双重转义）
        // value = HtmlUtil.escape(value);

        return value;
    }

    /**
     * 清洗 JSON 字符串中的 XSS 攻击向量
     *
     * @param jsonBody JSON 字符串
     * @return 清洗后的 JSON 字符串
     */
    public static String cleanJson(String jsonBody) {
        if (StringUtils.isEmpty(jsonBody)) {
            return jsonBody;
        }

        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(jsonBody);
            JsonNode cleanedNode = cleanJsonNode(rootNode);
            return OBJECT_MAPPER.writeValueAsString(cleanedNode);
        } catch (JsonProcessingException e) {
            return jsonBody;
        }
    }

    private static JsonNode cleanJsonNode(JsonNode node) {
        if (node.isTextual()) {
            String original = node.asText();
            String cleaned = clean(original);
            if (!original.equals(cleaned)) {
                return new TextNode(cleaned);
            }
            return node;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fieldNames = objectNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldNode = objectNode.get(fieldName);
                objectNode.set(fieldName, cleanJsonNode(fieldNode));
            }
            return objectNode;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, cleanJsonNode(arrayNode.get(i)));
            }
            return arrayNode;
        }

        return node;
    }
}
