`psql` 是 **PostgreSQL 的官方命令行客户端工具**，它是你与 PostgreSQL 数据库进行交互最直接、最强大、最常用的工具之一。

作为 Java 后端开发程序员，掌握 `psql` 能极大提升你对数据库的理解和操作效率，尤其是在调试、初始化数据、执行维护脚本时非常有用。

---

## 🧩 一、`psql` 是什么？

- **全称**：PostgreSQL Interactive Terminal（交互式终端）
- **类型**：命令行工具（CLI）
- **功能**：连接到 PostgreSQL 数据库，执行 SQL 命令，查看结果，管理数据库对象
- **类比**：
    - 类似于 MySQL 的 `mysql` 命令行客户端
    - 类似于 SQL Server 的 `sqlcmd` 或 `sqlplus`（Oracle）

> ✅ 它是 PostgreSQL 安装后自带的核心组件，无需额外安装（除非你只装了客户端库）。

---

## 🚀 二、`psql` 有什么作用？

| 作用 | 说明 |
|------|------|
| 🔌 **连接数据库** | 可以连接本地或远程的 PostgreSQL 实例 |
| 📜 **执行 SQL 语句** | 支持所有标准 SQL：`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE TABLE` 等 |
| 🛠️ **数据库管理** | 创建/删除数据库、用户、模式、扩展等 |
| 📂 **导入导出数据** | 使用 `\copy` 或 `COPY` 导入 CSV、导出查询结果 |
| 🧾 **查看数据库结构** | 查看表、索引、视图、函数等元信息（元数据） |
| 📊 **调试与性能分析** | 使用 `EXPLAIN` 分析查询计划，优化慢 SQL |
| 🔐 **用户与权限管理** | 创建角色、授予权限、修改密码等 |
| 📚 **执行脚本文件** | 批量执行 `.sql` 文件，适合初始化数据库 |

---

## 🖥️ 三、基本使用方式

### 1. 启动 `psql` 并连接数据库

#### 方式一：使用系统用户免密登录（推荐本地使用）

```bash
sudo -u postgres psql
```

这会以 `postgres` 系统用户身份登录，进入 `psql` 交互界面：

```text
psql (15.3)
Type "help" for help.

postgres=#
```

#### 方式二：指定用户和数据库

```bash
psql -U devuser -d myappdb -h localhost -p 5432
```

- `-U`：数据库用户名
- `-d`：数据库名
- `-h`：主机地址
- `-p`：端口

执行后会提示输入密码（如果需要）。

---

### 2. 常用 `psql` 元命令（以 `\` 开头）

`psql` 提供了很多**非 SQL 的快捷命令**（称为“元命令”），帮助你快速查看结构和状态。

| 命令 | 作用 |
|------|------|
| `\l` 或 `\list` | 列出所有数据库 |
| `\c dbname` 或 `\connect dbname` | 切换到指定数据库 |
| `\dt` | 列出当前数据库的所有表 |
| `\dt+` | 列出表并显示更多信息（如注释） |
| `\d tablename` | 查看表结构（字段、类型、约束） |
| `\dv` | 列出所有视图 |
| `\df` | 列出所有函数 |
| `\du` | 列出所有用户（角色） |
| `\x` | 切换“扩展显示模式”，适合查看宽表或 JSON 数据 |
| `\conninfo` | 显示当前连接信息 |
| `\?` | 查看所有元命令帮助 |
| `\h` | 查看 SQL 语法帮助（如 `\h CREATE TABLE`） |
| `\q` | 退出 `psql` |

📌 **示例**：
```sql
postgres=# \dt
         List of relations
 Schema |  Name  | Type  |  Owner   
--------+--------+-------+----------
 public | users  | table | devuser
(1 row)

postgres=# \d users
                   Table "public.users"
   Column   |  Type   | Collation | Nullable | Default 
------------+---------+-----------+----------+---------
 id         | integer |           | not null | 
 name       | text    |           |          | 
 profile    | jsonb   |           |          | 
 created_at | timestamp |         |          | now()
```

---

### 3. 执行 SQL 语句

在 `psql` 中可以直接写 SQL：

```sql
SELECT * FROM users WHERE name LIKE '张%';
UPDATE users SET profile = '{"age": 30}' WHERE id = 1;
EXPLAIN ANALYZE SELECT * FROM users ORDER BY created_at DESC;
```

> ✅ 注意：SQL 语句要以 `;` 结尾，否则不会执行。

---

### 4. 导入和导出数据

#### 导出查询结果到文件
```sql
\copy (SELECT * FROM users) TO '/tmp/users.csv' WITH CSV HEADER;
```

#### 导入 CSV 数据
```sql
\copy users FROM '/tmp/users.csv' WITH CSV HEADER;
```

> ⚠️ 注意路径权限：`psql` 运行在哪个用户下，就用哪个用户的权限读写文件。

#### 执行 SQL 脚本文件
```bash
psql -U devuser -d myappdb -f init_schema.sql
```

---

## 🎯 四、为什么 Java 开发者要学 `psql`？

| 场景 | 使用 `psql` 的优势 |
|------|------------------|
| 初始化数据库结构 | 快速执行 `schema.sql` 创建表、索引 |
| 调试接口返回数据 | 直接查表，验证数据是否正确写入 |
| 分析慢查询 | 用 `EXPLAIN ANALYZE` 看执行计划 |
| 查看表结构 | 比 DataGrip 更快（不用开 GUI） |
| 自动化脚本 | 在 Shell 脚本中调用 `psql` 实现数据库自动化 |
| 生产环境排查 | 服务器上通常只有命令行，没有图形工具 |

---

## 🧰 五、`psql` 与 DataGrip 的关系

| 对比项 | `psql` | DataGrip |
|--------|--------|---------|
| 类型 | 命令行工具 | 图形化客户端（GUI） |
| 速度 | 极快，适合脚本 | 稍慢，但功能丰富 |
| 学习成本 | 需要记忆命令 | 点击操作，易上手 |
| 适用场景 | 服务器维护、自动化、调试 | 日常开发、复杂查询、可视化 |
| 是否必须 | ✅ 推荐掌握 | ✅ 推荐使用 |

> 💡 **最佳实践**：
> - 日常开发用 **DataGrip** 写 SQL、设计表
> - 服务器维护、脚本执行、性能分析用 **`psql`**

---

## ✅ 六、实用技巧（Java 开发者常用）

### 1. 查看某个表的创建语句（反向工程）
```sql
\d+ users
```

### 2. 查看最近执行的慢查询（配合 `pg_stat_statements`）
```sql
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

SELECT query, calls, total_time FROM pg_stat_statements ORDER BY total_time DESC LIMIT 5;
```

### 3. 格式化 JSONB 数据显示
```sql
\x on
SELECT * FROM users WHERE id = 1;
```

输出会自动换行，更易读。

### 4. 设置自动补全和历史记录（可选）
编辑 `~/.psqlrc` 文件，添加：
```conf
\set HISTSIZE 10000
\set COMP_KEYWORDS_TYPECS
\set PROMPT1 '%n@%/%R%# '
```

---

## 📚 总结

| 问题 | 回答 |
|------|------|
| `psql` 是什么？ | PostgreSQL 的命令行客户端 |
| 有什么用？ | 连接数据库、执行 SQL、管理结构、调试性能 |
| Java 开发者需要吗？ | ✅ 非常需要！是高级开发的必备技能 |
| 和 DataGrip 冲突吗？ | ❌ 不冲突，互补使用 |
| 怎么学？ | 多用 `\dt`, `\d`, `\l`, `\c`, `\copy`, `EXPLAIN` |

---

🔧 下一步建议：
你可以现在就打开终端，运行：
```bash
sudo -u postgres psql
```
然后输入：
```sql
\l
\c postgres
\dt
SELECT version();
\q
```

感受一下 `psql` 的强大！

如果你需要，我也可以为你写一个“Java 开发常用 `psql` 命令速查表”PDF 模板 😊