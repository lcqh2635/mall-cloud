package com.mallcloud.api.gateway.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import cn.hutool.jwt.JWTUtil;
import com.mallcloud.api.gateway.exception.ExpiredTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import java.util.UUID;

/**
 * JWT 令牌全局过滤器
 * <p>
 * 核心职责：
 * 1. 拦截所有非白名单请求，校验 JWT Token 有效性
 * 2. 解析 Token 中的用户信息，注入到请求头传递给下游服务
 * 3. 统一处理认证失败场景，返回标准 JSON 响应
 *
 * @author mallcloud
 * &#064;date  2026-05-09
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，确保在其他过滤器之前执行
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {

    // 白名单建议改成配置化
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

    @NullMarked
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
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod().name())) {
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

            String traceId = UUID.randomUUID().toString();

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
                    .header("X-Trace-Id", traceId)
                    .header(HEADER_ROLES, roles != null ? roles : "")
                    .header(HEADER_PERMISSIONS, permissions != null ? permissions : "")
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            log.info("JWT 校验成功: userId={}, path={}, roles={}", userId, path, roles);

            // ========== 7. 继续执行过滤器链 ==========
            return chain.filter(mutatedExchange);

        } catch (ExpiredTokenException | JWTException e) {
            // Token 过期（如果 JWTUtil 支持抛出此异常）
            log.info("JWT Token 已过期: path={}, exp={}", path, e.getMessage());
            return unauthorized(response, "TOKEN_EXPIRED", "登录已过期，请重新登录");
        } catch (SecurityException e) {
            // JWT 库抛出的安全异常（如签名算法不匹配、密钥错误等）
            log.error("JWT 安全校验异常: path={}, error={}", path, e.getMessage());
            return unauthorized(response, "TOKEN_SECURITY_ERROR", "Token 安全校验失败");
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
        switch (userIdObj) {
            case null -> {
                return null;
            }
            case Number number -> {
                return number.longValue();
            }

            // 尝试字符串解析（兼容某些库的行为）
            case String s -> {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException e) {
                    log.warn("user_id 无法解析为 Long: value={}", userIdObj);
                    return null;
                }
            }
            default -> {
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

// Spring Cloud Gateway 全局过滤器 https://docs.springjava.cn/spring-cloud-gateway/reference/spring-cloud-gateway/global-filters.html

