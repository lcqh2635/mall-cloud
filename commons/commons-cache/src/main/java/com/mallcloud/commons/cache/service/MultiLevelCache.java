package com.mallcloud.commons.cache.service;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 多级缓存统一操作接口
 * <p>所有方法均返回 Mono/Flux，确保在 WebFlux 网关中不阻塞 Reactor 事件循环
 *
 * @param <K> 键类型（建议 String 或 Long）
 * @param <V> 值类型（建议基础类型或轻量 DTO）
 */
public interface MultiLevelCache<K, V> {

    /**
     * 异步获取缓存值（带加载器）
     * <p>查找顺序：L1 → L2 → Loader
     *
     * @param key    缓存键
     * @param loader 缓存未命中时的数据加载器（如查 DB 或远程调用）
     * @return 缓存值或加载结果
     */
    Mono<V> get(K key, Supplier<Mono<V>> loader);

    /**
     * 异步写入缓存（同步至 L1 与 L2）
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   L2 存活时间（L1 由配置自动管理）
     */
    Mono<Void> put(K key, V value, Duration ttl);

    /**
     * 异步失效缓存（清除 L1 + 删除 L2 + 广播集群）
     *
     * @param key 缓存键
     */
    Mono<Void> invalidate(K key);

    /**
     * 按模式批量失效（慎用，性能开销较大）
     *
     * @param pattern Key 匹配模式（如 "user:*"）
     */
    Mono<Void> invalidatePattern(String pattern);
}
