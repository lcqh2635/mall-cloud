`spring.sql.init` 是 Spring Boot 2.7+ 版本中引入的一组配置属性，用于**自动初始化数据库（Schema 和 Data）**，替代了旧版 `spring.datasource.schema` 和 `spring.datasource.data` 等过时配置。它提供了更统一、灵活和语义清晰的数据库初始化机制。

---

### ✅ **作用**
`spring.sql.init` 的主要作用是：
- 在应用启动时，**自动执行 SQL 脚本**来创建数据库结构（Schema）和插入初始数据（Data）。
- 支持多种数据库（如 MySQL、PostgreSQL、H2、Oracle 等），Spring Boot 会根据连接的数据库类型自动适配脚本语法。
- 可在开发、测试和生产环境中灵活控制是否启用初始化。

---

### 🎯 **使用场景**
| 场景 | 说明 |
|------|------|
| **开发环境** | 快速重建数据库结构 + 插入测试数据（如用户、角色等） |
| **测试环境** | 每次运行测试前清空并重新初始化数据库，保证测试隔离性 |
| **本地演示/POC** | 不需要手动建库建表，启动即用 |
| **CI/CD 流水线** | 自动化部署时自动初始化测试数据库 |

> ⚠️ 注意：**不建议在生产环境启用自动初始化**，除非你有严格的脚本管理流程（如通过 Flyway/Liquibase）。自动初始化可能覆盖线上数据！

---

### 📋 **常用配置项**

| 配置项 | 说明 |
|--------|------|
| `spring.sql.init.mode` | 控制初始化模式：`always`（总是）、`embedded`（仅嵌入式数据库）、`never`（从不） |
| `spring.sql.init.schema-locations` | 指定 Schema 初始化脚本路径（默认：`classpath*:schema*.sql`） |
| `spring.sql.init.data-locations` | 指定 Data 初始化脚本路径（默认：`classpath*:data*.sql`） |
| `spring.sql.init.platform` | 指定数据库平台（如 `mysql`, `postgresql`, `h2`），用于选择平台特定脚本 |
| `spring.sql.init.continue-on-error` | 是否在执行脚本出错时继续（默认：`false`） |
| `spring.sql.init.encoding` | SQL 文件编码（默认：`UTF-8`） |
| `spring.sql.init.separator` | SQL 脚本中语句分隔符（默认：`;`） |
| `spring.sql.init.generate-unique-name` | 是否为生成的脚本文件名添加唯一标识（用于避免冲突） |

---

### 📂 完整使用示例（带中文注释）

#### ✅ 目录结构
```
src/
└── main/
    └── resources/
        ├── schema-mysql.sql      # MySQL 数据库结构脚本
        ├── data-mysql.sql        # MySQL 初始数据脚本
        ├── schema-postgresql.sql # PostgreSQL 数据库结构脚本
        ├── data-postgresql.sql   # PostgreSQL 初始数据脚本
        └── application.yml       # Spring Boot 配置文件
```

---

#### 📄 `application.yml` 配置文件（完整示例）

```yaml
# ======================== 数据库初始化配置 ========================
spring:
  sql:
    # 控制初始化行为：开发环境设为 always，生产环境设为 never
    mode: always                    # 总是执行初始化（开发/测试用）
    
    # 指定 Schema 初始化脚本位置（支持通配符 *）
    schema-locations: classpath:schema-*.sql
    
    # 指定 Data 初始化脚本位置
    data-locations: classpath:data-*.sql
    
    # 根据数据库类型加载对应脚本（如使用 MySQL，则加载 schema-mysql.sql）
    platform: mysql                 # 指定平台，匹配 schema-*.sql 和 data-*.sql 中的平台后缀
    
    # 出错时是否继续执行后续脚本（生产环境建议 false）
    continue-on-error: false        # 错误时停止，防止部分初始化导致状态不一致
    
    # SQL 文件编码
    encoding: UTF-8                 # 推荐始终使用 UTF-8
    
    # SQL 语句分隔符（默认是 ;，如需修改可自定义）
    separator: ";"                  # 多条语句以分号分隔
    
    # 是否为脚本文件名生成唯一标识（一般不需要，用于多实例并发场景）
    generate-unique-name: false

# ======================== 数据源配置（配合使用）========================
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

# ======================== 其他建议配置 =========================
# 开发环境推荐使用 H2 内存数据库快速测试
# spring:
#   datasource:
#     url: jdbc:h2:mem:testdb
#     driver-class-name: org.h2.Driver
#     username: sa
#     password:
#   h2:
#     console:
#       enabled: true
#       path: /h2-console
```

---

#### 📄 `schema-mysql.sql`（数据库结构脚本）

```sql
-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30) NOT NULL UNIQUE
);
```

> 💡 注意：`IF NOT EXISTS` 是为了防止重复执行时报错，尤其在开发中频繁重启时很有用。

---

#### 📄 `data-mysql.sql`（初始数据脚本）

```sql
-- 插入默认用户
INSERT INTO users (username, email) VALUES 
('admin', 'admin@example.com'),
('user1', 'user1@example.com'),
('user2', 'user2@example.com');

-- 插入角色
INSERT INTO roles (name) VALUES 
('ROLE_ADMIN'),
('ROLE_USER');
```

---

### 🔍 运行效果
当你启动 Spring Boot 应用时：

1. Spring Boot 检测到 `spring.sql.init.mode=always`
2. 根据 `platform=mysql` 加载 `schema-mysql.sql` → 创建 `users` 和 `roles` 表
3. 加载 `data-mysql.sql` → 插入 3 个用户和 2 个角色
4. 应用正常启动，数据库已准备好

> ✅ 如果你切换为 `platform=postgresql`，则会自动加载 `schema-postgresql.sql` 和 `data-postgresql.sql`

---

### ⚠️ 最佳实践建议

| 建议 | 说明 |
|------|------|
| **开发/测试** | 使用 `mode: always` + `schema-*.sql` + `data-*.sql` 快速初始化 |
| **生产环境** | **禁用** `spring.sql.init`，改用 Flyway 或 Liquibase 管理版本化迁移 |
| **脚本命名规范** | 使用 `schema-<platform>.sql` 和 `data-<platform>.sql` 实现平台兼容 |
| **避免 DDL/DML 混合** | 将建表语句（DDL）与插入数据（DML）分离，便于维护 |
| **事务控制** | 默认每个脚本在一个事务中执行，失败则回滚，确保一致性 |

---

### ✅ 总结

| 功能 | `spring.sql.init` |
|------|------------------|
| 替代旧版 | `spring.datasource.schema/data` |
| 主要用途 | 自动初始化开发/测试数据库 |
| 强大特性 | 平台差异化脚本、错误处理、编码控制 |
| 生产建议 | ❌ 不推荐直接使用，应使用 Flyway/Liquibase |

> 💡 **推荐搭配**：开发阶段用 `spring.sql.init` 快速搭建，上线前迁移到 Flyway 实现版本可控的数据库变更管理。

这样配置后，你的团队就能实现“**拉代码 → 启动 → 数据库就绪**”的极致开发体验！