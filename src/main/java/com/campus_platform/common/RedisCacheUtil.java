package com.campus_platform.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * 标准化 Redis 缓存工具类
 * 完全替代 LocalCacheUtil
 */
@Component
public class RedisCacheUtil {

    // SpringBoot 自动注入针对 String 操作优化的 Redis 模板
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 存入缓存并直接利用 Redis 底层机制设置过期时间
    public void set(String key, String value, long expireSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
    }

    // 获取缓存（如果 Redis 判定过期，会自动返回 null，无需 Java 层干预）
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 删除缓存
    public void remove(String key) {
        stringRedisTemplate.delete(key);
    }
}