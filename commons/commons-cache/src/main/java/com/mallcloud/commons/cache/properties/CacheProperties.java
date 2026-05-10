package com.mallcloud.commons.cache.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 多级缓存全局配置属性
 *
 * <p>所有配置以 {@code mallcloud.cache} 为前缀，支持在 application.yml 中灵活配置。
 *
 * <p>完整配置示例：
 * <pre>
 * mallcloud:
 *   cache:
 *     enabled: true
 *     l1:
 *       enabled: true
 *       initial-capacity: 100
 *       maximum-size: 1000
 *       expire-after-write: 300     # 秒
 *       expire-after-access: 0      # 0 表示不启用，基于访问时间过期
 *     l2:
 *       enabled: true
 *       key-prefix: "mallcloud:"
 *       default-ttl: 1800           # 秒
 *       serializer: json            # json / kryo / jdk
 *     sync:
 *       enabled: true
 *       topic: "mallcloud:cache:sync"
 * </pre>
 *
 * @author mallcloud
 */
@Data
@ConfigurationProperties(prefix = "mallcloud.cache")
public class CacheProperties {
    /**
     * 是否启用缓存模块
     */
    private boolean enabled = true;

    /**
     * 全局默认配置（当实例未配置时使用）
     */
    private GlobalConfig global = new GlobalConfig();

    /**
     * 业务缓存实例配置 Map，Key 为缓存名称（如 jwt-blacklist, hot-product）
     */
    private Map<String, InstanceConfig> instances = new HashMap<>();


    /**
     * 是否启用整个缓存模块，默认 true
     */
    private boolean enabled = true;

    /**
     * L1 本地缓存（Caffeine）配置
     */
    @NestedConfigurationProperty
    private L1Config l1 = new L1Config();

    /**
     * L2 分布式缓存（Redis）配置
     */
    @NestedConfigurationProperty
    private L2Config l2 = new L2Config();

    /**
     * 多节点缓存同步配置
     */
    @NestedConfigurationProperty
    private SyncConfig sync = new SyncConfig();


    // ===================== L1 本地缓存配置 =====================

    /**
     * L1 Caffeine 本地缓存配置
     */
    @Setter
    @Getter
    public static class L1Config {

        /**
         * 是否启用 L1 本地缓存，默认 true
         */
        private boolean enabled = true;

        /**
         * 初始缓存容量（条目数），默认 100
         * <p>建议根据业务的热点数据量估算，避免频繁扩容带来的性能损耗。
         */
        private int initialCapacity = 100;

        /**
         * 最大缓存条目数，默认 1000
         * <p>超过此数量后 Caffeine 会按 W-TinyLFU 算法淘汰冷数据。
         */
        private long maximumSize = 1000L;

        /**
         * 写入后过期时间（秒），默认 300 秒（5 分钟）
         * <p>从写入时刻开始计时，超时后下次访问触发重新加载。
         * 设置为 0 则不启用此策略。
         */
        private long expireAfterWrite = 300L;

        /**
         * 最后一次访问后过期时间（秒），默认 0（不启用）
         * <p>从最后一次读/写时刻开始计时，适合访问频率决定生命周期的场景。
         * 设置为 0 则不启用此策略。
         */
        private long expireAfterAccess = 0L;

    }

    // ===================== L2 分布式缓存配置 =====================

    /**
     * L2 Redis 分布式缓存配置
     */
    @Setter
    @Getter
    public static class L2Config {

        /**
         * 是否启用 L2 Redis 缓存，默认 true
         */
        private boolean enabled = true;

        /**
         * Redis Key 全局前缀，默认 "mallcloud:cache:"
         * <p>最终 Key 格式：{prefix}{cacheName}:{key}
         * 示例："mallcloud:cache:user:12345"
         */
        private String keyPrefix = "mallcloud:cache:";

        /**
         * 默认 TTL（秒），默认 1800 秒（30 分钟）
         * <p>注解上显式指定的 TTL 会覆盖此全局默认值。
         */
        private long defaultTtl = 1800L;

        /**
         * 序列化方式，默认 json
         * <ul>
         *   <li>{@code json}  — Jackson 序列化，可读性强，适合调试</li>
         *   <li>{@code kryo}  — 二进制序列化，性能更高，体积更小</li>
         *   <li>{@code jdk}   — JDK 原生序列化，兼容性最强，但性能差</li>
         * </ul>
         */
        private String serializer = "json";

    }

    // ===================== 同步配置 =====================

    /**
     * 多节点 L1 缓存失效同步配置
     *
     * <p>原理：当某节点执行 {@code @CacheEvict} 时，除了清除自身 L1/L2 外，
     * 还会向 Redis 发布失效消息，集群内其他节点订阅后同步清除各自的 L1 缓存，
     * 避免本地缓存与 Redis 数据不一致导致的读旧数据问题。
     */
    @Setter
    @Getter
    public static class SyncConfig {

        /**
         * 是否启用多节点 L1 同步，默认 true
         */
        private boolean enabled = true;

        /**
         * Redis Pub/Sub 消息主题，默认 "mallcloud:cache:sync"
         * <p>多套环境部署时建议按环境区分，如 "dev:cache:sync"、"prod:cache:sync"。
         */
        private String topic = "mallcloud:cache:sync";

    }

    // ===================== 顶层 Getter/Setter =====================

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public L1Config getL1() {
        return l1;
    }

    public void setL1(L1Config l1) {
        this.l1 = l1;
    }

    public L2Config getL2() {
        return l2;
    }

    public void setL2(L2Config l2) {
        this.l2 = l2;
    }

    public SyncConfig getSync() {
        return sync;
    }

    public void setSync(SyncConfig sync) {
        this.sync = sync;
    }


    @Data
    public static class GlobalConfig {
        /**
         * Key 统一前缀，避免集群 Key 冲突
         */
        private String keyPrefix = "mall:cache:";
        /**
         * 是否自动暴露 Micrometer 指标
         */
        private boolean enableMetrics = true;
        /**
         * 默认降级策略：FAIL_OPEN(放行) / FAIL_CLOSED(拒绝)
         */
        private DegradationMode degradation = DegradationMode.FAIL_OPEN;
    }

    @Data
    public static class InstanceConfig {
        private boolean enabled = true;
        /**
         * L1 本地缓存最大容量
         */
        private long l1MaxSize = 10_000;
        /**
         * L1 存活时间（必须远小于 L2 TTL）
         */
        private Duration l1Ttl = Duration.ofSeconds(60);
        /**
         * L2 分布式缓存存活时间
         */
        private Duration l2Ttl = Duration.ofDays(30);
        /**
         * 集群 L1 失效广播频道
         */
        private String syncChannel = "cache:sync:default";
        /**
         * 本实例降级策略（覆盖全局）
         */
        private DegradationMode degradation;
    }
}
