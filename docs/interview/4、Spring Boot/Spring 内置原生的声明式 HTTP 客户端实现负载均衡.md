简短回答：**原生 `@HttpExchange` 本身不包含服务发现与负载均衡能力**。它只是一个声明式代理层，底层完全依赖你注入的
`RestClient` 或 `WebClient`。要实现服务发现与负载均衡，必须结合 **Spring Cloud LoadBalancer** 与对应的注册中心（Nacos /
Eureka / Consul 等）。

下面按 **原理 → 代码实现 → 你的架构落位建议 → 生产避坑** 为你完整梳理：

---

### 🔍 一、核心协作原理

```
@HttpExchange 接口
       ↓ (代理调用)
RestClient / WebClient
       ↓ (拦截器/Filter)
Spring Cloud LoadBalancer (本地负载均衡器)
       ↓ (查询实例列表)
DiscoveryClient (服务发现客户端)
       ↓
注册中心 (Nacos/Eureka/Consul) → 返回健康实例列表 → 按策略(轮询/权重/区域)选择节点 → 发起 HTTP 请求
```

> 💡 `@LoadBalanced` 注解的本质：在 `RestClient`/`WebClient` 构建时，自动注入一个拦截器，将请求的 `host` 替换为服务发现获取到的具体
`IP:Port`。

---

### 🛠 二、完整配置示例（以 `RestClient` 为例）

#### 1. 依赖准备（消费方 `order-service` 的 `pom.xml`）

```xml
<!-- 服务发现（以 Nacos 为例） -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
        <!-- 负载均衡核心 -->
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
        <!-- 你的 user-api 模块 -->
<dependency>
<groupId>com.example</groupId>
<artifactId>user-api</artifactId>
</dependency>
```

#### 2. `user-api` 模块提供自动装配配置

```java

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestClient.class)
@ConditionalOnBean(LoadBalancerClient.class) // 仅当消费方引入 LB 依赖时才生效
public class UserHttpClientAutoConfiguration {

    // ① 声明一个被 @LoadBalanced 标记的 Builder
    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean(name = "loadBalancedRestClientBuilder")
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // ② 基于该 Builder 创建声明式客户端
    @Bean
    @ConditionalOnMissingBean(UserHttpClient.class)
    public UserHttpClient userHttpClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
            @Value("${user.client.service-id:user-service}") String serviceId) {

        RestClient restClient = builder
                .baseUrl("http://" + serviceId) // ⚠️ 必须用服务名，不能写 IP:Port
                .defaultHeader("Accept", "application/json")
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(UserHttpClient.class);
    }
}
```

> 📌 自动装配入口别忘了注册：  
> `user-api/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`  
> 内容：`com.example.user.api.config.UserHttpClientAutoConfiguration`

#### 3. 消费方只需配置服务名

```yaml
# application.yml
user:
  client:
    service-id: user-service  # 对应 Nacos/Eureka 中注册的服务名
```

直接注入使用，无需关心实例 IP：

```java

@Service
public class OrderService {
    private final UserHttpClient userClient;

    public OrderService(UserHttpClient userClient) {
        this.userClient = userClient;
    }

    public void createOrder(String userId) {
        UserDto user = userClient.getUser(userId); // 自动 LB + 服务发现
    }
}
```

---

### 🏗 三、在你的架构中如何优雅落地？

| 模块         | 职责与依赖建议                                                                                               |
|------------|-------------------------------------------------------------------------------------------------------|
| `user-api` | 仅依赖 `spring-web` + `spring-cloud-loadbalancer(可选/optional)`。提供 `@ConditionalOnBean` 配置类，**不强制**绑定 LB。 |
| `user-biz` | 纯 Provider，**不引入**任何 LB 或客户端配置。只负责 `@RestController` 暴露 `/api/v1/users`。                              |
| 调用方服务      | 引入 `user-api` + `spring-cloud-starter-loadbalancer` + 注册中心客户端。自动激活 LB 客户端 Bean。                       |

✅ **优势**：`user-api` 保持轻量（<500KB），消费方可按需选择是否启用服务发现。单体/测试环境可不引 LB 依赖，直接配
`baseUrl: http://localhost:8081` 覆盖。

---

### ⚠️ 四、生产环境关键避坑指南

| 陷阱场景              | 正确做法                                                                                               |
|-------------------|----------------------------------------------------------------------------------------------------|
| `baseUrl` 写了具体 IP | 必须写 `http://服务名`。写 IP 会绕过 LoadBalancer 拦截器，导致 LB 失效。                                               |
| 多实例健康检查未生效        | LoadBalancer 默认依赖注册中心健康状态。需在注册中心开启健康检查（如 Nacos 心跳/探针）。                                             |
| 默认轮询不满足业务需求       | 可通过 `spring.cloud.loadbalancer.configurations` 配置权重、区域优先或自定义 `ReactorServiceInstanceLoadBalancer`。 |
| 客户端重试与 LB 冲突      | LB 本身**不包含重试**。需额外集成 Resilience4j 或 Spring Retry，在 `RestClient` 中配置 `defaultStatusHandler` + 重试策略。 |
| 虚拟线程环境下 LB 性能     | `RestClient + LoadBalancer` 完全兼容虚拟线程。确保未混用阻塞式连接池（推荐 `JdkClientHttpRequestFactory`）。                |

---

### 🔄 五、与 Spring Cloud OpenFeign 的对比

| 维度         | `@HttpExchange` + `RestClient`    | Spring Cloud OpenFeign |
|------------|-----------------------------------|------------------------|
| 服务发现/LB 集成 | 需手动配 `@LoadBalanced` + LB Starter | 开箱即用（默认集成）             |
| 底层协议       | `RestClient`/`WebClient`（官方主力）    | 旧版 HTTP 客户端（逐步迁移中）     |
| AOT/原生镜像兼容 | ✅ 官方深度优化                          | ⚠️ 需额外配置反射/代理提示        |
| 学习成本       | 低（标准 Spring 风格）                   | 中（Feign 专属注解/配置体系）     |

> 📌 **结论**：如果你的技术栈已转向 Spring Boot 3.2+/4.x，**强烈建议使用 `@HttpExchange + @LoadBalanced`**。Spring Cloud
> 官方已明确 OpenFeign 进入维护期，未来资源将倾斜至原生方案。

---

### 📦 需要我进一步提供什么？

1. `Resilience4j` 重试 + 熔断 与 `@HttpExchange` 的整合模板
2. Nacos 权重路由 / 同机房优先 的 LB 自定义配置
3. 虚拟线程环境下 `JdkClientHttpRequestFactory` 的压测调优参数
4. 完整 `user-api` Starter 工程结构（含 `optional` 依赖隔离 + 自动装配）

告诉我你的具体场景（注册中心类型、是否需要熔断重试、是否已开虚拟线程），我可直接输出生产级代码。