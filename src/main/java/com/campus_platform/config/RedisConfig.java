package com.campus_platform.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 【低耦合设计】抽取公共的 JSON 序列化器构建方法
     * 保证 Redis 缓存和 Spring Session 使用绝对一致的序列化规则
     */
    private GenericJackson2JsonRedisSerializer buildJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        //解决LocalDateTime无法转换的问题
        objectMapper.registerModule(new JavaTimeModule());

        //解决 LinkedHashMap 转换异常
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        //Key使用String序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        template.setValueSerializer(buildJsonSerializer());
        template.setHashValueSerializer(buildJsonSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // Spring Session 也严格使用这个增强版的序列化器
        return buildJsonSerializer();
    }
}