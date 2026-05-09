package com.mallcloud.commons.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT 配置属性
 *
 * <p>
 * 用于统一管理：
 * 1. JWT 基础配置
 * 2. Token 配置
 * 3. 安全配置
 * 4. Gateway Header 配置
 * 5. 白名单配置
 *
 * <p>
 * 推荐配合：
 * - application.yml
 * - Nacos
 * - Apollo
 * <p>
 * 动态配置管理。
 *
 * @author mallcloud
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    // =========================================================
    // 基础配置
    // =========================================================

    /**
     * 是否启用 JWT 认证
     */
    private boolean enabled = true;

    /**
     * JWT 签发者（Issuer）
     * <p>
     * 用于标识 Token 的签发系统。
     */
    private String issuer;

    /**
     * JWT 接收方（Audience）
     * <p>
     * 用于标识 Token 的目标客户端。
     */
    private String audience;

    // =========================================================
    // Token 配置
    // =========================================================

    /**
     * Access Token 有效期（秒）
     * <p>
     * 推荐：
     * 30 分钟 ~ 2 小时
     */
    private long accessTokenExpire = 7200;

    /**
     * Refresh Token 有效期（秒）
     * <p>
     * 推荐：
     * 7 天 ~ 30 天
     */
    private long refreshTokenExpire = 604800;

    /**
     * Token 前缀
     * <p>
     * 默认：
     * Bearer
     */
    private String tokenPrefix = "Bearer";

    /**
     * 请求头名称
     * <p>
     * 默认：
     * Authorization
     */
    private String headerName = "Authorization";

    /**
     * 是否启用 RefreshToken
     */
    private boolean enableRefreshToken = true;

    // =========================================================
    // 签名配置
    // =========================================================

    /**
     * JWT 签名算法
     * <p>
     * 可选：
     * HS256
     * HS384
     * HS512
     * RS256
     */
    private String algorithm = "HS256";

    /**
     * HMAC 对称加密密钥
     * <p>
     * HS256 时使用。
     */
    private String secretKey;

    /**
     * RSA 公钥
     * <p>
     * RS256 时使用。
     */
    private String publicKey;

    /**
     * RSA 私钥
     * <p>
     * RS256 时使用。
     */
    private String privateKey;

    // =========================================================
    // 安全配置
    // =========================================================

    /**
     * 时钟偏移容忍时间（秒）
     * <p>
     * 用于解决：
     * - 服务器时间不同步
     * - Docker 容器时间偏差
     */
    private long clockSkew = 5;

    /**
     * 是否启用 Token 黑名单
     */
    private boolean enableBlacklist = true;

    /**
     * Redis 黑名单 Key 前缀
     */
    private String blacklistPrefix = "jwt:blacklist:";

    /**
     * 是否允许多端登录
     * <p>
     * true:
     * 一个账号允许多个 Token
     * <p>
     * false:
     * 新登录会踢掉旧 Token
     */
    private boolean allowMultiLogin = true;

    // =========================================================
    // Gateway Header 配置
    // =========================================================

    /**
     * 用户ID透传 Header
     */
    private String userIdHeader = "X-User-Id";

    /**
     * 用户名透传 Header
     */
    private String usernameHeader = "X-Username";

    /**
     * TraceId 透传 Header
     */
    private String traceIdHeader = "X-Trace-Id";

    // =========================================================
    // 白名单配置
    // =========================================================

    /**
     * JWT 白名单路径
     * <p>
     * 白名单路径不会进行 JWT 校验。
     */
    private List<String> ignoreUrls = new ArrayList<>();
}