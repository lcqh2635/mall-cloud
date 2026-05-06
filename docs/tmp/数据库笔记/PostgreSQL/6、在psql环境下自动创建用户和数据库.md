非常好！作为 Java 后端开发程序员，你经常需要为新项目初始化数据库环境。下面我为你提供一个 **完整的自动化 SQL 脚本**，可以在 `sudo -u postgres psql` 管理员环境下一键执行，完成：

✅ 创建开发用户  
✅ 创建项目数据库  
✅ 自动连接到该数据库并创建数据表  
✅ 授予正确权限

脚本设计安全、规范，适合本地开发或测试环境使用。

---

## 📜 脚本文件：`init_project_db.sql`

```sql
-- +--------------------------------------------------+
-- |      PostgreSQL 项目初始化脚本（自动化）         |
-- |      用途：创建用户、数据库、表结构并授权         |
-- |      执行方式：sudo -u postgres psql -f init_project_db.sql |
-- +--------------------------------------------------+

-- Step 1: 创建项目专用数据库用户（避免使用超级用户）
-- 推荐命名：项目名或开发者名，如 myapp_dev
-- 权限说明：
--   - LOGIN：允许登录
--   - CREATEDB：允许创建数据库（便于本地开发）
--   - NOCREATEROLE：不能创建其他角色（安全）
--   - 避免 SUPERUSER，防止权限过大
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'myapp_dev') THEN
        CREATE USER myapp_dev WITH
            LOGIN
            PASSWORD 'MyAppDevPass!2025'  -- ⚠️ 建议在生产中使用更安全的密码管理方式
            CREATEDB
            NOCREATEROLE
            NOSUPERUSER;
        RAISE NOTICE '✅ 用户 myapp_dev 创建成功';
    ELSE
        RAISE NOTICE 'ℹ️  用户 myapp_dev 已存在，跳过创建';
    END IF;
END
$$;

-- Step 2: 创建项目数据库，并指定所有者为 myapp_dev
-- 这样该用户可以自由管理此数据库
-- 使用 UTF8 编码，支持中文等多语言
-- 排序规则使用默认即可（通常为 en_US.UTF-8）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'myapp_db') THEN
        CREATE DATABASE myapp_db
            OWNER = myapp_dev
            ENCODING = 'UTF8'
            LC_COLLATE = 'zh_CN.UTF-8'
            LC_CTYPE = 'zh_CN.UTF-8';
        RAISE NOTICE '✅ 数据库 myapp_db 创建成功，所有者为 myapp_dev';
    ELSE
        RAISE NOTICE 'ℹ️  数据库 myapp_db 已存在，跳过创建';
    END IF;
END
$$;

-- Step 3: 切换到新创建的数据库（注意：psql 中需用 \c，但这里用 DO 块无法切换）
-- 所以我们使用 \connect 元命令（只能在 psql 中执行），不能写在 DO 块里
-- 因此我们使用 psql 的 \c 命令（必须在脚本外部使用 \i 或 -f 执行）

-- 注意：以下命令必须在 psql 中执行，不能放在 DO 块中
\c myapp_db

-- Step 4: 在 myapp_db 中创建示例数据表（用户表）
-- 表设计说明：
--   - 使用 UUID 主键，适合分布式系统
--   - 添加 created_at 和 updated_at 时间戳
--   - profile 字段使用 JSONB 存储扩展信息（如昵称、头像）
--   - 添加索引提升查询性能
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    profile JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 为用户名和邮箱创建索引（提升登录查询性能）
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 为表添加注释
COMMENT ON TABLE users IS '用户信息表';
-- 为列添加注释
COMMENT ON COLUMN users.created_at IS '用户创建时间';
COMMENT ON COLUMN users.updated_at IS '用户信息最新一次更新时间';

-- Step 5: 插入一条测试数据（可选）
INSERT INTO users (username, email, password_hash, profile)
VALUES (
    'admin',
    'admin@myapp.com',
    'pbkdf2:sha256:260000$abc123$def456...',  -- 示例哈希，实际应由应用生成
    '{"nick": "管理员", "avatar": "/images/admin.jpg"}'
) ON CONFLICT (username) DO NOTHING;  -- 如果已存在则跳过

-- Step 6: 授予 myapp_dev 用户对所有表的权限（确保权限正确）
-- 虽然它是所有者，但显式授权更清晰
GRANT USAGE ON SCHEMA public TO myapp_dev;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO myapp_dev;
GRANT USAGE, UPDATE ON ALL SEQUENCES IN SCHEMA public TO myapp_dev;

-- Step 7: 设置默认权限（未来新建的表也会自动授权）
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO myapp_dev;

-- Step 8: 输出完成提示
\echo '🎉 项目数据库初始化完成！'
\echo '数据库名: myapp_db'
\echo '用户名: myapp_dev'
\echo '密码: MyAppDevPass!2025'
\echo '连接方式: psql -U myapp_dev -d myapp_db -W'
\echo 'Spring Boot 配置示例：'
\echo '  spring.datasource.url=jdbc:postgresql://localhost:5432/myapp_db'
\echo '  spring.datasource.username=myapp_dev'
\echo '  spring.datasource.password=MyAppDevPass!2025'
```

---

## ▶️ 如何执行这个脚本？

### 步骤 1：保存脚本到文件

```bash
# 创建并编辑脚本
nano init_project_db.sql
# 粘贴上面的内容，保存退出
```

### 步骤 2：以 `postgres` 用户执行脚本

```bash
sudo -u postgres psql -f init_project_db.sql
```

### ✅ 预期输出：
```
NOTICE:  ✅ 用户 myapp_dev 创建成功
NOTICE:  ✅ 数据库 myapp_db 创建成功，所有者为 myapp_dev
You are now connected to database "myapp_db" as user "postgres".
CREATE TABLE
CREATE INDEX
...
🎉 项目数据库初始化完成！
数据库名: myapp_db
用户名: myapp_dev
密码: MyAppDevPass!2025
...
```

---

## 🔐 安全建议（生产环境）

| 建议 | 说明 |
|------|------|
| 🔐 不要使用 `CREATEDB` | 生产环境用户不应能创建数据库 |
| 🔐 密码不要写在脚本中 | 使用环境变量或密钥管理工具 |
| 🔐 使用专用模式（schema） | 避免所有表都在 `public` |
| 🔐 限制连接来源 | 配置 `pg_hba.conf` 只允许特定 IP |

---

## 🧩 你可以根据项目修改的变量

| 变量 | 替换为你的值 |
|------|-------------|
| `myapp_dev` | 你的项目用户，如 `order_service` |
| `myapp_db` | 你的数据库名，如 `order_db` |
| `MyAppDevPass!2025` | 更强的密码 |
| `users` 表结构 | 改为你项目的实际表 |

---

## 🎁 附加：Spring Boot `application.yml` 配置示例

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myapp_db
    username: myapp_dev
    password: MyAppDevPass!2025
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate  # 推荐：验证表结构，不自动修改
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

---

这个脚本你可以在每个新项目开始时复用，只需修改用户名、数据库名和表结构即可，极大提升开发效率。

需要我为你生成一个 **可参数化的 shell 脚本 + SQL 模板**，支持传入项目名、密码等参数吗？欢迎继续提问！🚀