package com.mallcloud.api.gateway.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 全局 JWT 检查过滤器（执行顺序优先级最高）
 * 功能：
 *   1. 检查请求头中是否携带 Authorization: Bearer <token>
 *   2. 验证 Token 是否有效（签名、过期时间）
 *   3. 从 Token 中提取用户 ID、角色等信息
 *   4. 将用户信息注入 HTTP Header，传递给下游微服务
 *   5. 如果以上 Token 检查不通过，直接返回 401 未授权
 * <p>
 * ⚠️ 注意：此过滤器会拦截所有请求，除了 /auth/**（已在路由中跳过）
 */

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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtTokenFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;


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

    @NullMarked
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();

        // 1. 跳过不需要认证的路径（如 /auth/login）
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange); // 直接放行
        }

        // 2. 获取 Authorization 头
        String authorization = request.getHeaders().getFirst("Authorization");

        // 3. 验证是否包含 Bearer Token
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            logger.warn("请求缺少有效的 Authorization 头：{}", path);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 4. 提取 Token 字符串
        String token = authorization.substring(7); // "Bearer " 长度为7

        try {
            // 5. 解析并校验 JWT（验证签名、过期）

            // 验证 JWT Token 的有效性
            boolean verified = JWTUtil.verify(token, jwtSecretKey.getBytes(StandardCharsets.UTF_8));
            if (!verified) {
                logger.warn("JWT Token 无效：{}", path);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            JWT jwt = JWTUtil.parseToken(token);
            Long userId = (Long) jwt.getPayload("user_id");
            String roles = (String) jwt.getPayload("roles"); // 如 "USER,ADMIN"
            String permissions = (String) jwt.getPayload("permissions"); // 如 "USER,ADMIN"

            // 6. 将用户信息注入 Header，传递给下游服务（重要！）
            // 下游服务可通过 Header 获取：X-User-ID, X-Roles
            request.mutate()
                    .header("X-User-ID", String.valueOf(userId))
                    .header("X-Roles", roles)
                    .header("X-Permissions", permissions)
                    .build();

            logger.info("✅ JWT 验证成功，用户ID: {}, 路径: {}", userId, path);
        } catch (Exception e) {
            logger.error("❌ JWT 校验失败，路径: {}, 错误: {}", path, e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete(); // 返回 401
        }

        // 8. 继续执行后续过滤器和目标服务
        return chain.filter(exchange);
    }
}

// Spring Cloud Gateway 全局过滤器 https://docs.springjava.cn/spring-cloud-gateway/reference/spring-cloud-gateway/global-filters.html
