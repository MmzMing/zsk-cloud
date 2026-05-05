package com.zsk.common.core.utils;

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
 * <p>
 * 网关层清洗策略：仅移除真正危险的 XSS 攻击向量，不破坏合法内容。
 * 核心原则：
 * 1. 只针对 <script>、javascript:、vbscript:、onXXX= 事件处理器等不可争议的攻击向量
 * 2. 不使用 HtmlUtil.cleanHtmlTag()（会删除所有 HTML 标签，破坏合法内容）
 * 3. 不使用 HtmlUtil.escape()（会造成双重转义）
 * 4. 富文本/Markdown 内容的 XSS 防护由前端渲染层负责
 * </p>
 *
 * @author wuhuaming
 */
public class XssUtil {

    private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile(
            "<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );
    private static final Pattern OPENING_SCRIPT_PATTERN = Pattern.compile(
            "<\\s*script[^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );
    private static final Pattern CLOSING_SCRIPT_PATTERN = Pattern.compile(
            "<\\s*/\\s*script\\s*>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern JAVASCRIPT_PROTOCOL_PATTERN = Pattern.compile(
            "javascript\\s*:",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern VBSCRIPT_PROTOCOL_PATTERN = Pattern.compile(
            "vbscript\\s*:",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DATA_PROTOCOL_PATTERN = Pattern.compile(
            "data\\s*:\\s*text/html",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
            "\\bon(\\w+)\\s*=",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
            "expression\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 清洗字符串，仅移除不可争议的 XSS 攻击向量
     * <p>
     * 与旧版区别：
     * - 不再使用 HtmlUtil.cleanHtmlTag()（会删除所有 HTML 标签）
     * - 不再匹配 src= 属性（会误伤 JSON 中的 src 字段值）
     * - 不再匹配 eval()（会误伤技术笔记中的代码示例）
     * - 仅移除 script 标签、javascript/vbscript/data:text-html 协议、onXXX= 事件处理器
     * </p>
     *
     * @param value 待清洗的字符串
     * @return 清洗后的字符串
     */
    public static String clean(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }

        value = SCRIPT_TAG_PATTERN.matcher(value).replaceAll("");
        value = OPENING_SCRIPT_PATTERN.matcher(value).replaceAll("");
        value = CLOSING_SCRIPT_PATTERN.matcher(value).replaceAll("");
        value = JAVASCRIPT_PROTOCOL_PATTERN.matcher(value).replaceAll("");
        value = VBSCRIPT_PROTOCOL_PATTERN.matcher(value).replaceAll("");
        value = DATA_PROTOCOL_PATTERN.matcher(value).replaceAll("");
        value = EVENT_HANDLER_PATTERN.matcher(value).replaceAll("");
        value = EXPRESSION_PATTERN.matcher(value).replaceAll("");

        return value;
    }

    /**
     * 清洗 JSON 字符串中的 XSS 攻击向量
     * <p>
     * 仅遍历字符串值节点进行清洗，不改变 JSON 结构、字段顺序、数值精度。
     * </p>
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
