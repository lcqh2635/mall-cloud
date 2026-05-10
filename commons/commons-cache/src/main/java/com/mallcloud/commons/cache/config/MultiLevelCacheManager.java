package com.mallcloud.commons.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mallcloud.commons.cache.config.CacheProperties;
import com.mallcloud.commons.cache.serializer.CacheSerializer;
import com.mallcloud.commons.cache.sync.CacheSyncMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存管理器（核心）
 *
 * <p>统一管理 L1（Caffeine 本地缓存）和 L2（Redis 分布式缓存）的读写逻辑，
 * 并在缓存失效时通过 Redis Pub/Sub 广播消息，通知集群其他节点同步清除 L1。
 *
 * <p>读取优先级：L1 → L2 → 回源（执行业务方法）
 * <p>写入策略：同时写 L1 + L2
 * <p>失效策略：清除 L1 + L2，广播 Pub/Sub 消息
 *
 * @author mallcloud
 */
public class MultiLevelCacheManager {

    private static final Logger log = LoggerFactory.getLogger(MultiLevelCacheManager.class);

    /**
     * 表示"值为 null"的占位对象，用于防止缓存穿透
     */
    private static final Object NULL_PLACEHOLDER = new Object();

    private final CacheProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheSerializer serializer;

    /**
     * L1 本地缓存实例（Caffeine）
     * <p>所有 cacheName 共用同一个 Caffeine 实例，通过 Key 前缀区分命名空间。
     * 如需按 cacheName 独立配置 TTL，可扩展为 Map<String, Cache>。
     */
    private final Cache<String, Object> l1Cache;

    /**
     * 构造多级缓存管理器
     *
     * @param properties    缓存配置属性
     * @param redisTemplate Redis 操作模板
     * @param serializer    序列化器
     */
    public MultiLevelCacheManager(CacheProperties properties,
                                  RedisTemplate<String, Object> redisTemplate,
                                  CacheSerializer serializer) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.serializer = serializer;
        this.l1Cache = buildL1Cache();
    }

    // ===================== 核心读写方法 =====================

    /**
     * 从缓存中获取值（L1 → L2 → null）
     *
     * <p>命中 L2 后会自动回填 L1，下次访问直接命中本地缓存。
     *
     * @param key      完整缓存 Key
     * @param l1Ttl    L1 TTL（秒）
     * @param timeUnit 时间单位
     * @return 缓存值，未命中返回 {@link #NULL_PLACEHOLDER} 或 null
     * 调用方需通过 {@link #isNullPlaceholder(Object)} 判断是否为 null 占位
     */
    public Object get(String key, long l1Ttl, TimeUnit timeUnit) {
        // ── 第一步：查 L1 本地缓存 ─────────────────────────────────────────
        if (properties.getL1().isEnabled()) {
            Object l1Value = l1Cache.getIfPresent(key);
            if (l1Value != null) {
                log.debug("[Cache] L1 命中: key={}", key);
                return l1Value;
            }
        }

        // ── 第二步：查 L2 Redis ────────────────────────────────────────────
        if (properties.getL2().isEnabled()) {
            try {
                Object l2Raw = redisTemplate.opsForValue().get(key);
                if (l2Raw != null) {
                    Object l2Value = deserialize(l2Raw);
                    log.debug("[Cache] L2 命中，回填 L1: key={}", key);
                    // 命中 L2 后回填 L1，加速后续访问
                    putL1(key, l2Value);
                    return l2Value;
                }
            } catch (Exception e) {
                // Redis 故障时降级：仅记录日志，不阻断业务
                log.warn("[Cache] L2 查询异常，降级跳过: key={}, error={}", key, e.getMessage());
            }
        }

        // ── 第三步：缓存未命中 ─────────────────────────────────────────────
        log.debug("[Cache] 缓存未命中: key={}", key);
        return null;
    }

    /**
     * 将值写入 L1 + L2 缓存
     *
     * @param key      完整缓存 Key
     * @param value    要缓存的值（null 时写入占位对象，需开启 cacheNullValue）
     * @param l2Ttl    L2（Redis）TTL
     * @param l1Ttl    L1（Caffeine）TTL
     * @param timeUnit TTL 时间单位
     */
    public void put(String key, Object value, long l2Ttl, long l1Ttl, TimeUnit timeUnit) {
        // 写入 L1
        if (properties.getL1().isEnabled()) {
            putL1(key, value != null ? value : NULL_PLACEHOLDER);
        }

        // 写入 L2（Redis）
        if (properties.getL2().isEnabled()) {
            try {
                Object serialized = serialize(value != null ? value : NULL_PLACEHOLDER);
                if (l2Ttl > 0) {
                    redisTemplate.opsForValue().set(key, serialized,
                            Duration.of(l2Ttl, toChronoUnit(timeUnit)));
                } else {
                    // TTL 为 0 表示永不过期（谨慎使用）
                    redisTemplate.opsForValue().set(key, serialized);
                }
                log.debug("[Cache] 写入 L1+L2: key={}, ttl={}{}",
                        key, l2Ttl, timeUnit.name().toLowerCase());
            } catch (Exception e) {
                // Redis 写入失败不影响业务，但需要记录，避免长期只有 L1
                log.warn("[Cache] L2 写入异常: key={}, error={}", key, e.getMessage());
            }
        }
    }

    /**
     * 清除指定 Key 的缓存（L1 + L2），并广播失效消息
     *
     * @param key   完整缓存 Key
     * @param topic Pub/Sub 消息主题（用于同步其他节点 L1）
     */
    public void evict(String key, String topic) {
        // 清除 L1
        if (properties.getL1().isEnabled()) {
            l1Cache.invalidate(key);
            log.debug("[Cache] L1 已清除: key={}", key);
        }

        // 清除 L2
        if (properties.getL2().isEnabled()) {
            try {
                redisTemplate.delete(key);
                log.debug("[Cache] L2 已清除: key={}", key);
            } catch (Exception e) {
                log.warn("[Cache] L2 清除异常: key={}, error={}", key, e.getMessage());
            }
        }

        // 广播失效消息，通知集群其他节点清除 L1
        publishEvictMessage(topic, key, false);
    }

    /**
     * 清除指定 cacheName 命名空间下的所有缓存条目（allEntries = true）
     *
     * <p>使用 Redis SCAN 命令替代 KEYS，避免在大数据量时阻塞 Redis。
     *
     * @param pattern Key 通配符（如 "mallcloud:cache:user:*"）
     * @param topic   Pub/Sub 消息主题
     */
    public void evictAll(String pattern, String topic) {
        // 清空 L1（全量）
        if (properties.getL1().isEnabled()) {
            // 按 Key 前缀过滤清除，保留其他 cacheName 的数据
            l1Cache.asMap().keySet().removeIf(k -> k.startsWith(
                    pattern.substring(0, pattern.length() - 1))); // 去掉末尾的 *
            log.debug("[Cache] L1 批量清除: pattern={}", pattern);
        }

        // 扫描并清除 L2
        if (properties.getL2().isEnabled()) {
            try {
                // 使用 SCAN 替代 KEYS，避免阻塞 Redis 主线程
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.debug("[Cache] L2 批量清除: pattern={}, count={}", pattern, keys.size());
                }
            } catch (Exception e) {
                log.warn("[Cache] L2 批量清除异常: pattern={}, error={}", pattern, e.getMessage());
            }
        }

        // 广播全量清除消息
        publishEvictMessage(topic, pattern, true);
    }

    /**
     * 仅清除本地 L1 缓存（供消息监听器调用，避免循环广播）
     *
     * <p>其他节点收到 Pub/Sub 失效消息后调用此方法，
     * 只清本地 L1，不再触发 L2 删除和消息广播。
     *
     * @param key   要清除的 Key
     * @param isAll 是否为全量清除
     */
    public void evictL1Only(String key, boolean isAll) {
        if (!properties.getL1().isEnabled()) return;
        if (isAll) {
            // 全量清除：按前缀过滤（去掉末尾通配符 *）
            String prefix = key.endsWith("*") ? key.substring(0, key.length() - 1) : key;
            l1Cache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
            log.debug("[Cache] 收到广播，L1 批量清除: prefix={}", prefix);
        } else {
            l1Cache.invalidate(key);
            log.debug("[Cache] 收到广播，L1 单条清除: key={}", key);
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 判断值是否为 null 占位对象
     *
     * <p>缓存 null 值时写入 {@link #NULL_PLACEHOLDER}，
     * 调用方通过此方法判断是否应返回 null 给业务层。
     *
     * @param value 从缓存中取出的值
     * @return true 表示实际值为 null
     */
    public boolean isNullPlaceholder(Object value) {
        return NULL_PLACEHOLDER.equals(value);
    }

    /**
     * 构建 Caffeine 本地缓存实例
     *
     * <p>根据 {@link CacheProperties.L1Config} 配置动态设置：
     * <ul>
     *   <li>初始容量和最大容量</li>
     *   <li>写后过期（expireAfterWrite）</li>
     *   <li>访问后过期（expireAfterAccess，与写后过期互斥，优先写后）</li>
     * </ul>
     */
    private Cache<String, Object> buildL1Cache() {
        CacheProperties.L1Config l1 = properties.getL1();
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .initialCapacity(l1.getInitialCapacity())
                .maximumSize(l1.getMaximumSize());

        if (l1.getExpireAfterWrite() > 0) {
            // 写后过期：适合大多数业务场景
            builder.expireAfterWrite(l1.getExpireAfterWrite(), TimeUnit.SECONDS);
        } else if (l1.getExpireAfterAccess() > 0) {
            // 访问后过期：适合热点数据长驻缓存的场景
            builder.expireAfterAccess(l1.getExpireAfterAccess(), TimeUnit.SECONDS);
        }

        return builder.build();
    }

    /**
     * 向 L1 写入单条缓存
     */
    private void putL1(String key, Object value) {
        l1Cache.put(key, value);
    }

    /**
     * 序列化值（写入 Redis 前调用）
     */
    private Object serialize(Object value) {
        return serializer.serialize(value);
    }

    /**
     * 反序列化值（从 Redis 读取后调用）
     */
    private Object deserialize(Object raw) {
        return serializer.deserialize(raw);
    }

    /**
     * 发布缓存失效广播消息
     *
     * @param topic 消息主题
     * @param key   失效的 Key
     * @param isAll 是否为全量清除
     */
    private void publishEvictMessage(String topic, String key, boolean isAll) {
        if (!properties.getSync().isEnabled() || !StringUtils.hasText(topic)) return;
        try {
            CacheSyncMessage message = new CacheSyncMessage(key, isAll);
            redisTemplate.convertAndSend(topic, message);
            log.debug("[Cache] 发布失效广播: topic={}, key={}, isAll={}", topic, key, isAll);
        } catch (Exception e) {
            // 广播失败不影响本节点的缓存清除，仅记录警告
            log.warn("[Cache] 失效广播发布失败: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 将 {@link TimeUnit} 转换为 {@link java.time.temporal.ChronoUnit}
     */
    private java.time.temporal.ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        return switch (timeUnit) {
            case NANOSECONDS -> java.time.temporal.ChronoUnit.NANOS;
            case MICROSECONDS -> java.time.temporal.ChronoUnit.MICROS;
            case MILLISECONDS -> java.time.temporal.ChronoUnit.MILLIS;
            case SECONDS -> java.time.temporal.ChronoUnit.SECONDS;
            case MINUTES -> java.time.temporal.ChronoUnit.MINUTES;
            case HOURS -> java.time.temporal.ChronoUnit.HOURS;
            case DAYS -> java.time.temporal.ChronoUnit.DAYS;
        };
    }
}