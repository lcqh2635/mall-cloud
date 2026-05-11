在实际 Java 项目开发中，**自动记录和统计 PostgreSQL 的 `SELECT` 慢查询** 是性能监控和系统优化的关键环节。它能帮助你：

- 及时发现性能瓶颈
- 定位 N+1 查询、全表扫描等问题
- 优化数据库索引和 SQL 写法
- 避免线上接口因慢 SQL 而超时

下面我将为你详细介绍 **如何在 PostgreSQL + Java 环境中实现“自动记录和统计慢查询”**，包含：

✅ 数据库层配置  
✅ 监控扩展启用  
✅ Java 应用集成  
✅ 实际可落地的示例（含中文注释）

---

## 🧩 一、PostgreSQL 层：开启慢查询日志（`log_min_duration_statement`）

这是最基础也是最重要的一步：让 PostgreSQL 自动记录执行时间超过阈值的 SQL。

### ✅ 步骤 1：修改 `postgresql.conf` 配置文件

```bash
sudo vi /var/lib/pgsql/data/postgresql.conf
```

找到并修改以下配置：

```conf
# 记录执行时间超过 500ms 的 SQL（可根据需要调整）
log_min_duration_statement = 500

# 启用慢查询日志（默认可能关闭）
logging_collector = on

# 日志输出路径
log_directory = 'log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'

# 记录 SQL 语句和持续时间
log_statement = 'none'           # 不记录所有 SQL（避免日志过大）
log_duration = on                # 记录执行时间
```

> ⚠️ 建议：
> - 开发/测试环境设为 `100ms`
> - 生产环境设为 `500ms` 或 `1s`
> - 避免设为 `0`（记录所有 SQL，日志爆炸）

### ✅ 步骤 2：重启 PostgreSQL

```bash
sudo systemctl restart postgresql
```

### ✅ 步骤 3：验证慢查询日志

执行一个慢查询：

```sql
SELECT pg_sleep(1);  -- 睡眠 1 秒
```

查看日志文件：

```bash
tail -f /var/lib/pgsql/data/log/postgresql-*.log
```

你会看到类似输出：

```
LOG:  duration: 1002.345 ms  statement: SELECT pg_sleep(1);
```

✅ 说明慢查询已记录。

---

## 🛠️ 二、使用 `pg_stat_statements` 扩展：统计最耗时 SQL（推荐！）

`pg_stat_statements` 是 PostgreSQL 官方提供的**查询性能统计扩展**，可以：

- 统计每条 SQL 的执行次数、总耗时、平均耗时、最大耗时
- 帮你找出“最慢的 TOP 10 SQL”
- 支持 `SELECT`、`INSERT`、`UPDATE`、`DELETE`

### ✅ 步骤 1：启用 `pg_stat_statements` 扩展

```sql
-- 以 postgres 用户登录
sudo -u postgres psql

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
```

### ✅ 步骤 2：配置 `postgresql.conf`（确保已启用）

```conf
# 在 postgresql.conf 中添加
shared_preload_libraries = 'pg_stat_statements'
pg_stat_statements.track = all
```

> ⚠️ 修改后必须重启 PostgreSQL：
```bash
sudo systemctl restart postgresql
```

---

## 📊 三、查询慢 SQL 统计（实用 SQL 示例）

### ✅ 示例 1：查看执行时间最长的 TOP 10 SELECT 查询

```sql
-- 查询最耗时的 10 条 SELECT 语句（总耗时）
SELECT
    query,                      -- SQL 语句（自动去重参数）
    calls,                      -- 执行次数
    total_exec_time AS total_ms, -- 总耗时（毫秒）
    mean_exec_time AS avg_ms,   -- 平均耗时（毫秒）
    rows,                       -- 返回总行数
    100.0 * shared_blks_hit / NULLIF(shared_blks_hit + shared_blks_read, 0) AS hit_percent -- 缓存命中率
FROM
    pg_stat_statements
WHERE
    query LIKE 'SELECT%'        -- 只看 SELECT
    AND dbid = (SELECT oid FROM pg_database WHERE datname = current_database())
ORDER BY
    total_exec_time DESC
LIMIT 10;
```

> ✅ **用途**：找出“总耗时最高”的慢查询，优先优化。

---

### ✅ 示例 2：查看平均最慢的 SELECT 查询

```sql
-- 找出平均执行时间最长的 SQL（单次最慢）
SELECT
    query,
    calls,
    total_exec_time,
    mean_exec_time AS avg_ms,
    stddev_exec_time AS std_dev_ms  -- 标准差，看是否波动大
FROM
    pg_stat_statements
WHERE
    query LIKE 'SELECT%'
    AND mean_exec_time > 100  -- 平均超过 100ms
ORDER BY
    mean_exec_time DESC
LIMIT 10;
```

> ✅ **用途**：发现“每次都很慢”的 SQL，可能是缺少索引。

---

### ✅ 示例 3：查看执行最频繁的 SQL（高频小查询）

```sql
-- 找出执行次数最多的 SQL（可能引发 N+1 问题）
SELECT
    query,
    calls,
    total_exec_time,
    mean_exec_time
FROM
    pg_stat_statements
ORDER BY
    calls DESC
LIMIT 10;
```

> ✅ **用途**：识别 N+1 查询、循环中查数据库等问题。

---

### ✅ 示例 4：重置统计信息（定期使用）

```sql
-- 清空统计，重新开始（建议每周或每月重置）
SELECT pg_stat_statements_reset();
```

> ✅ **建议**：写个定时任务每月初执行一次。

---

## 🧱 四、Java 应用层：自动捕获和上报慢 SQL

除了数据库层，你还可以在 Java 应用中**自动捕获慢 SQL 并上报日志或监控系统**。

### 方案 1：使用 `p6spy`（推荐）

`p6spy` 是一个 JDBC 代理工具，可以拦截所有 SQL 并记录执行时间。

#### 步骤 1：添加依赖（Maven）

```xml
<dependency>
    <groupId>com.github.p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

#### 步骤 2：修改数据库 URL

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:p6spy:postgresql://localhost:5432/myapp_db
    username: myapp_dev
    password: MyAppDevPass!2025
```

#### 步骤 3：配置 `spy.properties`

```properties
# spy.properties
modulelist=com.p6spy.engine.logging.P6LogFactory,com.p6spy.engine.outage.P6OutageFactory
# 记录超过 500ms 的 SQL
executionThreshold=500
# 日志格式
logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat
customLogMessageFormat=%(currentTime) | %(executionTime) ms | %(category) | connection%(connectionId) | %(sqlSingleLine)
```

#### ✅ 效果：

日志输出：
```
15:30:21.123 | 680 ms | statement | connection1 | SELECT * FROM orders WHERE user_id = '123'
```

> ✅ 自动捕获慢 SQL，无需改代码。

---

### 方案 2：使用 HikariCP + 自定义 `ProxyConnection`

```java
// 在 DataSource 配置中添加慢 SQL 监控
@Bean
public HikariDataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:postgresql://localhost:5432/myapp_db");
    config.setUsername("myapp_dev");
    config.setPassword("MyAppDevPass!2025");
    config.setMetricRegistry(metricRegistry); // 集成 Micrometer
    return new HikariDataSource(config);
}
```

配合 **Micrometer + Prometheus**，可将 SQL 执行时间打点监控。

---

## 📈 五、可视化监控方案（可选）

| 工具 | 说明 |
|------|------|
| **Prometheus + Grafana** | 采集 `pg_stat_statements` 数据，做慢 SQL 仪表盘 |
| **pgAdmin 4** | 内置“Dashboard”可查看慢查询 |
| **Datadog / New Relic** | 商业 APM 工具，自动分析慢 SQL |
| **自研脚本 + 邮件报警** | 定时执行 SQL，发现慢查询发邮件 |

---

## ✅ 六、推荐完整流程（自动化慢查询监控）

```bash
1. PostgreSQL 开启 pg_stat_statements 和慢查询日志
2. Java 应用使用 p6spy 或 Micrometer 捕获 SQL 耗时
3. 定时任务每天执行：
   SELECT 最慢的 10 条 SQL
   发送邮件或企业微信通知
4. 开发人员每周 review 慢 SQL 报告
5. 优化索引、重写 SQL、修复 N+1
6. 重新统计，验证优化效果
```

---

## 🎁 七、附：慢查询监控脚本模板（shell）

```bash
#!/bin/bash
# slow_sql_report.sh
# 用途：每天发送慢 SQL 报告

OUTPUT=$(sudo -u postgres psql -d myapp_db -c "
SELECT query, calls, total_exec_time, mean_exec_time 
FROM pg_stat_statements 
WHERE mean_exec_time > 500 
ORDER BY total_exec_time DESC 
LIMIT 10;"
)

echo "【慢 SQL 报告】$(date)
以下 SQL 平均执行时间超过 500ms：

$OUTPUT
" | mail -s "PostgreSQL 慢查询告警" dev-team@example.com
```

> 可加入 `crontab` 每天执行。

---

## ✅ 总结

| 方案 | 优点 | 适用场景 |
|------|------|----------|
| `log_min_duration_statement` | 简单直接 | 所有环境 |
| `pg_stat_statements` | 统计精准，可排序 | 推荐必开 |
| `p6spy` | Java 层捕获，带堆栈 | 开发/测试 |
| `HikariCP + Micrometer` | 集成监控系统 | 生产环境 |
| 定时脚本 + 邮件 | 自动化告警 | 运维监控 |

---

如果你需要，我可以为你生成：
- 一份 **`pg_stat_statements` 查询模板 PDF**
- 一个 **Spring Boot + p6spy 的完整配置示例**
- 一个 **慢 SQL 自动化报告脚本**

欢迎继续提问！🚀