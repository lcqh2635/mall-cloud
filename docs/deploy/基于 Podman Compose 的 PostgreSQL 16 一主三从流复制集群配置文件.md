这是一个基于 Podman Compose 的 PostgreSQL 16 **一主三从**流复制集群配置文件。

> ⚠️ **重要生产提示**
> 此配置使用 Bitnami PostgreSQL 镜像的原生流复制功能，适用于**开发、测试及预发布环境**。如前文分析，生产环境的高可用故障转移请务必使用 **Patroni + etcd** 架构。本文件中的“一主三从”仅模拟多节点数据同步拓扑，**不具备自动故障转移能力**。当主库宕机时，需手动提升某个从库为新主。

### pg-cluster.yaml

```yaml
# ==========================================
# PostgreSQL 一主三从流复制集群 (Podman Compose)
# 适用场景: 开发/测试环境的数据同步拓扑模拟
# 注意: 此配置无自动故障转移，生产请使用 Patroni
# ==========================================
name: pg-ha-cluster

networks:
  pg-net:
    driver: bridge
    ipam:
      config:
        # 自定义子网，避免与宿主机或其他容器网络冲突
        - subnet: 172.31.0.0/16

services:
  # ------------------------------------------
  # 1. PostgreSQL 主库 (Primary)
  # 职责: 处理所有读写请求，向从库发送 WAL 日志
  # ------------------------------------------
  pg-primary:
    image: bitnami/postgresql:16
    container_name: pg-primary
    restart: always
    networks:
      - pg-net
    environment:
      # --- 基础账号配置 ---
      - POSTGRESQL_USERNAME=nacos
      - POSTGRESQL_PASSWORD=nacos123
      - POSTGRESQL_DATABASE=nacos_config
      # --- 主库复制配置 ---
      - POSTGRESQL_REPLICATION_MODE=master
      - POSTGRESQL_REPLICATION_USER=repl_user
      - POSTGRESQL_REPLICATION_PASSWORD=repl_pass_2024
      # --- 性能调优 (根据实际内存调整) ---
      # 最大连接数，需考虑 Nacos 集群 + 业务服务的并发连接
      - POSTGRESQL_MAX_CONNECTIONS=300
      # 共享缓冲区，建议为容器分配内存的 25%
      - POSTGRESQL_SHARED_BUFFERS=256MB
      # WAL 保留量，防止从库同步延迟导致主库清理掉尚未同步的 WAL
      - POSTGRESQL_WAL_KEEP_SIZE=1GB
    volumes:
      - pg_primary_data:/bitnami/postgresql
    ports:
      # 仅主库对外暴露端口，从库不对外暴露
      - "5432:5432"
    healthcheck:
      # 健康检查：确认数据库可接受连接且角色为主库
      test: ["CMD-SHELL", "pg_isready -U nacos -d nacos_config && [ \"$(psql -U nacos -d nacos_config -tAc 'SELECT pg_is_in_recovery()')\" = 'f' ]"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  # ------------------------------------------
  # 2. PostgreSQL 从库 1 (Standby-1)
  # 职责: 异步流复制，可作为只读查询节点或故障转移候选
  # ------------------------------------------
  pg-standby-1:
    image: bitnami/postgresql:16
    container_name: pg-standby-1
    restart: always
    networks:
      - pg-net
    environment:
      # --- 从库复制配置 ---
      - POSTGRESQL_REPLICATION_MODE=slave
      - POSTGRESQL_MASTER_HOST=pg-primary
      - POSTGRESQL_MASTER_PORT_NUMBER=5432
      - POSTGRESQL_REPLICATION_USER=repl_user
      - POSTGRESQL_REPLICATION_PASSWORD=repl_pass_2024
      # --- 从库只读查询优化 ---
      # 开启热备反馈，防止从库长查询被主库 WAL 清理中断
      - POSTGRESQL_HOT_STANDBY_FEEDBACK=on
      # 从库最大连接数 (只读查询通常不需要太多)
      - POSTGRESQL_MAX_CONNECTIONS=150
    volumes:
      - pg_standby1_data:/bitnami/postgresql
    depends_on:
      pg-primary:
        condition: service_healthy
    # 从库不暴露端口到宿主机，内部通过 pg-net 网络访问

  # ------------------------------------------
  # 3. PostgreSQL 从库 2 (Standby-2)
  # 职责: 异步流复制，提供额外的读取扩展能力和冗余
  # ------------------------------------------
  pg-standby-2:
    image: bitnami/postgresql:16
    container_name: pg-standby-2
    restart: always
    networks:
      - pg-net
    environment:
      - POSTGRESQL_REPLICATION_MODE=slave
      - POSTGRESQL_MASTER_HOST=pg-primary
      - POSTGRESQL_MASTER_PORT_NUMBER=5432
      - POSTGRESQL_REPLICATION_USER=repl_user
      - POSTGRESQL_REPLICATION_PASSWORD=repl_pass_2024
      - POSTGRESQL_HOT_STANDBY_FEEDBACK=on
      - POSTGRESQL_MAX_CONNECTIONS=150
    volumes:
      - pg_standby2_data:/bitnami/postgresql
    depends_on:
      pg-primary:
        condition: service_healthy

  # ------------------------------------------
  # 4. PostgreSQL 从库 3 (Standby-3)
  # 职责: 异步流复制，满足一主三从拓扑要求
  # ------------------------------------------
  pg-standby-3:
    image: bitnami/postgresql:16
    container_name: pg-standby-3
    restart: always
    networks:
      - pg-net
    environment:
      - POSTGRESQL_REPLICATION_MODE=slave
      - POSTGRESQL_MASTER_HOST=pg-primary
      - POSTGRESQL_MASTER_PORT_NUMBER=5432
      - POSTGRESQL_REPLICATION_USER=repl_user
      - POSTGRESQL_REPLICATION_PASSWORD=repl_pass_2024
      - POSTGRESQL_HOT_STANDBY_FEEDBACK=on
      - POSTGRESQL_MAX_CONNECTIONS=150
    volumes:
      - pg_standby3_data:/bitnami/postgresql
    depends_on:
      pg-primary:
        condition: service_healthy

# ==========================================
# 数据卷持久化 (每个节点独立存储)
# ==========================================
volumes:
  pg_primary_data:
    name: pg-primary-data
  pg_standby1_data:
    name: pg-standby1-data
  pg_standby2_data:
    name: pg-standby2-data
  pg_standby3_data:
    name: pg-standby3-data
```

### 启动与验证指南

#### 1. 启动集群
```bash
podman-compose -f pg-cluster.yaml up -d
```

#### 2. 验证流复制状态
在主库上执行以下 SQL，确认三个从库均已正常连接并处于 `streaming` 状态：
```bash
podman exec -it pg-primary psql -U nacos -d nacos_config -c \
"SELECT client_addr, state, sent_lsn, write_lsn, replay_lsn, sync_state 
 FROM pg_stat_replication;"
```

期望输出应包含 3 行记录，`state` 列均为 `streaming`，`sync_state` 为 `async`（异步复制）。

#### 3. 验证从库只读状态
在任意从库上确认其处于恢复模式（只读）：
```bash
podman exec -it pg-standby-1 psql -U nacos -d nacos_config -c \
"SELECT pg_is_in_recovery();"
```

返回 `t` 表示该节点为从库，仅接受只读查询。

### 关键设计说明

| 配置项 | 说明 |
| :--- | :--- |
| `WAL_KEEP_SIZE=1GB` | 主库保留至少 1GB 的 WAL 文件。若从库因网络抖动短暂断开，重连后可直接从主库拉取缺失的 WAL，避免全量重建 |
| `HOT_STANDBY_FEEDBACK=on` | 从库向主库反馈当前正在执行的长查询所需的 oldestXmin，防止主库 VACUUM 清理掉从库仍需的数据行，避免"取消查询"错误 |
| 健康检查含角色校验 | 不仅检查 `pg_isready`，还通过 `pg_is_in_recovery()` 确认节点确实是主库。防止在主库异常重启期间，Compose 误判健康状态导致从库过早启动 |
| 从库不暴露宿主机端口 | 遵循最小权限原则。从库仅供集群内部通信和应用只读查询使用，如需外部访问从库，可通过 HAProxy 或应用层 JDBC 多 Host 配置实现 |
| 独立命名数据卷 | 每个节点使用具名卷而非匿名卷，便于后续备份、迁移和排查问题时精确定位数据目录 |