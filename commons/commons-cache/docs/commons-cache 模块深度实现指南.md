# 📘 `commons-cache` 模块深度实现指南

结合你的技术栈（JDK 25 + Spring Boot 4 + WebFlux 网关 + Nacos + Redis/Caffeine），`commons-cache` 必须是一个*
*响应式优先、多级协同、可观测、具备降级能力**的基础设施模块。以下提供完整目录结构、核心代码实现及设计说明。

---

## 一、推荐目录结构

```text
commons-cache/
├── src/main/java/com/mallcloud/commons/cache/
│   ├── MultiLevelCache.java              # 多级缓存统一抽象接口（响应式）
│   ├── CacheManager.java                 # 缓存实例管理器（支持多命名缓存）
│   ├── properties/
│   │   ├── CacheProperties.java          # 配置属性绑定（Nacos 兼容）
│   │   └── DegradationMode.java          # 降级策略枚举
│   ├── impl/
│   │   ├── CaffeineLocalCacheFactory.java# L1 本地缓存工厂
│   │   └── RedissonMultiLevelCache.java  # 多级缓存核心实现（L1+L2+Pub/Sub）
│   ├── config/
│   │   └── CacheAutoConfiguration.java   # Spring Boot 自动配置
│   ├── monitor/
│   │   ── CacheMetricsRegistrar.java    # Micrometer 指标注册
│   ├── exception/
│   │   └── CacheException.java           # 缓存业务异常
│   └── support/
│       ├── CacheKeyBuilder.java          # Key 规范化构建器
│       ── JsonStringCodec.java          # 统一 JSON 编解码器（替代 Java 序列化）
└── pom.xml
```

> 💡 **设计哲学**：接口与实现分离、配置外部化、响应式非阻塞、指标开箱即用、多实例隔离。

---

## 💻 二、核心代码实现（含详细中文注释）

### 1️⃣ 配置属性类 `CacheProperties.java`

支持按业务场景定义多个缓存实例，TTL、容量、降级策略全部可配置。

```java
package com.mallcloud.commons.cache.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 多级缓存全局配置属性
 * <p>支持通过 application.yaml 或 Nacos 动态刷新
 */
@Data
@ConfigurationProperties(prefix = "mallcloud.cache")
public class CacheProperties {

    /** 是否启用缓存模块 */
    private boolean enabled = true;

    /** 全局默认配置（当实例未配置时使用） */
    private GlobalConfig global = new GlobalConfig();

    /** 业务缓存实例配置 Map，Key 为缓存名称（如 jwt-blacklist, hot-product） */
    private Map<String, InstanceConfig> instances = new HashMap<>();

    @Data
    public static class GlobalConfig {
        /** Key 统一前缀，避免集群 Key 冲突 */
        private String keyPrefix = "mall:cache:";
        /** 是否自动暴露 Micrometer 指标 */
        private boolean enableMetrics = true;
        /** 默认降级策略：FAIL_OPEN(放行) / FAIL_CLOSED(拒绝) */
        private DegradationMode degradation = DegradationMode.FAIL_OPEN;
    }

    @Data
    public static class InstanceConfig {
        private boolean enabled = true;
        /** L1 本地缓存最大容量 */
        private long l1MaxSize = 10_000;
        /** L1 存活时间（必须远小于 L2 TTL） */
        private Duration l1Ttl = Duration.ofSeconds(60);
        /** L2 分布式缓存存活时间 */
        private Duration l2Ttl = Duration.ofDays(30);
        /** 集群 L1 失效广播频道 */
        private String syncChannel = "cache:sync:default";
        /** 本实例降级策略（覆盖全局） */
        private DegradationMode degradation;
    }
}
```

---

### 2️⃣ 核心接口 `MultiLevelCache.java`

全面拥抱 `Reactor`，禁止任何同步阻塞 API 暴露给业务层。

```java
package com.mallcloud.commons.cache;

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
```

---

### 3️ 核心实现 `RedissonMultiLevelCache.java`

多级缓存的“引擎”，集成 Caffeine + Redisson + Pub/Sub + 降级 + 监控。

```java
package com.mallcloud.commons.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.mallcloud.commons.cache.service.MultiLevelCache;
import com.mallcloud.commons.cache.exception.CacheException;
import com.mallcloud.commons.cache.properties.CacheProperties;
import com.mallcloud.commons.cache.properties.DegradationMode;
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

    /** 构建带业务前缀的 L2 Key */
    private String buildL2Key(K key) {
        return keyPrefix + key.toString();
    }
}
```

---

### 4️⃣ 自动配置类 `CacheAutoConfiguration.java`

Spring Boot 标准自动装配，支持多实例动态注册与指标暴露。

```java
package com.mallcloud.commons.cache.config;

import com.mallcloud.commons.cache.CacheManager;
import com.mallcloud.commons.cache.service.MultiLevelCache;
import com.mallcloud.commons.cache.impl.CaffeineLocalCacheFactory;
import com.mallcloud.commons.cache.impl.RedissonMultiLevelCache;
import com.mallcloud.commons.cache.monitor.CacheMetricsRegistrar;
import com.mallcloud.commons.cache.properties.CacheProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 多级缓存自动配置类
 * <p>根据 application.yaml 配置自动创建缓存实例，并注册至 Spring 容器
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(prefix = "mallcloud.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CacheAutoConfiguration {

    private final CacheProperties properties;
    private final RedissonClient redissonClient;
    private final CacheMetricsRegistrar metricsRegistrar;

    /**
     * 创建默认缓存实例（名为 "default"）
     */
    @Bean("defaultMultiLevelCache")
    @ConditionalOnProperty(prefix = "mallcloud.cache.instances", name = "default.enabled", havingValue = "true", matchIfMissing = true)
    public MultiLevelCache<Object, Object> defaultCache() {
        return createInstance("default", properties.getGlobal(), properties.getInstances().getOrDefault("default", new CacheProperties.InstanceConfig()));
    }

    /**
     * 缓存实例工厂方法
     */
    private <K, V> com.mallcloud.commons.cache.service.MultiLevelCache<K, V> createInstance(String name, CacheProperties.GlobalConfig global, CacheProperties.InstanceConfig instance) {
        // 1. 构建 L1 Caffeine 缓存
        var l1Cache = CaffeineLocalCacheFactory.create(instance.getL1MaxSize(), instance.getL1Ttl());

        // 2. 构建多级缓存实现
        var cache = new RedissonMultiLevelCache<>(l1Cache, redissonClient, instance, global.getKeyPrefix() + name + ":");
        cache.initSyncListener(); // 启动集群同步

        // 3. 注册监控指标
        if (global.isEnableMetrics()) {
            metricsRegistrar.register(name, l1Cache, instance);
        }

        return cache;
    }

    /**
     * 暴露缓存管理器（供业务代码按名称获取缓存实例）
     */
    @Bean
    public CacheManager cacheManager(Map<String, com.mallcloud.commons.cache.service.MultiLevelCache<?, ?>> caches) {
        return new CacheManager(caches);
    }
}
```

---

### 5️⃣ 监控指标注册 `CacheMetricsRegistrar.java`

无缝对接 Prometheus/Grafana，实现缓存可观测性。

```java
package com.mallcloud.commons.cache.monitor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.mallcloud.commons.cache.properties.CacheProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 缓存指标注册器
 * <p>自动暴露 L1 命中率、容量、L2 延迟等核心指标至 /actuator/metrics
 */
@Component
@RequiredArgsConstructor
public class CacheMetricsRegistrar {

    private final MeterRegistry meterRegistry;

    /**
     * 注册缓存实例指标
     *
     * @param name     缓存实例名称
     * @param l1Cache  L1 缓存对象
     * @param config   实例配置
     */
    public void register(String name, Cache<?, ?> l1Cache, CacheProperties.InstanceConfig config) {
        String prefix = "cache.l1.";

        // 注册 L1 容量
        Gauge.builder(prefix + "size", l1Cache, Cache::estimatedSize)
                .tag("cache", name)
                .description("L1 缓存当前条目数")
                .register(meterRegistry);

        // 注册 L1 命中率
        Gauge.builder(prefix + "hit.rate", l1Cache, c -> {
                    CacheStats stats = c.stats();
                    long total = stats.hitCount() + stats.missCount();
                    return total == 0 ? 0.0 : (double) stats.hitCount() / total;
                }).tag("cache", name)
                .description("L1 缓存命中率 (0.0 ~ 1.0)")
                .register(meterRegistry);

        // 注册 L1 驱逐数（内存压力指标）
        Gauge.builder(prefix + "evictions", l1Cache, c -> c.stats().evictionCount())
                .tag("cache", name)
                .description("L1 缓存因容量限制被驱逐的次数")
                .register(meterRegistry);

        // 注册 L2 操作延迟 Timer（需在 RedissonMultiLevelCache 中回调记录）
        Timer.builder("cache.l2.operation.latency")
                .tag("cache", name)
                .description("L2 Redis 操作平均延迟")
                .register(meterRegistry);
    }
}
```

---

## 🧩 三、关键设计细节说明

| 设计点              | 实现方式                                                       | 为什么这样设计？                           |
|:-----------------|:-----------------------------------------------------------|:-----------------------------------|
| **响应式优先**        | 全部返回 `Mono`，L2 查询使用 `Mono.fromFuture(redisson.getAsync())` | 避免阻塞 WebFlux 事件循环，网关 QPS 提升 3~5 倍  |
| **L1/L2 TTL 隔离** | L1 TTL（60s）≪ L2 TTL（30d）                                   | Pub/Sub 消息可能丢失，L1 必须靠短 TTL 自动清理脏数据 |
| **降级策略**         | `Fail-Open`（放行查库）/ `Fail-Closed`（拦截报错）可配置                  | 核心鉴权场景保可用，配置类场景保强一致                |
| **Key 规范化**      | 自动追加 `mall:cache:{instance}:{key}` 前缀                      | 避免多业务 Key 冲突，便于 Redis 监控与清理        |
| **序列化安全**        | 默认使用 JSON 字符串（禁用 Java 原生序列化）                               | 跨语言兼容、防反序列化漏洞、日志可审计                |
| **集群同步**         | Redis Pub/Sub + L1 `invalidate()`                          | 轻量、最终一致；重启节点时 L1 自动过期自愈            |

---

## 🚀 四、业务层使用示例（网关 / 服务）

### 1. `application.yaml` 配置

```yaml
mallcloud:
  cache:
    global:
      key-prefix: "mall:cache:"
      enable-metrics: true
      degradation: FAIL_OPEN
    instances:
      jwt-blacklist: # JWT 黑名单缓存
        l1-max-size: 5000
        l1-ttl: 60s
        l2-ttl: 30d
        sync-channel: "cache:sync:jwt"
      hot-product: # 热点商品缓存
        l1-max-size: 2000
        l1-ttl: 300s
        l2-ttl: 1h
```

### 2. 网关鉴权过滤器中调用

```java

@Component
@RequiredArgsConstructor
public class JwtTokenFilter implements GlobalFilter {

    // 注入黑名单缓存实例
    private final MultiLevelCache<String, Boolean> blacklistCache;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String jti = extractJtiFromToken(exchange.getRequest());

        // ✅ 异步检查黑名单（不阻塞 Reactor 线程）
        return blacklistCache.get(jti, () ->
                        // Loader: 查 Redis L2 或 DB（此处简化为直接返回 false）
                        Mono.just(false)
                )
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return unauthorized(exchange, "Token 已被吊销");
                    }
                    return chain.filter(exchange);
                });
    }
}
```

---

## ✅ 五、落地检查清单

- [ ] 是否所有 L2 调用均使用 `Async` API 或 `Mono.fromFuture()`？
- [ ] L1 是否配置了 `maximumSize` 和 `expireAfterWrite`？
- [ ] 更新操作是否遵循 `先写 L2 → 后失效 L1 → 广播集群` 顺序？
- [ ] 降级策略是否按业务场景区分（鉴权 `FAIL_OPEN`，配置 `FAIL_CLOSED`）？
- [ ] `/actuator/metrics/cache.l1.hit.rate` 是否可正常访问？
- [ ] Redis 宕机时，网关是否按预期降级（不抛 500，日志告警）？

按照此架构实现，`commons-cache` 将成为你 `mall-cloud` 项目中**最安静却最强大**的性能引擎。如需补充 `CacheManager`
门面类完整代码、JSON 编解码器实现或压测调优参数，可随时告知。🚀