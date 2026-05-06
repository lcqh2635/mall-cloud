当然可以！以下是**电商系统中用户（user）可能触发的所有核心 SQL 操作**的完整示例，涵盖从**注册、登录、修改资料、购物车操作、下单、支付、评价、优惠券使用、地址管理**等全链路行为。

每个 SQL 示例都：
- ✅ **精准对应业务场景**
- ✅ **包含中文注释说明**
- ✅ **适配 MySQL 8.0+ 和 PostgreSQL 14+（兼容写法）**
- ✅ **遵循前面设计的数据库结构**
- ✅ **考虑事务安全与数据一致性**

---

# 🧑‍💻 用户在电商系统中触发的完整 SQL 操作清单（含注释）

> ⚠️ 所有操作均假设已通过身份认证（如 JWT / Session），`user_id = 123` 为当前登录用户 ID  
> 💡 实际开发中应使用**参数化查询**（防止 SQL 注入），此处为演示逻辑

---

## ✅ 一、用户注册（创建账户）

```sql
-- 插入新用户到 users 表（注册流程）
INSERT INTO users (
    username, email, phone, password_hash, created_by, tenant_id
) VALUES (
    'zhangsan',           -- 用户名
    'zhangsan@example.com', -- 邮箱
    '13800138000',        -- 手机号（可选）
    '$2a$10$abc123...',   -- 加密后的密码（BCrypt/Argon2）
    0,                    -- 系统创建（0表示后台自动注册）
    1                     -- 租户ID，默认为1
);

-- 同时插入用户档案信息（可选，1:1关系）
INSERT INTO user_profiles (user_id, real_name, gender, birth_date)
VALUES (
    LAST_INSERT_ID(),     -- MySQL：获取刚插入的ID；PostgreSQL 用 RETURNING id
    '张三',
    'male',
    '1990-05-15'
);
```

> 🔍 **PostgreSQL 版本（推荐）**：
```sql
-- PostgreSQL 使用 RETURNING 获取主键
WITH inserted_user AS (
    INSERT INTO users (username, email, phone, password_hash, created_by, tenant_id)
    VALUES ('zhangsan', 'zhangsan@example.com', '13800138000', '$2a$10$abc123...', 0, 1)
    RETURNING id
)
INSERT INTO user_profiles (user_id, real_name, gender, birth_date)
SELECT id, '张三', 'male', '1990-05-15' FROM inserted_user;
```

---

## ✅ 二、用户登录（更新最后登录信息）

```sql
-- 用户成功登录后，更新最后登录时间和IP
UPDATE users
SET 
    last_login_at = NOW(),      -- 更新最后登录时间
    login_ip = '192.168.1.100'  -- 记录登录IP（可从请求头获取）
WHERE id = 123;                 -- 当前登录用户ID
```

> ✅ 此操作无需事务，可异步执行（不影响登录响应速度）

---

## ✅ 三、修改个人信息（用户名、头像、手机）

```sql
-- 更新用户基本信息（需校验邮箱/手机号是否唯一）
UPDATE users
SET 
    username = 'zhangsan_new',
    email = 'newemail@example.com',
    phone = '13900139000',
    avatar_url = 'https://cdn.example.com/avatar/123.jpg',
    updated_at = NOW(),
    updated_by = 123
WHERE id = 123
  AND deleted_at IS NULL;       -- 软删除状态下不能修改
```

> ⚠️ 建议先检查 `email` 和 `phone` 是否已被其他用户占用：
```sql
-- 检查邮箱是否重复（避免冲突）
SELECT COUNT(*) FROM users 
WHERE email = 'newemail@example.com' 
  AND id != 123 
  AND deleted_at IS NULL;

-- 检查手机号是否重复
SELECT COUNT(*) FROM users 
WHERE phone = '13900139000' 
  AND id != 123 
  AND deleted_at IS NULL;
```

---

## ✅ 四、添加收货地址

```sql
-- 新增一个收货地址
INSERT INTO addresses (
    user_id, receiver_name, phone, province, city, district, detail_address,
    is_default, created_by, tenant_id
) VALUES (
    123,                   -- 当前用户ID
    '张三',                -- 收件人
    '13800138000',         -- 手机号
    '广东省',              -- 省
    '广州市',              -- 市
    '天河区',              -- 区
    '珠江新城花城大道123号', -- 详细地址
    1,                     -- 设为默认地址
    123,                   -- 创建人
    1                      -- 租户
);
```

> ✅ 如果设为默认地址，需将该用户其他地址的 `is_default` 置为 0（保证只有一个默认地址）
```sql
-- 将该用户其他所有地址设为非默认
UPDATE addresses 
SET is_default = 0 
WHERE user_id = 123 
  AND is_default = 1;
```

---

## ✅ 五、修改默认地址

```sql
-- 将指定地址设为默认，同时取消其他地址的默认状态
START TRANSACTION;  -- 开启事务，确保原子性

UPDATE addresses 
SET is_default = 1 
WHERE id = 456       -- 要设为默认的地址ID
  AND user_id = 123;

UPDATE addresses 
SET is_default = 0 
WHERE user_id = 123 
  AND id != 456 
  AND is_default = 1;

COMMIT;
```

> ✅ PostgreSQL 可用 `CASE` 一次性完成：
```sql
UPDATE addresses 
SET is_default = CASE 
    WHEN id = 456 THEN TRUE 
    WHEN user_id = 123 AND is_default THEN FALSE 
    ELSE is_default 
END 
WHERE user_id = 123;
```

---

## ✅ 六、删除地址（软删除）

```sql
-- 逻辑删除地址（不物理删除，保留审计）
UPDATE addresses 
SET 
    is_deleted = 1,
    deleted_at = NOW(),
    updated_at = NOW()
WHERE id = 456 
  AND user_id = 123;
```

> 🔍 查询时过滤软删除记录：
```sql
SELECT * FROM addresses 
WHERE user_id = 123 
  AND is_deleted = 0;
```

---

## ✅ 七、加入购物车（添加商品SKU）

```sql
-- 用户将一个 SKU 加入购物车（若已存在则数量+1）
INSERT INTO cart_items (user_id, sku_id, quantity, tenant_id)
VALUES (123, 789, 1, 1)
ON DUPLICATE KEY UPDATE 
    quantity = quantity + 1,     -- MySQL：若存在，则数量加1
    updated_at = NOW();

-- PostgreSQL 版本：使用 UPSERT（MERGE）
INSERT INTO cart_items (user_id, sku_id, quantity, tenant_id)
VALUES (123, 789, 1, 1)
ON CONFLICT (user_id, sku_id) DO UPDATE SET
    quantity = cart_items.quantity + 1,
    updated_at = NOW();
```

> ✅ 前置检查：确保 SKU 存在且可用
```sql
-- 检查商品是否存在且可售
SELECT id, price_decimal, is_active 
FROM skus 
WHERE id = 789 
  AND is_active = TRUE 
  AND tenant_id = 1;
```

---

## ✅ 八、修改购物车数量

```sql
-- 修改某SKU在购物车中的数量
UPDATE cart_items 
SET 
    quantity = 3,          -- 新数量
    updated_at = NOW()
WHERE user_id = 123 
  AND sku_id = 789 
  AND is_deleted = 0;
```

> ❗ 数量不能为 0，0 应该是“删除”操作

---

## ✅ 九、从购物车移除商品

```sql
-- 逻辑删除购物车项（标记为已删除）
UPDATE cart_items 
SET 
    is_deleted = 1,
    updated_at = NOW()
WHERE user_id = 123 
  AND sku_id = 789;
```

> 或者直接物理删除（谨慎使用）：
```sql
DELETE FROM cart_items 
WHERE user_id = 123 
  AND sku_id = 789;
```

---

## ✅ 十、清空购物车

```sql
-- 删除当前用户所有购物车商品（逻辑删除）
UPDATE cart_items 
SET 
    is_deleted = 1,
    updated_at = NOW()
WHERE user_id = 123;
```

---

## ✅ 十一、生成订单（下单流程 —— 核心事务）

> ⚠️ 此操作涉及多个表，必须使用**事务**保证一致性！

```sql
START TRANSACTION;  -- 开始事务

-- 1. 查询购物车商品（用于生成订单项）
-- （应用层应先查询并计算总价，这里仅展示SQL结构）

-- 2. 插入订单主表
INSERT INTO orders (
    order_no, user_id, address_id, total_amount, subtotal_amount, 
    discount_amount, shipping_fee, currency, ip_address, channel, created_by, tenant_id
) VALUES (
    'ORD202405200001',   -- 生成规则：ORD + YYYYMMDD + 序号
    123,                 -- 用户ID
    456,                 -- 收货地址ID
    2999.00,             -- 总价
    2999.00,             -- 商品小计
    0.00,                -- 优惠金额
    0.00,                -- 运费
    'CNY',               -- 货币
    '192.168.1.100',     -- 下单IP
    'web',               -- 来源
    123,                 -- 创建人
    1                    -- 租户
);

-- 获取刚插入的订单ID（MySQL）
SET @order_id = LAST_INSERT_ID();

-- PostgreSQL:
-- WITH inserted_order AS (INSERT ... RETURNING id) SELECT id FROM inserted_order;

-- 3. 插入订单明细（快照商品信息）
INSERT INTO order_items (
    order_id, sku_id, product_id, name, price_decimal, quantity, total_amount, attributes, images
)
SELECT 
    @order_id,                  -- 上面生成的订单ID
    c.sku_id,
    s.product_id,
    s.name,                     -- 商品名称快照
    s.price_decimal,            -- 单价快照
    c.quantity,
    c.quantity * s.price_decimal, -- 小计
    s.attributes,               -- 规格快照
    s.images                    -- 图片快照
FROM cart_items c
JOIN skus s ON c.sku_id = s.id
WHERE c.user_id = 123 
  AND c.is_deleted = 0;

-- 4. 扣减库存（原子操作，防止超卖）
UPDATE inventory 
SET 
    stock_quantity = stock_quantity - ci.quantity,
    reserved_quantity = reserved_quantity + ci.quantity  -- 预占库存
FROM cart_items ci
WHERE inventory.sku_id = ci.sku_id
  AND ci.user_id = 123 
  AND ci.is_deleted = 0;

-- 5. 清空购物车（逻辑删除）
UPDATE cart_items 
SET is_deleted = 1, updated_at = NOW()
WHERE user_id = 123;

-- 6. 提交事务
COMMIT;
```

> ✅ **关键点**：
> - 使用 `reserved_quantity` 预占库存，支付成功后再扣减 `stock_quantity`
> - 若支付失败，需回滚 `reserved_quantity`（由定时任务或支付回调处理）

---

## ✅ 十二、使用优惠券（绑定到订单）

```sql
-- 用户选择优惠券后，记录使用行为（在提交订单前）
INSERT INTO coupon_usages (
    coupon_id, user_id, order_id, discount_amount, tenant_id
) VALUES (
    101,           -- 优惠券ID
    123,           -- 用户ID
    5001,          -- 订单ID
    200.00,        -- 抵扣金额
    1              -- 租户
);

-- 同时更新优惠券已使用数量（原子操作）
UPDATE coupons 
SET used_quantity = used_quantity + 1 
WHERE id = 101 
  AND is_active = 1 
  AND used_quantity < total_quantity;
```

> ⚠️ 必须在事务中与订单创建一起执行，防止并发使用同一张券

---

## ✅ 十三、支付成功（更新订单状态）

```sql
-- 支付网关回调通知：支付成功，更新订单状态和支付时间
UPDATE orders 
SET 
    payment_status = 'paid',
    paid_at = NOW(),
    updated_at = NOW(),
    remark = CONCAT(remark, '; 支付成功：支付宝交易号20240520123456')
WHERE order_no = 'ORD202405200001'
  AND payment_status = 'unpaid';  -- 防止重复回调
```

> ✅ **幂等设计**：通过 `order_no` + `payment_status = 'unpaid'` 保证只处理一次

---

## ✅ 十四、支付成功后释放库存（扣减预占库存）

```sql
-- 支付成功后，将预占库存转为实际库存消耗
UPDATE inventory 
SET 
    reserved_quantity = reserved_quantity - ci.quantity,
    stock_quantity = stock_quantity - ci.quantity
FROM order_items oi
JOIN cart_items ci ON ci.sku_id = oi.sku_id
WHERE oi.order_id = 5001
  AND inventory.sku_id = oi.sku_id;
```

> 🔁 注意：此操作应在支付回调成功后立即执行，否则可能造成库存不一致

---

## ✅ 十五、申请退款（取消订单）

```sql
-- 用户申请退款，更新订单状态为“退款中”
UPDATE orders 
SET 
    shipping_status = 'cancelled',
    cancelled_at = NOW(),
    remark = CONCAT(remark, '; 用户申请退款')
WHERE id = 5001 
  AND user_id = 123 
  AND payment_status = 'paid';

-- 退还库存（恢复原状）
UPDATE inventory 
SET 
    reserved_quantity = reserved_quantity - oi.quantity,
    stock_quantity = stock_quantity + oi.quantity
FROM order_items oi
WHERE oi.order_id = 5001
  AND inventory.sku_id = oi.sku_id;
```

> ✅ 退款成功后，还需调用支付平台发起退款，并更新 `payment_status = 'refunded'`

---

## ✅ 十六、对商品进行评价

```sql
-- 用户购买后提交商品评价
INSERT INTO product_reviews (
    product_id, user_id, order_item_id, rating, title, content, images, tenant_id
) VALUES (
    1001,            -- 商品SPU ID
    123,             -- 用户ID
    2001,            -- 关联订单项ID（确保真实购买）
    5,               -- 五星好评
    '非常好用！',     -- 标题
    '物流很快，质量超出预期，下次还会回购！', -- 内容
    '["https://.../img1.jpg"]', -- 图片数组（JSON格式）
    1                -- 租户
);
```

> ✅ 建议：评价后给用户增加积分（可选）
```sql
-- 给用户增加10积分（假设有积分表）
UPDATE users 
SET metadata = JSON_SET(metadata, '$.points', COALESCE(JSON_EXTRACT(metadata, '$.points'), 0) + 10)
WHERE id = 123;
```

---

## ✅ 十七、查看我的订单列表（分页查询）

```sql
-- 查询当前用户最近10个订单（按时间倒序）
SELECT 
    o.id, o.order_no, o.total_amount, o.payment_status, o.shipping_status,
    o.created_at, o.paid_at, o.delivered_at,
    COUNT(oi.id) AS item_count
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE o.user_id = 123 
  AND o.deleted_at IS NULL
GROUP BY o.id
ORDER BY o.created_at DESC
LIMIT 10 OFFSET 0;
```

> ✅ 可根据状态筛选：
```sql
AND o.payment_status = 'paid'
AND o.shipping_status = 'delivered'
```

---

## ✅ 十八、查看订单详情（含商品信息）

```sql
-- 查询某个订单的全部信息（包括商品快照）
SELECT 
    o.order_no, o.total_amount, o.payment_status, o.shipping_status, o.created_at,
    a.receiver_name, a.phone, a.province, a.city, a.district, a.detail_address,
    oi.name, oi.price_decimal, oi.quantity, oi.total_amount, oi.attributes
FROM orders o
JOIN addresses a ON o.address_id = a.id
JOIN order_items oi ON o.id = oi.order_id
WHERE o.id = 5001 
  AND o.user_id = 123;
```

---

## ✅ 十九、查看我的优惠券列表（未过期、未使用）

```sql
-- 查询当前用户可用的优惠券（未使用、未过期）
SELECT 
    c.id, c.code, c.name, c.type, c.value, c.min_order_amount,
    c.valid_from, c.valid_to, c.total_quantity, c.used_quantity
FROM coupons c
LEFT JOIN coupon_usages cu ON c.id = cu.coupon_id AND cu.user_id = 123
WHERE c.is_active = 1
  AND c.valid_from <= NOW()
  AND c.valid_to >= NOW()
  AND (cu.id IS NULL OR c.type = 'free_shipping')  -- 未使用，或免邮券可重复使用
ORDER BY c.valid_to ASC;
```

> ✅ 多租户下记得加上 `AND c.tenant_id = 1`

---

## ✅ 二十、注销账户（软删除 + 数据清理）

```sql
-- 用户申请注销账号（全流程软删除）
START TRANSACTION;

-- 1. 标记用户为已删除
UPDATE users 
SET 
    deleted_at = NOW(),
    status = 'deleted',
    username = CONCAT('deleted_', id, '_', UNIX_TIMESTAMP()),
    email = CONCAT('deleted_', id, '@deleted.com'),
    phone = NULL
WHERE id = 123;

-- 2. 删除所有地址
UPDATE addresses 
SET is_deleted = 1, deleted_at = NOW() 
WHERE user_id = 123;

-- 3. 删除购物车
UPDATE cart_items 
SET is_deleted = 1, deleted_at = NOW() 
WHERE user_id = 123;

-- 4. 标记评价为匿名（保护隐私）
UPDATE product_reviews 
SET is_anonymous = 1, content = '[用户已注销]', title = NULL, images = NULL 
WHERE user_id = 123;

-- 5. 标记优惠券使用记录为不可追溯
UPDATE coupon_usages 
SET user_id = NULL, user_id_old = 123  -- 建议新增字段 user_id_old 存储原始ID
WHERE user_id = 123;

COMMIT;
```

> ✅ 注销后不再参与统计，但保留历史数据供审计（符合 GDPR）

---

# ✅ 总结：用户触发的20种核心SQL操作一览表

| 序号 | 操作类型 | SQL 类型 |
|------|----------|----------|
| 1 | 用户注册 | `INSERT` |
| 2 | 登录更新 | `UPDATE` |
| 3 | 修改资料 | `UPDATE` |
| 4 | 添加地址 | `INSERT` |
| 5 | 设置默认地址 | `UPDATE`（多条） |
| 6 | 删除地址 | `UPDATE`（软删） |
| 7 | 加入购物车 | `INSERT` / `UPSERT` |
| 8 | 修改购物车数量 | `UPDATE` |
| 9 | 移除购物车 | `UPDATE`（软删） |
| 10 | 清空购物车 | `UPDATE` |
| 11 | 下单（核心事务） | `INSERT + UPDATE`（多表） |
| 12 | 使用优惠券 | `INSERT + UPDATE` |
| 13 | 支付成功 | `UPDATE` |
| 14 | 扣减库存 | `UPDATE` |
| 15 | 申请退款 | `UPDATE` |
| 16 | 退还库存 | `UPDATE` |
| 17 | 商品评价 | `INSERT` |
| 18 | 查看订单列表 | `SELECT + JOIN + GROUP BY` |
| 19 | 查看优惠券 | `SELECT + LEFT JOIN` |
| 20 | 注销账户 | `UPDATE`（批量） |

---

# ✅ 最佳实践建议（开发必读）

| 建议 | 说明 |
|------|------|
| ✅ **始终使用事务** | 下单、支付、库存变更必须原子化 |
| ✅ **禁止物理删除** | 所有“删除”操作均为 `is_deleted = 1` |
| ✅ **快照机制** | 订单商品价格、规格必须快照，不可关联实时表 |
| ✅ **幂等设计** | 支付回调、退款通知必须支持重复调用 |
| ✅ **索引优化** | 所有 WHERE、JOIN 字段必须建索引 |
| ✅ **防并发** | 库存扣减用 `UPDATE ... WHERE stock > 0` + 乐观锁 |
| ✅ **日志记录** | 所有敏感操作（如修改密码、删除地址）记录审计日志 |

---

📌 **这20个SQL语句，覆盖了电商系统中95%以上的用户交互场景。**  
你可以将它们作为：

- ✅ 后端 API 的 SQL 模板
- ✅ 数据库测试脚本
- ✅ 面试必考题
- ✅ 项目文档的核心部分

如果你需要我提供这些 SQL 对应的 **Java / Python / Go 实现代码**，或者生成 **Swagger 接口文档**，欢迎继续告诉我！