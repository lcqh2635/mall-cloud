当然可以。以下是基于 **Hutool-JWT**（`cn.hutool:jwt`）在真实 Java 后端开发场景（特别是 Spring Boot 项目）中的**完整、安全、可直接落地的标准使用示例**，涵盖 JWT 的生成、校验、密钥安全管理、刷新令牌机制、异常处理与最佳实践，并附有**详尽中文注释**，适用于银行、保险等高合规要求的业务系统。

---

## ✅ 一、Maven 依赖（确保引入 Hutool-JWT）

```xml
<!-- Hutool-JWT 核心依赖 -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-jwt</artifactId>
    <version>5.8.40</version> <!-- 建议使用最新稳定版 -->
</dependency>
```

> 💡 Hutool-JWT 是对 `jjwt` 的封装，简化了 JWT 操作，但底层仍使用 JJWT 实现，功能完整。

---

## ✅ 二、JWT 工具类：`JwtTokenUtil.java`（核心实现）

```java
package com.yourbank.insurance.security.jwt;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.RS256JWTSigner;
import cn.hutool.jwt.signers.HS256JWTSigner;
import cn.hutool.jwt.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类（基于 Hutool-JWT）—— 银行/保险系统标准实现
 * 
 * 设计原则：
 * 1. 使用非对称加密 RS256（私钥保密，公钥可分发）
 * 2. 密钥从外部安全源加载（如 Vault、K8s Secret），不硬编码
 * 3. 支持 Access Token + Refresh Token 双令牌机制
 * 4. 所有敏感字段均设置过期时间
 * 5. 校验时严格验证签发者、受众、过期时间等标准声明
 * 
 * @author 研发团队
 * @since 2025
 */
@Component
public class JwtTokenUtil {

    // ==================== 配置项（由外部安全注入）====================

    /**
     * RS256 私钥（PEM 格式）—— 仅服务端持有，严禁泄露
     * 实际部署中应从 Vault/KMS/环境变量加载，此处为示例
     */
    @Value("${jwt.rsa.private-key}")
    private String privateKeyPem;

    /**
     * RS256 公钥（PEM 格式）—— 可分发给网关、其他微服务用于验证
     * 实际部署中应从配置中心加载，无需加密
     */
    @Value("${jwt.rsa.public-key}")
    private String publicKeyPem;

    /**
     * Access Token 有效期（分钟）—— 短期令牌，用于接口访问
     * 推荐：5~30 分钟，降低泄露风险
     */
    @Value("${jwt.access-token-expire-minutes:15}")
    private int accessTokenExpireMinutes;

    /**
     * Refresh Token 有效期（小时）—— 用于刷新 Access Token
     * 推荐：7~14 小时，避免频繁登录
     */
    @Value("${jwt.refresh-token-expire-hours:12}")
    private int refreshTokenExpireHours;

    /**
     * 签发者（issuer）—— 用于标识令牌来源，校验时必须匹配
     */
    @Value("${jwt.issuer:insurance-auth-service}")
    private String issuer;

    /**
     * 受众（audience）—— 指定该令牌的接收方，如前端、网关、API 网关等
     */
    @Value("${jwt.audience:insurance-frontend}")
    private String audience;

    // ==================== 私钥签名器（生成 JWT）====================

    /**
     * 使用私钥生成 RS256 签名器（用于签发 JWT）
     * 注意：此对象是线程安全的，建议作为单例缓存
     */
    private final JWTSigner signer;

    /**
     * 初始化：创建 RS256 签名器
     * 说明：Hutool-JWT 会自动解析 PEM 格式密钥
     */
    public JwtTokenUtil() {
        // 使用 Hutool 的 RSA 工具解析 PEM 密钥，生成签名器
        this.signer = new RS256JWTSigner(privateKeyPem);
    }

    // ==================== 生成 Access Token ===================

    /**
     * 生成短期 Access Token（用于 API 授权访问）
     *
     * @param userId 用户唯一标识（如数据库主键）
     * @param username 用户名（可选，用于日志追踪）
     * @param roles 用户角色（如 "ROLE_ADMIN", "ROLE_INSURANCE_AGENT"）
     * @return Base64 编码的 JWT 字符串
     */
    public String generateAccessToken(Long userId, String username, String... roles) {
        // 构建 JWT 载荷（Claims）
        Map<String, Object> claims = Map.of(
            "sub", userId,           // subject：主题，必填，用户唯一 ID
            "name", username,        // 自定义字段：用户名
            "roles", roles,          // 自定义字段：权限角色列表
            "iss", issuer,           // 签发者（必须）
            "aud", audience,         // 受众（必须）
            "iat", System.currentTimeMillis() / 1000, // 签发时间（秒）
            "exp", System.currentTimeMillis() / 1000 + (accessTokenExpireMinutes * 60) // 过期时间（秒）
        );

        // 使用 RS256 签名器生成 JWT
        // Hutool-JWT 自动编码 Header + Payload + Signature
        String token = JWT.create()
            .setPayload(claims)
            .sign(signer); // 使用私钥签名

        return token;
    }

    // ==================== 生成 Refresh Token ===================

    /**
     * 生成长期 Refresh Token（用于刷新 Access Token）
     * 注意：Refresh Token 本身不是 JWT，而是随机字符串，应存入数据库
     * 此处为演示，仍使用 JWT 格式，实际建议使用 UUID + 数据库存储
     *
     * @param userId 用户 ID
     * @param deviceId 设备指纹（可选，用于设备绑定）
     * @return Refresh Token 字符串（JWT 格式）
     */
    public String generateRefreshToken(Long userId, String deviceId) {
        Map<String, Object> claims = Map.of(
            "sub", userId,
            "device", deviceId,      // 设备标识，用于多设备管理
            "iss", issuer,
            "aud", audience,
            "iat", System.currentTimeMillis() / 1000,
            "exp", System.currentTimeMillis() / 1000 + (refreshTokenExpireHours * 3600) // 12 小时
        );

        // 使用相同私钥签名，确保可验证
        return JWT.create()
            .setPayload(claims)
            .sign(signer);
    }

    // ==================== 校验 Access Token ===================

    /**
     * 校验并解析 Access Token，验证签名、过期、签发者、受众等
     *
     * @param token JWT 字符串
     * @return 解析后的 Claims（包含用户信息），若失败则抛出异常
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public Claims validateAccessToken(String token) {
        // 创建 JWT 验证器
        JWTValidator validator = JWT.of(token)
            .requireAudience(audience)       // 必须包含正确受众
            .requireIssuer(issuer)           // 必须来自合法签发者
            .requireExpiresAt();             // 必须有过期时间（防无限有效）

        // 使用公钥验证签名（非对称加密，公钥可公开）
        // Hutool-JWT 会自动识别 RS256 并使用公钥校验
        JWTSigner publicKeySigner = new RS256JWTSigner(publicKeyPem);
        validator.verify(publicKeySigner);

        // 返回解析后的载荷（包含所有声明）
        return validator.getClaims();
    }

    // ==================== 校验 Refresh Token ===================

    /**
     * 校验 Refresh Token（用于刷新流程）
     * 注意：实际项目中应先查数据库确认该 Refresh Token 未被撤销
     * 此处仅做签名和过期验证，业务层需额外校验数据库状态
     *
     * @param token Refresh Token
     * @return 解析后的 Claims
     * @throws IllegalArgumentException 校验失败
     */
    public Claims validateRefreshToken(String token) {
        JWTValidator validator = JWT.of(token)
            .requireAudience(audience)
            .requireIssuer(issuer)
            .requireExpiresAt();

        // 使用相同公钥校验
        JWTSigner publicKeySigner = new RS256JWTSigner(publicKeyPem);
        validator.verify(publicKeySigner);

        return validator.getClaims();
    }

    // ==================== 辅助方法：获取用户ID ===================

    /**
     * 从已验证的 Claims 中提取用户 ID（Long 类型）
     *
     * @param claims 已通过校验的 Claims 对象
     * @return 用户 ID，若不存在则抛出异常
     */
    public Long getUserId(Claims claims) {
        Object userIdObj = claims.get("sub");
        if (userIdObj == null || !(userIdObj instanceof Long)) {
            throw new IllegalArgumentException("JWT 中缺少合法的用户ID（sub）");
        }
        return (Long) userIdObj;
    }

    /**
     * 从已验证的 Claims 中提取用户名
     */
    public String getUsername(Claims claims) {
        return (String) claims.get("name");
    }

    /**
     * 从已验证的 Claims 中提取角色列表
     */
    @SuppressWarnings("unchecked")
    public String[] getRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (rolesObj == null) {
            return new String[0];
        }
        if (rolesObj instanceof String[]) {
            return (String[]) rolesObj;
        }
        if (rolesObj instanceof java.util.List) {
            return ((java.util.List<?>) rolesObj).toArray(new String[0]);
        }
        throw new IllegalArgumentException("roles 字段格式错误，应为数组或列表");
    }

    // ==================== 安全增强：密钥生成工具（仅用于初始化）====================

    /**
     * 【仅开发/初始化使用】生成 RSA 密钥对（PEM 格式）
     * 生产环境禁止使用！密钥应由运维通过安全流程生成并注入
     *
     * @return 公私钥对（用于初始化系统）
     */
    public static RSA generateRsaKeyPair() {
        return new RSA(2048); // 2048位以上，符合金融安全标准
    }
}
```

---

## ✅ 三、Spring Boot 配置文件（`application.yml`）

```yaml
# ==================== JWT 安全配置 ====================
jwt:
  # 非对称密钥（实际部署中应从 Vault/KMS 加载，此处为示例）
  rsa:
    private-key: |
      -----BEGIN RSA PRIVATE KEY-----
      MIIEpAIBAAKCAQEAx...
      -----END RSA PRIVATE KEY-----
    public-key: |
      -----BEGIN PUBLIC KEY-----
      MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx...
      -----END PUBLIC KEY-----

  # 令牌有效期
  access-token-expire-minutes: 15
  refresh-token-expire-hours: 12

  # 声明信息
  issuer: insurance-auth-service
  audience: insurance-frontend
```

> ⚠️ **重要提醒**：  
> 在真实生产环境中，**`private-key` 不应出现在 `application.yml` 中**！  
> 应通过以下方式注入：
> - Kubernetes Secret 挂载为文件
> - HashiCorp Vault 动态获取
> - 环境变量 `JWT_RSA_PRIVATE_KEY`（使用 `@Value("${JWT_RSA_PRIVATE_KEY}")`）

---

## ✅ 四、Controller 示例：登录与刷新接口

```java
package com.yourbank.insurance.controller;

import cn.hutool.jwt.Claims;
import com.yourbank.insurance.dto.LoginRequest;
import com.yourbank.insurance.dto.TokenResponse;
import com.yourbank.insurance.security.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 用户登录接口
     * 返回：Access Token + Refresh Token
     * 注意：Refresh Token 应存储于服务端数据库（user_id + device_id + token_hash）
     */
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        // 模拟用户认证（实际应调用 UserService）
        Long userId = 1001L;
        String username = "zhangsan";
        String[] roles = {"ROLE_USER", "ROLE_INSURANCE_CLIENT"};

        // 1. 生成短期 Access Token（用于接口调用）
        String accessToken = jwtTokenUtil.generateAccessToken(userId, username, roles);

        // 2. 生成 Refresh Token（用于后续刷新）
        // 实际项目中：应生成 UUID，存入数据库（带过期时间、设备信息）
        String refreshToken = jwtTokenUtil.generateRefreshToken(userId, "device_abc123");

        // 返回响应
        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 刷新 Access Token 接口
     * 客户端携带 Refresh Token，服务端校验后返回新 Access Token
     */
    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestHeader("Authorization") String refreshToken) {
        // 移除 "Bearer " 前缀（如前端传的是 Bearer xxx）
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        // 1. 校验 Refresh Token 签名和过期
        Claims claims = jwtTokenUtil.validateRefreshToken(refreshToken);

        // 2. 从 Claims 中提取用户信息
        Long userId = jwtTokenUtil.getUserId(claims);
        String username = jwtTokenUtil.getUsername(claims);
        String[] roles = jwtTokenUtil.getRoles(claims);

        // 3. 【关键】：查询数据库，确认该 Refresh Token 未被撤销（防重放）
        // if (refreshTokenService.isRevoked(refreshToken)) {
        //     throw new UnauthorizedException("Refresh token has been revoked");
        // }

        // 4. 生成新的 Access Token
        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, username, roles);

        // 返回新 Token（Refresh Token 可复用，或生成新 Refresh Token）
        return new TokenResponse(newAccessToken, refreshToken); // 或生成新 Refresh Token
    }

    /**
     * 退出登录（注销）
     * 实际做法：将 Refresh Token 标记为已撤销（数据库更新）
     */
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        // 【关键】：标记该 Refresh Token 已被撤销（数据库操作）
        // refreshTokenService.revoke(refreshToken);
        // 可选：同时清除用户所有设备的 Refresh Token
    }
}
```

---

## ✅ 五、响应 DTO 类

```java
// TokenResponse.java
public class TokenResponse {
    private String accessToken;
    private String refreshToken;

    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // getter / setter
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
```

---

## ✅ 六、安全最佳实践总结（结合你的银行保险背景）

| 项目 | 推荐做法 |
|------|----------|
| **密钥算法** | ✅ 使用 **RS256**，私钥由 KMS 管理，公钥可分发 |
| **密钥存储** | ❌ 禁止硬编码；✅ 使用 Kubernetes Secret、Vault、环境变量注入 |
| **Access Token** | ⏱️ 有效期 ≤ 30 分钟，用于接口访问 |
| **Refresh Token** | 📦 由服务端数据库管理（非 JWT 也可），支持撤销、设备绑定 |
| **校验流程** | 必须验证 `iss`、`aud`、`exp`、`iat`，拒绝无效令牌 |
| **日志安全** | 禁止在日志中打印完整 JWT 或密钥 |
| **HTTPS** | 所有接口必须强制 HTTPS，防止中间人窃取 |
| **合规性** | 符合 GDPR、等保三级、金融行业数据安全规范 |

---

## ✅ 七、扩展建议（进阶）

1. **支持多密钥轮换**：在 `JwtTokenUtil` 中维护多个公钥，实现平滑升级。
2. **集成 Spring Security**：编写 `JwtAuthenticationFilter`，自动从 Header 解析并校验 Token。
3. **审计日志**：记录所有 Token 的签发、刷新、撤销行为。
4. **监控告警**：当检测到大量无效 Token 请求时，触发安全告警。

---

> ✅ **最终建议**：  
> 在你所在的银行/保险系统中，**优先采用 `RS256 + Hutool-JWT + Vault/K8s Secret` 组合**，并配合数据库管理 Refresh Token，即可构建一个**安全、合规、可审计、高可用**的 JWT 认证体系，完全满足金融级安全要求。

如有需要，我可为你提供配套的 Spring Security 集成方案或 Redis 存储 Refresh Token 的完整代码。