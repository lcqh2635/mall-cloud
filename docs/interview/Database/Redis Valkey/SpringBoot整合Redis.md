具体的使用请查看：缓存系统目录章节

Servlet环境

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

Redis连接配置

```yaml
spring:
  data:
    redis:
      host: 192.168.56.10
      password: 479368
```

配置Bean

```java
/**
 * 使用 @EnableCaching 开启 Spring Cache 缓存功能
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    // CacheManager 是在 redisTemplate 存放的基础上进一步封装，将key和value打包成 cache 缓存对象进行管理
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(factory);

        // 自定义缓存配置，将value序列化改成json，同时设置默认过期时间为2小时
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 设置 Cache 默认自动过期时间
                .entryTtl(Duration.ofHours(2))
                // 默认key以 String 序列化，value 以 JDK 的方式，下面改成 json 方式
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
        // 设置缓存默认前缀
        redisCacheConfiguration.prefixCacheNameWith(CacheConstant.DEFAULT_PREFIX);
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }
}
```

封装Redis工具类

```
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, CacheConstant.DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public void remove(String key) {
        redisTemplate.delete(key);
    }
}
```



WebFlux环境整合Redis

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

其他的都一样