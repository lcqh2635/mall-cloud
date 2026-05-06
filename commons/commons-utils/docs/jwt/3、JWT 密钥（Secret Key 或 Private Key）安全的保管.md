JWT 的密钥（Secret Key 或 Private Key）是整个认证体系的安全基石。一旦泄露，攻击者可伪造任意用户身份，后果极其严重。在真实开发（尤其是金融、保险等高合规要求场景）中，密钥管理必须遵循**最小暴露原则**和**纵深防御策略**。以下是标准且推荐的实践方案：

---

## 一、密钥类型选择：对称 vs 非对称

| 类型 | 算法示例 | 密钥保管要求 | 适用场景 |
|------|--------|------------|--------|
| **对称加密** | HS256, HS512 | **所有服务共享同一个密钥** | 单体应用、内部可信服务 |
| **非对称加密** | RS256, ES256 | **私钥严格保密，公钥可公开分发** | 微服务、第三方集成、高安全系统 |

✅ **强烈推荐**：在生产环境（尤其是银行/保险系统）**优先使用 RS256（RSA 非对称）**，避免密钥在多个服务间共享带来的泄露风险。

---

## 二、密钥安全保管的核心原则

1. **绝不硬编码**：禁止将密钥写死在代码、配置文件（如 `application.yml`）中。
2. **最小权限访问**：只有授权服务/人员能访问密钥。
3. **加密存储**：密钥本身应加密保存。
4. **定期轮换**：降低长期暴露风险。
5. **审计日志**：记录密钥访问行为。

---

## 三、真实开发中的推荐做法（按安全等级排序）

### ✅ 方案 1：使用专业密钥管理服务（KMS）—— **最高安全等级（推荐用于生产）**

- **原理**：密钥由云服务商（如 AWS KMS、Azure Key Vault、阿里云 KMS、HashiCorp Vault）托管，应用通过 API 动态获取。
- **优势**：
    - 密钥永不落地（不出现在服务器磁盘/内存明文）。
    - 自动轮换、细粒度权限控制、完整审计日志。
    - 符合 GDPR、等保、金融合规要求。
- **Spring Boot 集成示例（以 HashiCorp Vault 为例）**：
  ```yaml
  # bootstrap.yml
  spring:
    cloud:
      vault:
        uri: https://vault.example.com
        token: ${VAULT_TOKEN}
        kv:
          enabled: true
          backend: secret
  ```
  ```java
  @Value("${jwt.rsa.private-key}")
  private String privateKeyPem;
  ```
  实际密钥从 Vault 动态拉取，不在 Git 或配置中心明文存储。

> 💡 **适用场景**：金融、保险、政府等强合规系统。**这是你所在行业的首选方案**。

---

### ✅ 方案 2：环境变量 + 启动时注入（适用于中小项目）

- **做法**：
    - 密钥通过 CI/CD 流程或运维脚本注入为环境变量。
    - 应用启动时读取 `System.getenv("JWT_SECRET")`。
- **示例（Docker）**：
  ```dockerfile
  # 不要写在 Dockerfile 中！
  docker run -e JWT_SECRET=$(cat /secure/jwt.key) myapp
  ```
- **安全加固**：
    - 环境变量文件权限设为 `600`（仅属主可读）。
    - 使用 `docker secrets`（Swarm）或 Kubernetes `Secrets`。
- **Kubernetes 示例**：
  ```yaml
  env:
    - name: JWT_SECRET
      valueFrom:
        secretKeyRef:
          name: jwt-secret
          key: secret
  ```

> ⚠️ **注意**：避免 `ps` 命令泄露环境变量（K8s Secret 相对安全）。

---

### ⚠️ 方案 3：配置中心加密存储（需谨慎）

- **做法**：将密钥加密后存入 Nacos、Apollo、Spring Cloud Config。
- **必须满足**：
    - 配置中心本身启用 HTTPS + 认证。
    - 密钥使用 **独立的加密密钥（Data Key）** 加密，而 Data Key 由 KMS 管理。
    - 应用启动时解密（如 Jasypt）。
- **Jasypt 示例**：
  ```properties
  jwt.secret=ENC(G5t2Uk8x...)
  ```
  启动参数：`-Djasypt.encryptor.password=${DECRYPT_KEY}`（DECRYPT_KEY 仍需安全注入）。

> ❌ **不推荐**：若 DECRYPT_KEY 仍硬编码或明文传入，则无实质提升。

---

### ❌ 绝对禁止的做法

| 做法 | 风险 |
|------|------|
| 写死在 `application.yml` 并提交 Git | 代码泄露 = 密钥泄露 |
| 通过 HTTP 明文传输密钥 | 中间人攻击可截获 |
| 使用弱密钥（如 "secret123"） | 易被暴力破解（HS256 要求 ≥ 256 位熵） |
| 多环境共用同一密钥 | 一个环境泄露影响全部 |

---

## 四、密钥生成与轮换规范

### 1. **密钥生成**
- **HS256**：使用安全随机源生成 ≥ 256 位（32 字节）密钥：
  ```bash
  openssl rand -base64 32
  ```
- **RS256**：生成 2048 位以上 RSA 密钥对：
  ```bash
  openssl genrsa -out jwt-private.pem 2048
  openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
  ```

### 2. **密钥轮换**
- **定期更换**（如每 90 天）。
- **平滑过渡**：
    - 支持多密钥验证（如新旧两个公钥）。
    - 先部署新公钥，再切换签发端使用新私钥。
    - 旧 JWT 过期后停用旧密钥。

---

## 五、Spring Boot 实践建议（结合你的技术栈）

1. **使用非对称加密（RS256）**：
   ```java
   // 从安全来源加载私钥
   private static final PrivateKey privateKey = loadPrivateKeyFromVault();
   
   String jwt = Jwts.builder()
       .setSubject("user123")
       .signWith(privateKey, SignatureAlgorithm.RS256)
       .compact();
   ```

2. **验证时只依赖公钥**（可安全分发）：
   ```java
   // 公钥可放在配置中心（无需加密）
   PublicKey publicKey = loadPublicKey();
   Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
   ```

3. **禁止在日志中打印密钥**：
   ```java
   // application.yml
   logging:
     level:
       com.yourpackage.security: WARN  # 避免 DEBUG 日志泄露
   ```

---

## 总结：真实开发推荐路径

| 场景 | 推荐方案 |
|------|--------|
| **银行/保险生产环境** | **HashiCorp Vault / 云厂商 KMS + RS256** |
| **内部可信微服务** | Kubernetes Secrets + RS256（私钥挂载为 Volume） |
| **单体应用测试环境** | 环境变量注入 + HS256（密钥 ≥ 32 字节随机） |
| **绝对避免** | Git 提交、配置文件明文、弱密钥 |

> 🔐 **核心思想**：密钥不是“配置”，而是“凭证”，应以管理数据库密码的同等甚至更高标准对待。

如果你正在推动团队落地 Spring Boot 安全方案，建议优先引入 Vault 或云 KMS，并配合非对称 JWT 签名，这将显著提升系统安全水位并满足合规审计要求。