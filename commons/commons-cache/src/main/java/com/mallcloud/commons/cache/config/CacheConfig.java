package com.mallcloud.commons.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * 使用 @EnableCaching 开启 Spring Cache 缓存功能
 */
@EnableCaching
@Configuration
public class CacheConfig {

    @Resource
    private CacheProperties cacheProperties;

    /**
     * L1：本地缓存（Caffeine）
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats());
        return cacheManager;
    }

    /**
     * L2：Redis 分布式缓存
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        // 自定义缓存配置，将value序列化改成json，同时设置默认过期时间为2小时
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置 Cache 默认自动过期时间
                .entryTtl(Duration.ofHours(2))
                // 默认key以 String 序列化，value 以 JDK 的方式，下面改成 json 方式
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
                .disableCachingNullValues()
                .prefixCacheNameWith(cacheProperties.getRedis().getKeyPrefix());

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 组合缓存管理器：先查 Caffeine，再查 Redis
     * 注意：Spring Cache 不自动写穿透，需手动控制
     */
    @Bean
    public CacheManager compositeCacheManager(RedisConnectionFactory factory) {
        return new CompositeCacheManager(
                caffeineCacheManager(),
                redisCacheManager(factory)
        );
    }
}

// 关于缓存的使用参考 SpringBoot 官方文档： https://docs.spring.io/spring-boot/reference/io/caching.html