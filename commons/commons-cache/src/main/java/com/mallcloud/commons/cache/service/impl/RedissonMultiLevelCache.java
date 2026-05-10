package com.mallcloud.commons.cache.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.mallcloud.commons.cache.exception.CacheException;
import com.mallcloud.commons.cache.properties.CacheProperties;
import com.mallcloud.commons.cache.properties.DegradationMode;
import com.mallcloud.commons.cache.service.MultiLevelCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Redisson 多级缓存实现
 * <p>L1: Caffeine (进程内高速缓存)
 * <p>L2: Redis/Redisson (分布式共享缓存)
 * <p>同步机制: Redis Pub/Sub 广播 L1 失效
 * <p>线程模型: 全面异步化，绝不阻塞 Reactor 线程
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonMultiLevelCache<K, V> implements MultiLevelCache<K, V> {

    private final Cache<K, V> l1Cache;              // L1 本地缓存
    private final RedissonClient redisson;           // L2 客户端
    private final CacheProperties.InstanceConfig config; // 实例配置
    private final String keyPrefix;                  // Key 前缀

    /**
     * 初始化集群同步监听器
     * <p>网关节点启动时自动订阅频道，收到失效消息后清除本地 L1
     */
    public void initSyncListener() {
        RTopic topic = redisson.getTopic(config.getSyncChannel());
        topic.addListener(Object.class, (channel, msg) -> {
            @SuppressWarnings("unchecked")
            K key = (K) msg;
            l1Cache.invalidate(key);
            log.debug("收到集群缓存失效广播，已清理 L1: {}", key);
        });
    }

    @Override
    public Mono<V> get(K key, Supplier<Mono<V>> loader) {
        // 1. 优先查 L1（纯内存，纳秒级）
        V l1Value = l1Cache.getIfPresent(key);
        if (l1Value != null) {
            return Mono.just(l1Value);
        }

        // 2. L1 Miss → 异步查 L2（使用 Redisson Async API，不阻塞线程）
        String l2Key = buildL2Key(key);
        return Mono.fromFuture(redisson.getBucket(l2Key).getAsync())
                .doOnNext(l2Value -> {
                    // L2 命中则回填 L1，提升后续请求速度
                    if (l2Value != null) {
                        l1Cache.put(key, (V) l2Value);
                    }
                })
                // 3. L2 Miss → 执行 Loader（如查数据库）
                .switchIfEmpty(Mono.defer(loader))
                .doOnNext(v -> {
                    // Loader 返回值写入 L2（L1 会在下次 get 时自动填充）
                    if (v != null) {
                        put(key, v, config.getL2Ttl()).subscribe();
                    }
                })
                // 4. 降级策略：L2 宕机或网络异常时
                .onErrorResume(e -> {
                    log.error("L2 缓存查询失败，触发降级: {}", e.getMessage());
                    if (config.getDegradation() == DegradationMode.FAIL_OPEN) {
                        // Fail-Open：放行请求，依赖 Loader 直接查库
                        return loader.get();
                    } else {
                        // Fail-Closed：阻断请求，返回业务异常
                        return Mono.error(new CacheException("缓存服务不可用，请求已拦截", e));
                    }
                });
    }

    @Override
    public Mono<Void> put(K key, V value, Duration ttl) {
        String l2Key = buildL2Key(key);
        return Mono.fromRunnable(() -> {
            // 写入 L2（Redisson 异步操作封装为同步块，因写入操作轻量）
            redisson.getBucket(l2Key).set(value, ttl);
            // 同步写入 L1
            l1Cache.put(key, value);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> invalidate(K key) {
        String l2Key = buildL2Key(key);
        return Mono.fromRunnable(() -> {
            // 1. 删除 L2
            redisson.getBucket(l2Key).delete();
            // 2. 清除 L1
            l1Cache.invalidate(key);
            // 3. 发布失效广播（通知集群其他节点清 L1）
            redisson.getTopic(config.getSyncChannel()).publish(key);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> invalidatePattern(String pattern) {
        // Redisson 支持 Scan 删除，生产环境建议限制最大扫描数量防 OOM
        return Mono.fromRunnable(() -> {
            redisson.getKeys().deleteByPattern(keyPrefix + pattern);
            // L1 无法按模式精确清除，依赖 TTL 自动过期（故 L1 TTL 必须短）
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * 构建带业务前缀的 L2 Key
     */
    private String buildL2Key(K key) {
        return keyPrefix + key.toString();
    }
}