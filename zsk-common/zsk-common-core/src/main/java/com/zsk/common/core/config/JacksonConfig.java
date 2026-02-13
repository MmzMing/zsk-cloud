package com.zsk.common.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson配置
 *
 * @author zsk
 */
@AutoConfiguration
public class JacksonConfig {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 创建并配置一个Jackson2ObjectMapperBuilderCustomizer Bean，用于自定义Jackson ObjectMapper的行为。
     * <p>
     * 该方法通过Lambda表达式返回一个Jackson2ObjectMapperBuilderCustomizer实例，
     * 用于设置日期时间格式、时区、序列化包含策略以及禁用某些默认特性。
     * 同时注册JavaTimeModule以支持Java 8的时间类型（LocalDateTime、LocalDate、LocalTime）的序列化和反序列化。
     *
     * @return Jackson2ObjectMapperBuilderCustomizer 实例，用于进一步定制ObjectMapper
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 设置默认的日期时间格式和时区
            builder.simpleDateFormat(DATE_TIME_FORMAT);
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            // 配置序列化时只包含非空字段
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);

            // 禁用将日期写为时间戳的功能，以及在遇到未知属性时抛出异常的功能
            builder.featuresToDisable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES
            );

            // 启用美化输出
            builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT);

            // 注册JavaTimeModule以支持Java 8的时间类型处理
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            // 为LocalDateTime类型添加自定义的序列化器和反序列化器
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));

            // 为LocalDate类型添加自定义的序列化器和反序列化器
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));

            // 为LocalTime类型添加自定义的序列化器和反序列化器
            javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
            javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));

            // 将JavaTimeModule应用到builder中
            builder.modules(javaTimeModule);
        };
    }
}
