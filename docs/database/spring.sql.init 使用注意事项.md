是的，**默认情况下，每次 Spring Boot 项目启动都会自动执行 `spring.sql.init` 配置的 SQL 脚本** —— **但前提是满足初始化条件**。

---

### ✅ **关键结论：是否每次启动都执行？**

| 条件 | 是否执行？ | 说明 |
|------|------------|------|
| `spring.sql.init.mode=always` | ✅ **是** | **每次启动都执行**，无论数据库是否存在或是否已初始化 |
| `spring.sql.init.mode=embedded` | ✅ **仅当使用嵌入式数据库时执行** | 如 H2、HSQL、Derby 等。**非嵌入式数据库（如 MySQL、PostgreSQL）不执行** |
| `spring.sql.init.mode=never` | ❌ **从不执行** | 完全禁用初始化功能 |
| 数据库中已有表结构，且脚本中有 `CREATE TABLE IF NOT EXISTS` | ✅ **仍会执行** | 脚本仍会被运行，但不会报错（因为用了 `IF NOT EXISTS`） |
| 数据库中已有数据，脚本中有 `INSERT` | ✅ **仍会执行** | 会**重复插入数据**，可能导致主键冲突或数据冗余！ |

> 🔥 **重点提醒**：  
> **Spring Boot 的 `spring.sql.init` 是“无状态”的初始化机制 —— 它不会判断“是否已经初始化过”，它只认配置和脚本文件的存在！**

---

### 📌 示例说明

#### 场景 1：`mode: always` + 启动两次
```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema-mysql.sql
      data-locations: classpath:data-mysql.sql
```

- 第一次启动：
    - 执行 `schema-mysql.sql` → 创建 `users` 表
    - 执行 `data-mysql.sql` → 插入 3 条用户记录
- 第二次启动：
    - **再次执行** `schema-mysql.sql` → 如果没有 `IF NOT EXISTS`，会报错（表已存在）
    - **再次执行** `data-mysql.sql` → **再次插入 3 条相同数据** → 可能违反唯一约束（如 `username UNIQUE`）

👉 **结果：数据重复、主键冲突、应用启动失败！**

---

### ✅ 如何避免重复执行导致的问题？

#### ✅ 方法一：在 SQL 脚本中使用安全语法（推荐）

```sql
-- schema-mysql.sql
CREATE TABLE IF NOT EXISTS users ( ... );  -- 安全建表
CREATE TABLE IF NOT EXISTS roles ( ... );

-- data-mysql.sql
INSERT INTO users (username, email) 
SELECT 'admin', 'admin@example.com' 
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
```

> ✅ 使用 `IF NOT EXISTS` 和 `WHERE NOT EXISTS` 可以**幂等执行**（多次执行也不会出错或重复）

#### ✅ 方法二：开发阶段用 `mode: always`，生产环境设为 `never`

```yaml
# application-dev.yml（开发环境）
spring:
  sql:
    init:
      mode: always

# application-prod.yml（生产环境）
spring:
  sql:
    init:
      mode: never
```

#### ✅ 方法三：改用 Flyway / Liquibase（生产推荐）

这两个工具会记录每个迁移脚本的执行状态（在 `flyway_schema_history` 表中），确保：

- 每个脚本只执行一次
- 支持版本控制、回滚、顺序执行
- 适合团队协作和生产部署

```xml
<!-- Maven 依赖示例 -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

> 💡 **最佳实践**：  
> **开发/测试** → 用 `spring.sql.init` 快速搭建  
> **预生产/生产** → **必须改用 Flyway/Liquibase**

---

### 🚫 常见错误场景（不加判断直接写 INSERT）

```sql
-- data-mysql.sql（错误写法）
INSERT INTO users VALUES (1, 'admin', 'admin@xxx.com');
INSERT INTO users VALUES (2, 'user1', 'user1@xxx.com');
```

→ 第二次启动时会报错：

```
Duplicate entry '1' for key 'PRIMARY'
```

因为你试图插入已存在的主键！

---

### ✅ 总结：什么时候会执行？

| 条件 | 是否执行 |
|------|----------|
| `mode=always` | ✅ **每次启动都执行**（危险！需幂等脚本） |
| `mode=embedded` | ✅ **仅嵌入式数据库**（如 H2）启动时执行 |
| `mode=never` | ❌ 从不执行 |
| 数据库不存在 | ✅ 执行（会创建） |
| 数据库存在但无表 | ✅ 执行（建表） |
| 数据库存在且有表 | ✅ **仍然执行**（可能报错或重复） |

---

### ✅ 最佳建议（强烈推荐）

| 场景 | 推荐做法 |
|------|----------|
| **本地开发** | 使用 `mode: always` + 幂等脚本（`IF NOT EXISTS` + `WHERE NOT EXISTS`） |
| **单元测试** | 使用 `H2` + `mode: always`，每次测试前干净重建 |
| **CI/CD 测试环境** | 使用 `mode: always`，配合内存数据库（H2） |
| **预生产/生产环境** | **禁用 `spring.sql.init`**，改用 **Flyway** 或 **Liquibase** 管理数据库变更 |

---

### 🛠️ 附：如何快速验证是否被执行？

在 `application.yml` 中开启日志：

```yaml
logging:
  level:
    org.springframework.boot.jdbc: DEBUG
```

启动后你会看到类似日志：

```
o.s.b.jdbc.init.ScriptStatementRunner : Executing SQL script from class path resource [schema-mysql.sql]
o.s.b.jdbc.init.ScriptStatementRunner : Executed SQL script from class path resource [schema-mysql.sql] in 45 ms.
o.s.b.jdbc.init.ScriptStatementRunner : Executing SQL script from class path resource [data-mysql.sql]
o.s.b.jdbc.init.ScriptStatementRunner : Executed SQL script from class path resource [data-mysql.sql] in 12 ms.
```

> 如果看到这些日志，就说明脚本被成功执行了。

---

✅ **一句话总结**：
> **`spring.sql.init` 默认每次启动都执行脚本 —— 你必须自己保证脚本是“幂等”的，否则就会出错！**  
> 生产环境请务必改用 Flyway/Liquibase！