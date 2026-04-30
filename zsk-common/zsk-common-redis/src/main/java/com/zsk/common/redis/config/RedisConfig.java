package com.zsk.common.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Redis配置
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-14
 */
@Configuration
@EnableCaching
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfig implements CachingConfigurer {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 创建适用于 Redis 序列化的 ObjectMapper
     * 注册 JavaTimeModule 以支持 Java 8 时间类型
     */
    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));

        mapper.registerModule(javaTimeModule);
        return mapper;
    }

    /**
     * 自定义 RedisTemplate
     * 增强序列化方式，使用 Jackson2JsonRedisSerializer
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(createRedisObjectMapper(), Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 自定义 RedisTemplate<String, Object>
     * 用于需要 String 类型 key 的场景
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<String, Object> stringKeyRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(createRedisObjectMapper(), Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
