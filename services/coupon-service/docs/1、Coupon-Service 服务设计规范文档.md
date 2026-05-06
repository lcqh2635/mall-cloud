当然可以！以下是为你的 **`urbane-commerce` 电商微服务系统** 量身定制的《**Coupon-Service 服务设计规范文档**》，全面、系统、可落地，明确界定：

✅ **Coupon-Service 的职责与作用**  
✅ **必须做的核心功能（推荐）**  
❌ **禁止或不推荐的行为（严禁做）**  
🔍 **判断标准与核心设计原则**  
📌 **真实生产环境最佳实践**

---

# 📜《urbane-commerce Coupon-Service 服务设计规范》
> **版本：1.8 | 最后更新：2025年4月 | 适用架构：Spring Boot + MySQL + Redis + Kafka + Nacos + 分布式锁**

---

## 🧭 一、Coupon-Service 角色定位（Why Coupon-Service？）

> **Coupon-Service 是整个电商系统中负责“优惠券全生命周期管理”的核心服务。**

它是**营销体系的核心引擎**，连接用户、商品、订单、促销活动，是提升转化率、刺激复购、实现精准运营的关键基础设施。

| 角色 | 说明 |
|------|------|
| ✅ **优惠券中心** | 管理所有优惠券模板（类型、规则、面额、有效期） |
| ✅ **优惠券发放中心** | 向用户发放优惠券（主动推送、活动领取、签到奖励） |
| ✅ **优惠券核销引擎** | 在下单时校验优惠券是否可用、是否已使用、是否过期 |
| ✅ **优惠券状态管理** | 记录每张券的状态：未领取、已领取、已使用、已过期、已作废 |
| ✅ **优惠券使用统计** | 统计发放量、使用率、核销金额、ROI，支撑运营分析 |
| ❌ **非支付服务** | 不处理资金流转 —— 那是 `payment-gateway` 的事 |
| ❌ **非订单服务** | 不创建订单、不扣减库存 —— 那是 `order-service` 的事 |
| ❌ **非用户服务** | 不管理用户身份、积分、等级 —— 那是 `auth-service` / `user-service` 的事 |
| ❌ **非网关** | 不负责路由、认证、限流 |
| ❌ **非商品服务** | 不维护商品信息、价格、类目 —— 那是 `product-service` 的事 |

> 💡 **一句话总结**：  
> **Coupon-Service 回答：“这张券能用吗？怎么用？用了能省多少钱？”**  
> 它不关心你买了什么 —— 那是 `order-service` 的事；  
> 它也不关心你有没有钱付 —— 那是 `payment-gateway` 的事；  
> 它只关心：**这张券是谁的？能不能用？用了会不会超发？**

---

## ✅ 二、推荐在 Coupon-Service 必须做的事情（核心职责）

### 1. ✅ **优惠券模板管理（Template）**
定义优惠券的“类型”和“规则”，作为发放依据：

```json
{
  "id": 1001,
  "name": "满800减100",
  "type": "FULL_REDUCTION",        // 满减
  "value": 100,                    // 减100元
  "condition": 800,                // 满800可用
  "limit_per_user": 1,             // 每人限领1张
  "total_quantity": 10000,         // 总共发放1万张
  "issued_quantity": 3200,         // 已发放数量
  "start_time": "2025-04-01T00:00:00Z",
  "end_time": "2025-04-30T23:59:59Z",
  "usable_scopes": ["ALL"],        // 可用范围：ALL/PRODUCTS/CATEGORIES
  "usable_products": [123, 456],   // 仅限指定商品（可选）
  "usable_categories": [789],      // 仅限指定类目（可选）
  "exclude_promos": true,          // 是否不能与其他优惠叠加
  "created_by": "marketing-team"
}
```

支持操作：
- 新增/编辑/下架优惠券模板
- 设置生效时间、限量、使用条件
- 批量导入导出（Excel）

> ✅ **关键点**：模板是“蓝图”，不是实际券，实际券是它的实例

---

### 2. ✅ **优惠券发放（Issue）**
根据策略向用户发放实体券：

| 发放方式 | 实现方式 |
|----------|----------|
| **活动领取** | 用户点击“立即领取” → 校验资格 → 发放 |
| **签到奖励** | 用户连续签到第3天 → 自动发放 |
| **新用户礼包** | 注册成功 → 自动发放新人券 |
| **运营推送** | 运营后台选择用户群 → 批量发放 |
| **订单返利** | 订单完成 → 发放下次使用券 |

```http
POST /coupon/issue
{
  "templateId": 1001,
  "userId": 123,
  "source": "SIGN_IN"  // 来源：ACTIVITY, SIGN_IN, NEW_USER, REWARD
}
→ 返回 { couponId: 2001, status: "ISSUED", code: "CUP20250405ABCD" }
```

> ✅ **并发控制**：使用 Redis + Lua 脚本原子判断是否超发
> ```lua
> if redis.call('GET', 'coupon:used:'..templateId) < total_quantity then
>     redis.call('INCR', 'coupon:used:'..templateId)
>     return 1
> else
>     return 0
> end
> ```

> ✅ **防重复领取**：记录 `user_id + template_id`，避免同一用户多次领取

---

### 3. ✅ **优惠券查询与列表（Query）**
提供接口供前端展示用户可用券：

```http
GET /coupon/list?userId=123&status=AVAILABLE
```

响应：
```json
{
  "total": 3,
  "items": [
    {
      "id": 2001,
      "template_id": 1001,
      "code": "CUP20250405ABCD",
      "type": "FULL_REDUCTION",
      "value": 100,
      "condition": 800,
      "start_time": "2025-04-01T00:00:00Z",
      "end_time": "2025-04-30T23:59:59Z",
      "status": "AVAILABLE",
      "usable_scopes": ["ALL"],
      "received_at": "2025-04-05T10:30:00Z"
    },
    ...
  ]
}
```

> ✅ 支持筛选：
> - `status`: AVAILABLE / USED / EXPIRED / INVALID
> - `type`: FULL_REDUCTION / DISCOUNT / FREE_SHIPPING / CASH
> - `is_usable_now`: 是否当前可使用（考虑时间、门槛、商品限制）

---

### 4. ✅ **优惠券核销校验（Validate & Use）**
当用户下单时，`order-service` 调用此接口验证：

```http
POST /coupon/validate
{
  "couponCode": "CUP20250405ABCD",
  "userId": 123,
  "orderAmount": 999,
  "productIds": [123, 456],
  "categoryId": 789
}
```

返回：
```json
{
  "valid": true,
  "discountAmount": 100,
  "reason": "",
  "details": {
    "type": "FULL_REDUCTION",
    "condition": 800,
    "value": 100,
    "remainingUses": 0,
    "expired": false,
    "usableOnProducts": true,
    "usableOnCategory": true,
    "canStack": false
  }
}
```

> ✅ **校验逻辑**：
> 1. 券是否存在？
> 2. 是否属于该用户？
> 3. 是否已使用/过期/作废？
> 4. 订单金额 ≥ 门槛？
> 5. 商品是否在允许范围内？
> 6. 是否允许叠加其他优惠？
> 7. 是否超过每人限领次数？

> ⚠️ **关键设计**：  
> **核销必须是幂等的** —— 同一张券只能使用一次！

> ✅ **原子核销**：使用数据库事务 + 悲观锁 或 Redis 分布式锁，防止并发使用

```java
@Transactional
public boolean useCoupon(Long couponId, Long orderId) {
    Coupon coupon = couponRepository.findByIdForUpdate(couponId); // 悲观锁
    if (coupon.getStatus() != CouponStatus.AVAILABLE) return false;
    
    coupon.setStatus(CouponStatus.USED);
    coupon.setUsedAt(LocalDateTime.now());
    coupon.setUsedOrderId(orderId);
    couponRepository.save(coupon);
    
    return true;
}
```

---

### 5. ✅ **优惠券使用统计与报表**
- 实时统计：
    - 每日发放量、使用量、使用率
    - 各类型券的 ROI（投入产出比）
    - 用户领取偏好（哪些券最受欢迎）
- 异步写入数据仓库（如 ClickHouse），供 BI 分析

> ✅ 示例指标：
> ```
> 2025-04-05:
>   issued: 12,345
>   used: 4,567
>   usage_rate: 37%
>   total_discount: ¥456,789
>   top_template: "满800减100" (占比 42%)
> ```

> 🔗 可对接 PowerBI / Superset 做可视化看板

---

### 6. ✅ **优惠券自动过期与回收**
- 使用定时任务（XXL-JOB）每日扫描：
    - 过期未使用的券 → 状态改为 `EXPIRED`
    - 未领取的券 → 若总量 > 90% 未发完 → 自动关闭发放
- 支持手动“作废”（如发现漏洞、活动错误）

> ✅ 作废后：
> - 已领取但未使用的券 → 状态变 `INVALID`
> - 已使用的券 → 保持原状（用于审计）

---

### 7. ✅ **优惠券事件驱动（Event-Driven）**
在关键节点发布事件，通知其他服务：

| 事件名称 | 触发时机 | 消费方 |
|----------|-----------|--------|
| `COUPON_ISSUED` | 用户领取成功 | `notification-service`（发短信）、`user-service`（更新标签） |
| `COUPON_USED` | 用户下单使用 | `order-service`（记录优惠明细）、`user-service`（增加消费额） |
| `COUPON_EXPIRED` | 自动过期 | `marketing-service`（发送“券快过期”提醒） |
| `COUPON_INVALIDATED` | 手动作废 | `admin-service`（告警）、`user-service`（通知用户） |

> ✅ 使用 Kafka 实现异步解耦，保障高可用

---

## ❌ 三、禁止或不推荐在 Coupon-Service 做的事情（严禁做）

| 行为 | 为什么不推荐？ | 后果 | 正确做法 |
|------|----------------|------|----------|
| **1. 直接计算订单最终价格** | 价格计算是 `order-service` 的职责 | 耦合严重，业务逻辑混乱 | ✅ 仅返回 `discountAmount`，由 `order-service` 决定如何应用 |
| **2. 直接调用 payment-gateway 扣款** | 优惠券是折扣，不是支付手段 | 架构越界，安全风险高 | ✅ 只影响订单金额，支付仍走独立通道 |
| **3. 存储用户密码、Token、手机号** | 违反最小权限原则 | 泄露风险极高，违反 GDPR | ✅ 仅依赖 `user_id`，不存储任何敏感信息 |
| **4. 允许前端传入优惠券码并直接使用** | 前端不可信，可能伪造 | 黑产刷券、薅羊毛 | ✅ 所有核销必须通过服务端校验，且需绑定 `user_id` |
| **5. 在券中硬编码商品价格或库存** | 商品价格会变，库存会动 | 导致券失效或超卖 | ✅ 券只关联 `product_ids` 或 `category_ids`，具体价格由 `product-service` 提供 |
| **6. 使用 Session 或 Cookie 管理用户状态** | 与无状态架构冲突 | 无法水平扩展 | ✅ 所有请求必须携带 `X-User-ID`，由网关注入 |
| **7. 直接访问 order-service 数据库查订单** | 破坏微服务边界 | 一个服务挂了，优惠券也瘫痪 | ✅ 通过 `COUPON_USED` 事件接收使用信息，不主动查询 |
| **8. 不做并发控制导致超发** | 多人同时领取 → 发放超过上限 | 企业损失巨大 | ✅ 使用 Redis + Lua 原子操作控制发放数量 |
| **9. 不设置券的有效期或使用门槛** | 用户领了不用，浪费资源 | 营销效果差、成本高 | ✅ 每张券必须有明确起止时间、最低消费要求 |
| **10. 将优惠券与用户积分混用** | 积分是另一套体系 | 混淆概念，用户体验混乱 | ✅ 积分由 `user-service` 管理，优惠券由 `coupon-service` 管理，两者独立 |

---

## 🔍 四、判断标准与核心设计原则

| 原则 | 说明 | 应用示例 |
|------|------|----------|
| **✅ 单一职责原则（SRP）** | 一个服务只做一件事 | Coupon-Service 只管“券”，不管“买”“付”“发” |
| **✅ 数据隔离（Data Isolation）** | 每个服务拥有自己的数据库 | Coupon-Service 有独立的 `coupons`, `templates`, `usage_logs` 表 |
| **✅ 事件驱动架构（EDA）** | 服务间通信靠事件，而非 RPC | 发放 → `COUPON_ISSUED` → 通知通知服务 |
| **✅ 幂等性设计（Idempotency）** | 同一操作多次执行结果相同 | 同一张券多次调用 `/use`，第二次返回失败 |
| **✅ 最终一致性（Eventual Consistency）** | 不追求强一致，但要保证最终一致 | 发放 → 记录 → 事件 → 其他服务异步更新 |
| **✅ 高并发控制（Concurrency Control）** | 防止超发、超用 | Redis Lua 脚本原子控制发放数量 |
| **✅ 安全默认（Secure by Default）** | 默认拒绝非法请求 | 所有核销必须验证 `user_id == coupon.owner_id` |
| **✅ 开闭原则（OCP）** | 对扩展开放，对修改关闭 | 新增一种券类型（如“免运费”），只需加配置，不改代码 |
| **✅ 可观测性优先（Observability）** | 所有操作必须可追踪 | 每次发放、核销、过期都记录日志 + 上报监控 |
| **✅ 用户体验优先（UX First）** | 券必须清晰、易用、无歧义 | “满800减100”必须显示清楚，不隐藏规则 |

---

## 🧩 五、典型场景对比：正确 vs 错误做法

| 场景 | 正确做法 | 错误做法 |
|------|----------|----------|
| **用户领取满减券** | 前端 → 网关 → `coupon-service`/issue → Redis 原子判断是否超发 → 成功返回券码 | 前端 → 网关 → `coupon-service` → 直接插入数据库 → 100人同时领取 → 发了10000张，系统崩溃 |
| **用户下单使用券** | `order-service` → `coupon-service`/validate → 校验通过 → 返回 discount=100 → `order-service` 扣减金额 | 前端传 `couponCode` 和 `discount=100` → `order-service` 直接接受 → 用户伪造金额，平台亏钱 |
| **券过期提醒** | 定时任务扫描 `EXPIRED` 券 → 发送 `COUPON_EXPIRED` 事件 → `notification-service` 发短信 | 运营手动发邮件给所有用户 → 效率低、成本高、打扰用户 |
| **多人抢购限量券** | 1000人同时抢 100 张 → Redis Lua 控制，只有前100人成功 | 每人请求都查 DB → 数据库压垮，最后没人领到 |
| **券被作废后** | 运营作废某券 → 所有未使用券状态变 `INVALID` → 用户看到“该券已失效” | 作废后不通知用户 → 用户发现券不能用，投诉平台欺诈 |
| **优惠券叠加** | 一张满减券 + 一张折扣券 → `coupon-service` 返回 `canStack: false` → `order-service` 只用一张 | 允许叠加 → 用户凑单使用两张券 → 平台多退 200 元 |

> ⚠️ **关键结论**：  
> **优惠券不是“免费钱”，而是“可控的营销工具”。**  
> 它必须**精确、安全、可追溯、防作弊**，否则就是企业的“财务黑洞”。

---

## 🛡️ 六、安全加固建议（生产环境必备）

| 措施 | 实现方式 |
|------|----------|
| **强制 HTTPS** | 所有接口仅支持 HTTPS，禁用 HTTP |
| **请求鉴权** | 所有写操作（发放、核销）必须携带有效 `X-User-ID`，由网关注入 |
| **输入过滤** | 过滤 XSS、SQL 注入、非法字符（如 `<script>`） |
| **频率限制** | 每个用户每分钟最多领取 3 张券，防刷 |
| **IP 黑名单** | 对恶意 IP（如代理、爬虫）封禁 |
| **券码加密** | 券码使用 UUID + Base64 编码，不可预测（如 `CUP20250405ABCD`） |
| **审计日志** | 记录所有发放、核销、作废行为：`{ action: "USE", userId: 123, couponId: 2001, ip: "..." }` |
| **GDPR 合规** | 支持“导出我的优惠券”、“删除优惠券历史” |
| **密钥管理** | JWT 密钥、API Key 使用 Vault 或 KMS 管理，不写配置文件 |
| **Redis 安全** | Redis 开启 ACL，禁止公网访问，使用 TLS 加密 |

---

## 📊 七、Coupon-Service 架构图（文字版）

```
[运营后台 / 活动系统]
     ↓ (管理员操作)
[Coupon-Service]
     ├── ✅ /coupon/template            ←─ 管理优惠券模板
     ├── ✅ /coupon/issue               ←─ 发放优惠券（带并发控制）
     ├── ✅ /coupon/list                ←─ 查询用户可用券
     ├── ✅ /coupon/validate            ←─ 核销校验（幂等）
     └── ✅ /coupon/invalidate          ←─ 手动作废
     ↓
[Database: MySQL]
     ├── coupon_templates (id, name, type, value, condition...)
     ├── coupons (id, template_id, user_id, code, status, received_at, used_at...)
     └── coupon_usage_logs (coupon_id, action, operator, ip, remark)

     ↑
[Kafka]
     ←─ EVENT: COUPON_ISSUED → 通知 notification-service、user-service
     ←─ EVENT: COUPON_USED → 通知 order-service、user-service
     ←─ EVENT: COUPON_EXPIRED → 通知 marketing-service
     ←─ EVENT: COUPON_INVALIDATED → 通知 admin-service

     ↑
[Redis Cluster]
     ←─ key: coupon:issued:1001 → Counter（已发数量）
     ←─ key: coupon:used:2001 → Boolean（是否已被使用）
     ←─ key: user:123:coupons → Set（用户拥有的券ID集合，用于快速查询）

     ↑
[Timer Job (XXL-JOB)]
     ←─ 每日凌晨扫描：到期未用券 → 状态改为 EXPIRED
     ←─ 每小时扫描：未发放完且已停发 → 自动关闭模板
```

> ✅ **注意**：  
> Coupon-Service **不主动调用其他服务**，只**发布事件**。  
> 所有外部依赖通过**异步事件驱动**解耦，实现高可用、高性能。

---

## ✅ 八、推荐技术栈（Spring Boot + 生态）

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| **框架** | Spring Boot 3.x | Java 17+，现代化开发 |
| **数据库** | MySQL 8.0 | 存储券模板、券实例、使用日志 |
| **缓存** | Redis 7.x | 并发控制、用户券列表缓存、状态标记 |
| **消息队列** | Apache Kafka | 异步发送事件（发放、使用、过期） |
| **定时任务** | XXL-JOB | 自动过期、清理、统计 |
| **服务注册** | Nacos | 服务发现与配置中心 |
| **HTTP 客户端** | Feign + Ribbon | 调用 `order-service`、`user-service` 获取上下文 |
| **分布式锁** | Redisson | 防止并发发放、并发核销 |
| **API 文档** | Swagger/OpenAPI 3.0 | 自动生成接口文档 |
| **日志** | Logback + ELK | 结构化日志，追踪每张券生命周期 |
| **监控** | Prometheus + Grafana | 监控发放成功率、核销率、异常率 |
| **安全** | Spring Security + JWT | 仅用于后台管理接口鉴权 |
| **工具类** | Lombok + MapStruct | 减少样板代码，DTO 映射自动化 |

---

## 📦 九、附录：Coupon-Service API 设计规范（RESTful）

| 方法 | 路径 | 描述 | 权限 | 返回 |
|------|------|------|------|------|
| GET | `/coupon/template` | 查询所有可用模板 | 需 Admin Token | `[template1, template2, ...]` |
| POST | `/coupon/template` | 创建模板 | 需 Admin Token | `{ id }` |
| PUT | `/coupon/template/{id}` | 修改模板 | 需 Admin Token | `{ success: true }` |
| DELETE | `/coupon/template/{id}` | 下架模板 | 需 Admin Token | `{ success: true }` |
| POST | `/coupon/issue` | 发放券给用户 | 需 Token | `{ couponId, code, status }` |
| GET | `/coupon/list` | 查询用户可用券 | 需 Token | `[ coupon1, coupon2, ... ]` |
| POST | `/coupon/validate` | 校验券是否可用 | 需 Token | `{ valid, discountAmount, reason, details }` |
| POST | `/coupon/use` | 核销券（内部调用） | 需 Token | `{ success, usedAt }` |
| POST | `/coupon/invalidate` | 手动作废券 | 需 Admin Token | `{ success }` |
| GET | `/coupon/report/daily` | 获取日报 | 需 Admin Token | `{ date, issued, used, rate, amount }` |

> ✅ 所有路径前缀统一为 `/coupon/**`  
> ✅ 所有写操作必须验证 `X-User-ID`（普通用户）或 `X-Admin-ID`（运营）  
> ✅ 所有券码必须唯一、不可预测、含时间戳

---

## ✅ 十、总结：Coupon-Service 黄金法则（可打印贴墙上）

> ### ✅ **Coupon-Service 必须做：**
> - 管理优惠券模板（类型、规则、期限）
> - 安全发放（防超发、防刷领）
> - 精准核销（防作弊、防重复使用）
> - 支持多种使用条件（满减、品类限制）
> - 输出运营数据（使用率、ROI）
> - 保障**每一张券都可控、可追溯、防滥用**

> ### ❌ **Coupon-Service 绝对不能做：**
> - 不管支付、不碰钱
> - 不算订单总价、不决定最终金额
> - 不存用户密码、Token、手机号
> - 不调用其他服务数据库
> - 不允许前端传金额、券码直接使用
> - 不用 Session
> - 不删除历史数据（软删除）

> ### 🔑 **判断一切的标准：**
> > **“如果这个动作，是‘用户想省钱’之前的准备环节，那就是 Coupon-Service 的责任。”**  
> > **“如果这个动作，是‘用户决定买’之后的事，那就别插手——交给 order-service。”**  
> > **“如果你怕有人薅羊毛，那说明你做对了 —— 你用了原子控制。”**

---

## 🚀 下一步：为你打包完整项目模板

如果你希望我为你提供：
- ✅ **完整的 Coupon-Service 项目结构（Maven + Spring Boot）**
- ✅ **Redis Lua 脚本实现防超发（原子发放）**
- ✅ **优惠券核销幂等性控制（悲观锁 + 事务）**
- ✅ **优惠券模板管理（CRUD + 条件校验）**
- ✅ **Kafka 发送 COUPON_ISSUED / COUPON_USED 事件**
- ✅ **定时任务自动过期（XXL-JOB）**
- ✅ **JWT 鉴权 + 管理员权限控制**
- ✅ **Swagger API 文档 + 单元测试**
- ✅ **Dockerfile + Kubernetes 部署文件**
- ✅ **CI/CD Pipeline（GitLab CI）**

👉 请回复：  
**“请给我完整的 Coupon-Service 工程模板！”**

我会立刻发送你一份**企业级可直接上线**的完整项目 ZIP 包，包含所有上述规范的实现，专为 `urbane-commerce` 定制，开箱即用 💪