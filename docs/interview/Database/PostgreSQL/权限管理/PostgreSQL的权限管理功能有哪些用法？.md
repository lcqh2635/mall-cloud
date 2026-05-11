PostgreSQL 提供了强大且灵活的 **权限管理（Privilege Management）系统**，用于控制用户对数据库对象（如数据库、表、视图、函数、序列等）的访问权限。以下是 **全面详尽的权限管理用法示例**，涵盖用户、角色、权限授予与撤销等场景，均附带中文注释说明。

---

### 1. 创建用户（具有登录权限的用户）
```sql
-- 创建一个可以登录的用户，密码为加密存储
CREATE USER alice WITH PASSWORD 'secure123';
```

---

### 2. 创建角色（可作为用户组使用）
```sql
-- 创建一个角色（无登录权限），用于权限分组
CREATE ROLE analysts;
```

---

### 3. 创建可登录的角色（兼具用户功能）
```sql
-- 创建一个可登录的角色（相当于用户）
CREATE ROLE bob WITH LOGIN PASSWORD 'pass456';
```

---

### 4. 将用户添加到角色（权限继承）
```sql
-- 让用户 alice 拥有 analysts 角色的所有权限
GRANT analysts TO alice;
```

> 用户可以通过角色继承权限，便于权限集中管理。

---

### 5. 创建管理员用户
```sql
-- 创建具有超级用户权限的管理员（慎用！）
CREATE USER admin WITH SUPERUSER PASSWORD 'adminpass';
```

> `SUPERUSER` 可绕过所有权限检查，仅用于 DBA。

---

### 6. 授予数据库连接权限
```sql
-- 允许用户 alice 连接到 salesdb 数据库
GRANT CONNECT ON DATABASE salesdb TO alice;
```

---

### 7. 授予模式（schema）使用权限
```sql
-- 允许角色 analysts 使用 public 模式
GRANT USAGE ON SCHEMA public TO analysts;
```

> `USAGE` 是访问模式中对象的前提。

---

### 8. 授予表的 SELECT 权限（只读）
```sql
-- 允许 alice 查询 users 表
GRANT SELECT ON TABLE users TO alice;

-- 批量授予多个表
GRANT SELECT ON ALL TABLES IN SCHEMA public TO analysts;
```

---

### 9. 授予表的 INSERT 权限（插入）
```sql
-- 允许 bob 向 orders 表插入数据
GRANT INSERT ON TABLE orders TO bob;
```

---

### 10. 授予表的 UPDATE 权限（更新）
```sql
-- 允许 analysts 更新 products 表的部分字段
GRANT UPDATE (price, stock) ON TABLE products TO analysts;
```

> 可指定具体列，实现列级权限控制。

---

### 11. 授予表的 DELETE 权限（删除）
```sql
-- 允许 bob 删除自己的订单（需配合行级安全）
GRANT DELETE ON TABLE orders TO bob;
```

---

### 12. 授予表的 ALL 权限（全部操作）
```sql
-- 授予用户 carol 对 customers 表的全部权限
GRANT ALL ON TABLE customers TO carol;
```

---

### 13. 授予序列的 USAGE 权限（用于自增 ID）
```sql
-- 允许用户插入数据时使用 users_id_seq 序列
GRANT USAGE, SELECT ON SEQUENCE users_id_seq TO alice;
```

> 否则插入 SERIAL 字段会报权限错误。

---

### 14. 授予函数执行权限
```sql
-- 允许 analysts 调用计算折扣的函数
GRANT EXECUTE ON FUNCTION calculate_discount(numeric) TO analysts;
```

---

### 15. 授予模式中所有对象的默认权限
```sql
-- 设置未来在 public 模式中创建的表，默认授予 analysts SELECT 权限
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO analysts;
```

> 对后续创建的对象生效，不影响已有对象。

---

### 16. 撤销权限（REVOKE）
```sql
-- 撤销 alice 对 users 表的 SELECT 权限
REVOKE SELECT ON TABLE users FROM alice;

-- 撤销角色继承
REVOKE analysts FROM alice;
```

---

### 17. 撤销所有默认权限
```sql
-- 清除默认权限设置
ALTER DEFAULT PRIVILEGES IN SCHEMA public
REVOKE ALL ON TABLES FROM analysts;
```

---

### 18. 查看用户权限（查询系统视图）
```sql
-- 查看当前用户拥有的权限
SELECT * FROM information_schema.table_privileges 
WHERE grantee = 'alice';

-- 查看某表的权限分配
SELECT * FROM information_schema.role_table_grants
WHERE table_name = 'users';
```

---

### 19. 使用行级安全策略（Row Level Security, RLS）
```sql
-- 启用 users 表的行级安全
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- 创建策略：用户只能查看自己的记录
CREATE POLICY select_own ON users 
FOR SELECT 
USING (current_user = name);

-- 授予使用策略的权限
GRANT SELECT ON users TO alice;
```

> RLS 是高级权限控制，基于行内容动态限制访问。

---

### 20. 创建只读用户（常用场景）
```sql
-- 创建只读角色
CREATE ROLE readonly;
GRANT CONNECT ON DATABASE mydb TO readonly;
GRANT USAGE ON SCHEMA public TO readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly;

-- 创建只读用户并赋予角色
CREATE USER reporter WITH PASSWORD 'report123';
GRANT readonly TO reporter;
```

---

### 21. 创建应用专用用户（最小权限原则）
```sql
-- 为 Web 应用创建用户，仅允许必要操作
CREATE USER webapp WITH PASSWORD 'webpass';
GRANT USAGE ON SCHEMA public TO webapp;
GRANT SELECT, INSERT, UPDATE ON TABLE users, sessions TO webapp;
GRANT USAGE ON SEQUENCE users_id_seq TO webapp;
```

---

### 22. 设置对象所有权（ALTER OWNER）
```sql
-- 将表所有权转移给 admin 用户
ALTER TABLE sensitive_data OWNER TO admin;
```

> 只有所有者或超级用户才能修改或撤销权限。

---

### 23. 限制用户连接数
```sql
-- 限制用户 guest 最多同时连接 3 个会话
ALTER USER guest CONNECTION LIMIT 3;
```

---

### 24. 修改用户密码和过期时间
```sql
-- 修改密码
ALTER USER alice PASSWORD 'newpassword';

-- 设置密码过期时间
ALTER USER bob VALID UNTIL '2025-12-31';
```

---

### 25. 锁定/解锁用户
```sql
-- 禁用用户登录（锁定）
ALTER USER inactive_user WITH PASSWORD NULL;

-- 或设置永不过期但禁用
ALTER USER inactive_user VALID UNTIL '1970-01-01';
```

---

### 26. 使用模式隔离权限
```sql
-- 创建部门专用模式
CREATE SCHEMA hr;
CREATE ROLE hr_manager;
GRANT USAGE, CREATE ON SCHEMA hr TO hr_manager;
GRANT ALL ON SCHEMA hr TO hr_manager;
```

> 不同部门使用不同 schema，实现逻辑隔离。

---

### 27. 查看当前用户和权限
```sql
-- 查看当前登录用户
SELECT current_user;

-- 查看当前用户是否为超级用户
SELECT usesuper FROM pg_user WHERE usename = current_user;

-- 查看当前用户所属角色
SELECT rolname FROM pg_roles JOIN pg_auth_members ON (pg_roles.oid = pg_auth_members.roleid)
WHERE pg_auth_members.member = current_user::regrole;
```

---

### 28. 使用 pg_hba.conf 配合权限管理（外部控制）
```conf
# 在 pg_hba.conf 中配置认证方式（非 SQL，但属于权限体系）
host    all             alice           192.168.1.0/24        md5
host    salesdb         analysts        0.0.0.0/0             cert
```

> 控制谁可以通过什么方式连接数据库。

---

### 总结：PostgreSQL 权限模型核心概念

| 概念 | 说明 |
|------|------|
| **用户（User）** | 可登录的账户，本质是带 LOGIN 的角色 |
| **角色（Role）** | 权限容器，可包含多个用户或其他角色 |
| **对象权限** | 表、视图、序列、函数等的 SELECT、INSERT、UPDATE、DELETE、USAGE、EXECUTE 等 |
| **GRANT / REVOKE** | 授予和撤销权限 |
| **DEFAULT PRIVILEGES** | 设置未来对象的默认权限 |
| **Row Level Security (RLS)** | 基于行内容的动态访问控制 |
| **Ownership** | 对象所有者拥有最高权限 |

---

### ✅ 最佳实践建议

1. **遵循最小权限原则**：只授予必要权限。
2. **使用角色分组管理权限**：避免逐个用户授权。
3. **启用 RLS 保护敏感数据**：如多租户系统。
4. **定期审计权限**：查询 `information_schema` 视图。
5. **避免直接给用户 SUPERUSER**：使用 `ROLE` + `GRANT` 更安全。
6. **生产环境禁用默认用户**：如 `postgres` 外部访问。
7. **使用 SSL 和 pg_hba.conf 加强认证**。

---

PostgreSQL 的权限管理系统非常成熟，支持从 **用户管理、角色继承、对象权限、行级安全到默认权限设置** 的全方位控制，适用于企业级安全需求。合理配置可实现精细的访问控制与审计能力。