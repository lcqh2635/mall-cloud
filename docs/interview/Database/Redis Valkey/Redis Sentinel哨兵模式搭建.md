当然可以！下面是一份 **详细、生产级的 `compose.yaml` 配置文档**，用于在 Docker 或 Podman 环境中部署 **Redis Sentinel 哨兵模式（1主2从 + 3哨兵）**，并附带完整的中文注释说明。

这套方案适用于开发、测试或预生产环境，具备高可用性，支持自动故障转移。

---

# 📚 使用 `compose.yaml` 部署 Redis Sentinel（哨兵模式）
## ✅ 1主2从 + 3哨兵 · 支持持久化、密码认证、数据卷挂载

> **适用系统**：Linux（Fedora/CentOS/Ubuntu）、macOS、Windows（Docker Desktop）  
> **容器引擎**：Docker 或 Podman（支持 `podman-compose`）  
> **Redis 版本**：7.2-alpine（轻量稳定）  
> **目标**：实现 Redis 高可用，主节点宕机后自动切换

---

## 一、整体架构

```
                +----------------+
                |   Sentinel1    |  ← 监控主从状态，决策故障转移
                +----------------+
                        ↑
         +--------------+--------------+
         |              |              |
+--------v----+ +-----v------+ +-----v------+
|  Master     | |  Slave1    | |  Slave2    |
|  (写)       | |  (读)      | |  (读)      |
+-------------+ +------------+ +------------+
```

- **Master**：处理写请求
- **Slave1/Slave2**：从主节点同步数据，处理读请求
- **Sentinel1/2/3**：监控 Redis 实例，自动完成故障转移

> ✅ 3个哨兵确保“多数派”决策，避免脑裂

---

## 二、目录结构

```text
redis-sentinel/
├── compose.yaml
├── master/
│   └── redis.conf          # 主节点配置
├── slave1/
│   └── redis.conf          # 从节点1配置
├── slave2/
│   └── redis.conf          # 从节点2配置
├── sentinel1/
│   └── sentinel.conf       # 哨兵1配置
├── sentinel2/
│   └── sentinel.conf       # 哨兵2配置
├── sentinel3/
│   └── sentinel.conf       # 哨兵3配置
└── data/
    ├── master/
    ├── slave1/
    ├── slave2/
    └── sentinel/           # 哨兵状态文件
```

---

## 三、`compose.yaml` 文件（带中文注释）

```yaml
version: '3.8'

services:
  # ==================== 主节点 ====================
  redis-master:
    image: redis:7.2-alpine
    container_name: redis-master
    hostname: redis-master
    ports:
      - "6379:6379"  # 暴露主节点端口（客户端写操作）
    volumes:
      # 挂载主节点配置文件
      - ./master/redis.conf:/usr/local/etc/redis/redis.conf:ro
      # 挂载数据目录，持久化 RDB/AOF
      - ./data/master:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-sentinel-net:
        # 固定 IP 便于哨兵识别
        ipv4_address: 172.21.0.10
    restart: always
    # 以非 root 用户运行（安全）
    user: "1000:1000"

  # ==================== 从节点1 ====================
  redis-slave1:
    image: redis:7.2-alpine
    container_name: redis-slave1
    hostname: redis-slave1
    ports:
      - "6380:6379"  # 映射为 6380，避免端口冲突
    volumes:
      - ./slave1/redis.conf:/usr/local/etc/redis/redis.conf:ro
      - ./data/slave1:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-sentinel-net:
        ipv4_address: 172.21.0.11
    restart: always
    depends_on:
      - redis-master  # 确保主节点先启动
    user: "1000:1000"

  # ==================== 从节点2 ====================
  redis-slave2:
    image: redis:7.2-alpine
    container_name: redis-slave2
    hostname: redis-slave2
    ports:
      - "6381:6379"
    volumes:
      - ./slave2/redis.conf:/usr/local/etc/redis/redis.conf:ro
      - ./data/slave2:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-sentinel-net:
        ipv4_address: 172.21.0.12
    restart: always
    depends_on:
      - redis-master
    user: "1000:1000"

  # ==================== 哨兵节点1 ====================
  redis-sentinel1:
    image: redis:7.2-alpine
    container_name: redis-sentinel1
    ports:
      - "26379:26379"  # 哨兵通信端口
    volumes:
      # 挂载哨兵配置文件
      - ./sentinel1/sentinel.conf:/usr/local/etc/redis/sentinel.conf:ro
      # 挂载哨兵工作目录（保存状态）
      - ./data/sentinel:/sentinel:Z
    command: ["redis-sentinel", "/usr/local/etc/redis/sentinel.conf"]
    networks:
      redis-sentinel-net:
        ipv4_address: 172.21.0.21
    restart: always
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    user: "1000:1000"

  # ==================== 哨兵节点2 ====================
  redis-sentinel2:
    image: redis:7.2-alpine
    container_name: redis-sentinel2
    ports:
      - "26380:26379"
    volumes:
      - ./sentinel2/sentinel.conf:/usr/local/etc/redis/sentinel.conf:ro
      - ./data/sentinel:/sentinel:Z
    command: ["redis-sentinel", "/usr/local/etc/redis/sentinel.conf"]
    networks:
      redis-sentinel-net:
        ipv4_address: 172.21.0.22
    restart: always
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    user: "1000:1000"

  # ==================== 哨兵节点3 ====================
  redis-sentinel3:
    image: redis:7.2-alpine
    container_name: redis-sentinel3
    ports:
      - "26381:26379"
    volumes:
      - ./sentinel3/sentinel.conf:/usr/local/etc/redis/sentinel.conf:ro
      - ./data/sentinel:/sentinel:Z
    command: ["redis-sentinel", "/usr/local/etc/redis/sentinel.conf"]
    networks:
      redis-sentinel-net:
        ipv4_address: 172.21.0.23
    restart: always
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    user: "1000:1000"

# ==================== 自定义网络 ====================
# 使用固定子网，确保 IP 稳定，便于哨兵通信
networks:
  redis-sentinel-net:
    driver: bridge
    ipam:
      config:
        - subnet: 172.21.0.0/16

# ==================== 数据卷（可选） ====================
# 使用命名卷可实现更灵活的持久化管理
volumes:
  master-data:
  slave1-data:
  slave2-data:
  sentinel-data:
```

---

## 四、配置文件详解

### 1. `master/redis.conf`

```conf
# 主节点配置
port 6379
bind 0.0.0.0
protected-mode no
daemonize no
pidfile /var/run/redis.pid
loglevel notice
logfile ""
# 数据目录（与 docker volume 一致）
dir /data
# 持久化：每 15 分钟至少 1 次修改则生成 RDB
save 900 1
save 300 10
save 60 10000
rdbcompression yes
dbfilename dump.rdb
# 开启 AOF 持久化（更安全）
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
# 设置访问密码
requirepass MyStrongPassword123!
# 主节点无需 replicaof
```

---

### 2. `slave1/redis.conf`（`slave2/redis.conf` 类似）

```conf
# 从节点配置
port 6379
bind 0.0.0.0
protected-mode no
daemonize no
loglevel notice
logfile ""
dir /data
save 900 1
save 300 10
save 60 10000
rdbcompression yes
dbfilename dump.rdb
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
# 设置密码
requirepass MyStrongPassword123!
# 指定主节点（使用 compose 中的 hostname）
replicaof redis-master 6379
# 主节点密码
masterauth MyStrongPassword123!
```

---

### 3. `sentinel1/sentinel.conf`（其他哨兵配置相同）

```conf
# 哨兵配置
port 26379
bind 0.0.0.0
protected-mode no
daemonize no
# 哨兵工作目录
dir /sentinel
# 监控名为 mymaster 的主节点
# 最后参数 2 表示至少 2 个哨兵同意才能进行故障转移
sentinel monitor mymaster redis-master 6379 2
# 主节点密码
sentinel auth-pass mymaster MyStrongPassword123!
# 5 秒内未响应视为主观下线（SDOWN）
sentinel down-after-milliseconds mymaster 5000
# 故障转移超时时间（毫秒）
sentinel failover-timeout mymaster 10000
# 故障转移期间最多 1 个从节点同步数据
sentinel parallel-syncs mymaster 1
# 哨兵之间通信超时
sentinel deny-scripts-reconfig yes
```

> ⚠️ 所有哨兵配置文件内容完全相同。

---

## 五、启动与验证

### 1. 启动服务

```bash
cd redis-sentinel
docker-compose up -d
# 或使用 Podman
podman-compose up -d
```

---

### 2. 查看日志

```bash
docker-compose logs redis-sentinel1
```

应看到：
```
+monitor master mymaster 172.21.0.10:6379 quorum 2
+sdown slave redis-slave1:6379 ... 
```

---

### 3. 验证哨兵状态

```bash
# 进入任一哨兵容器
docker exec -it redis-sentinel1 sh

# 查看哨兵监控的主节点
redis-cli -p 26379 SENTINEL get-master-addr-by-name mymaster

# 查看所有节点状态
redis-cli -p 26379 SENTINEL slaves mymaster
redis-cli -p 26379 INFO sentinel
```

---

### 4. 模拟故障转移测试

```bash
# 停止主节点
docker stop redis-master

# 等待 10 秒
# 再次查询主节点地址
redis-cli -p 26379 SENTINEL get-master-addr-by-name mymaster
# 应返回某个 slave 的 IP 和端口
```

重启 `redis-master` 后，它将自动变为从节点。

---

## 六、Java 客户端连接方式（Spring Boot 示例）

```yaml
# application.yml
spring:
  redis:
    password: MyStrongPassword123!
    timeout: 5s
    lettuce:
      pool:
        max-active: 8
    sentinel:
      master: mymaster
      nodes:
        - 127.0.0.1:26379
        - 127.0.0.1:26380
        - 127.0.0.1:26381
```

客户端会自动从哨兵获取当前主节点地址。

---

## 七、生产环境建议

| 项目 | 建议 |
|------|------|
| **持久化** | 同时开启 RDB + AOF |
| **密码强度** | 使用强密码，避免泄露 |
| **网络隔离** | 哨兵和 Redis 放在内网，限制外部访问 |
| **监控** | 使用 Prometheus + Grafana 监控哨兵状态 |
| **升级** | 先升级从节点，再升级主节点 |
| **备份** | 定期备份 `/data` 目录下的 RDB/AOF 文件 |

---

## 八、总结

✅ 本方案实现了：

- 高可用：主节点宕机自动切换
- 数据安全：AOF + RDB 持久化
- 安全访问：密码认证
- 易于维护：配置分离、数据卷挂载
- 可扩展：支持横向扩展从节点

> 🚀 推荐用于 **开发、测试、中小生产环境**。  
> 🔒 生产环境建议结合 **Kubernetes + StatefulSet** 实现更高级的运维能力。

---

如果你需要，我可以为你提供：

- 自动化部署脚本（Shell）
- Prometheus 监控 Redis/Sentinel 的配置
- Spring Boot 集成 Sentinel 的完整项目
- K8s 版本的 Helm Chart

欢迎继续提问！