# 🔐 JwtTokenFilter 代码评审与优化方案

你的过滤器整体思路正确，但存在**几个关键问题**可能导致功能失效或安全隐患。下面逐一分析并给出完善示例。

---

## 🔴 关键问题分析

### 1️⃣ 请求突变未生效（最严重❗）

```java
// ❌ 错误写法：突变后未设置回 exchange
request.mutate()
    .header("X-User-ID", String.valueOf(userId))
    .build(); // 创建的新请求对象被丢弃了！

// ✅ 正确写法：将突变后的请求设置回 exchange
ServerHttpRequest newRequest = request.mutate()
    .header("X-User-ID", String.valueOf(userId))
    .build();
return chain.filter(exchange.mutate().request(newRequest).build());
```

**影响**：下游服务根本收不到 `X-User-ID` 等 header，导致认证信息丢失！

---

### 2️⃣ 密钥硬编码（高危安全漏洞）

```java
// ❌ 危险：密钥写死在代码中，泄露后所有 Token 可伪造
boolean verified = JWTUtil.verify(token, "123456".getBytes());
```

**✅ 修复**：从配置中心读取，支持动态刷新：
```java
@Value("${jwt.secret-key}")
private String jwtSecretKey;

// 使用时
boolean verified = JWTUtil.verify(token, jwtSecretKey.getBytes(StandardCharsets.UTF_8));
```

---

### 3️⃣ 响应体缺失，前端无法友好提示

```java
// ❌ 只设置状态码，无响应体
response.setStatusCode(HttpStatus.UNAUTHORIZED);
return response.setComplete();
```

**✅ 修复**：返回标准 JSON 错误响应：
```java
response.setStatusCode(HttpStatus.UNAUTHORIZED);
response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
String errorMsg = "{\"code\":401,\"message\":\"Token 无效或已过期\"}";
DataBuffer buffer = response.bufferFactory().wrap(errorMsg.getBytes(StandardCharsets.UTF_8));
return response.writeWith(Mono.just(buffer));
```

---

### 4️⃣ 异常处理过于粗糙

| 异常类型 | 当前处理 | 建议处理 |
|---------|---------|---------|
| Token 过期 | 401 | 401 + 特定错误码 `TOKEN_EXPIRED` |
| 签名失败 | 401 | 401 + `TOKEN_INVALID` |
| 格式错误 | 401 | **400** + `TOKEN_MALFORMED` |
| 解析异常 | 401 | **500** + 记录日志（可能是服务端问题） |

---

### 5️⃣ 白名单匹配不精确

```java
// ❌ startsWith 可能误放行：/auth-test 也会被放过
if (path.startsWith("/auth/")) { ... }
```

**✅ 修复**：使用精确匹配或 AntPathMatcher：
```java
// 方案A：精确前缀匹配（推荐）
if (path.matches("^/auth/(login|register|refresh)$")) { ... }

// 方案B：配置化白名单 + Ant 路径匹配
private static final List<String> WHITE_LIST = Arrays.asList(
    "/auth/login", "/auth/register", "/auth/refresh", "/public/**"
);
private final AntPathMatcher pathMatcher = new AntPathMatcher();

private boolean isWhiteListed(String path) {
    return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
}
```

---

### 6️⃣ 类型转换不安全

```java
// ❌ 直接强转，payload 类型不符时抛 ClassCastException
Long userId = (Long) jwt.getPayload("user_id");
```

**✅ 修复**：安全转换 + 空值检查：
```java
Object userIdObj = jwt.getPayload("user_id");
if (userIdObj instanceof Number) {
    Long userId = ((Number) userIdObj).longValue();
    // ...
} else {
    logger.error("Token 中 user_id 类型错误: {}", userIdObj);
    return unauthorized(response, "TOKEN_PAYLOAD_INVALID");
}
```

---

## ✅ 完善后的代码示例（含详细中文注释）

```java
package com.mallcloud.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 令牌全局过滤器
 * <p>
 * 核心职责：
 * 1. 拦截所有非白名单请求，校验 JWT Token 有效性
 * 2. 解析 Token 中的用户信息，注入到请求头传递给下游服务
 * 3. 统一处理认证失败场景，返回标准 JSON 响应
 *
 * @author mallcloud
 * @date 2026-05-09
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，确保在其他过滤器之前执行
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {

    /**
     * 认证放行路径白名单（支持 Ant 风格通配符）
     * <p>
     * 以下路径跳过 JWT 校验，通常包括：
     * - 认证相关接口：登录、注册、刷新令牌
     * - 公共接口：健康检查、静态资源
     * - 第三方回调：支付回调、短信回调
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/auth/logout",          // 登出可能也需要验证，按需调整
            "/actuator/**",          // 监控端点
            "/public/**",            // 公共资源
            "/v3/api-docs/**",       // Swagger 文档
            "/swagger-ui/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * JWT 密钥（生产环境务必从配置中心读取，禁止硬编码！）
     * <p>
     * 建议配置方式：
     * 1. application.yml: jwt.secret-key=${JWT_SECRET:default-key-change-me}
     * 2. 结合 Nacos/Apollo 实现密钥动态刷新
     * 3. 使用 JWK（JSON Web Key）支持密钥轮换
     */
    @Value("${jwt.secret-key:default-secret-key-change-me-in-production}")
    private String jwtSecretKey;

    /**
     * 自定义响应头：传递用户信息给下游服务
     * <p>
     * 命名规范：使用 X- 前缀表示自定义头，避免与标准头冲突
     */
    private static final String HEADER_USER_ID = "X-User-ID";
    private static final String HEADER_ROLES = "X-Roles";
    private static final String HEADER_PERMISSIONS = "X-Permissions";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();

        // ========== 1. 白名单放行 ==========
        if (isWhiteListed(path)) {
            log.debug("白名单路径放行: {}", path);
            return chain.filter(exchange);
        }

        // ========== 2. 处理 CORS 预检请求 ==========
        // OPTIONS 请求是浏览器发起的预检请求，不应进行认证校验
        if (HttpHeaders.OPTIONS.equalsIgnoreCase(request.getMethodValue())) {
            return chain.filter(exchange);
        }

        // ========== 3. 提取并校验 Authorization 头 ==========
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("请求缺少有效 Authorization 头: path={}, method={}", path, request.getMethod());
            return unauthorized(response, "TOKEN_MISSING", "请求头缺少 Bearer Token");
        }

        // 提取 Token 字符串（移除 "Bearer " 前缀）
        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            log.warn("Token 内容为空: path={}", path);
            return unauthorized(response, "TOKEN_EMPTY", "Token 不能为空");
        }

        try {
            // ========== 4. 验证 JWT 签名 ==========
            // 注意：JWTUtil 应使用 HMAC-SHA256 等强算法，避免使用 HS256 以外的弱算法
            boolean verified = JWTUtil.verify(token, jwtSecretKey.getBytes(StandardCharsets.UTF_8));
            if (!verified) {
                log.warn("JWT 签名验证失败: path={}, token-prefix={}", path, token.substring(0, Math.min(20, token.length())));
                return unauthorized(response, "TOKEN_INVALID", "Token 签名验证失败");
            }

            // ========== 5. 解析 Token 载荷 ==========
            JWT jwt = JWTUtil.parseToken(token);

            // 安全提取用户信息（避免直接强转导致 ClassCastException）
            Long userId = extractUserId(jwt);
            if (userId == null) {
                log.error("Token 中缺少必需字段 user_id: path={}", path);
                return unauthorized(response, "TOKEN_PAYLOAD_INVALID", "Token 缺少用户标识");
            }

            String roles = extractStringPayload(jwt, "roles");
            String permissions = extractStringPayload(jwt, "permissions");

            // ========== 6. 将用户信息注入请求头（关键步骤！） ==========
            // ⚠️ ServerHttpRequest 是不可变的，必须通过 mutate() 创建新实例，
            // 并通过 exchange.mutate().request(newRequest).build() 设置回 exchange
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(HEADER_USER_ID, String.valueOf(userId))
                    .header(HEADER_ROLES, roles != null ? roles : "")
                    .header(HEADER_PERMISSIONS, permissions != null ? permissions : "")
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            log.info("JWT 校验成功: userId={}, path={}, roles={}", userId, path, roles);

            // ========== 7. 继续执行过滤器链 ==========
            return chain.filter(mutatedExchange);

        } catch (SecurityException e) {
            // JWT 库抛出的安全异常（如签名算法不匹配、密钥错误等）
            log.error("JWT 安全校验异常: path={}, error={}", path, e.getMessage());
            return unauthorized(response, "TOKEN_SECURITY_ERROR", "Token 安全校验失败");
        } catch (ExpiredTokenException e) {
            // Token 过期（如果 JWTUtil 支持抛出此异常）
            log.info("JWT Token 已过期: path={}, exp={}", path, e.getExpiredTime());
            return unauthorized(response, "TOKEN_EXPIRED", "登录已过期，请重新登录");
        } catch (Exception e) {
            // 其他未知异常（避免泄露敏感信息）
            log.error("JWT 处理未知异常: path={}, error={}", path, e.getMessage(), e);
            // 生产环境建议返回通用错误，避免暴露内部细节
            return unauthorized(response, "TOKEN_PROCESS_ERROR", "认证服务异常");
        }
    }

    /**
     * 判断路径是否在白名单中（支持 Ant 风格通配符）
     *
     * @param path 请求路径
     * @return 是否放行
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 安全提取 user_id（支持 Number 类型兼容）
     * <p>
     * JWT payload 中的数字可能被解析为 Integer/Long/Double，
     * 统一转换为 Long 避免类型转换异常
     */
    private Long extractUserId(JWT jwt) {
        Object userIdObj = jwt.getPayload("user_id");
        if (userIdObj == null) {
            return null;
        }
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        // 尝试字符串解析（兼容某些库的行为）
        if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                log.warn("user_id 无法解析为 Long: value={}", userIdObj);
                return null;
            }
        }
        return null;
    }

    /**
     * 安全提取字符串类型载荷
     *
     * @param jwt JWT 对象
     * @param key 载荷键名
     * @return 字符串值或 null
     */
    private String extractStringPayload(JWT jwt, String key) {
        Object value = jwt.getPayload(key);
        if (value == null) {
            return null;
        }
        return value.toString(); // toString() 比强转更安全
    }

    /**
     * 统一返回 401 认证失败响应（JSON 格式）
     *
     * @param response 响应对象
     * @param code     业务错误码
     * @param message  用户友好提示
     * @return Mono<Void> 响应完成信号
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String code, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().add("X-Error-Code", code); // 便于前端区分错误类型

        // 构建标准错误响应体
        String jsonResponse = String.format(
                "{\"success\":false,\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}",
                code, message, System.currentTimeMillis()
        );

        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
```

---

## 🔧 配套配置建议（application.yml）

```yaml
jwt:
  secret-key: ${JWT_SECRET:change-me-in-production}  # 生产环境务必通过环境变量设置！
  # 可选：Token 过期时间（用于生成时的参考）
  expiration: 7200  # 2 小时
  # 可选：刷新令牌有效期
  refresh-expiration: 604800  # 7 天

# 网关全局超时设置（避免下游服务慢导致网关连接堆积）
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 3000    # 连接超时 3 秒
        response-timeout: 30s    # 响应超时 30 秒
```

---

## 🧪 测试建议

```bash
# 1. 测试白名单放行（应直接通过）
curl -X POST http://gateway/auth/login -d '{"username":"test"}'

# 2. 测试无 Token 请求（应返回 401 + JSON）
curl -v http://gateway/user/profile

# 3. 测试无效 Token（应返回 TOKEN_INVALID）
curl -v http://gateway/user/profile \
  -H "Authorization: Bearer invalid.token.here"

# 4. 测试有效 Token（应透传 X-User-ID 到下游）
curl -v http://gateway/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxx" \
  | jq '.headers'  # 观察下游收到的请求头

# 5. 测试 OPTIONS 预检请求（应放行）
curl -X OPTIONS http://gateway/user/profile \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST"
```

---

## 📋 优化清单总结

| 优化项 | 优先级 | 说明 |
|--------|--------|------|
| ✅ 修复 request.mutate() 未生效问题 | 🔴 必须 | 否则下游收不到用户信息 |
| ✅ 密钥从配置读取，禁止硬编码 | 🔴 必须 | 安全底线 |
| ✅ 返回标准 JSON 错误响应 | 🟡 建议 | 提升前端体验 |
| ✅ 精细化异常处理 + 错误码 | 🟡 建议 | 便于问题排查 |
| ✅ 白名单使用 AntPathMatcher | 🟡 建议 | 匹配更精确 |
| ✅ 安全提取 payload 字段 | 🟢 推荐 | 避免类型转换异常 |
| ✅ 放行 OPTIONS 预检请求 | 🟢 推荐 | 支持 CORS 正常流程 |
| 🔜 增加 Token 刷新机制 | ⚪ 后续 | 提升用户体验 |
| 🔜 集成 Redis 实现 Token 黑名单 | ⚪ 后续 | 支持主动登出 |

如果还有具体业务场景（如多租户、灰度发布、Token 刷新等），可以告诉我，我帮你进一步扩展方案 🚀