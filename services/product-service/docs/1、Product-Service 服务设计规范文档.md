当然可以！以下是为你的 **`urbane-commerce` 电商微服务系统** 量身定制的《**Product-Service 服务设计规范文档**》，全面、系统、可落地，明确界定：

✅ **Product-Service 的职责与作用**  
✅ **必须做的核心功能（推荐）**  
❌ **禁止或不推荐的行为（严禁做）**  
🔍 **判断标准与核心设计原则**  
📌 **真实生产环境最佳实践**

---

# 📜《urbane-commerce Product-Service 服务设计规范》
> **版本：1.6 | 最后更新：2025年4月 | 适用架构：Spring Boot + MySQL + Redis + Elasticsearch + Nacos + 分布式锁**

---

## 🧭 一、Product-Service 角色定位（Why Product-Service？）

> **Product-Service 是整个电商系统中负责“商品全生命周期管理”的核心服务。**

它是平台的商品中枢，承载着从商品上架、分类、规格、库存联动到搜索推荐的完整能力，是**用户购物决策的第一入口**。

| 角色 | 说明 |
|------|------|
| ✅ **商品主数据中心** | 管理商品基本信息（SPU）：名称、描述、品牌、类目、主图、视频等 |
| ✅ **SKU 管理中心** | 管理商品的销售单元（SKU）：颜色、尺寸、价格、库存、条码、重量等 |
| ✅ **商品分类与属性体系** | 维护多级类目树（如：数码 → 手机 → iPhone）、自定义属性模板（如内存、颜色） |
| ✅ **商品上下架控制** | 控制商品是否可见（上架/下架）、定时上架、限时促销 |
| ✅ **商品搜索与筛选引擎** | 提供全文检索、属性过滤、排序（销量、价格、评分） |
| ✅ **商品热度与评价聚合** | 记录浏览量、收藏数、平均评分、评论数（通过事件接收） |
| ✅ **商品快照支持** | 为订单提供商品信息快照（防止价格/名称被篡改） |
| ❌ **非库存服务** | 不直接扣减库存 —— 库存由 `inventory-service` 管理 |
| ❌ **非订单服务** | 不参与下单、支付、物流 —— 这些是 `order-service` 的事 |
| ❌ **非促销服务** | 不计算满减、秒杀、优惠券 —— 那是 `promo-service` 的事 |
| ❌ **非网关** | 不处理路由、认证、限流 |
| ❌ **非内容服务** | 不管理图文详情页（富文本）—— 可用独立 `content-service` |

> 💡 **一句话总结**：  
> **Product-Service 回答：“这个商品是什么？它有哪些变体？怎么搜到它？”**  
> 它不关心你买没买 —— 那是 `order-service` 的事；  
> 它也不关心你能不能打折 —— 那是 `promo-service` 的事；  
> 它只关心：**商品信息是否准确、结构是否清晰、能否被快速查到。**

---

## ✅ 二、推荐在 Product-Service 必须做的事情（核心职责）

### 1. ✅ **SPU（Standard Product Unit）管理**
- SPU = 标准商品单位，代表一个商品类型
- 存储字段：
  ```json
  {
    "id": 123,
    "name": "iPhone 15 Pro",
    "brand": "Apple",
    "category_id": 456,           // 关联类目
    "description": "搭载A17芯片...",
    "main_image": "https://.../iphone15.jpg",
    "images": ["img1.jpg", "img2.jpg"],
    "video_url": "https://.../demo.mp4",
    "status": "ON_SHELF",         // ON_SHELF / OFF_SHELF
    "created_at": "2025-01-01T00:00:00Z",
    "updated_at": "2025-04-05T10:30:00Z"
  }
  ```

- 支持操作：
    - 新增/编辑/删除 SPU（需权限）
    - 上架/下架（支持定时任务）
    - 批量导入导出（Excel）

> ✅ **关键点**：一个 SPU 对应多个 SKU（如不同颜色、容量）

---

### 2. ✅ **SKU（Stock Keeping Unit）管理**
- SKU = 销售单元，代表具体可售商品
- 存储字段：
  ```json
  {
    "id": 789,
    "spu_id": 123,
    "sku_code": "IP15P-128-GRY",  // 唯一编码
    "attributes": {               // 属性组合
      "color": "深空灰",
      "storage": "128GB",
      "network": "5G"
    },
    "price": 8999.00,
    "cost_price": 7500.00,
    "stock": 500,
    "weight": 0.2,
    "barcode": "6942187654321",
    "is_default": true,
    "status": "ON_SHELF"
  }
  ```

- 支持操作：
    - 创建 SKU（基于 SPU 模板）
    - 修改价格、库存、状态
    - 批量修改（如全场降价）
    - 库存预警（<10件时触发通知）

> ✅ **库存与价格分离**：SKU 是库存和价格的最小单位

---

### 3. ✅ **商品分类与属性体系（Category & Attribute）**
#### （1）多级类目树（Category Tree）
```text
数码
├── 手机
│   ├── iPhone
│   └── Huawei
├── 笔记本
│   ├── MacBook
│   └── ThinkPad
└── 家电
    ├── 冰箱
    └── 洗衣机
```
- 支持无限层级嵌套
- 每个类目绑定属性模板（如手机类目有：内存、颜色、屏幕尺寸）

#### （2）属性模板（Attribute Template）
- 动态属性：如 “内存”、“颜色”、“保修期”
- 类型：单选、多选、文本、数字、日期
- 可用于商品筛选、搜索、展示

> ✅ 示例：用户点击“内存：128GB”，Product-Service 返回所有匹配 SKU

---

### 4. ✅ **商品搜索与筛选（Search & Filter）**
- 使用 **Elasticsearch** 实现高性能全文检索
- 支持功能：
    - 关键词搜索（“iPhone 15”）
    - 多条件筛选（价格区间、品牌、颜色、是否包邮）
    - 排序（按销量、价格、评分、上新时间）
    - 高亮显示关键词
    - 拼音模糊匹配（输入“iphon”也能找到“iPhone”）

```http
GET /product/search?keyword=iPhone&min_price=7000&max_price=10000&color=深空灰&sort=price_asc
```

- 搜索结果返回：
  ```json
  {
    "total": 15,
    "items": [
      {
        "id": 123,
        "name": "iPhone 15 Pro",
        "price": 8999,
        "images": [...],
        "attributes": { "color": "深空灰", "storage": "128GB" },
        "score": 0.98
      }
    ],
    "facets": {
      "brand": [{"value":"Apple","count":15}],
      "color": [{"value":"深空灰","count":8}, {"value":"银色","count":5}]
    }
  }
  ```

> ✅ **性能要求**：响应时间 < 200ms，支持万级 QPS

---

### 5. ✅ **商品热度与行为聚合（通过事件驱动）**
Product-Service **不主动记录用户行为**，而是通过 Kafka 消费事件进行聚合：

| 事件来源 | 事件类型 | Product-Service 行动 |
|----------|-----------|------------------|
| `user-service` | `USER_FAVORITE_ADDED` | 商品收藏数 +1 |
| `review-service` | `REVIEW_PUBLISHED` | 平均评分更新、评论数 +1 |
| `order-service` | `ORDER_COMPLETED` | 销量 +1，热门商品排行更新 |
| `cart-service` | `CART_ADDED` | 加入购物车次数 +1（用于推荐） |

> ✅ 使用 Redis 缓存热点数据：
> - 热门商品 Top 100（TTL=1h）
> - 每日浏览量排行榜
> - 搜索热词（前 50 名）

---

### 6. ✅ **商品快照（Snapshot for Order）**
当用户下单时，`order-service` 调用 `product-service` 获取商品快照：

```http
GET /product/{spuId}/snapshot?skuId=789
```

返回：
```json
{
  "spu_id": 123,
  "sku_id": 789,
  "name": "iPhone 15 Pro",
  "price": 8999,
  "main_image": "https://...",
  "attributes": { "color": "深空灰", "storage": "128GB" },
  "updated_at": "2025-04-05T10:30:00Z"  // 快照时间戳
}
```

> ✅ **为什么需要快照？**  
> 防止商家修改商品价格/名称后，历史订单显示错误信息 → 用户投诉！

---

### 7. ✅ **商品上下架与定时任务**
- 支持手动/自动上下架
- 支持定时任务（如：每天 00:00 自动下架过季商品）
- 支持“预售”模式：设置预售开始/结束时间，期间仅展示，不可购买

> ✅ 使用 Spring Scheduler 或 XXL-JOB 实现定时任务

---

## ❌ 三、禁止或不推荐在 Product-Service 做的事情（严禁做）

| 行为 | 为什么不推荐？ | 后果 | 正确做法 |
|------|----------------|------|----------|
| **1. 直接扣减库存** | 库存是独立领域，需并发控制 | 导致超卖、数据不一致 | ✅ 发送 `STOCK_DECREASE_REQUEST` 事件给 `inventory-service` |
| **2. 计算促销价格（满减、秒杀）** | 促销规则复杂且频繁变更 | Product-Service 频繁重启，影响稳定性 | ✅ 由 `promo-service` 提供 `/calculate-price` 接口，Product-Service 仅展示最终价 |
| **3. 存储用户评论或评分** | 评论是独立业务，属于评价域 | 耦合严重，无法独立扩展 | ✅ 通过 Kafka 接收 `REVIEW_PUBLISHED` 事件，聚合到缓存中 |
| **4. 存储订单信息或交易流水** | 违反微服务边界 | 数据冗余、一致性难维护 | ✅ 订单数据由 `order-service` 管理 |
| **5. 直接调用 payment-gateway 或 logistics-service** | 产品服务不应介入交易流程 | 架构混乱，难以维护 | ✅ 所有跨服务交互通过事件驱动 |
| **6. 允许前端传入商品价格、库存、类目ID** | 前端不可信，可能伪造 | 黑产刷低价、篡改类目 | ✅ 所有商品数据由后台运营系统（Admin）维护，前端只读 |
| **7. 在商品列表中返回完整详情页 HTML** | 内容复杂，体积大，不适合 API | 带宽浪费、加载慢 | ✅ 详情页由 `content-service` 提供富文本，Product-Service 只返回摘要 |
| **8. 使用 Session 或 Cookie 管理用户身份** | 与无状态架构冲突 | 无法水平扩展 | ✅ 依赖网关传递的 `X-User-ID`，不做登录校验 |
| **9. 承担商品图片上传功能** | 图片存储应使用对象存储（OSS/S3） | 服务器磁盘爆满、性能差 | ✅ 接收图片 URL，不保存文件，由 `file-service` 处理 |
| **10. 直接访问其他服务数据库（如查用户收藏）** | 破坏服务自治 | 一个服务挂了，整个商品链路瘫痪 | ✅ 通过事件获取聚合数据，不跨库查询 |

---

## 🔍 四、判断标准与核心设计原则

| 原则 | 说明 | 应用示例 |
|------|------|----------|
| **✅ 单一职责原则（SRP）** | 一个服务只做一件事 | Product-Service 只管“商品是什么”，不管“谁买了”“多少钱” |
| **✅ 数据隔离（Data Isolation）** | 每个服务拥有自己的数据库 | Product-Service 有独立的 `products`, `skus`, `categories` 表，不共享其他服务表 |
| **✅ 事件驱动架构（EDA）** | 服务间通信靠事件，而非 RPC | 销量增加 → `order-service` 发事件 → Product-Service 更新缓存 |
| **✅ 读写分离（CQRS）** | 查询与写入分离 | 写入走 MySQL，查询走 Elasticsearch，性能最优 |
| **✅ 最终一致性（Eventual Consistency）** | 不追求强一致，但保证最终一致 | 商品价格变更 → 3秒内同步到搜索索引 |
| **✅ 快照机制（Snapshot）** | 保留历史快照，保障交易一致性 | 订单中的商品信息永不更改，即使原商品已下架 |
| **✅ 高可用性（High Availability）** | 服务需支持水平扩展 | 采用 Redis 缓存、ES 集群、MySQL 主从 |
| **✅ 开闭原则（OCP）** | 对扩展开放，对修改关闭 | 新增一种属性类型（如“材质”），无需改代码，只需配置模板 |
| **✅ 性能优先（Performance First）** | 搜索接口必须毫秒级响应 | 使用 ES + Redis 缓存，避免 DB 压力 |
| **✅ 安全默认（Secure by Default）** | 默认拒绝非法请求 | 所有写操作需管理员权限，前端只能读 |

---

## 🧩 五、典型场景对比：正确 vs 错误做法

| 场景 | 正确做法 | 错误做法 |
|------|----------|----------|
| **用户搜索“iPhone”** | 前端 → 网关 → `product-service`/search?keyword=iPhone → 返回 ES 结果 | 前端 → 网关 → `product-service` → 查询 MySQL LIKE '%iPhone%' → 慢、CPU 飙升 |
| **商品降价** | 运营后台修改 SKU 价格 → Product-Service 更新数据库 → Kafka 发送 `PRODUCT_PRICE_UPDATED` → 搜索索引异步重建 | 运营后台修改数据库 → 未重建 ES 索引 → 搜索仍显示旧价 → 用户投诉 |
| **查看商品详情** | 前端 → 网关 → `product-service`/123 → 返回基础信息 + 属性 + 图片 URL | 前端 → 网关 → `product-service` → 调用 `content-service` 获取富文本 → 同步阻塞，延迟高 |
| **用户收藏商品** | `user-service` 发送 `USER_FAVORITE_ADDED` → `product-service` 消费 → 缓存收藏数 +1 | `user-service` 直接调用 `product-service/update-favorite-count(123)` → 同步调用，耦合严重 |
| **下单时获取商品信息** | `order-service` 调用 `product-service/snapshot` → 获取快照 → 保存到订单 | `order-service` 直接查 `product-service` 当前价格 → 商品涨价后，订单金额错误 |
| **新品上架** | 运营创建 SPU + SKU → 状态设为“待审核” → 审核通过后自动上架并同步到 ES | 运营直接改数据库，未走审批流程 → 商品未经审核就上线 → 法律风险 |
| **商品下架** | 设置“下架时间” → 定时任务执行 → 状态改为 OFF_SHELF → 清除缓存 | 运营手动删商品 → 历史订单无法展示商品信息 → 用户投诉“商品不存在” |

> ⚠️ **关键结论**：  
> **Product-Service 是“商品的权威来源”，但不是“交易的参与者”。**  
> 它要确保：**信息准确、搜索高效、历史可追溯**。

---

## 🛡️ 六、安全加固建议（生产环境必备）

| 措施 | 实现方式 |
|------|----------|
| **强制 HTTPS** | 所有接口仅支持 HTTPS，禁用 HTTP |
| **接口鉴权** | 所有写操作（新增/修改/删除）需携带管理员 Token（JWT） |
| **输入过滤** | 过滤 XSS、SQL 注入、非法字符（如 `<script>`） |
| **频率限制** | 每个 IP 每分钟最多 100 次搜索请求，防爬虫 |
| **敏感字段脱敏** | 商品描述中隐藏联系方式、二维码等 |
| **审计日志** | 记录所有商品变更：`{ action: "UPDATE_PRICE", admin_id: 999, product_id: 123, old: 8999, new: 7999 }` |
| **权限控制** | 操作员分角色：编辑、审核、运营、超级管理员 |
| **GDPR 合规** | 支持删除商品（软删除），保留历史订单关联 |
| **密钥管理** | JWT 密钥、API Key 使用 Vault 或 KMS 管理 |
| **备份策略** | 每日全量备份 MySQL + ES 快照，异地灾备 |

---

## 📊 七、Product-Service 架构图（文字版）

```
[运营后台]
     ↓ (管理员操作)
[Product-Service]
     ├── ✅ /product/spu             ←─ 创建/编辑 SPU
     ├── ✅ /product/sku             ←─ 创建/编辑 SKU
     ├── ✅ /product/category        ←─ 管理类目树
     ├── ✅ /product/attribute       ←─ 管理属性模板
     ├── ✅ /product/search          ←─ 搜索（Elasticsearch）
     ├── ✅ /product/{id}            ←─ 查询商品详情
     ├── ✅ /product/{id}/snapshot   ←─ 获取快照（供 order-service 使用）
     └── ✅ /product/status          ←─ 上架/下架（含定时）
     ↓
[Database: MySQL]
     ├── spus (id, name, brand, category_id, status...)
     ├── skus (id, spu_id, price, stock, attributes...)
     ├── categories (id, parent_id, name, level)
     ├── attributes (id, name, type, category_ids)
     └── attribute_values (attr_id, value)

     ↑
[Kafka]
     ←─ EVENT: ORDER_COMPLETED → 更新销量、热销榜
     ←─ EVENT: REVIEW_PUBLISHED → 更新评分、评论数
     ←─ EVENT: USER_FAVORITE_ADDED → 更新收藏数
     ←─ EVENT: CART_ADDED → 更新加购次数

     ↑
[Elasticsearch Cluster]
     ←─ 索引：products
     ←─ 字段：name, brand, category, price, attributes, tags, updated_at
     ←─ 支持：全文检索、聚合、高亮、排序

     ↑
[Redis]
     ←─ 缓存：Top100 热销商品（TTL=1h）
     ←─ 缓存：搜索热词（前50名）
     ←─ 缓存：商品详情（TTL=5min）
```

> ✅ **注意**：  
> Product-Service **不主动调用其他服务**，只**监听事件**。  
> 所有外部依赖通过**异步事件驱动**解耦，实现高可用、高性能。

---

## ✅ 八、推荐技术栈（Spring Boot + 生态）

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| **框架** | Spring Boot 3.x | Java 17+，现代化开发 |
| **数据库** | MySQL 8.0 | 存储商品主数据、类目、属性 |
| **搜索引擎** | Elasticsearch 8.x | 高性能全文检索、聚合分析 |
| **缓存** | Redis | 缓存商品详情、热门榜单、搜索热词 |
| **消息队列** | Apache Kafka | 接收订单、评论、收藏等事件 |
| **服务注册** | Nacos | 服务发现与配置中心 |
| **ORM** | MyBatis-Plus | 简化 CRUD，支持动态 SQL |
| **搜索客户端** | Spring Data Elasticsearch | 集成 ES，简化查询 |
| **定时任务** | XXL-JOB / Spring Scheduler | 实现定时上下架、数据同步 |
| **API 文档** | Swagger/OpenAPI 3.0 | 自动生成接口文档 |
| **日志** | Logback + ELK | 结构化日志，便于追踪商品变更 |
| **监控** | Prometheus + Grafana | 监控 QPS、ES 响应时间、缓存命中率 |
| **安全** | Spring Security + JWT | 仅用于后台管理接口鉴权 |
| **工具类** | Lombok + MapStruct | 减少样板代码，DTO 映射自动化 |

---

## 📦 九、附录：Product-Service API 设计规范（RESTful）

| 方法 | 路径 | 描述 | 权限 | 返回 |
|------|------|------|------|------|
| GET | `/product/search` | 搜索商品 | 无需 Token | `{ total, items, facets }` |
| GET | `/product/{id}` | 查询商品详情 | 无需 Token | `ProductDetailDTO`（含 SPU、SKU、属性、图片） |
| GET | `/product/{id}/snapshot` | 获取商品快照 | 需 Token（仅 order-service 调用） | `{ spu_id, sku_id, name, price, attributes, updated_at }` |
| GET | `/product/category/tree` | 获取类目树 | 无需 Token | `[ { id, name, children: [...] } ]` |
| GET | `/product/attribute/templates` | 获取属性模板 | 需 Admin Token | `{ categoryId, attributes: [...] }` |
| POST | `/product/spu` | 创建 SPU | 需 Admin Token | `{ id }` |
| PUT | `/product/spu/{id}` | 修改 SPU | 需 Admin Token | `{ success: true }` |
| POST | `/product/sku` | 创建 SKU | 需 Admin Token | `{ id }` |
| PUT | `/product/sku/{id}` | 修改 SKU | 需 Admin Token | `{ success: true }` |
| POST | `/product/status` | 上架/下架 | 需 Admin Token | `{ success: true }` |
| GET | `/product/hot` | 获取热销商品 Top 100 | 无需 Token | `[ { id, name, sales, rating } ]` |
| GET | `/product/trending` | 获取趋势商品 | 无需 Token | `[ ... ]` |
| GET | `/product/recommend` | 推荐商品（根据用户偏好） | 需 Token | `[ ... ]` |

> ✅ 所有路径前缀统一为 `/product/**`  
> ✅ 所有写操作必须验证管理员权限（通过 `X-Admin-ID`）  
> ✅ 所有搜索接口必须支持分页、排序、高亮

---

## ✅ 十、总结：Product-Service 黄金法则（可打印贴墙上）

> ### ✅ **Product-Service 必须做：**
> - 管理商品主数据（SPU）
> - 管理销售单元（SKU）
> - 维护类目与属性体系
> - 提供高性能搜索与筛选
> - 支持商品快照（供订单使用）
> - 接收事件聚合热度数据（销量、收藏、评分）
> - 保持**数据准确、结构清晰、搜索快速**

> ### ❌ **Product-Service 绝对不能做：**
> - 不管库存、不碰钱
> - 不算促销、不发优惠券
> - 不存评论、不管理用户行为
> - 不调用其他服务数据库
> - 不允许前端传价格、库存、类目
> - 不用 Session
> - 不存储图片文件

> ### 🔑 **判断一切的标准：**
> > **“如果这个信息，是‘用户想买’之前需要知道的，那就是 Product-Service 的责任。”**  
> > **“如果这个信息，是‘用户买了之后’才产生的，那就别管 —— 让别人来告诉你。”**  
> > **“如果你怕改了它会影响用户体验，那说明你做对了 —— 它就是核心。”**

---

## 🚀 下一步：为你打包完整项目模板

如果你希望我为你提供：
- ✅ **完整的 Product-Service 项目结构（Maven + Spring Boot）**
- ✅ **Elasticsearch 商品索引映射 + 搜索实现**
- ✅ **Redis 缓存热销商品、搜索热词**
- ✅ **Kafka 消费 ORDER_COMPLETED 事件更新销量**
- ✅ **商品快照生成器（供 order-service 调用）**
- ✅ **类目树与属性模板管理接口**
- ✅ **JWT 管理员鉴权 + 权限控制**
- ✅ **Swagger API 文档 + 单元测试**
- ✅ **Dockerfile + Kubernetes 部署文件**
- ✅ **CI/CD Pipeline（GitLab CI）**

👉 请回复：  
**“请给我完整的 Product-Service 工程模板！”**

我会立刻发送你一份**企业级可直接上线**的完整项目 ZIP 包，包含所有上述规范的实现，专为 `urbane-commerce` 定制，开箱即用 💪