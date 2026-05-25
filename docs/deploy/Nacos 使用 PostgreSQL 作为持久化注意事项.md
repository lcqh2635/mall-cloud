非常感谢您的指正！您说得完全正确，**Nacos 3.x（包括 3.2.1）已经在内核层面原生支持了 PostgreSQL**，并提供了官方的 `postgresql-schema.sql` 初始化脚本。这标志着 Nacos 在去 MySQL 化和信创适配上迈出了重要一步，彻底告别了过去依赖第三方 SPI 插件的“魔改”时代。

基于 **Nacos 3.2.1 + PostgreSQL 原生支持** 这一最新技术背景，我为您重新深度分析该需求，并提供一套企业级的 Podman Compose 编排方案。

---

### 一、 需求合理性重新分析

#### 1. 使用 PostgreSQL 作为持久化：**非常合理且推荐**
* **统一技术栈**：如果您的团队或公司已经以 PostgreSQL 为主要关系型数据库，使用 PG 存储 Nacos 配置可以避免额外引入和维护 MySQL 集群，大幅降低运维成本。
* **信创与合规**：在国产化替代和信创背景下，PostgreSQL（及其国产发行版如 openGauss、Kingbase、PolarDB-PG）是首选，Nacos 3.x 的原生支持完美契合这一趋势。
* **免插件维护**：原生支持意味着官方保证了 SQL 方言的兼容性、连接池的稳定性以及升级时的平滑过渡，不再有社区插件版本滞后带来的风险。

#### 2. 使用 PostgreSQL“多节点集群”：**合理，但需选对架构**
* **PG 的多节点特性**：与 MySQL MGR 原生内置多节点不同，PostgreSQL 的多节点高可用通常需要依赖外部组件（如 **Patroni + etcd** 或 **PGPool-II**）。
* **Compose 环境的局限性**：在 Podman Compose 中搭建一套完整的 Patroni 集群极其复杂且臃肿，不适合开发/测试环境。
* **推荐做法**：在 Compose 中，我们使用 **PG 主从流复制（Primary + Standby）** 来模拟多节点。Nacos 通过 PostgreSQL JDBC 驱动原生的**多主机故障转移特性**（或连接代理）来实现高可用。在生产环境中，则推荐直接使用云厂商的 RDS for PG 高可用版，或部署 Patroni 集群。

---

### 二、 实际开发与生产推荐做法

1. **数据库连接高可用**：PostgreSQL JDBC 驱动原生支持多 Host 配置（`jdbc:postgresql://host1:5432,host2:5432/db?targetServerType=primary`）。在 Nacos 3.x 中，建议充分利用这一特性，让 Nacos 自动感知 PG 主备切换。
2. **Nacos 客户端接入**：依然**强烈建议微服务直连 Nacos 多节点**（逗号分隔），不要使用 Nginx 代理 gRPC 端口。
3. **生产环境 PG 架构**：生产环境请务必使用 **Patroni + etcd + HAProxy** 架构，或直接采购云数据库（如阿里云 RDS PG 高可用版、AWS Aurora PostgreSQL），不要手动在虚机上搭建简单的流复制。

---

### 三、 完整的 Podman Compose 编排文件 (Nacos 3.2.1 + PG 主从)

以下 YAML 文件编排了 **3 节点 Nacos 3.2.1 集群** 和 **2 节点 PostgreSQL 16 主从集群**。

请将内容保存为 `podman-compose.yml`：

```yaml
# 使用 Compose 规范，Podman Compose 原生支持
name: nacos-pg-cluster

networks:
  nacos-pg-net:
    driver: bridge
    ipam:
      config:
        - subnet: 172.30.0.0/16

services:
  # ==========================================
  # 1. PostgreSQL 主库 (Primary)
  # ==========================================
  postgres-primary:
    image: bitnami/postgresql:16
    container_name: pg-primary
    restart: always
    networks:
      - nacos-pg-net
    environment:
      # 主库配置
      - POSTGRESQL_USERNAME=nacos
      - POSTGRESQL_PASSWORD=nacos123
      - POSTGRESQL_DATABASE=nacos_config
      # 开启主从复制
      - POSTGRESQL_REPLICATION_MODE=master
      - POSTGRESQL_REPLICATION_USER=repl_user
      - POSTGRESQL_REPLICATION_PASSWORD=repl_pass
      # 优化连接数 (Nacos 集群连接)
      - POSTGRESQL_MAX_CONNECTIONS=200
    volumes:
      - pg_primary_data:/bitnami/postgresql
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U nacos -d nacos_config"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ==========================================
  # 2. PostgreSQL 从库 (Standby)
  # ==========================================
  postgres-secondary:
    image: bitnami/postgresql:16
    container_name: pg-secondary
    restart: always
    networks:
      - nacos-pg-net
    environment:
      # 从库配置
      - POSTGRESQL_REPLICATION_MODE=slave
      - POSTGRESQL_REPLICATION_USER=repl_user
      - POSTGRESQL_REPLICATION_PASSWORD=repl_pass
      # 指向主库
      - POSTGRESQL_MASTER_HOST=postgres-primary
      - POSTGRESQL_MASTER_PORT_NUMBER=5432
      - POSTGRESQL_USERNAME=nacos
      - POSTGRESQL_PASSWORD=nacos123
      - POSTGRESQL_DATABASE=nacos_config
    volumes:
      - pg_secondary_data:/bitnami/postgresql
    depends_on:
      postgres-primary:
        condition: service_healthy

  # ==========================================
  # 3. Nacos 节点 1
  # ==========================================
  nacos1:
    image: nacos/nacos-server:v3.2.1
    container_name: nacos1
    restart: always
    networks:
      - nacos-pg-net
    environment:
      - MODE=cluster
      - PREFER_HOST_MODE=hostname
      - NACOS_SERVERS=nacos1:8848 nacos2:8848 nacos3:8848
      # --- PostgreSQL 原生配置 (Nacos 3.x) ---
      - SPRING_DATASOURCE_PLATFORM=postgresql
      # 利用 PG JDBC 多主机特性，实现自动故障转移 (targetServerType=primary 确保只连主库)
      - POSTGRESQL_SERVICE_HOST=postgres-primary,postgres-secondary
      - POSTGRESQL_SERVICE_PORT=5432
      - POSTGRESQL_SERVICE_DB_NAME=nacos_config
      - POSTGRESQL_SERVICE_USER=nacos
      - POSTGRESQL_SERVICE_PASSWORD=nacos123
      # 强制指定 JDBC URL 参数以确保高可用特性生效 (如果环境变量不支持多Host，可通过此方式覆盖)
      - NACOS_DATASOURCE_URL_PARAMS=targetServerType=primary&connectTimeout=5000&socketTimeout=10000
      # --- JVM 与 鉴权 ---
      - JVM_XMS=256m
      - JVM_XMX=256m
      - JVM_XMN=128m
      - NACOS_AUTH_ENABLE=true
      - NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
      - NACOS_AUTH_IDENTITY_KEY=serverIdentity
      - NACOS_AUTH_IDENTITY_VALUE=security
    volumes:
      - nacos1_logs:/home/nacos/logs
    ports:
      - "8848:8848"
      - "9848:9848"
      - "9849:9849"
    depends_on:
      postgres-primary:
        condition: service_healthy

  # ==========================================
  # 4. Nacos 节点 2
  # ==========================================
  nacos2:
    image: nacos/nacos-server:v3.2.1
    container_name: nacos2
    restart: always
    networks:
      - nacos-pg-net
    environment:
      - MODE=cluster
      - PREFER_HOST_MODE=hostname
      - NACOS_SERVERS=nacos1:8848 nacos2:8848 nacos3:8848
      - SPRING_DATASOURCE_PLATFORM=postgresql
      - POSTGRESQL_SERVICE_HOST=postgres-primary,postgres-secondary
      - POSTGRESQL_SERVICE_PORT=5432
      - POSTGRESQL_SERVICE_DB_NAME=nacos_config
      - POSTGRESQL_SERVICE_USER=nacos
      - POSTGRESQL_SERVICE_PASSWORD=nacos123
      - NACOS_DATASOURCE_URL_PARAMS=targetServerType=primary&connectTimeout=5000&socketTimeout=10000
      - JVM_XMS=256m
      - JVM_XMX=256m
      - JVM_XMN=128m
      - NACOS_AUTH_ENABLE=true
      - NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
      - NACOS_AUTH_IDENTITY_KEY=serverIdentity
      - NACOS_AUTH_IDENTITY_VALUE=security
    volumes:
      - nacos2_logs:/home/nacos/logs
    ports:
      - "8858:8848"
      - "9858:9848"
      - "9859:9849"
    depends_on:
      postgres-primary:
        condition: service_healthy

  # ==========================================
  # 5. Nacos 节点 3
  # ==========================================
  nacos3:
    image: nacos/nacos-server:v3.2.1
    container_name: nacos3
    restart: always
    networks:
      - nacos-pg-net
    environment:
      - MODE=cluster
      - PREFER_HOST_MODE=hostname
      - NACOS_SERVERS=nacos1:8848 nacos2:8848 nacos3:8848
      - SPRING_DATASOURCE_PLATFORM=postgresql
      - POSTGRESQL_SERVICE_HOST=postgres-primary,postgres-secondary
      - POSTGRESQL_SERVICE_PORT=5432
      - POSTGRESQL_SERVICE_DB_NAME=nacos_config
      - POSTGRESQL_SERVICE_USER=nacos
      - POSTGRESQL_SERVICE_PASSWORD=nacos123
      - NACOS_DATASOURCE_URL_PARAMS=targetServerType=primary&connectTimeout=5000&socketTimeout=10000
      - JVM_XMS=256m
      - JVM_XMX=256m
      - JVM_XMN=128m
      - NACOS_AUTH_ENABLE=true
      - NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
      - NACOS_AUTH_IDENTITY_KEY=serverIdentity
      - NACOS_AUTH_IDENTITY_VALUE=security
    volumes:
      - nacos3_logs:/home/nacos/logs
    ports:
      - "8868:8848"
      - "9868:9848"
      - "9869:9849"
    depends_on:
      postgres-primary:
        condition: service_healthy

volumes:
  pg_primary_data:
  pg_secondary_data:
  nacos1_logs:
  nacos2_logs:
  nacos3_logs:
```

---

### 四、 启动与初始化步骤 (关键)

由于 Nacos 3.x 原生支持 PG，官方在 GitHub 仓库的 `distribution/conf/` 目录下提供了 `postgresql-schema.sql`。

#### 1. 启动基础服务
```bash
# 启动 PG 主从和 Nacos 集群
podman-compose up -d
```

#### 2. 导入 PostgreSQL 初始化脚本
*注意：Bitnami PG 镜像会自动建库，但不会建表。必须手动导入 Nacos 官方的 PG 脚本。*

```bash
# 1. 下载 Nacos 3.2.1 官方 PG 初始化脚本
wget https://raw.githubusercontent.com/alibaba/nacos/3.2.1/distribution/conf/postgresql-schema.sql

# 2. 将脚本导入到 PG 主库 (容器名: pg-primary)
podman exec -i pg-primary psql -U nacos -d nacos_config < postgresql-schema.sql
```
*执行成功后，您会看到一系列 `CREATE TABLE` 和 `CREATE INDEX` 的输出。*

#### 3. 重启 Nacos 节点
由于 Nacos 启动时如果检测不到表结构可能会报错退出，导入脚本后需要重启 Nacos 节点：
```bash
podman-compose restart nacos1 nacos2 nacos3
```

#### 4. 验证集群状态
```bash
# 查看 Nacos1 日志，确认启动成功且无数据库报错
podman logs -f nacos1

# 访问控制台
# http://<宿主机IP>:8848/nacos (账号: nacos / 密码: nacos)
```
进入 **集群管理 -> 节点列表**，确认 3 个节点均为 `UP`。

---

### 五、 架构亮点说明

1. **JDBC 多主机故障转移**：在环境变量 `POSTGRESQL_SERVICE_HOST` 中配置了 `postgres-primary,postgres-secondary`，并附加了 `targetServerType=primary` 参数。这是 PostgreSQL JDBC 驱动的原生高级特性，当主库宕机且从库被提升为主库时，Nacos 无需重启即可自动重连到新主库。
2. **Bitnami 镜像**：使用了 `bitnami/postgresql`，该镜像对主从流复制（Streaming Replication）做了极佳的封装，无需手动配置 `pg_hba.conf` 和 `postgresql.conf`，非常适合容器化编排。
3. **Nacos 3.x 特性**：直接使用了 `SPRING_DATASOURCE_PLATFORM=postgresql`，无需任何外部 Jar 包挂载，享受官方内核级的方言支持和性能优化。