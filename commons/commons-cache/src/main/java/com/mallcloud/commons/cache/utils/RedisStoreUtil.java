package com.mallcloud.commons.cache.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Redis 存储工具类
 * 提供对 Redis 操作的封装，简化 Redis 的使用
 *
 * @author urbane
 * @since 1.0.0
 */
public class RedisStoreUtil {

    /**
     * Redis 操作模板类
     * 用于执行各种 Redis 操作
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取指定键的值
     *
     * @param key Redis键名，不能为空
     * @return 键对应的值，如果键不存在则返回null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置键值对，使用默认过期时间
     * 默认过期时间由CacheConstant.DEFAULT_EXPIRE定义
     *
     * @param key Redis键名，不能为空
     * @param value 键对应的值，可以为null
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置键值对，指定过期时间(单位:秒)
     *
     * @param key Redis键名，不能为空
     * @param value 键对应的值，可以为null
     * @param timeout 过期时间，单位为秒，必须大于0
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置键值对，指定过期时间和时间单位
     *
     * @param key Redis键名，不能为空
     * @param value 键对应的值，可以为null
     * @param timeout 过期时间，必须大于0
     * @param timeUnit 时间单位，不能为空
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 删除指定键
     *
     * @param key 要删除的Redis键名，不能为空
     */
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 为指定键设置新的过期时间
     *
     * @param key Redis键名，不能为空
     * @param timeout 过期时间，必须大于0
     * @param timeUnit 时间单位，不能为空
     */
    public void expire(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }
}
