太棒了！你已经掌握了 **Spring Boot + PostgreSQL 读写分离** 的基础实现，现在我们来进入 **进阶阶段**：使用 **Apache ShardingSphere** 实现 **分库分表 + 读写分离** 的企业级解决方案。

> ✅ **为什么选择 ShardingSphere？**
> - 开源（Apache 顶级项目）
> - 无侵入：兼容 JPA、MyBatis、原生 JDBC
> - 支持读写分离、分库分表、加密、影子库等
> - 提供 **ShardingSphere-JDBC**（轻量级）和 **ShardingSphere-Proxy**（透明网关）两种模式

---

## 🚀 一、架构目标

我们将实现：

```
                   +---------------------+
                   |   Application       |
                   |   ShardingSphere-JDBC |
                   +----------+----------+
                              |
         +---------------------+----------------------+
         |                                            |
+--------v---------+                     +------------v------------+
|   master_db_0     |<--- 流复制 --------|   replica_db_0 (read)     |
|   (write)         |                   |                           |
|   192.168.1.10:5432 |                   |   192.168.1.11:5432       |
+-------------------+                   +---------------------------+

+--------v---------+                     +------------v------------+
|   master_db_1     |<--- 流复制 --------|   replica_db_1 (read)     |
|   (write)         |                   |                           |
|   192.168.1.12:5432 |                   |   192.168.1.13:5432       |
+-------------------+                   +---------------------------+
```

- ✅ **读写分离**：写走主库，读走从库
- ✅ **分库分表**：按 `user_id` 模 2 分到两个库，每库一张表
- ✅ **自动路由**：SQL 自动路由到正确库和表

---

## 📦 二、前置条件

1. **4 台 PostgreSQL 实例**（可用 Docker 模拟）
    - `master_db_0`：192.168.1.10:5432
    - `replica_db_0`：192.168.1.11:5432
    - `master_db_1`：192.168.1.12:5432
    - `replica_db_1`：192.168.1.13:5432
2. **主从复制已配置完成**
3. **Spring Boot 项目**

---

## 📁 三、项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/example/sharding/
│   │       ├── ShardingSphereConfig.java
│   │       ├── service/UserService.java
│   │       └── model/User.java
│   └── resources/
│       └── application.yml
```

---

## ✅ 四、Maven 依赖（ShardingSphere-JDBC）

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- ShardingSphere-JDBC 核心依赖 -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
        <version>5.3.2</version>
    </dependency>
</dependencies>
```

---

## ✅ 五、`application.yml` 配置（核心）

```yaml
# application.yml
spring:
  shardingsphere:
    # ========== 数据源配置 ==========
    datasource:
      names: master0,slave0,master1,slave1

      # 主库 0
      master0:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: org.postgresql.Driver
        jdbc-url: jdbc:postgresql://192.168.1.10:5432/myapp_db
        username: myapp_dev
        password: MyAppDevPass!2025

      # 从库 0
      slave0:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: org.postgresql.Driver
        jdbc-url: jdbc:postgresql://192.168.1.11:5432/myapp_db
        username: myapp_dev
        password: MyAppDevPass!2025

      # 主库 1
      master1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: org.postgresql.Driver
        jdbc-url: jdbc:postgresql://192.168.1.12:5432/myapp_db
        username: myapp_dev
        password: MyAppDevPass!2025

      # 从库 1
      slave1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: org.postgresql.Driver
        jdbc-url: jdbc:postgresql://192.168.1.13:5432/myapp_db
        username: myapp_dev
        password: MyAppDevPass!2025

    # ========== 读写分离配置 ==========
    rules:
      # 读写分离
      readwrite-splitting:
        data-sources:
          # 逻辑数据源名称：ds0
          ds0:
            write-data-source-name: master0
            read-data-source-names: slave0
            load-balancer-name: round_robin
          # 逻辑数据源名称：ds1
          ds1:
            write-data-source-name: master1
            read-data-source-names: slave1
            load-balancer-name: round_robin

      # 分库分表规则
      sharding:
        # 逻辑表配置
        tables:
          t_user:
            # 实际表：ds0.t_user_0, ds0.t_user_1, ds1.t_user_0, ds1.t_user_1
            actual-data-nodes: ds${0..1}.t_user_${0..1}
            # 分库策略：按 user_id 模 2 决定用 ds0 还是 ds1
            database-strategy:
              standard:
                sharding-column: user_id
                sharding-algorithm-name: database-inline
            # 分表策略：按 id 模 2 决定用 _0 还是 _1
            table-strategy:
              standard:
                sharding-column: id
                sharding-algorithm-name: table-inline

        # 分片算法
        sharding-algorithms:
          database-inline:
            type: INLINE
            props:
              algorithm-expression: ds${user_id % 2}
          table-inline:
            type: INLINE
            props:
              algorithm-expression: t_user_${id % 2}

        # 默认策略
        default-database-strategy:
          none: # 不分库（用于其他表）
        default-table-strategy:
          none: # 不分表

    # ========== 全局配置 ==========
    props:
      sql-show: true  # 显示实际执行的 SQL（调试用）
```

---

## ✅ 六、实体类 `User.java`

```java
// com/example/sharding/model/User.java
package com.example.sharding.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 用于分库的字段

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "email", length = 100)
    private String email;

    // 构造函数、getter、setter
    public User() {}

    public User(Long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    // getter 和 setter 省略
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

---

## ✅ 七、Service 层测试

```java
// com/example/sharding/service/UserService.java
package com.example.sharding.service;

import com.example.sharding.model.User;
import com.example.sharding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建用户
     * 根据 userId % 2 路由到 ds0 或 ds1
     * 根据 id % 2 决定存入 t_user_0 或 t_user_1
     */
    @Transactional
    public User createUser(Long userId, String username, String email) {
        User user = new User(userId, username, email);
        return userRepository.save(user);
    }

    /**
     * 查询所有用户
     * 自动从对应的从库读取
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 按 ID 查询
     */
    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }
}
```

---

## ✅ 八、Repository

```java
// com/example/sharding/repository/UserRepository.java
package com.example.sharding.repository;

import com.example.sharding.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
}
```

---

## ✅ 九、工作流程说明

| 操作 | 路由逻辑 |
|------|----------|
| `save(user)`，`userId=100` | `100 % 2 = 0` → 路由到 `ds0`（master0）<br>`id % 2` → 决定存 `t_user_0` 或 `t_user_1` |
| `findAll()` | 从 `ds0.slave0` 和 `ds1.slave1` 读取 |
| `findById(id)` | 根据 `id % 2` 确定表，自动从对应从库读取 |

---

## ✅ 十、验证 SQL 路由

启用 `sql-show: true` 后，你会看到类似日志：

```log
[INFO ] ShardingSphere-SQL - Logic SQL: INSERT INTO t_user ...
[INFO ] ShardingSphere-SQL - Actual SQL: ds0 ::: INSERT INTO t_user_0 ...
[INFO ] ShardingSphere-SQL - Actual SQL: ds0 ::: SELECT * FROM t_user_0 ...
```

---

## ✅ 十一、ShardingSphere 优势总结

| 特性 | 说明 |
|------|------|
| 🔌 无侵入 | 原有 JPA/MyBatis 代码无需修改 |
| 🧩 灵活分片 | 支持自定义分片算法（Java 类） |
| 🔄 读写分离 | 自动读从库，写主库 |
| 🔐 数据加密 | 敏感字段自动加解密 |
| 🧹 影子库 | 测试 SQL 走影子库，不影响生产 |
| 📊 监控 | 集成 Prometheus、OpenTelemetry |

---

## ✅ 十二、进阶建议

| 场景 | 推荐方案 |
|------|----------|
| 更复杂分片逻辑 | 实现 `StandardShardingAlgorithm` 接口 |
| 多租户 | 按 `tenant_id` 分库 |
| 全文搜索 | 结合 Elasticsearch 使用 |
| 高可用 | 使用 `ShardingSphere-Proxy` + Keepalived |
| 自动化运维 | 使用 `ShardingSphere-Scaling` 在线数据迁移 |

---

## 🎁 附加：Docker 快速搭建主从复制（可选）

```bash
# 启动主库
docker run -d --name pg-master \
  -e POSTGRES_PASSWORD=123456 \
  -p 5432:5432 \
  postgres:15

# 启动从库（需配置流复制）
# 可使用 docker-compose + repmgr 实现
```

---

## ✅ 总结

你现在已经掌握了：

- ✅ **ShardingSphere-JDBC 的集成方式**
- ✅ **分库分表 + 读写分离的配置**
- ✅ **基于 `user_id` 和 `id` 的路由策略**
- ✅ **无侵入式接入现有项目**

这套方案适用于 **高并发、大数据量、读多写少** 的系统，如电商、社交、内容平台。

---

如果你需要，我可以为你生成：
- 一个 **ShardingSphere-Proxy 的配置模板**
- 一个 **自定义分片算法的 Java 实现**
- 一个 **分库分表 + 读写分离 + 数据加密的完整项目**

欢迎继续提问！🚀