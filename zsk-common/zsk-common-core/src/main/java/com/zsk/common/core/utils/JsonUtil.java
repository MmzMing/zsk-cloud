package com.zsk.common.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Jackson JSON 工具类
 * 提供类似 FastJSON 的便捷 API，支持链式操作和泛型转换
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 序列化：忽略 null 值字段
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 禁用日期转为时间戳，使用 ISO-8601 格式
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 支持 JDK 8 日期时间类型
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        // 反序列化：允许未知字段（防止因 JSON 中有而 Java 类中没有的字段报错）
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 反序列化：允许基本类型为 null（int 等默认为 0）
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

        // 美化输出（开发调试时使用）
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // ==================== 序列化（对象转 JSON）====================

    /**
     * 对象转 JSON 字符串
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转 JSON 失败", e);
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /**
     * 对象转 JSON 字符串 (兼容旧代码)
     */
    public static String toJson(Object obj) {
        return toJsonString(obj);
    }

    /**
     * 对象转 JSON 字符串（压缩格式，无换行）
     */
    public static String toJsonCompact(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.copy()
                    .disable(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转 JSON 失败", e);
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /**
     * 对象转格式化后的 JSON 字符串
     */
    public static String toJsonPretty(Object obj) {
        return toJsonString(obj); // 默认已开启美化
    }

    /**
     * 对象转字节数组
     */
    public static byte[] toJsonBytes(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转 JSON 字节数组失败", e);
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    // ==================== 反序列化（JSON 转对象）====================

    /**
     * JSON 字符串转对象（支持泛型）
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON 转对象失败: {}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 字符串转对象（支持复杂泛型，如 List<User>）
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON 转对象失败: {}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 字符串转对象（支持参数化类型，如 new TypeReference<List<User>>() {}）
     */
    public static <T> T parseObject(String json, JavaType javaType) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.error("JSON 转对象失败: {}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * InputStream 转对象
     */
    public static <T> T parseObject(InputStream inputStream, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, clazz);
        } catch (IOException e) {
            log.error("InputStream 转对象失败", e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * InputStream 转对象（支持泛型）
     */
    public static <T> T parseObject(InputStream inputStream, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, typeReference);
        } catch (IOException e) {
            log.error("InputStream 转对象失败", e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    // ==================== 集合类型转换 ====================

    /**
     * JSON 转 List
     */
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            JavaType listType = OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, clazz);
            return OBJECT_MAPPER.readValue(json, listType);
        } catch (JsonProcessingException e) {
            log.error("JSON 转 List 失败: {}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 转 Map（键为 String，值为 Object）
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("JSON 转 Map 失败: {}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 转指定类型的 Map
     */
    public static <K, V> Map<K, V> parseMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            JavaType mapType = OBJECT_MAPPER.getTypeFactory()
                    .constructMapType(Map.class, keyClass, valueClass);
            return OBJECT_MAPPER.readValue(json, mapType);
        } catch (JsonProcessingException e) {
            log.error("JSON 转 Map 失败: {}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    // ==================== Tree Model 操作（类似 FastJSON 的 JSONObject/JSONArray）====================

    /**
     * 创建新的 ObjectNode（类似 FastJSON 的 JSONObject）
     */
    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 创建新的 ArrayNode（类似 FastJSON 的 JSONArray）
     */
    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 读取 JSON 为 JsonNode
     */
    public static JsonNode readTree(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("读取 JSON Tree 失败: {}", json, e);
            throw new RuntimeException("JSON 解析失败", e);
        }
    }

    /**
     * 读取 JSON 为 ObjectNode
     */
    public static ObjectNode readObjectNode(String json) {
        try {
            return (ObjectNode) OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("读取 ObjectNode 失败: {}", json, e);
            throw new RuntimeException("JSON 解析失败", e);
        }
    }

    /**
     * 读取 JSON 为 ArrayNode
     */
    public static ArrayNode readArrayNode(String json) {
        try {
            return (ArrayNode) OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("读取 ArrayNode 失败: {}", json, e);
            throw new RuntimeException("JSON 解析失败", e);
        }
    }

    /**
     * JsonNode 中获取字段值（安全获取，避免 NPE）
     */
    public static String getString(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? null : fieldNode.asText();
    }

    public static Integer getInt(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? null : fieldNode.asInt();
    }

    public static Long getLong(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? null : fieldNode.asLong();
    }

    public static Double getDouble(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? null : fieldNode.asDouble();
    }

    public static Boolean getBoolean(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? null : fieldNode.asBoolean();
    }

    /**
     * JsonNode 转为指定类型对象
     */
    public static <T> T convertValue(JsonNode node, Class<T> clazz) {
        if (node == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            log.error("JsonNode 转换失败", e);
            throw new RuntimeException("JSON 转换失败", e);
        }
    }

    // ==================== 对象转换（Map/POJO 互转）====================

    /**
     * Map 转 POJO
     */
    public static <T> T mapToPojo(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

    /**
     * POJO 转 Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> pojoToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(obj, Map.class);
    }

    /**
     * 对象类型转换（如 UserDTO 转 UserVO）
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(source, targetClass);
    }

    /**
     * 对象类型转换（支持泛型）
     */
    public static <T> T convert(Object source, TypeReference<T> typeReference) {
        if (source == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(source, typeReference);
    }

    // ==================== 深拷贝 ====================

    /**
     * 深拷贝对象（通过序列化/反序列化实现）
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return (T) OBJECT_MAPPER.readValue(
                    OBJECT_MAPPER.writeValueAsString(obj),
                    obj.getClass()
            );
        } catch (JsonProcessingException e) {
            log.error("深拷贝失败", e);
            throw new RuntimeException("深拷贝失败", e);
        }
    }

    /**
     * 深拷贝对象（支持泛型）
     */
    public static <T> T deepCopy(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(
                    OBJECT_MAPPER.writeValueAsString(obj),
                    clazz
            );
        } catch (JsonProcessingException e) {
            log.error("深拷贝失败", e);
            throw new RuntimeException("深拷贝失败", e);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 验证字符串是否为有效 JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 获取 ObjectMapper 实例（用于高级自定义操作）
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 创建 ObjectMapper 副本（用于临时自定义配置，不影响全局）
     */
    public static ObjectMapper copyObjectMapper() {
        return OBJECT_MAPPER.copy();
    }

    /**
     * 合并两个 JSON 对象（source 覆盖 target 的相同字段）
     */
    public static String merge(String targetJson, String sourceJson) {
        try {
            JsonNode targetNode = OBJECT_MAPPER.readTree(targetJson);
            JsonNode sourceNode = OBJECT_MAPPER.readTree(sourceJson);

            if (targetNode instanceof ObjectNode && sourceNode instanceof ObjectNode) {
                ((ObjectNode) targetNode).setAll((ObjectNode) sourceNode);
                return OBJECT_MAPPER.writeValueAsString(targetNode);
            }
            throw new IllegalArgumentException("合并操作仅支持 JSON 对象");
        } catch (JsonProcessingException e) {
            log.error("JSON 合并失败", e);
            throw new RuntimeException("JSON 合并失败", e);
        }
    }

    private JsonUtil() {
        throw new AssertionError("工具类禁止实例化");
    }
}