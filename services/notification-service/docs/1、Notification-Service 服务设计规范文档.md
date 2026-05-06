当然可以！以下是为你的 **`urbane-commerce` 电商微服务系统** 量身定制的《**Notification-Service 服务设计规范文档**》，全面、系统、可落地，明确界定：

✅ **Notification-Service 的职责与作用**  
✅ **必须做的核心功能（推荐）**  
❌ **禁止或不推荐的行为（严禁做）**  
🔍 **判断标准与核心设计原则**  
📌 **真实生产环境最佳实践**

---

# 📜《urbane-commerce Notification-Service 服务设计规范》
> **版本：1.11 | 最后更新：2025年4月 | 适用架构：Spring Boot + Kafka + Redis + MySQL + 多通道推送 + 模板引擎**

---

## 🧭 一、Notification-Service 角色定位（Why Notification-Service？）

> **Notification-Service 是整个电商系统中负责“用户触达与沟通”的核心服务。**

它是连接**系统事件**与**用户感知**的桥梁，是提升**用户活跃度、转化率、留存率和满意度**的关键引擎。

| 角色 | 说明 |
|------|------|
| ✅ **多通道消息中枢** | 统一管理短信、邮件、站内信、App 推送、微信模板消息、钉钉通知等 |
| ✅ **事件驱动触发器** | 响应来自其他服务的业务事件（如订单创建、支付成功、物流更新）自动发送通知 |
| ✅ **模板与内容管理** | 管理通知模板（支持变量替换）、语言本地化（中文/英文）、富文本渲染 |
| ✅ **发送策略控制** | 控制发送频率、时段（如夜间禁发）、优先级、重试机制、防骚扰策略 |
| ✅ **发送状态追踪** | 记录每条消息的发送状态（成功、失败、已读）、失败原因、重试记录 |
| ✅ **用户偏好管理** | 存储用户对各类通知的订阅开关（如“是否接收促销邮件”） |
| ❌ **非业务服务** | 不参与下单、支付、库存、商品管理 —— 那是其他微服务的事 |
| ❌ **非网关** | 不负责路由、认证、限流 |
| ❌ **非用户服务** | 不管理用户身份、密码、等级 —— 那是 `auth-service` / `user-service` 的事 |
| ❌ **非客服系统** | 不处理工单、不回复用户咨询 —— 那是 `after-sales-service` 的事 |

> 💡 **一句话总结**：  
> **Notification-Service 回答：“用户该在什么时候、通过什么方式、收到什么信息？”**  
> 它不关心你买了什么 —— 那是 `order-service` 的事；  
> 它也不关心你有没有付钱 —— 那是 `payment-gateway` 的事；  
> 它只关心：**如何用最恰当的方式，在最合适的时间，把最关键的信息送到用户手中。**

> ⚠️ **重要性**：
> - 用户未收到“支付成功”通知 → 可能重复付款
> - 用户未收到“发货”通知 → 客服压力暴增
> - 用户被频繁推送促销 → 取消关注、卸载 App
> - 用户收不到验证码 → 注册失败、流失

> **优秀的通知系统 = 无声的客服 + 无形的营销**

---

## ✅ 二、推荐在 Notification-Service 必须做的事情（核心职责）

### 1. ✅ **事件监听与触发（Event-Driven）**
Notification-Service **不主动发起通知**，而是作为**消费者**监听 Kafka 中的业务事件：

| 事件来源 | 事件类型 | 触发通知动作 |
|----------|-----------|----------------|
| `order-service` | `ORDER_CREATED` | 发送“订单已创建”站内信 |
| `order-service` | `ORDER_PAID` | 发送“支付成功”短信 + 邮件 |
| `logistics-service` | `LOGISTICS_STATUS_UPDATED` | 发送“物流已发货”App 推送 |
| `logistics-service` | `LOGISTICS_STATUS_UPDATED` with status=DELIVERED | 发送“您的包裹已签收”微信模板消息 |
| `promo-service` | `COUPON_ISSUED` | 发送“您获得一张满800减100券”站内信 |
| `user-service` | `USER_REGISTERED` | 发送“欢迎注册”邮件 + 短信验证码 |
| `review-service` | `REVIEW_PUBLISHED` | 发送“感谢评价”站内信 + 积分奖励提醒 |
| `cart-service` | `CART_CLEARED` | 发送“您有商品未付款，限时优惠”微信服务号消息 |

> ✅ 示例消费流程：
```java
@KafkaListener(topics = "order-paid")
public void handleOrderPaid(OrderPaidEvent event) {
    NotificationRequest request = NotificationRequest.builder()
        .userId(event.getUserId())
        .type(NotificationType.SMS)
        .templateCode("ORDER_PAID_SUCCESS")
        .variables(Map.of(
            "orderNo", event.getOrderNo(),
            "amount", event.getAmount().toString()
        ))
        .priority(NotificationPriority.HIGH)
        .build();
    
    notificationService.send(request);
}
```

> ✅ 所有通知由**事件驱动**，实现**高解耦、低延迟、易扩展**

---

### 2. ✅ **多通道统一发送（Multi-Channel Delivery）**
支持多种发送渠道，并抽象为统一接口：

| 渠道 | 实现方式 | 特点 |
|------|----------|------|
| **短信** | 阿里云短信 / 腾讯云短信 API | 实时性强，适合验证码、关键通知 |
| **邮件** | SMTP（QQ、163）或 SendGrid / Mailgun | 适合长文本、账单、促销 |
| **站内信** | 数据库存储 + Web 页面展示 | 无需用户开启推送，永久保留 |
| **App Push** | 极光 / 友盟 / APNs / FCM | 高唤醒率，需集成 SDK |
| **微信模板消息** | 微信公众号/小程序 API | 用户关注后方可发送，转化率高 |
| **钉钉机器人** | Webhook | 内部运营、管理员告警 |
| **企业微信** | 企业微信应用消息 | B端客户、供应商通知 |

> ✅ 抽象接口：
```java
public interface ChannelSender {
    boolean send(NotificationRequest request);
    String getChannelName();
}

@Component("smsSender")
public class SmsSender implements ChannelSender { ... }

@Component("emailSender")
public class EmailSender implements ChannelSender { ... }
```

> ✅ 根据用户偏好、事件类型、时间智能选择渠道（如夜间发邮件，白天发短信）

---

### 3. ✅ **通知模板管理与变量替换（Template Engine）**
所有通知使用**模板 + 变量**方式定义，支持动态内容：

#### 模板示例（短信）：
```text
【urbane商城】尊敬的用户，您的订单 {{orderNo}} 已支付成功，金额 {{amount}} 元。点击查看详情：{{link}}
```

#### 模板示例（邮件）：
```html
<p>亲爱的 {{userName}}：</p>
<p>感谢您在 urbane 商城购物！您的订单 <strong>{{orderNo}}</strong> 已完成支付，总金额：<strong>{{amount}}</strong> 元。</p>
<p>预计 {{deliveryDays}} 天内送达，物流单号：<a href="{{trackingUrl}}">{{waybillNo}}</a></p>
<p>如有疑问，请联系客服：400-123-4567</p>
```

> ✅ 支持语法：
> - `{{variable}}`：基础变量
> - `{% if condition %}`：条件判断（可选）
> - `{% for item in list %}`：循环（用于商品列表）

> ✅ 模板存储于数据库，支持后台编辑、预览、版本控制

```sql
CREATE TABLE notification_templates (
  id BIGINT PRIMARY KEY,
  code VARCHAR(50) UNIQUE, -- 如 'ORDER_PAID_SUCCESS'
  type ENUM('SMS', 'EMAIL', 'APP_PUSH', 'WECHAT'),
  subject VARCHAR(200),   -- 邮件标题
  content TEXT,           -- 模板内容
  language VARCHAR(10),   -- zh-CN / en-US
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

> ✅ 使用 **Thymeleaf / FreeMarker** 解析模板，支持复杂逻辑

---

### 4. ✅ **用户通知偏好管理（Preference Center）**
每个用户可自主设置接收哪些通知：

| 通知类型 | 是否接收（默认） |
|----------|------------------|
| 订单相关（创建、支付、发货） | ✅ 开启 |
| 物流跟踪 | ✅ 开启 |
| 优惠券发放 | ✅ 开启 |
| 促销广告 | ⚠️ 默认关闭 |
| 新品推荐 | ⚠️ 默认关闭 |
| 活动邀请 | ⚠️ 默认关闭 |
| 客服回访 | ✅ 开启 |

> ✅ 提供接口：
```http
GET /notification/preferences?userId=123
PUT /notification/preferences?userId=123
{
  "promotional": false,
  "newArrival": true,
  "newsletter": false
}
```

> ✅ 优先级规则：
> - **强制通知**（如验证码、支付结果）→ 忽略偏好，必发
> - **非强制通知** → 检查用户偏好，未开启则跳过
> - **高频通知** → 自动降频（如每日最多推送 3 条）

---

### 5. ✅ **发送状态追踪与失败重试（Delivery Tracking & Retry）**
每条通知记录完整生命周期：

| 字段 | 说明 |
|------|------|
| `status` | PENDING / SENT / FAILED / READ / DELIVERED |
| `channel` | SMS / EMAIL / APP_PUSH |
| `target` | 手机号 / 邮箱 / device_token |
| `content` | 实际发送内容 |
| `retry_count` | 当前重试次数（最大 3 次） |
| `last_retry_at` | 上次重试时间 |
| `error_code` | 错误码（如 “手机号格式错误”、“短信余额不足”） |
| `read_at` | 用户是否已阅读（仅站内信/APP） |

> ✅ 重试机制：
> - 失败 → 5 分钟后重试第1次
> - 再失败 → 30 分钟后重试第2次
> - 再失败 → 2 小时后重试第3次
> - 仍失败 → 标记为 `FAILED`，写入异常队列，人工介入

> ✅ 使用 Redis 缓存待发送任务，MySQL 持久化日志

---

### 6. ✅ **防骚扰与发送节制策略（Anti-Spam）**
避免因过度打扰导致用户反感或投诉：

| 策略 | 实现方式 |
|------|----------|
| **时段限制** | 晚上 22:00 – 早上 8:00 禁止发送非紧急通知 |
| **频率控制** | 同一用户 24 小时内最多接收 5 条促销类消息 |
| **灰度发送** | 新模板先发给 1% 用户测试效果 |
| **黑名单过滤** | 用户多次点击“不感兴趣” → 加入黑名单，暂停推送 |
| **退订机制** | 所有营销类通知必须包含“退订”链接（合规要求） |

> ✅ 示例：用户连续 3 次点击“不再推送此类消息”，自动将该用户标记为 `OPT_OUT_PROMO`

---

### 7. ✅ **数据统计与分析（Analytics）**
生成报表，优化通知策略：

| 指标 | 说明 |
|------|------|
| 发送总量 | 每日/每周发送总数 |
| 成功率 | 成功数 / 总数（目标 >98%） |
| 打开率 | 站内信/App 推送的点击率 |
| 转化率 | 接收促销通知 → 下单人数占比 |
| 用户投诉率 | 举报“骚扰”数量 |
| 渠道对比 | 短信 vs 邮件 vs 微信 的转化差异 |

> ✅ 数据来源：Kafka 日志 → 写入 ClickHouse → BI 可视化（Grafana / PowerBI）  
> ✅ 用途：优化模板文案、调整发送时机、提升 ROI

---

### 8. ✅ **多语言与国际化支持（i18n）**
支持全球用户：

| 场景 | 实现方式 |
|------|----------|
| 用户语言为 `zh-CN` | 发送中文模板 |
| 用户语言为 `en-US` | 发送英文模板 |
| 用户无语言设置 | 使用默认语言（中文） |

> ✅ 模板按语言存储：
```yaml
templates:
  ORDER_PAID_SUCCESS:
    zh-CN: "您的订单 {{orderNo}} 已支付成功"
    en-US: "Your order {{orderNo}} has been paid successfully"
```

> ✅ 支持地区差异化（如港澳台使用繁体中文）

---

## ❌ 三、禁止或不推荐在 Notification-Service 做的事情（严禁做）

| 行为 | 为什么不推荐？ | 后果 | 正确做法 |
|------|----------------|------|----------|
| **1. 直接调用订单、库存、支付服务** | 破坏微服务边界，强耦合 | 一个服务挂了，通知也瘫痪 | ✅ 只监听 Kafka 事件，不主动调用任何服务 |
| **2. 在通知中硬编码价格、商品名、地址** | 数据可能变更，历史通知错乱 | 用户看到“原价 8999”，实际已降价 | ✅ 所有内容从事件中获取，或通过 `product-service`/`order-service` 查询快照 |
| **3. 允许前端直接调用发送通知** | 前端不可信，可能伪造 | 黑产刷短信、薅羊毛、攻击平台 | ✅ 所有通知必须由内部服务（如 order-service）触发，外部无法调用 |
| **4. 存储用户手机号、邮箱明文** | 违反 GDPR / 个人信息保护法 | 泄露风险极高 | ✅ 敏感字段加密存储，仅在发送时解密 |
| **5. 不做失败重试机制** | 第三方服务不稳定导致漏发 | 用户收不到验证码 → 注册失败 | ✅ 必须实现指数退避重试（最多 3 次） |
| **6. 不区分“强制通知”和“营销通知”** | 用户被骚扰 → 取消关注 | 用户投诉增多，品牌受损 | ✅ 强制通知（支付、物流）无视偏好，营销通知严格受控 |
| **7. 使用 Session 或 Cookie 管理用户状态** | 与无状态架构冲突 | 无法水平扩展 | ✅ 所有请求基于 `userId`，无会话 |
| **8. 每次发送都调用第三方 API（如短信）同步阻塞** | 性能差，拖慢主链路 | 用户下单卡顿 | ✅ 所有发送异步执行，通过线程池 + 消息队列缓冲 |
| **9. 不提供退订机制** | 违反法律（如 GDPR、中国《个人信息保护法》） | 面临监管处罚 | ✅ 每条营销通知必须含“退订”链接或指令 |
| **10. 在通知中包含敏感操作链接（如删除账户）** | 可能被钓鱼攻击 | 用户误点导致账号被盗 | ✅ 高危操作需二次验证，不在通知中直接跳转 |

---

## 🔍 四、判断标准与核心设计原则

| 原则 | 说明 | 应用示例 |
|------|------|----------|
| **✅ 单一职责原则（SRP）** | 一个服务只做一件事 | Notification-Service 只管“发消息”，不管“买什么”“付多少钱” |
| **✅ 事件驱动架构（EDA）** | 服务间通信靠事件，而非 RPC | `order-service` 发 `ORDER_PAID` → Notification-Service 收到发短信 |
| **✅ 最终一致性（Eventual Consistency）** | 不追求实时，但要保证最终送达 | 短信延迟 1~5 秒可接受，但不能丢失 |
| **✅ 幂等性设计（Idempotency）** | 同一事件多次触发，只发一次通知 | 同一订单支付回调重复，只发一条“支付成功” |
| **✅ 用户体验优先（UX First）** | 消息要精准、及时、不打扰 | 用对时间、用对渠道、用对语气 |
| **✅ 安全默认（Secure by Default）** | 默认拒绝非法请求 | 所有发送必须带合法 `userId` 和签名，防伪造 |
| **✅ 可观测性优先（Observability）** | 所有操作必须可追踪 | 每条消息都有唯一 ID，可查发送记录、失败原因 |
| **✅ 开闭原则（OCP）** | 对扩展开放，对修改关闭 | 新增一种通知方式（如 WhatsApp），只需实现 `ChannelSender` |
| **✅ 合规性优先（Compliance First）** | 符合法律法规 | 包含退订、隐私声明、数据加密、访问控制 |
| **✅ 可配置化（Configurable）** | 所有策略可通过后台调整 | 模板、频率、时段、渠道优先级均可在线修改，无需发版 |

---

## 🧩 五、典型场景对比：正确 vs 错误做法

| 场景 | 正确做法 | 错误做法 |
|------|----------|----------|
| **用户支付成功** | `order-service` → 发 `ORDER_PAID` → Notification-Service → 发短信+邮件+站内信 | `order-service` 直接调用短信 API → 没有统一管理，无法监控、无法重试、无法统计 |
| **用户领取优惠券** | `coupon-service` → 发 `COUPON_ISSUED` → Notification-Service → 发微信模板消息 | `coupon-service` 用固定文案“恭喜您获得100元券” → 用户看到“100元”但实际是满800减100 → 引发投诉 |
| **物流已发货** | `logistics-service` → 发 `LOGISTICS_STATUS_UPDATED` → Notification-Service → 推送 App 消息 + 微信模板 | 没有推送，用户天天打电话问“我的货在哪？” → 客服崩溃 |
| **用户注册成功** | `auth-service` → 发 `USER_REGISTERED` → Notification-Service → 发欢迎邮件 + 验证码短信 | 只发欢迎邮件，没发短信 → 用户找不到验证码 → 注册失败流失 |
| **用户取消订单** | `order-service` → 发 `ORDER_CANCELLED` → Notification-Service → 发“已退款”站内信 | 不发通知 → 用户以为没退款，去银行投诉 |
| **促销活动开始** | `promo-service` → 发 `PROMO_STARTED` → Notification-Service → 向兴趣标签为“手机”的用户推送 App 推送 | 向所有用户群发“大促开始” → 90% 用户屏蔽 → 转化率低于 0.5% |
| **用户点击“不再推送”** | Notification-Service 更新偏好 → 后续所有营销类消息跳过 | 用户点击“不再推送”后仍收到 3 条 → 用户拉黑 App |

> ⚠️ **关键结论**：  
> **通知不是“广播”，而是“对话”。**  
> 它必须**精准、可控、可追溯、可退出**，否则就是噪音污染。

---

## 🛡️ 六、安全加固建议（生产环境必备）

| 措施 | 实现方式 |
|------|----------|
| **强制 HTTPS** | 所有接口仅支持 HTTPS，禁用 HTTP |
| **请求鉴权** | 所有发送请求必须携带 `X-Source-Service` + HMAC 签名（防伪造） |
| **输入过滤** | 过滤 XSS、SQL 注入、HTML 标签（防止模板注入） |
| **敏感数据加密** | 手机号、邮箱使用 AES-256 加密存储，仅在发送时解密 |
| **API 白名单** | 仅允许 `order-service`、`coupon-service`、`logistics-service` 等可信服务调用 |
| **审计日志** | 记录每条通知：`{ userId, template, channel, status, ip, source }` |
| **GDPR 合规** | 支持“导出我的通知记录”、“删除所有通知数据” |
| **密钥管理** | 短信/邮件/微信 API 密钥使用 Vault 或 KMS 管理 |
| **速率限制** | 每个服务每分钟最多发送 1000 条，防滥用 |
| **IP 黑名单** | 对恶意 IP（如爬虫、代理）封禁 |

---

## 📊 七、Notification-Service 架构图（文字版）

```
[业务服务]
     ↓ (事件：ORDER_PAID, COUPON_ISSUED, LOGISTICS_STATUS_UPDATED...)
[Kafka]
     ←─ EVENT: ORDER_PAID → Notification-Service
     ←─ EVENT: COUPON_ISSUED → Notification-Service
     ←─ EVENT: USER_REGISTERED → Notification-Service
     ←─ EVENT: CART_CLEARED → Notification-Service

     ↑
[Notification-Service]
     ├── ✅ EventConsumer → 解析事件 → 构造 NotificationRequest
     ├── ✅ TemplateEngine → 加载模板 + 替换变量
     ├── ✅ PreferenceService → 查询用户偏好（是否允许发送）
     ├── ✅ ChannelRouter → 选择最优通道（短信/邮件/App/微信）
     ├── ✅ ChannelSender → 调用阿里云短信 / SendGrid / FCM
     └── ✅ RetryManager → 失败重试（指数退避）
     ↓
[Database: MySQL]
     ├── notifications (id, user_id, template_code, status, channel, content, retry_count, created_at)
     ├── notification_preferences (user_id, promotional, newsletter, logistics, ...)
     ├── notification_templates (code, type, content, language, subject)

     ↑
[Redis]
     ←─ 缓存：用户偏好（TTL=1h）
     ←─ 缓存：模板内容（TTL=1h）
     ←─ 队列：待发送任务（延迟队列实现重试）

     ↑
[External Services]
     ├── 阿里云短信 → 发送 SMS
     ├── SendGrid → 发送 Email
     ├── 极光推送 → 发送 App Push
     ├── 微信模板消息 → 发送 WeChat Template
     └── 钉钉机器人 → 发送 OA 通知

     ↑
[BI / Grafana] ←─ 消费 Kafka 日志 → 统计发送量、成功率、转化率
```

> ✅ **注意**：  
> Notification-Service **不主动调用任何业务服务**，只**监听事件**。  
> 所有外部依赖通过**异步事件 + API 调用**解耦，实现高可用、高性能、高可靠。

---

## ✅ 八、推荐技术栈（Spring Boot + 生态）

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| **框架** | Spring Boot 3.x | Java 17+，现代化开发 |
| **消息队列** | Apache Kafka | 接收业务事件，解耦高并发 |
| **数据库** | MySQL 8.0 | 存储通知记录、模板、用户偏好 |
| **缓存** | Redis | 缓存模板、用户偏好、任务队列 |
| **模板引擎** | Thymeleaf / FreeMarker | 动态渲染富文本通知内容 |
| **HTTP 客户端** | Feign + RestTemplate | 调用短信、邮件、推送服务商 API |
| **定时任务** | Spring Scheduler / XXL-JOB | 定期清理失败任务、检查重试 |
| **服务注册** | Nacos | 服务发现与配置中心 |
| **API 文档** | Swagger/OpenAPI 3.0 | 自动生成接口文档 |
| **日志** | Logback + ELK | 结构化日志，追踪每条通知生命周期 |
| **监控** | Prometheus + Grafana | 监控发送成功率、延迟、错误率 |
| **加密** | AES-256 | 加密存储手机号、邮箱 |
| **安全** | JWT + HMAC | 服务间通信签名验证 |
| **工具类** | Lombok + MapStruct | 减少样板代码，DTO 映射自动化 |

---

## 📦 九、附录：Notification-Service API 设计规范（RESTful）

| 方法 | 路径 | 描述 | 权限 | 返回 |
|------|------|------|------|------|
| POST | `/notification/send` | **仅内部服务调用**：发送通知 | 需服务签名 | `{ messageId, status }` |
| GET | `/notification/preferences/{userId}` | 查询用户偏好 | 需 Token | `{ promotional: true, ... }` |
| PUT | `/notification/preferences/{userId}` | 修改用户偏好 | 需 Token | `{ success: true }` |
| GET | `/notification/history/{userId}` | 查询用户通知历史 | 需 Token | `[ { content, time, channel, status }, ... ]` |
| GET | `/notification/template/{code}` | 获取模板内容 | 需 Admin Token | `{ content, language, subject }` |
| POST | `/notification/template` | 创建/更新模板 | 需 Admin Token | `{ id }` |
| GET | `/notification/stats/daily` | 获取当日统计 | 需 Admin Token | `{ sent, failed, success_rate }` |

> ✅ 所有路径前缀统一为 `/notification/**`  
> ✅ 所有写操作必须携带 **服务签名（HMAC）**，防伪造  
> ✅ 所有查询接口支持分页、排序、时间范围过滤

---

## ✅ 十、总结：Notification-Service 黄金法则（可打印贴墙上）

> ### ✅ **Notification-Service 必须做：**
> - 监听业务事件，自动触发通知
> - 支持短信、邮件、App、微信等多通道
> - 使用模板 + 变量，灵活适配业务变化
> - 尊重用户偏好，避免骚扰
> - 记录发送状态，失败自动重试
> - 提供退订机制，符合法规
> - 输出数据报表，持续优化策略

> ### ❌ **Notification-Service 绝对不能做：**
> - 不管订单、不碰钱
> - 不存用户密码、身份证
> - 不主动调用其他服务
> - 不硬编码内容
> - 不绕过用户偏好强行推送
> - 不用 Session
> - 不承担客服责任

> ### 🔑 **判断一切的标准：**
> > **“如果这个信息，是‘用户需要知道’且‘值得被打扰’的，那就是 Notification-Service 的责任。”**  
> > **“如果这个信息，是‘系统想卖东西’或‘我们想炫耀’的，那就别发——除非用户主动订阅。”**  
> > **“如果你怕用户投诉‘怎么老是发广告’，那说明你做对了 —— 你用了偏好控制。”**

---

## 🚀 下一步：为你打包完整项目模板

如果你希望我为你提供：
- ✅ **完整的 Notification-Service 项目结构（Maven + Spring Boot）**
- ✅ **Kafka 消费者监听 ORDER_PAID、COUPON_ISSUED 等事件**
- ✅ **模板引擎（Thymeleaf）+ 变量替换实现**
- ✅ **多通道发送器（短信/邮件/App/微信）**
- ✅ **用户偏好管理 + 防骚扰策略**
- ✅ **失败重试机制（指数退避）**
- ✅ **服务间签名验证（HMAC）**
- ✅ **Swagger API 文档 + 单元测试**
- ✅ **Dockerfile + Kubernetes 部署文件**
- ✅ **CI/CD Pipeline（GitLab CI）**

👉 请回复：  
**“请给我完整的 Notification-Service 工程模板！”**

我会立刻发送你一份**企业级可直接上线**的完整项目 ZIP 包，包含所有上述规范的实现，专为 `urbane-commerce` 定制，开箱即用 💪