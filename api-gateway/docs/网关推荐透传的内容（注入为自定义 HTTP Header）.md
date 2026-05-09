这是一个非常经典的微服务架构设计问题。网关鉴权后的**“请求头改造”**直接决定了下游服务的性能、安全性和开发体验。下面按你的疑问逐一拆解，并给出企业级最佳实践。

---

## 📦 一、推荐透传的内容（注入为自定义 HTTP Header）

网关完成 JWT 校验后，应提取 **业务真正需要的身份上下文**，转化为标准 HTTP Header 传递给下游：

| 透传内容           | 建议 Header 名                         | 说明                                               |
|:---------------|:------------------------------------|:-------------------------------------------------|
| **用户唯一标识**     | `Mall-User-Id` 或 `X-User-Id`        | 下游查询数据、记录操作人的核心字段                                |
| **租户标识**（SaaS） | `Mall-Tenant-Id`                    | 多租户数据隔离必传                                        |
| **角色列表**       | `Mall-Roles`                        | 逗号分隔，如 `USER,VIP,ADMIN`。供下游 `@PreAuthorize` 快速校验 |
| **权限标识**       | `Mall-Permissions`                  | 可选。若权限较多，建议只传核心权限或改用角色+资源级校验                     |
| **链路追踪ID**     | `Mall-Trace-Id`                     | 与日志系统联动，实现全链路追踪                                  |
| **客户端信息**      | `Mall-Client-Ip` / `Mall-Device-Id` | 风控、限流、审计使用（网关已做反向代理，原始 IP 需透传）                   |

> 💡 **命名建议**：避免使用 `X-` 前缀（IETF 已废弃），推荐业务前缀如 `Mall-` 或 `App-`，清晰且符合现代 HTTP 规范。

---

## 🚫 二、不推荐 / 严禁透传的内容

| 不推荐内容                                          | 原因                                  |
|:-----------------------------------------------|:------------------------------------|
| **原始 JWT Token** (`Authorization: Bearer xxx`) | 下游无需重复解析；完整 Token 落入业务日志会引发严重安全泄露风险 |
| **敏感声明**（密码哈希、内部状态位、密钥）                        | 违反最小权限原则，业务服务根本不需要                  |
| **时间类声明**（`exp`, `iat`, `nbf`）                 | 网关已负责过期校验，下游不应关心 Token 生命周期         |
| **签名/算法字段**（`alg`, `kid`, `sig`）               | 校验工作已在网关完成，下游持有无意义且增加解析负担           |
| **网关内部路由头**（如 `X-Forwarded-Uri` 等）             | 除非特定运维需求，否则污染业务服务请求上下文              |

---

## 🧠 三、为什么这么选择？（核心架构原则）

### 1. ⚡ 性能原则：避免重复计算

JWT 解析（Base64Url 解码 + HMAC-SHA256/RS256 验签）是 **CPU 密集型操作**。  
如果在 10 个微服务里都校验一次 Token，网关的鉴权优势就完全浪费了。**网关验一次，下游直接读 Header**，性能提升 5~10 倍。

### 2. 🔒 安全原则：最小权限 & 防泄露

- 业务服务日志如果打印了完整 Request，原始 Token 会明文落入磁盘/ELK。一旦日志库被拖，攻击者可伪造任意身份。
- 下游服务只接收 `User-Id` 和 `Roles`，即使被入侵，攻击者也无法直接拿到可复用的凭证。

### 3. 🔗 解耦原则：下游“零依赖”

- 业务服务**不需要引入 JWT 库**，**不需要配置公钥/密钥**，**不需要写鉴权过滤器**。
- 网关更换签名算法（如 HS256 → RS256）、引入双 Token 机制，业务服务**完全无感知**。

### 4. 🌐 信任边界：内网可信模型

此设计基于一个前提：**微服务之间运行在受控内网（VPC / Service Mesh / mTLS）**。  
网关是“安检口”，业务服务是“办事大厅”。安检口验证身份证后，大厅只需知道“此人是谁、有什么权限”，无需再次核对身份证原件。

---

## 四、抵达业务服务时，是否依旧携带 Token？

**✅ 主流企业级实践：不携带（推荐 Strip/Remove）**

### 为什么建议移除？

Spring Cloud Gateway 默认会**原样转发所有请求头**。如果不处理，下游会同时收到：

- `Authorization: Bearer eyJ...`（原始 Token）
- `Mall-User-Id: 1001`（解析结果）

这会导致：

1. 下游开发者可能误用 Token 再次解析，破坏架构约定
2. 日志审计时出现冗余且敏感的数据
3. 增加请求头体积，影响 HTTP/2 头部压缩效率

### 网关过滤器中如何移除？

```java
// 在你的 JwtTokenFilter 中，mutate 请求时显式移除 Authorization 头
ServerHttpRequest mutatedRequest = request.mutate()
                .header(HttpHeaders.AUTHORIZATION, (String) null) // 🔑 关键：移除原始 Token
                .header("Mall-User-Id", String.valueOf(userId))
                .header("Mall-Roles", roles)
                .header("Mall-Trace-Id", traceId)
                .build();
```

> ⚠️ 注意：`header(name, (String) null)` 是 Spring WebFlux 中移除 Header 的标准写法。

---

### 什么场景下需要保留 Token？（例外情况）

| 场景                           | 处理方式                                                        |
|:-----------------------------|:------------------------------------------------------------|
| **零信任架构**（服务间不互信）            | 下游必须重新验签，Token 需保留                                          |
| **业务服务需调用第三方 OAuth2 接口**     | Token 作为业务凭证透传（如微信登录、支付回调）                                  |
| **合规审计要求保留原始凭证**             | 网关校验后，将 Token 放入非业务头如 `Mall-Original-Token`，仅用于审计日志，业务逻辑不读取 |
| **跨网关传递**（Gateway → Gateway） | 需保留原 Token 供下一级网关校验                                         |

**常规电商/业务系统 95% 的场景都适用“网关验签 + 移除 Token + 透传解析头”模式。**

---

## 🛠️ 五、完整过滤器改造示例（整合你的需求）

```java

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthGlobalFilter implements GlobalFilter {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private static final List<String> WHITE_LIST = Arrays.asList("/auth/login", "/auth/refresh", "/actuator/**");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单直接放行
        if (WHITE_LIST.stream().anyMatch(p -> pathMatcher.match(p, path))) {
            return chain.filter(exchange);
        }

        // 2. 提取 Token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            // 3. 网关验签（纯 CPU 计算，无网络 IO）
            JWT jwt = JWTUtil.parseAndVerify(token, secretKey);

            Long userId = jwt.getPayload("user_id").asLong();
            String roles = jwt.getPayload("roles").asString();
            String traceId = exchange.getRequest().getHeaders().getFirst("Mall-Trace-Id");
            if (traceId == null) traceId = IdUtil.fastSimpleUUID();

            // 4. ✅ 改造请求头：移除 Token，注入解析后的业务头
            ServerHttpRequest newRequest = request.mutate()
                    .header(HttpHeaders.AUTHORIZATION, (String) null) // 移除原始 Token
                    .header("Mall-User-Id", String.valueOf(userId))
                    .header("Mall-Roles", roles != null ? roles : "GUEST")
                    .header("Mall-Trace-Id", traceId)
                    .header("Mall-Client-Ip", getRealIp(request)) // 透传真实 IP
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (ExpiredTokenException e) {
            return unauthorized(exchange, "Token expired");
        } catch (Exception e) {
            return unauthorized(exchange, "Token verification failed");
        }
    }

    private String getRealIp(ServerHttpRequest request) {
        // 处理 Nginx/SLB 反向代理的 IP 透传
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        return ip != null ? ip.split(",")[0].trim() : request.getRemoteAddress().getAddress().getHostAddress();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"code\":401,\"message\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
```

---

## ✅ 总结决策表

| 问题               | 推荐方案                                                            | 核心理由                     |
|:-----------------|:----------------------------------------------------------------|:-------------------------|
| **透传什么？**        | `Mall-User-Id`, `Mall-Roles`, `Mall-Trace-Id`, `Mall-Client-Ip` | 下游只需身份上下文，无需凭证原件         |
| **不透传什么？**       | 原始 `Authorization` 头、`exp/iat`、签名、敏感声明                          | 防日志泄露、降解析开销、最小权限         |
| **下游是否带 Token？** | **强烈建议移除**（`header(AUTH, null)`）                                | 网关已兜底安全，下游零 JWT 依赖，架构更清晰 |
| **安全前提**         | 微服务运行在**可信内网**（VPC/安全组/Service Mesh）                            | 业务服务“盲信”网关注入的 Header     |

按照这个模式设计，你的微服务将具备：**网关高性能拦截 + 业务服务极简开发 + 全链路可观测 + 安全边界清晰** 的企业级特质。

如果需要下游服务如何优雅读取这些 Header 并自动注入到 `SecurityContext` 的代码模板，我可以继续提供。