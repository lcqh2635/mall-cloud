非常好的进阶问题！在真实企业级项目中，配置往往不是扁平的 key=value，而是**多层嵌套结构**（如数据库连接池配置、多数据源、缓存策略、第三方服务集成等）。`@ConfigurationProperties` 完全支持嵌套对象绑定，这是它强大和实用的核心特性之一。

下面我为你提供一个 **综合性、真实项目级、带完整中文注释的嵌套配置绑定示例** —— 我们将模拟一个「多平台消息推送服务」的 Starter 配置结构，包含：

- 多平台开关（微信、短信、邮件）
- 各平台独立配置（含嵌套对象）
- 列表配置（多个短信通道）
- Map 配置（邮件模板）
- 校验注解（@NotBlank、@Min 等）
- IDE 友好提示（@Description）

---

# 🧩 Spring Boot 3 嵌套配置绑定实战示例 —— 多平台消息推送服务

## 🎯 配置目标

我们希望在 `application.yaml` 中支持如下结构：

```yaml
my:
  push:
    enabled: true
    default-channel: WECHAT
    wechat:
      enabled: true
      app-id: "wx1234567890"
      app-secret: "secret-key-here"
      template-id: "TEMPLATE_001"
      timeout-ms: 5000
    sms:
      enabled: true
      channels:
        - name: "阿里云短信"
          access-key: "ali-access-key"
          secret-key: "ali-secret-key"
          sign-name: "【我的公司】"
          region: "cn-hangzhou"
          enabled: true
        - name: "腾讯云短信"
          access-key: "tencent-access-key"
          secret-key: "tencent-secret-key"
          sign-name: "【腾信科技】"
          region: "ap-guangzhou"
          enabled: false
    email:
      enabled: false
      smtp-host: "smtp.exmail.qq.com"
      smtp-port: 465
      username: "notify@company.com"
      password: "password123"
      from-address: "notify@company.com"
      templates:
        welcome: "欢迎注册，您的验证码是：{{code}}"
        reset-password: "重置密码链接：{{link}}"
        order-confirm: "订单{{orderId}}已确认，金额{{amount}}元"
```

---

## 📁 项目结构

```
src/main/java/com/example/starter/config/
├── PushServiceProperties.java           // 根配置类
├── WechatConfig.java                    // 微信子配置
├── SmsConfig.java                       // 短信子配置
├── SmsChannel.java                      // 短信通道（列表元素）
└── EmailConfig.java                     // 邮件子配置
```

---

## ✅ 1. 根配置类：`PushServiceProperties.java`

```java
package com.example.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.Description;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 🌟 核心配置类：绑定 my.push.* 所有嵌套配置
 * 此类是整个消息推送服务的总入口配置
 */
@ConfigurationProperties(prefix = "my.push")
@Validated // 启用字段校验（需配合 jakarta.validation 注解）
public class PushServiceProperties {

    /**
     * 📌 是否启用整个推送服务
     * 默认 true，设为 false 可完全关闭所有推送功能
     */
    @Description("是否启用整个消息推送服务")
    private boolean enabled = true;

    /**
     * 📌 默认推送通道（枚举值：WECHAT / SMS / EMAIL）
     * 当未指定通道时使用此默认值
     */
    @Description("默认推送通道（WECHAT / SMS / EMAIL）")
    @NotBlank(message = "默认通道不能为空")
    private String defaultChannel = "WECHAT";

    // ========== 嵌套对象：微信配置 ==========
    /**
     * 📌 微信推送配置（嵌套对象）
     * 使用 @Valid 启用子对象校验
     * 使用 @NestedConfigurationProperty 标记为嵌套配置（IDE 友好）
     */
    @Valid
    @NestedConfigurationProperty
    @NotNull(message = "微信配置不能为空")
    private WechatConfig wechat = new WechatConfig();

    // ========== 嵌套对象：短信配置 ==========
    /**
     * 📌 短信推送配置（含列表）
     */
    @Valid
    @NestedConfigurationProperty
    @NotNull(message = "短信配置不能为空")
    private SmsConfig sms = new SmsConfig();

    // ========== 嵌套对象：邮件配置 ==========
    /**
     * 📌 邮件推送配置（含 Map 模板）
     */
    @Valid
    @NestedConfigurationProperty
    @NotNull(message = "邮件配置不能为空")
    private EmailConfig email = new EmailConfig();

    // ========== Getter & Setter ==========
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(String defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public WechatConfig getWechat() {
        return wechat;
    }

    public void setWechat(WechatConfig wechat) {
        this.wechat = wechat;
    }

    public SmsConfig getSms() {
        return sms;
    }

    public void setSms(SmsConfig sms) {
        this.sms = sms;
    }

    public EmailConfig getEmail() {
        return email;
    }

    public void setEmail(EmailConfig email) {
        this.email = email;
    }
}
```

> 💡 `@NestedConfigurationProperty`：非必需，但推荐添加，可帮助 IDE 和 Spring Boot 生成更准确的配置元数据。

---

## ✅ 2. 微信子配置：`WechatConfig.java`

```java
package com.example.starter.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.Description;

/**
 * 🌟 微信推送子配置
 * 对应 YAML 中的 my.push.wechat.*
 */
public class WechatConfig {

    /**
     * 是否启用微信推送
     */
    @Description("是否启用微信推送通道")
    private boolean enabled = true;

    /**
     * 微信 AppID
     */
    @Description("微信 AppID")
    @NotBlank(message = "微信 AppID 不能为空")
    private String appId;

    /**
     * 微信 AppSecret
     */
    @Description("微信 AppSecret")
    @NotBlank(message = "微信 AppSecret 不能为空")
    private String appSecret;

    /**
     * 模板 ID
     */
    @Description("微信模板消息 ID")
    @NotBlank(message = "模板 ID 不能为空")
    private String templateId;

    /**
     * 请求超时时间（毫秒）
     */
    @Description("微信接口请求超时时间（毫秒）")
    @Min(value = 1000, message = "超时时间不能小于1000毫秒")
    private int timeoutMs = 5000;

    // ========== Getter & Setter ==========
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
```

---

## ✅ 3. 短信配置（含列表）：`SmsConfig.java`

```java
package com.example.starter.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.Description;

import java.util.ArrayList;
import java.util.List;

/**
 * 🌟 短信推送配置（包含多个通道）
 * 对应 YAML 中的 my.push.sms.*
 */
public class SmsConfig {

    /**
     * 是否启用短信推送
     */
    @Description("是否启用短信推送通道")
    private boolean enabled = true;

    /**
     * 📌 短信通道列表（支持配置多个供应商）
     * 使用 @Valid 校验列表中每个元素
     */
    @Description("短信通道配置列表")
    @Valid
    @NotEmpty(message = "至少需要配置一个短信通道")
    private List<SmsChannel> channels = new ArrayList<>();

    // ========== Getter & Setter ==========
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<SmsChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<SmsChannel> channels) {
        this.channels = channels;
    }
}
```

---

## ✅ 4. 短信通道对象：`SmsChannel.java`

```java
package com.example.starter.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.Description;

/**
 * 🌟 单个短信通道配置
 * 作为 SmsConfig.channels 列表中的元素
 */
public class SmsChannel {

    /**
     * 通道名称（如“阿里云短信”）
     */
    @Description("短信通道名称")
    @NotBlank(message = "通道名称不能为空")
    private String name;

    /**
     * Access Key（供应商提供）
     */
    @Description("短信服务 Access Key")
    @NotBlank(message = "Access Key 不能为空")
    private String accessKey;

    /**
     * Secret Key（供应商提供）
     */
    @Description("短信服务 Secret Key")
    @NotBlank(message = "Secret Key 不能为空")
    private String secretKey;

    /**
     * 短信签名（如【我的公司】）
     */
    @Description("短信签名")
    @NotBlank(message = "短信签名不能为空")
    private String signName;

    /**
     * 区域/数据中心（如 cn-hangzhou）
     */
    @Description("服务区域")
    @NotBlank(message = "区域不能为空")
    private String region;

    /**
     * 是否启用此通道
     */
    @Description("是否启用此短信通道")
    private boolean enabled = true;

    // ========== Getter & Setter ==========
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
```

---

## ✅ 5. 邮件配置（含 Map）：`EmailConfig.java`

```java
package com.example.starter.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.Description;

import java.util.HashMap;
import java.util.Map;

/**
 * 🌟 邮件推送配置
 * 包含 SMTP 设置 + 模板 Map
 * 对应 YAML 中的 my.push.email.*
 */
public class EmailConfig {

    /**
     * 是否启用邮件推送
     */
    @Description("是否启用邮件推送通道")
    private boolean enabled = false;

    /**
     * SMTP 服务器地址
     */
    @Description("SMTP 服务器地址")
    @NotBlank(message = "SMTP 地址不能为空")
    private String smtpHost;

    /**
     * SMTP 端口
     */
    @Description("SMTP 端口")
    @Min(value = 1, message = "端口必须大于0")
    private int smtpPort = 25;

    /**
     * 登录用户名（邮箱地址）
     */
    @Description("SMTP 登录邮箱")
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String username;

    /**
     * 登录密码或授权码
     */
    @Description("SMTP 登录密码或授权码")
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 发件人地址（必须与 username 一致或被授权）
     */
    @Description("发件人邮箱地址")
    @Email(message = "发件人邮箱格式不正确")
    @NotBlank(message = "发件人邮箱不能为空")
    private String fromAddress;

    /**
     * 📌 邮件模板 Map
     * key = 模板名称（如 welcome, reset-password）
     * value = 模板内容（支持占位符如 {{code}}）
     */
    @Description("邮件模板配置（key=模板名, value=模板内容）")
    private Map<String, String> templates = new HashMap<>();

    // ========== Getter & Setter ==========
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public Map<String, String> getTemplates() {
        return templates;
    }

    public void setTemplates(Map<String, String> templates) {
        this.templates = templates;
    }
}
```

---

## ✅ 6. 在自动配置类中启用配置绑定

```java
package com.example.starter;

import com.example.starter.config.PushServiceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类：启用 PushServiceProperties 的绑定
 */
@Configuration
@ConditionalOnProperty(prefix = "my.push", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(PushServiceProperties.class) // ⭐ 关键：启用嵌套配置绑定
public class PushServiceAutoConfiguration {

    // 可注入 PushServiceProperties 使用
}
```

---

## ✅ 7. 添加依赖以支持校验和元数据生成（pom.xml）

```xml
<dependencies>
    <!-- 启用 @ConfigurationProperties 校验 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- 生成配置元数据，支持 IDE 提示 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## ✅ 8. 编译后效果（IDEA 中的配置提示）

当你在 `application.yaml` 中输入：

```yaml
my:
  push:
    wechat:
      app-id: █
```

IDEA 会自动提示：

> **微信 AppID**  
> 类型：String  
> 描述：微信 AppID  
> 约束：不能为空  
> 默认值：无

并支持自动补全、错误高亮（如填写数字会提示“必须是字符串”）。

---

## ✅ 9. 运行时校验效果

如果配置缺失或非法（如 `my.push.wechat.app-id` 未填写），应用启动时会报错：

```
Binding to target org.springframework.boot.context.properties.bind.BindException: 
Failed to bind properties under 'my.push.wechat' to com.example.starter.config.WechatConfig:

    Reason: Validation failed for object 'wechatConfig'. Errors: 
    - Field error in object 'wechatConfig' on field 'appId': 
      rejected value [null]; codes [...]; arguments [...]; 
      default message [微信 AppID 不能为空]
```

---

## 🧭 总结：嵌套配置最佳实践

| 特性 | 实现方式 | 说明 |
|------|----------|------|
| **嵌套对象** | 直接定义字段 + 类型 | `private WechatConfig wechat;` |
| **列表绑定** | `List<T>` + `@Valid` | 支持 YAML 数组格式 |
| **Map 绑定** | `Map<K,V>` | key 支持字符串，value 支持简单类型或对象 |
| **校验支持** | `@Validated` + `jakarta.validation` | 启动时校验配置合法性 |
| **IDE 提示** | `@Description` + `configuration-processor` | 极大提升开发体验 |
| **默认值** | 在字段声明时赋值 | `private int timeoutMs = 5000;` |

---

## 💡 真实项目建议

- **配置类按功能模块拆分**（如 `CacheProperties`, `DataSourceProperties`）
- **复杂对象使用 `@Valid` 递归校验**
- **敏感字段（密码、密钥）使用 `@Value("${my.push.email.password}")` + Jasypt 加密**
- **为每个字段添加 `@Description`，生成企业级配置文档**
- **使用 `@ConstructorBinding`（不可变配置）替代 setter（Spring Boot 2.2+）**

---

✅ 这个示例完全可用于真实企业项目，结构清晰、扩展性强、校验完善、IDE 友好！

如需我为你扩展「加密配置」「动态刷新」「多环境 profile」等高级特性，请随时告诉我！

祝你开发顺利，成为配置大师！🚀