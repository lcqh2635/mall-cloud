当然可以！以下是为你的 **`urbane-commerce` 电商微服务系统** 量身定制的《**Inventory-Service 服务设计规范文档**》，全面、系统、可落地，明确界定：

✅ **Inventory-Service 的职责与作用**  
✅ **必须做的核心功能（推荐）**  
❌ **禁止或不推荐的行为（严禁做）**  
🔍 **判断标准与核心设计原则**  
📌 **真实生产环境最佳实践**

---

# 📜《urbane-commerce Inventory-Service 服务设计规范》
> **版本：1.9 | 最后更新：2025年4月 | 适用架构：Spring Boot + MySQL + Redis + Kafka + 分布式锁 + 乐观锁**

---

## 🧭 一、Inventory-Service 角色定位（Why Inventory-Service？）

> **Inventory-Service 是整个电商系统中负责“商品库存精准管理”的核心服务。**

它是**防止超卖、保障交易履约、支撑高并发秒杀**的关键基础设施，是**订单系统能否成功下单的底层基石**。

| 角色 | 说明 |
|------|------|
| ✅ **库存管理中心** | 管理每个 SKU 的实时可用库存数量 |
| ✅ **库存预占引擎** | 在用户加购或下单前，临时锁定库存，防止并发超卖 |
| ✅ **库存扣减与释放** | 在订单创建时正式扣减，在取消/超时后释放库存 |
| ✅ **多仓库存管理** | 支持多个仓库（如北京仓、上海仓），智能分配库存 |
| ✅ **库存预警与补货提醒** | 当库存低于阈值时，自动触发通知 |
| ✅ **库存同步与对账** | 与外部系统（ERP、WMS）对接，保证数据一致性 |
| ❌ **非订单服务** | 不创建订单、不处理支付 —— 那是 `order-service` 的事 |
| ❌ **非商品服务** | 不维护商品名称、价格、类目 —— 那是 `product-service` 的事 |
| ❌ **非促销服务** | 不计算优惠券、满减规则 —— 那是 `coupon-service` 的事 |
| ❌ **非网关** | 不负责路由、认证、限流 |
| ❌ **非用户服务** | 不管理用户身份、积分、等级 —— 那是 `auth-service` / `user-service` 的事 |

> 💡 **一句话总结**：  
> **Inventory-Service 回答：“这个商品还有多少能卖？”**  
> 它不关心你买了什么 —— 那是 `order-service` 的事；  
> 它也不关心你能不能打折 —— 那是 `coupon-service` 的事；  
> 它只关心：**库存是否真实、是否可锁、是否可扣、是否可回滚。**

> ⚠️ **重要性**：  
> 一个库存系统出错，可能导致：
> - 用户下单成功 → 实际无货 → 退款 + 客诉
> - 秒杀活动超卖 → 企业巨额赔偿
> - 多仓库存混乱 → 发错货、物流成本飙升

---

## ✅ 二、推荐在 Inventory-Service 必须做的事情（核心职责）

### 1. ✅ **SKU 库存模型管理（核心数据结构）**
每个 SKU（销售单元）对应唯一库存记录：

```json
{
  "sku_id": 789,
  "total_stock": 1000,      // 总库存
  "available_stock": 800,   // 可售库存 = 总库存 - 已预占 - 已锁定
  "locked_stock": 150,      // 已被预占但未确认（购物车加购）
  "reserved_stock": 50,     // 已被订单锁定（待支付）
  "warehouse_id": "WH-BJ",  // 所属仓库
  "last_updated": "2025-04-05T10:30:00Z"
}
```

> ✅ **关键字段说明**：
> - `available_stock` = `total_stock - locked_stock - reserved_stock`
> - `locked_stock`：由 `cart-service` 预占（最长5分钟）
> - `reserved_stock`：由 `order-service` 创建订单时锁定（等待支付）
> - 所有操作必须原子化，避免竞态条件

---

### 2. ✅ **库存预占（Pre-lock / Pre-allocate）**
当用户将商品加入购物车时，`cart-service` 发送事件：

```json
{
  "type": "PRE_ALLOCATE_STOCK",
  "userId": 123,
  "items": [
    { "skuId": 789, "quantity": 2 },
    { "skuId": 101, "quantity": 1 }
  ],
  "ttl": 300  // 预占有效期：5分钟
}
```

→ Inventory-Service 做以下操作：
1. 检查 `available_stock >= quantity`
2. 若满足 → 将 `available_stock -= quantity`，`locked_stock += quantity`
3. 写入 Redis 缓存：`inventory:lock:sku_789:uid_123`，TTL=300s
4. 返回 `{ success: true, allocated: [ { skuId, qty } ] }`
5. 若不足 → 返回 `{ success: false, insufficient: [789] }`

> ✅ **实现方式**：
> - 使用 **Redis + Lua 脚本** 实现原子操作（防并发超发）
> - 同时更新 MySQL 数据库（持久化）
> - **不直接扣减总库存，仅预占**

```lua
-- Redis Lua 脚本示例（原子预占）
local sku = KEYS[1]
local qty = tonumber(ARGV[1])
local lockKey = "inventory:lock:" .. sku

if redis.call('GET', sku .. ':available') >= qty then
    redis.call('DECRBY', sku .. ':available', qty)
    redis.call('INCRBY', sku .. ':locked', qty)
    redis.call('SETEX', lockKey, ARGV[2], 1) -- 设置过期时间
    return 1
else
    return 0
end
```

> ✅ **超时自动释放**：Redis TTL 到期后，后台任务自动恢复库存

---

### 3. ✅ **库存正式扣减（Deduct）**
当用户完成下单，`order-service` 发送事件：

```json
{
  "type": "DEDUCT_STOCK",
  "orderId": 456,
  "items": [
    { "skuId": 789, "quantity": 2 },
    { "skuId": 101, "quantity": 1 }
  ]
}
```

→ Inventory-Service 做：
1. 校验该订单是否已扣减（幂等）
2. 检查 `locked_stock >= quantity`（必须是预占过的）
3. 执行：
   ```sql
   UPDATE inventory 
   SET 
     available_stock = available_stock - :qty,
     locked_stock = locked_stock - :qty,
     total_sold = total_sold + :qty
   WHERE sku_id = :sku_id AND locked_stock >= :qty
   ```
4. 使用 **数据库乐观锁**（version 字段）防止并发冲突
5. 成功 → 返回 `{ success: true }`  
   失败 → 返回 `{ success: false, reason: "库存已被其他订单占用" }`

> ✅ **为什么用乐观锁？**  
> 高并发下悲观锁性能差，乐观锁通过 version 控制，失败后重试更高效

```java
@Version
private Integer version; // 数据库字段

// 更新时：
int rows = repository.updateBySkuAndVersion(skuId, qty, currentVersion);
if (rows == 0) {
    throw new StockConflictException("库存已被修改，请重试");
}
```

---

### 4. ✅ **库存释放（Release）**
以下情况需释放库存：

| 场景 | 触发方式 | 动作 |
|------|----------|------|
| 用户取消订单 | `order-service` 发送 `ORDER_CANCELLED` 事件 | 释放 `reserved_stock` |
| 订单超时未支付 | 定时任务扫描 > 30min 未支付订单 | 释放 `reserved_stock` |
| 预占超时 | Redis TTL 过期 + 后台任务 | 释放 `locked_stock` |
| 物流失败 | `logistics-service` 发送 `DELIVERY_FAILED` | 释放 `reserved_stock` |

> ✅ **释放逻辑**：
> ```java
> update inventory 
> set available_stock = available_stock + :qty,
>     locked_stock = locked_stock - :qty
> where sku_id = :sku_id
> ```

> ⚠️ **注意**：  
> 一旦订单状态变为 `PAID` 或 `COMPLETED`，库存不可再释放！

---

### 5. ✅ **多仓库存管理（Multi-Warehouse）**
支持分布式仓储，按策略智能分配：

| 策略 | 说明 |
|------|------|
| **就近发货** | 根据用户地址匹配最近仓库 |
| **库存优先** | 优先使用库存最多的仓库 |
| **成本最低** | 选择物流成本最低的仓库 |
| **预售仓** | 预售商品走专用仓 |

```json
{
  "sku_id": 789,
  "warehouses": [
    { "id": "WH-BJ", "stock": 500, "priority": 1 },
    { "id": "WH-SH", "stock": 300, "priority": 2 },
    { "id": "WH-GZ", "stock": 100, "priority": 3 }
  ]
}
```

→ `order-service` 请求 `/inventory/distribute?skuId=789&addr=xxx`  
→ 返回最合适的仓库及可用库存

> ✅ 支持跨仓调拨（通过 `transfer-service` 异步执行）

---

### 6. ✅ **库存预警与补货提醒**
设置库存阈值，自动触发告警：

```yaml
# application.yml
inventory:
  alert:
    low_stock_threshold: 10    # 低于10件预警
    out_of_stock_threshold: 0  # 无货时触发
    notify_channels: ["email", "dingtalk"]
```

→ 当 `available_stock <= 10` 时：
- 发送事件 `STOCK_LOW_ALERT` → `notification-service` 发钉钉/邮件
- 推送至运营后台看板

> ✅ 可联动采购系统（ERP）自动生成补货单

---

### 7. ✅ **库存对账与异步同步**
与外部系统（如 ERP、WMS、第三方仓库）保持数据一致：

| 方向 | 机制 |
|------|------|
| **外部 → 内部** | WMS 每小时推送库存变更 → Inventory-Service 接收并更新 |
| **内部 → 外部** | 每日凌晨同步全量库存给 ERP |
| **异常检测** | 每日比对差异 > 5% → 自动告警 |

> ✅ 使用 Kafka 异步消费消息，确保最终一致性

---

### 8. ✅ **秒杀库存专项优化**
针对高并发场景（如双11、新品首发）：

| 优化点 | 实现方式 |
|--------|----------|
| **库存分片** | 将 1000 件拆成 10 个分片（每片100），分散请求压力 |
| **本地缓存** | Redis 缓存库存快照，减少 DB 压力 |
| **队列削峰** | 使用 RabbitMQ/Kafka 缓冲请求，异步扣减 |
| **限流降级** | 超过 QPS 阈值 → 返回“库存紧张” |
| **预热加载** | 活动前 10 分钟预加载库存到 Redis |

> ✅ 示例：秒杀 SKU 789，总库存 1000，分 10 片：
> - 每片库存：100
> - 每片独立 Redis Key：`inventory:skusec:789:shard:1`, ..., `:shard:10`
> - 请求随机打到某一片，降低热点竞争

---

## ❌ 三、禁止或不推荐在 Inventory-Service 做的事情（严禁做）

| 行为 | 为什么不推荐？ | 后果 | 正确做法 |
|------|----------------|------|----------|
| **1. 直接参与订单创建** | 订单是 `order-service` 的职责 | 耦合严重，无法独立部署 | ✅ 只响应 `DEDUCT_STOCK` 事件，不主动调用 order-service |
| **2. 计算商品价格或优惠** | 价格是 `product-service`，优惠是 `coupon-service` | 架构混乱，重复逻辑 | ✅ 只管“有多少”，不管“多少钱” |
| **3. 存储用户信息（如 userId、手机号）** | 违反最小权限原则 | 泄露风险高，违反 GDPR | ✅ 仅记录 `orderId`，不关联用户隐私 |
| **4. 允许前端直接传库存数量或操作指令** | 前端不可信，可能伪造 | 黑产刷库存、篡改数据 | ✅ 所有操作必须由内部服务（cart/order）发起，带签名 |
| **5. 使用 Session 或 Cookie 管理状态** | 与无状态架构冲突 | 无法水平扩展 | ✅ 所有请求基于事件驱动，无会话 |
| **6. 直接访问其他服务数据库（如查订单）** | 破坏微服务边界 | 一个服务挂了，库存也瘫痪 | ✅ 通过 Kafka 事件接收订单状态变化 |
| **7. 不做并发控制导致超卖** | 多人同时下单 → 库存扣成负数 | 企业赔钱、客户投诉、品牌受损 | ✅ 必须使用 Redis Lua + 乐观锁 + 分片 |
| **8. 不设置库存预占超时** | 用户加购后不付款 → 库存长期锁定 | 导致真实买家买不到 | ✅ 预占必须有 TTL（建议 5~15 分钟） |
| **9. 在库存中硬编码商品名称、价格** | 商品信息会变，库存应独立 | 导致历史数据错误 | ✅ 只存 `sku_id`，具体信息从 `product-service` 获取 |
| **10. 使用悲观锁（SELECT FOR UPDATE）处理高并发** | 性能极差，容易死锁 | 秒杀时服务器崩溃 | ✅ 使用乐观锁 + Redis + 分片 |

---

## 🔍 四、判断标准与核心设计原则

| 原则 | 说明 | 应用示例 |
|------|------|----------|
| **✅ 单一职责原则（SRP）** | 一个服务只做一件事 | Inventory-Service 只管“库存”，不管“订单”“价格”“用户” |
| **✅ 数据隔离（Data Isolation）** | 每个服务拥有自己的数据库 | 库存表独立，不共享 product-service 或 order-service 的表 |
| **✅ 幂等性设计（Idempotency）** | 同一操作多次执行结果相同 | 同一订单多次发送 `DEDUCT_STOCK`，第二次返回成功但无变化 |
| **✅ 事件驱动架构（EDA）** | 服务间通信靠事件，而非 RPC | `cart-service` 发 `PRE_ALLOCATE` → `inventory-service` 收到处理 |
| **✅ 最终一致性（Eventual Consistency）** | 不追求强一致，但要保证最终一致 | 加购预占 → 下单扣减 → 取消释放，允许几秒延迟 |
| **✅ 高并发抗压能力（High Concurrency）** | 必须支持万级 QPS | 使用 Redis + 分片 + Lua + 乐观锁，避免数据库瓶颈 |
| **✅ 安全默认（Secure by Default）** | 默认拒绝非法请求 | 所有扣减必须来自可信服务（带签名或 Token） |
| **✅ 可观测性优先（Observability）** | 所有操作必须可追踪 | 每次预占、扣减、释放都记录日志 + 上报监控 |
| **✅ 开闭原则（OCP）** | 对扩展开放，对修改关闭 | 新增一种库存类型（如“预售”），只需加配置，不改代码 |
| **✅ 事务补偿机制（Saga Pattern）** | 操作失败时可回滚 | 预占失败 → 不扣减；扣减失败 → 释放预占 |

---

## 🧩 五、典型场景对比：正确 vs 错误做法

| 场景 | 正确做法 | 错误做法 |
|------|----------|----------|
| **用户加购最后一件商品** | `cart-service` → 发 `PRE_ALLOCATE` → Redis Lua 原子判断 → 成功锁定 → 用户看到“库存紧张” | `cart-service` → 查询 DB 得到 1 件 → 未加锁 → 10 人同时加购 → 全部成功 → 最后一人下单时发现无货 → 超卖 |
| **用户下单购买** | `order-service` → 发 `DEDUCT_STOCK` → 乐观锁更新 → 成功 → 库存从 800 → 798 | `order-service` → 直接写 SQL `UPDATE stock = stock - 1` → 无版本控制 → 两个请求同时执行 → 库存从 1 → -1 |
| **秒杀活动开始** | 预热库存到 Redis，分 10 片，请求随机打到不同分片，Redis 扣减，异步落库 | 所有请求直接打到 MySQL，DB 压垮，响应超时，用户全部失败 |
| **用户取消订单** | `order-service` → 发 `ORDER_CANCELLED` → `inventory-service` 释放 `reserved_stock` → 库存恢复 | 用户取消后，库存仍被锁定，直到超时 → 真实买家买不到 |
| **多仓发货** | `order-service` → 调用 `/inventory/distribute?addr=xxx` → 返回最优仓 → 下单扣该仓库存 | `order-service` 自己选仓 → 选了远的仓 → 物流成本翻倍，用户收货慢 |
| **库存预警** | 库存 < 10 件 → 发送 `STOCK_LOW_ALERT` → 运营收到钉钉 → 补货 | 没有预警 → 库存归零才发现 → 错失销售机会 |

> ⚠️ **关键结论**：  
> **库存不是“静态数字”，而是“动态资源”。**  
> 它必须**精确、安全、可追溯、抗高并发**，否则就是企业的“定时炸弹”。

---

## 🛡️ 六、安全加固建议（生产环境必备）

| 措施 | 实现方式 |
|------|----------|
| **强制 HTTPS** | 所有接口仅支持 HTTPS，禁用 HTTP |
| **请求鉴权** | 所有写操作（扣减、预占）必须携带服务签名（JWT 或 HMAC） |
| **输入过滤** | 过滤 XSS、SQL 注入、非法字符（如 `<script>`） |
| **频率限制** | 每个 SKU 每秒最多 100 次请求，防刷 |
| **IP 白名单** | 仅允许 `order-service`、`cart-service`、`cron-job` 调用 |
| **审计日志** | 记录所有库存变更：`{ action: "DEDUCT", skuId: 789, orderId: 456, qty: 2, ip: "10.0.0.1", user: "order-service" }` |
| **GDPR 合规** | 不存储用户敏感信息，不保留个人关联数据 |
| **密钥管理** | 服务间通信密钥使用 Vault 或 KMS 管理 |
| **Redis 安全** | Redis 开启 ACL，禁止公网访问，使用 TLS 加密 |
| **备份策略** | 每日全量备份 MySQL，异地灾备 |

---

## 📊 七、Inventory-Service 架构图（文字版）

```
[Cart-Service]
     ↓ (事件：PRE_ALLOCATE_STOCK)
[Kafka]
     ←─ EVENT: PRE_ALLOCATE_STOCK → Inventory-Service
     ←─ EVENT: DEDUCT_STOCK        → Inventory-Service
     ←─ EVENT: ORDER_CANCELLED     → Inventory-Service
     ←─ EVENT: DELIVERY_FAILED     → Inventory-Service

     ↑
[Inventory-Service]
     ├── ✅ /inventory/pre-allocate  ←─ Redis Lua 原子预占
     ├── ✅ /inventory/deduct        ←─ 乐观锁扣减
     ├── ✅ /inventory/release       ←─ 释放库存
     ├── ✅ /inventory/distribute    ←─ 多仓分配
     └── ✅ /inventory/alert         ←─ 库存预警
     ↓
[Database: MySQL]
     └── inventories (sku_id, total_stock, available_stock, locked_stock, reserved_stock, version, warehouse_id)

     ↑
[Redis Cluster]
     ├── key: inventory:sku:789:available → Integer
     ├── key: inventory:sku:789:locked   → Integer
     ├── key: inventory:lock:789:uid:123 → String (TTL=300s)
     └── key: inventory:shard:789:1..10  → 分片库存（秒杀专用）

     ↑
[Timer Job (XXL-JOB)]
     ←─ 每分钟扫描：超时预占 → 自动释放 locked_stock
     ←─ 每小时扫描：库存差异 → 同步 ERP/WMS
     ←─ 每日凌晨：生成库存报表

     ↑
[ERP / WMS / Third-party System]
     ←─ 每日同步全量库存（异步 Kafka）
     ←─ 实时推送库存变更（异步 Kafka）
```

> ✅ **注意**：  
> Inventory-Service **不主动调用任何外部服务**，只**监听事件**。  
> 所有外部依赖通过**Kafka 事件驱动**解耦，实现高可用、高性能、高可靠。

---

## ✅ 八、推荐技术栈（Spring Boot + 生态）

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| **框架** | Spring Boot 3.x | Java 17+，现代化开发 |
| **数据库** | MySQL 8.0 | 主库存储，支持乐观锁（version 字段） |
| **缓存** | Redis 7.x | 高频读写、原子操作、分片存储、TTL 控制 |
| **消息队列** | Apache Kafka | 异步接收预占、扣减、释放事件 |
| **定时任务** | XXL-JOB | 自动释放超时预占、库存同步、报表生成 |
| **服务注册** | Nacos | 服务发现与配置中心 |
| **分布式锁** | Redisson | 用于复杂业务流程锁（如跨仓调拨） |
| **HTTP 客户端** | Feign + Ribbon | 调用 `product-service` 获取 SKU 详情（可选） |
| **API 文档** | Swagger/OpenAPI 3.0 | 自动生成接口文档 |
| **日志** | Logback + ELK | 结构化日志，追踪每笔库存变更 |
| **监控** | Prometheus + Grafana | 监控 QPS、扣减成功率、超卖率、Redis 命中率 |
| **安全** | JWT + HMAC | 服务间通信签名验证，防伪造 |
| **工具类** | Lombok + MapStruct | 减少样板代码，DTO 映射自动化 |

---

## 📦 九、附录：Inventory-Service API 设计规范（RESTful）

| 方法 | 路径 | 描述 | 权限 | 返回 |
|------|------|------|------|------|
| POST | `/inventory/pre-allocate` | 预占库存（由 cart-service 调用） | 需服务签名 | `{ success, allocated: [{skuId, qty}], failed: [] }` |
| POST | `/inventory/deduct` | 正式扣减库存（由 order-service 调用） | 需服务签名 | `{ success, reason }` |
| POST | `/inventory/release` | 释放库存（由 order-service 调用） | 需服务签名 | `{ success }` |
| GET | `/inventory/distribute` | 智能分配仓库（根据地址） | 需服务签名 | `{ warehouseId, availableStock }` |
| GET | `/inventory/status/{skuId}` | 查询库存状态 | 需服务签名 | `{ total, available, locked, reserved }` |
| POST | `/inventory/alert` | 手动触发库存预警 | 需 Admin Token | `{ triggered: true }` |
| GET | `/inventory/report/daily` | 获取日报 | 需 Admin Token | `{ date, total_sold, low_stock_skus }` |

> ✅ 所有路径前缀统一为 `/inventory/**`  
> ✅ 所有写操作必须验证 **服务签名**（非用户 Token）  
> ✅ 所有接口必须幂等，支持重试

---

## ✅ 十、总结：Inventory-Service 黄金法则（可打印贴墙上）

> ### ✅ **Inventory-Service 必须做：**
> - 精准管理每个 SKU 的库存数量
> - 支持预占（防超卖）、扣减（防并发）、释放（防死锁）
> - 支持多仓分配、智能调度
> - 提供高并发抗压能力（Redis + 分片 + 乐观锁）
> - 保障**每一笔交易都有真实库存支撑**

> ### ❌ **Inventory-Service 绝对不能做：**
> - 不管订单、不碰钱
> - 不算价格、不发优惠券
> - 不存用户隐私
> - 不调用其他服务数据库
> - 不允许前端传库存、不信任外部请求
> - 不用 Session
> - 不删除历史数据（软删除）

> ### 🔑 **判断一切的标准：**
> > **“如果这个动作，是‘商品能不能卖’的前提，那就是 Inventory-Service 的责任。”**  
> > **“如果这个动作，是‘用户决定买’之后的事，那就别插手——交给 order-service。”**  
> > **“如果你怕别人薅羊毛、怕超卖、怕系统崩，那说明你做对了 —— 你用了 Redis Lua + 乐观锁。”**

---

## 🚀 下一步：为你打包完整项目模板

如果你希望我为你提供：
- ✅ **完整的 Inventory-Service 项目结构（Maven + Spring Boot）**
- ✅ **Redis Lua 脚本实现原子预占（防超卖）**
- ✅ **乐观锁扣减库存（MySQL version 字段）**
- ✅ **多仓库存分配策略（基于地址）**
- ✅ **Kafka 消费 PRE_ALLOCATE / DEDUCT / RELEASE 事件**
- ✅ **定时任务自动释放超时预占（XXL-JOB）**
- ✅ **服务间通信签名（HMAC + JWT）**
- ✅ **Swagger API 文档 + 单元测试**
- ✅ **Dockerfile + Kubernetes 部署文件**
- ✅ **CI/CD Pipeline（GitLab CI）**

👉 请回复：  
**“请给我完整的 Inventory-Service 工程模板！”**

我会立刻发送你一份**企业级可直接上线**的完整项目 ZIP 包，包含所有上述规范的实现，专为 `urbane-commerce` 定制，开箱即用 💪