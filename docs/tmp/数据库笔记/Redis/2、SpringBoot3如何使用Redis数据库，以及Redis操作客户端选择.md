非常棒的问题！在 **Spring Boot 3** 项目中集成和使用 Redis 是后端开发中的常见需求。下面我将从 **使用方式、客户端选择、区别对比、推荐方案** 四个方面为你详细讲解。

---

## 一、Spring Boot 3 中如何使用 Redis？

### 1. 添加依赖（Maven）

Spring Boot 3 默认使用 **Lettuce** 作为 Redis 客户端，推荐使用 `spring-boot-starter-data-redis`。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- 可选：如果你使用 JSON 序列化 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

> ⚠️ 注意：Spring Boot 3 要求 Java 17+，且不再支持 Jedis 作为默认客户端（但仍可手动引入）。

---

### 2. 配置 `application.yml`

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: yourpassword  # 如果有
    database: 0
    timeout: 5s
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms  # 永久等待
```

---

### 3. 使用 `RedisTemplate` 或 `StringRedisTemplate`

#### ✅ 推荐：注入 `RedisTemplate` 进行操作

```java
@Service
public class UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void setUser(Long userId, User user) {
        redisTemplate.opsForValue().set("user:" + userId, user, Duration.ofMinutes(30));
    }

    public User getUser(Long userId) {
        return (User) redisTemplate.opsForValue().get("user:" + userId);
    }

    public void deleteCache(Long userId) {
        redisTemplate.delete("user:" + userId);
    }
}
```

> 🔍 提示：`RedisTemplate` 默认使用 **JDK 序列化**，存储的是二进制格式，不易读。建议自定义序列化方式。

---

### 4. 自定义序列化（推荐使用 JSON）

```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用 Jackson2JsonRedisSerializer 序列化对象
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LazyLoadingConfigurer.construct(om.getPolymorphicTypeValidator()), ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(om);

        // 设置 Key 和 Value 的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

这样存储的 Redis 数据是 **可读的 JSON 格式**，便于调试。

---

### 5. 使用 `@Cacheable` 注解（声明式缓存）

```java
@Cacheable(value = "users", key = "#id")
public User getUserById(Long id) {
    // 模拟数据库查询
    return userRepository.findById(id);
}

@CacheEvict(value = "users", key = "#id")
public void updateUser(User user) {
    userRepository.save(user);
}
```

> 需要开启缓存支持：
> ```java
> @SpringBootApplication
> @EnableCaching
> public class Application { ... }
> ```

---

## 二、Redis 操作客户端有哪些？区别是什么？

| 客户端 | 简介 | 特点 | 适用场景 |
|-------|------|------|----------|
| **Lettuce** | 基于 Netty 的高性能客户端，Spring Boot 2.x/3.x 默认 | 线程安全、支持同步/异步/响应式、连接共享、资源消耗低 | ✅ 推荐，尤其在 Spring Boot 项目中 |
| **Jedis** | 老牌客户端，简单直接 | 轻量、API 简单，但非线程安全（需连接池） | 适合简单项目或学习，不推荐在 Spring Boot 3 中作为主客户端 |
| **Redisson** | 高级客户端，封装了分布式对象、锁、队列等 | 提供分布式锁、分布式集合、延迟队列等高级功能 | 复杂分布式场景（如分布式限流、分布式任务调度） |

---

### 详细对比（Lettuce vs Jedis vs Redisson）

| 对比项 | Lettuce | Jedis | Redisson |
|--------|--------|--------|----------|
| **线程安全** | ✅ 是（单连接可多线程共享） | ❌ 否（每个线程需独立连接或使用连接池） | ✅ 是 |
| **连接模型** | 基于 Netty，异步非阻塞 | 同步阻塞 I/O | 基于 Netty，异步 |
| **资源消耗** | 低（连接可共享） | 高（频繁创建连接） | 中等 |
| **功能丰富度** | 基础操作 + Stream 等 | 基础操作 | ⭐⭐⭐⭐⭐（分布式对象、锁、服务等） |
| **Spring Boot 支持** | ✅ 官方默认集成 | ⚠️ 需手动配置 | ✅ 可集成，但非默认 |
| **学习成本** | 低 | 低 | 中高 |
| **性能** | 高（尤其高并发） | 高（但连接管理差） | 中等（功能多，有开销） |

---

## 三、如何选择？推荐方案

### ✅ 推荐组合（Spring Boot 3 项目）：

| 场景 | 推荐客户端 | 说明 |
|------|------------|------|
| **普通缓存、数据存取** | **Lettuce + RedisTemplate** | Spring Boot 默认，性能好，线程安全，易于集成 |
| **需要分布式锁、分布式集合、延迟队列** | **Redisson** | 提供 `RLock`、`RBlockingQueue`、`RDelayedQueue` 等高级功能 |
| **简单脚本或测试** | Jedis（仅学习） | 不推荐用于生产，除非遗留系统 |

---

### 🎯 推荐使用策略：

1. **90% 场景：使用 Lettuce**
    - 缓存、计数器、会话存储、简单消息队列（List/Stream）
    - 配合 `RedisTemplate` 或 `StringRedisTemplate`

2. **10% 复杂场景：引入 Redisson**
    - 分布式锁（Redlock）
    - 分布式限流（RateLimiter）
    - 延迟任务（RDelayedQueue）
    - 分布式集合（RList、RMap）

> 💡 可以同时引入 Lettuce 和 Redisson，各司其职。

---

## 四、Redisson 使用示例（分布式锁）

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.24.1</version>
</dependency>
```

```java
@Autowired
private RedissonClient redissonClient;

public void deductStock() {
    RLock lock = redissonClient.getLock("stock:lock");
    try {
        if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
            // 执行扣库存逻辑
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } finally {
        lock.unlock();
    }
}
```

---

## 五、总结：Spring Boot 3 中 Redis 使用建议

| 项目 | 建议 |
|------|------|
| **客户端选择** | 优先使用 **Lettuce**，复杂分布式功能用 **Redisson** |
| **序列化** | 使用 `Jackson2JsonRedisSerializer`，避免 JDK 序列化乱码 |
| **连接池** | Lettuce 默认支持连接池（通过 `LettucePoolingConfiguration`） |
| **缓存注解** | 使用 `@Cacheable`、`@CacheEvict` 简化代码 |
| **高可用** | 配合 Redis 哨兵或集群使用，配置多个节点 |
| **监控** | 可集成 `Spring Boot Actuator` + `RedisHealthIndicator` |

---

## 下一步学习建议

你可以继续深入：

- Redis 持久化（RDB/AOF）配置与原理
- Redis 哨兵（Sentinel）和集群（Cluster）搭建
- 缓存穿透、击穿、雪崩的解决方案（布隆过滤器、空值缓存、互斥锁）
- Redis Stream 实现消息队列
- Redis 与 Spring Cache 的深度整合

如果你需要，我可以为你提供：

- Spring Boot 3 + Redis + Redisson 完整示例项目结构
- Redis 集群配置教程
- 分布式锁实战代码
- Redis 性能调优建议

欢迎继续提问！🚀