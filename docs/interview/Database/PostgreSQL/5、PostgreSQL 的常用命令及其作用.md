非常好！作为 Java 后端开发程序员，掌握 PostgreSQL 的常用命令不仅能提升开发效率，还能帮助你更好地理解数据库结构、调试问题和优化性能。

PostgreSQL 的命令可以分为 **两大类**：

1. **SQL 命令（标准 SQL）**：用于操作数据和定义结构（如 `SELECT`, `CREATE TABLE`）
2. **psql 元命令（Meta-commands）**：是 `psql` 客户端提供的快捷命令，以 `\` 开头，用于管理连接、查看结构、导入导出等

---

## 🗂️ 一、PostgreSQL 常用命令分类与作用

| 分类 | 命令类型 | 作用 |
|------|----------|------|
| 🔹 数据查询 | `SELECT` | 查询数据 |
| 🔹 数据操作 | `INSERT`, `UPDATE`, `DELETE` | 增删改数据 |
| 🔹 结构定义 | `CREATE`, `ALTER`, `DROP` | 创建/修改/删除表、索引等 |
| 🔹 事务控制 | `BEGIN`, `COMMIT`, `ROLLBACK` | 控制事务 |
| 🔹 权限管理 | `GRANT`, `REVOKE` | 授予权限 |
| 🔹 psql 元命令 | `\l`, `\dt`, `\d`, `\c`, `\copy` 等 | 查看数据库信息、切换、导入导出 |
| 🔹 性能分析 | `EXPLAIN`, `EXPLAIN ANALYZE` | 分析 SQL 执行计划 |

---

## 💡 二、实际开发中常用的命令示例（带中文注释）

### ✅ 1. 【psql 元命令】查看数据库和表结构（日常开发最常用）

```sql
-- 列出所有数据库（相当于 show databases）
\l

-- 连接到指定数据库（切换数据库）
\c myappdb

-- 查看当前数据库中所有表
\dt

-- 查看某张表的结构（字段、类型、约束）
\d users

-- 查看表的详细信息（包括注释、索引）
\dt+ orders

-- 查看当前连接信息
\conninfo

-- 查看所有用户/角色
\du
```

> 🔍 **适用场景**：刚接手项目，想快速了解数据库结构。

---

### ✅ 2. 【结构定义】创建用户表和订单表（建模常用）

```sql
-- 创建用户并设置密码，需要在 sudo -u postgres psql 管理员的环境下执行
-- 创建了一个名为 lcqh2635 的数据库用户，并赋予它 创建数据库的权限（CREATEDB）
CREATE USER lcqh2635 WITH PASSWORD '123456' CREATEDB;
-- 列出所有用户（角色）
-- sudo -u postgres psql -c "\du;"

-- 切换到新建的 lcqh2635 用户环境
-- psql -U lcqh2635 -h localhost -p 5432 -W

DROP DATABASE IF EXISTS blog_cloud;
-- 创建一个数据库，并归属该用户
CREATE DATABASE blog_cloud OWNER lcqh2635;

DROP TABLE IF EXISTS employees;
-- 创建一个名为 'employees' 的新表
CREATE TABLE employees (
    -- 由于 employee_id 是 SERIAL 类型，因此在插入数据时不需要指定它的值，它会自动递增
                           employee_id SERIAL PRIMARY KEY,  -- 自动递增的整数，作为主键
                           first_name VARCHAR(50) NOT NULL, -- 员工名字，不能为空
                           last_name VARCHAR(50) NOT NULL,  -- 员工姓氏，不能为空
                           email VARCHAR(100) UNIQUE NOT NULL, -- 员工电子邮件地址，不能为空且唯一
                           phone VARCHAR(20),                -- 员工电话号码
                           hire_date DATE NOT NULL,          -- 入职日期，不能为空
                           salary NUMERIC(10, 2) CHECK (salary > 0), -- 工资，数值类型，必须大于0
                           department_id INT, -- 部门ID，外键关联到departments表
                           create_by VARCHAR(64) NULL DEFAULT '', -- 创建人
                           create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间，默认为当前时间戳
                           update_by VARCHAR(64) NULL DEFAULT '', -- 修改人
                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 修改时间，默认为当前时间戳
                           deleted int2 NOT NULL DEFAULT 0
);
-- 为表添加注释
COMMENT ON TABLE employees IS '员工信息表';
-- 为列添加注释
COMMENT ON COLUMN employees.employee_id IS '员工编号';
COMMENT ON COLUMN employees.first_name IS '员工名字';
COMMENT ON COLUMN employees.last_name IS '员工姓氏';
COMMENT ON COLUMN employees.email IS '员工电子邮件地址';
COMMENT ON COLUMN employees.phone IS '员工电话号码';
COMMENT ON COLUMN employees.hire_date IS '入职日期';
COMMENT ON COLUMN employees.salary IS '工资';
COMMENT ON COLUMN employees.department_id IS '部门编号';
COMMENT ON COLUMN employees.create_by IS '创建人';
COMMENT ON COLUMN employees.create_time IS '创建时间';
COMMENT ON COLUMN employees.update_by IS '修改时间';
COMMENT ON COLUMN employees.update_time IS '修改时间';
COMMENT ON COLUMN employees.deleted IS '逻辑删除';

-- 创建索引以加速查询
-- CREATE INDEX idx_email ON employees(email);
-- CREATE INDEX idx_department_id ON employees(department_id);

-- 创建用户表，使用 UUID 主键（适合分布式系统）
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- 自动生成 UUID
    username VARCHAR(50) UNIQUE NOT NULL,           -- 用户名唯一
    email VARCHAR(100) UNIQUE NOT NULL,             -- 邮箱唯一
    password_hash TEXT NOT NULL,                    -- 密码哈希（不要存明文）
    profile JSONB DEFAULT '{}',                     -- 用户扩展信息（如昵称、头像）
    created_at TIMESTAMP DEFAULT NOW(),             -- 创建时间
    updated_at TIMESTAMP DEFAULT NOW()
);
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';
COMMENT ON TABLE users IS '用户表';


-- 创建订单表，关联用户
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,                          -- 自增主键
    user_id UUID NOT NULL REFERENCES users(id),     -- 外键关联用户
    order_no VARCHAR(32) UNIQUE NOT NULL,           -- 订单号
    amount DECIMAL(10,2) NOT NULL,                  -- 金额
    status VARCHAR(20) DEFAULT 'pending',           -- 状态：待支付、已支付等
    items JSONB,                                    -- 订单商品列表（JSON 格式）
    created_at TIMESTAMP DEFAULT NOW()
);

-- 为订单表的 user_id 字段创建索引（提升查询性能）
CREATE INDEX idx_orders_user_id ON orders(user_id);

-- 为订单状态创建索引（用于状态筛选）
CREATE INDEX idx_orders_status ON orders(status);
```

> 🔍 **适用场景**：Spring Boot 项目初始化数据库结构。

---

### ✅ 3. 【数据操作】插入、更新、删除数据

```sql
-- 插入一条用户数据，并返回生成的 ID（RETURNING 非常有用）
INSERT INTO users (username, email, password_hash, profile)
VALUES ('zhangsan', 'zhangsan@example.com', 'hashed_password_123', '{"nick": "张三", "age": 28}')
RETURNING id;

-- 插入订单，使用 RETURNING 获取订单号
INSERT INTO orders (user_id, order_no, amount, items)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ORD20250405001', 299.99,
        '[{"name": "Java书", "price": 99.99}, {"name": "PostgreSQL指南", "price": 199.99}]')
RETURNING order_no;

-- 更新用户信息（如修改昵称）
UPDATE users
SET profile = jsonb_set(profile, '{nick}', '"张小三"'),  -- 修改 JSON 字段
    updated_at = NOW()
WHERE username = 'zhangsan';

-- 删除已取消的订单（软删除更推荐用 status 字段）
DELETE FROM orders
WHERE status = 'cancelled' AND created_at < NOW() - INTERVAL '30 days';
```

> 🔍 **适用场景**：MyBatis 或 JPA 执行 CRUD 操作前的 SQL 验证。

---

### ✅ 4. 【数据查询】复杂查询示例（报表、接口数据）

```sql
-- 查询每个用户的订单总数和总金额（分组统计）
SELECT 
    u.username,
    COUNT(o.id) AS order_count,
    COALESCE(SUM(o.amount), 0) AS total_amount
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.username
ORDER BY total_amount DESC;

-- 使用窗口函数：给订单按用户分组排名（如“每个用户最新3笔订单”）
SELECT 
    username,
    order_no,
    amount,
    status,
    created_at,
    ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) AS rn
FROM orders o
JOIN users u ON o.user_id = u.id
-- 外层筛选：只取每个用户的前3条
WHERE rn <= 3;

-- 模糊搜索用户（支持中文）
SELECT * FROM users
WHERE username ILIKE '%san%' OR profile->>'nick' ILIKE '%张%';
-- ILIKE 表示不区分大小写

-- 查询包含“PostgreSQL”书籍的订单
SELECT * FROM orders
WHERE items @> '[{"name": "PostgreSQL指南"}]';
-- @> 表示 JSONB 包含
```

> 🔍 **适用场景**：写复杂接口 SQL、生成报表、替代 Java 中的 Stream 处理。

---

### ✅ 5. 【性能分析】查看执行计划（排查慢查询）

```sql
-- 查看 SQL 的执行计划（不真正执行）
EXPLAIN SELECT * FROM orders WHERE status = 'paid';

-- 查看实际执行时间和行数（用于性能调优）
EXPLAIN ANALYZE SELECT * FROM orders WHERE user_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';

-- 如果发现是 Seq Scan（全表扫描），说明缺少索引，应添加
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
```

> 🔍 **适用场景**：接口响应慢，怀疑数据库查询性能问题。

---

### ✅ 6. 【导入导出】数据迁移与备份

```sql
-- 导出用户表为 CSV 文件（带表头）
\copy users TO '/tmp/users.csv' WITH CSV HEADER;

-- 从 CSV 文件导入数据（适合初始化测试数据）
\copy users FROM '/tmp/test_users.csv' WITH CSV HEADER;

-- 导出某个查询的结果
\copy (SELECT * FROM orders WHERE created_at > NOW() - INTERVAL '7 days') TO '/tmp/recent_orders.csv' WITH CSV HEADER;
```

> 🔍 **适用场景**：
> - 测试环境初始化数据
> - 生产问题排查时导出日志
> - 数据迁移脚本

---

### ✅ 7. 【事务控制】保证数据一致性（如下单流程）

```sql
-- 开始事务
BEGIN;

-- 插入订单
INSERT INTO orders (user_id, order_no, amount, items)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ORD20250405002', 199.99, '[{"name": "Spring书", "price": 199.99}]');

-- 更新用户积分（假设有一个字段）
UPDATE users SET profile = jsonb_set(profile, '{points}', (COALESCE(profile->>'points', '0')::int + 10)::text)
WHERE id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';

-- 提交事务（两条操作一起成功）
COMMIT;

-- 如果中间出错，可以回滚
-- ROLLBACK;
```

> 🔍 **适用场景**：Spring 的 `@Transactional` 注解底层就是这么工作的。

---

### ✅ 8. 【权限管理】创建应用专用用户（安全最佳实践）

```sql
-- 创建一个只用于应用连接的用户
CREATE USER appuser WITH PASSWORD 'secure_app_password';

-- 授予对特定数据库的连接权限
GRANT CONNECT ON DATABASE myappdb TO appuser;

-- 授予对 public 模式的所有表的 CRUD 权限
GRANT USAGE ON SCHEMA public TO appuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO appuser;

-- 设置默认权限（新表也自动授权）
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO appuser;
```

> 🔍 **适用场景**：Spring Boot 配置 `spring.datasource.username=appuser`，避免使用超级用户。

---

## 📚 三、总结：Java 开发者应掌握的核心命令

| 类别 | 必会命令 | 用途 |
|------|----------|------|
| 🔍 查看结构 | `\l`, `\dt`, `\d`, `\c` | 快速了解数据库 |
| 🛠️ 建表索引 | `CREATE TABLE`, `CREATE INDEX` | 设计数据模型 |
| 💾 增删改查 | `INSERT`, `UPDATE`, `DELETE`, `SELECT` | 日常开发 |
| 📊 复杂查询 | `JOIN`, `GROUP BY`, `WINDOW FUNCTION` | 写报表 SQL |
| ⚡ 性能调优 | `EXPLAIN ANALYZE` | 排查慢查询 |
| 📦 数据迁移 | `\copy` | 导入导出测试数据 |
| 🔐 安全权限 | `CREATE USER`, `GRANT` | 配置生产环境 |

---

## 🎁 附加建议

1. **把常用 SQL 写成 `.sql` 文件**，如 `init.sql`, `migrate_v1.sql`
2. **在 Spring Boot 中使用 `schema.sql` 和 `data.sql` 自动初始化**
3. **用 `psql -f init.sql` 批量执行脚本**
4. **结合 DataGrip 使用**：写 SQL 用 GUI，查结构用 `\dt`，两全其美

---

如果你需要，我可以为你生成一个：
- **PostgreSQL 常用命令速查表（PDF）**
- **Spring Boot + PostgreSQL 的完整初始化脚本模板**
- **Java 实体类与 PostgreSQL 表结构映射对照表**

欢迎继续提问！🚀