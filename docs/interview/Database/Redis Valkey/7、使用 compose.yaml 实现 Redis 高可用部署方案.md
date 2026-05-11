当然可以！以下是为 **Fedora 工作站** 环境设计的 **Redis 高可用部署方案**，使用 `compose.yaml`（Docker Compose 或 Podman Compose）方式，分别实现：

- ✅ **Redis Sentinel 模式（1主2从 + 3哨兵）**
- ✅ **Redis Cluster 模式（3主3从）**

> ⚠️ 说明：
> - 支持 **Docker** 和 **Podman**（Fedora 推荐）
> - 使用 `podman-compose` 或 `docker-compose` 均可运行
> - 所有配置文件包含 **中文注释**，便于理解与维护

---

# 📚 Redis 高可用部署：Sentinel 与 Cluster 模式（Compose 部署）

> **适用系统**：Fedora 39/40, RHEL, CentOS Stream  
> **容器引擎**：Docker 或 Podman  
> **Compose 工具**：`docker-compose` 或 `podman-compose`

---

## 一、准备工作

### 1. 安装 Podman 与 Podman-Compose（如未安装）

```bash
# 安装 Podman（通常已预装）
sudo dnf install podman -y

# 安装 podman-compose
sudo dnf install python3-pip -y
pip3 install --user podman-compose
```

> 添加 `~/.local/bin` 到 PATH：
> ```bash
> echo 'export PATH=$PATH:~/.local/bin' >> ~/.bashrc
> source ~/.bashrc
> ```

### 2. 创建项目目录

```bash
mkdir -p ~/redis-deploy/{sentinel,cluster}
cd ~/redis-deploy
```

---

## 二、方式一：Redis Sentinel 模式（1主2从 + 3哨兵）

### 📁 目录结构

```text
sentinel/
├── compose.yaml
├── master/
│   └── redis.conf
├── slave1/
│   └── redis.conf
├── slave2/
│   └── redis.conf
└── sentinel/
    ├── sentinel1.conf
    ├── sentinel2.conf
    └── sentinel3.conf
```

---

### ✅ 1. `sentinel/compose.yaml`

```yaml
version: '3.8'

services:
  # 主节点
  redis-master:
    image: redis:7.2-alpine
    container_name: redis-master
    ports:
      - "6379:6379"
    volumes:
      # 挂载主节点配置文件
      - ./master/redis.conf:/usr/local/etc/redis/redis.conf:Z
      # 持久化数据目录
      - ./data/master:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      - redis-net
    restart: always
    # 以特定用户运行（安全）
    user: "1000:1000"

  # 从节点 1
  redis-slave1:
    image: redis:7.2-alpine
    container_name: redis-slave1
    ports:
      - "6380:6379"
    volumes:
      - ./slave1/redis.conf:/usr/local/etc/redis/redis.conf:Z
      - ./data/slave1:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      - redis-net
    restart: always
    depends_on:
      - redis-master
    user: "1000:1000"

  # 从节点 2
  redis-slave2:
    image: redis:7.2-alpine
    container_name: redis-slave2
    ports:
      - "6381:6379"
    volumes:
      - ./slave2/redis.conf:/usr/local/etc/redis/redis.conf:Z
      - ./data/slave2:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      - redis-net
    restart: always
    depends_on:
      - redis-master
    user: "1000:1000"

  # 哨兵节点 1
  redis-sentinel1:
    image: redis:7.2-alpine
    container_name: redis-sentinel1
    ports:
      - "26379:26379"
    volumes:
      - ./sentinel/sentinel1.conf:/usr/local/etc/redis/sentinel.conf:Z
    command: ["redis-sentinel", "/usr/local/etc/redis/sentinel.conf"]
    networks:
      - redis-net
    restart: always
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    user: "1000:1000"

  # 哨兵节点 2
  redis-sentinel2:
    image: redis:7.2-alpine
    container_name: redis-sentinel2
    ports:
      - "26380:26379"
    volumes:
      - ./sentinel/sentinel2.conf:/usr/local/etc/redis/sentinel.conf:Z
    command: ["redis-sentinel", "/usr/local/etc/redis/sentinel.conf"]
    networks:
      - redis-net
    restart: always
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    user: "1000:1000"

  # 哨兵节点 3
  redis-sentinel3:
    image: redis:7.2-alpine
    container_name: redis-sentinel3
    ports:
      - "26381:26379"
    volumes:
      - ./sentinel/sentinel3.conf:/usr/local/etc/redis/sentinel.conf:Z
    command: ["redis-sentinel", "/usr/local/etc/redis/sentinel.conf"]
    networks:
      - redis-net
    restart: always
    depends_on:
      - redis-master
      - redis-slave1
      - redis-slave2
    user: "1000:1000"

# 自定义网络，确保容器间通信
networks:
  redis-net:
    driver: bridge

# 数据卷（可选，这里用 bind mount）
volumes:
  master-data:
  slave1-data:
  slave2-data:
```

---

### ✅ 2. 配置文件（示例）

#### `sentinel/master/redis.conf`

```conf
# 主节点配置
port 6379
bind 0.0.0.0
protected-mode no
daemonize no
pidfile /var/run/redis.pid
loglevel notice
logfile ""
dir /data
save 900 1
save 300 10
save 60 10000
rdbcompression yes
dbfilename dump.rdb
# 开启 AOF 持久化
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
# 设置密码
requirepass MySecurePass123!
# 主节点无需 replicaof
```

#### `sentinel/slave1/redis.conf`（slave2 类似）

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
requirepass MySecurePass123!
# 指定主节点（服务名在 compose 中为 redis-master）
replicaof redis-master 6379
# 主节点密码
masterauth MySecurePass123!
```

#### `sentinel/sentinel/sentinel1.conf`（其他两个类似）

```conf
# 哨兵配置
port 26379
bind 0.0.0.0
protected-mode no
daemonize no
dir /tmp
# 监控名为 mymaster 的主节点，2 个哨兵同意即可故障转移
sentinel monitor mymaster redis-master 6379 2
# 主节点密码
sentinel auth-pass mymaster MySecurePass123!
# 5 秒内未响应视为下线
sentinel down-after-milliseconds mymaster 5000
# 故障转移超时时间
sentinel failover-timeout mymaster 10000
# 最多 1 个 slave 同时同步
sentinel parallel-syncs mymaster 1
```

---

### ✅ 3. 启动 Sentinel 集群

```bash
cd sentinel
podman-compose up -d
# 或 docker-compose up -d
```

### ✅ 4. 验证

```bash
# 查看哨兵状态
podman exec -it redis-sentinel1 redis-cli -p 26379 INFO sentinel

# 查看主节点
podman exec -it redis-sentinel1 redis-cli -p 26379 SENTINEL get-master-addr-by-name mymaster
```

---

## 三、方式二：Redis Cluster 模式（3主3从）

### 📁 目录结构

```text
cluster/
├── compose.yaml
├── nodes/
│   ├── redis7000.conf
│   ├── redis7001.conf
│   ├── redis7002.conf
│   ├── redis7003.conf
│   ├── redis7004.conf
│   └── redis7005.conf
└── data/
    ├── 7000/
    ├── 7001/
    └── ...（共6个）
```

---

### ✅ 1. `cluster/compose.yaml`

```yaml
version: '3.8'

services:
  redis-node1:
    image: redis:7.2-alpine
    container_name: redis-node1
    hostname: redis-node1
    ports:
      - "7000:7000"
      - "17000:17000"
    volumes:
      - ./nodes/redis7000.conf:/usr/local/etc/redis/redis.conf:Z
      - ./data/7000:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-cluster:
        ipv4_address: 172.20.0.10
    restart: always
    user: "1000:1000"

  redis-node2:
    image: redis:7.2-alpine
    container_name: redis-node2
    hostname: redis-node2
    ports:
      - "7001:7001"
      - "17001:17001"
    volumes:
      - ./nodes/redis7001.conf:/usr/local/etc/redis/redis.conf:Z
      - ./data/7001:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-cluster:
        ipv4_address: 172.20.0.11
    restart: always
    user: "1000:1000"

  redis-node3:
    image: redis:7.2-alpine
    container_name: redis-node3
    hostname: redis-node3
    ports:
      - "7002:7002"
      - "17002:17002"
    volumes:
      - ./nodes/redis7002.conf:/usr/local/etc/redis/redis.conf:Z
      - ./data/7002:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-cluster:
        ipv4_address: 172.20.0.12
    restart: always
    user: "1000:1000"

  redis-node4:
    image: redis:7.2-alpine
    container_name: redis-node4
    hostname: redis-node4
    ports:
      - "7003:7003"
      - "17003:17003"
    volumes:
      - ./nodes/redis7003.conf:/usr/local/etc/redis/redis.conf:Z
      - ./data/7003:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-cluster:
        ipv4_address: 172.20.0.13
    restart: always
    user: "1000:1000"

  redis-node5:
    image: redis:7.2-alpine
    container_name: redis-node5
    hostname: redis-node5
    ports:
      - "7004:7004"
      - "17004:17004"
    volumes:
      - ./nodes/redis7004.conf:/usr/local/etc/redis/redis.conf:Z
      - ./data/7004:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-cluster:
        ipv4_address: 172.20.0.14
    restart: always
    user: "1000:1000"

  redis-node6:
    image: redis:7.2-alpine
    container_name: redis-node6
    hostname: redis-node6
    ports:
      - "7005:7005"
      - "17005:17005"
    volumes:
      - ./nodes/redis7005.conf:/usr/local/etc/redis/redis.conf:Z
      - ./data/7005:/data:Z
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
    networks:
      redis-cluster:
        ipv4_address: 172.20.0.15
    restart: always
    user: "1000:1000"

# 自定义网络，固定 IP 便于集群通信
networks:
  redis-cluster:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16

volumes:
  data7000:
  data7001:
  # ... 其他可选
```

---

### ✅ 2. 示例配置文件 `cluster/nodes/redis7000.conf`

```conf
# Redis Cluster 节点配置
port 7000
bind 0.0.0.0
protected-mode no
daemonize no
pidfile /var/run/redis_7000.pid
loglevel notice
logfile ""
# 数据目录
dir /data
# 开启集群模式
cluster-enabled yes
# 集群配置文件（自动生成）
cluster-config-file nodes.conf
# 节点超时时间
cluster-node-timeout 15000
# 开启持久化
appendonly yes
appendfsync everysec
# 密码
requirepass MySecurePass123!
masterauth MySecurePass123!
# 开启 AOF
aof-use-rdb-preamble yes
```

> 其他节点（7001~7005）配置相同，仅修改 `port`。

---

### ✅ 3. 初始化 Redis Cluster

启动容器后，执行以下命令创建集群：

```bash
cd cluster

# 进入任一节点执行集群创建
podman exec -it redis-node1 redis-cli \
  --cluster create \
  172.20.0.10:7000 172.20.0.11:7001 172.20.0.12:7002 \
  172.20.0.13:7003 172.20.0.14:7004 172.20.0.15:7005 \
  --cluster-replicas 1 \
  -a MySecurePass123!
```

> `-a` 提供密码，`--cluster-replicas 1` 表示每个主节点配 1 个从节点

---

### ✅ 4. 验证集群状态

```bash
podman exec -it redis-node1 redis-cli -c -p 7000 -a MySecurePass123! CLUSTER INFO
podman exec -it redis-node1 redis-cli -c -p 7000 -a MySecurePass123! CLUSTER NODES
```

---

## 四、通用说明与建议

| 项目 | 说明 |
|------|------|
| 🔐 密码 | 所有节点使用相同密码，`requirepass` 和 `masterauth` 都要设置 |
| 📦 持久化 | 建议开启 `appendonly yes` + `save` |
| 🧯 SELinux | 使用 `:Z` 标签确保 Podman 正确挂载卷 |
| 🔄 升级 | 修改镜像版本后 `podman-compose up -d` 自动重建 |
| 📊 监控 | 可接入 Prometheus + Grafana |
| 💾 备份 | 定期备份 `/data` 目录下的 RDB/AOF 文件 |

---

## 五、总结

| 方案 | 适用场景 | 优势 | 注意事项 |
|------|----------|------|----------|
| **Sentinel** | 中小项目、简单高可用 | 架构简单，支持 multi-key 操作 | 无法水平扩展 |
| **Cluster** | 大型项目、海量数据 | 支持分片、水平扩展 | multi-key 操作受限 |

---

## 六、下一步建议

你可以继续：

- 将 `compose.yaml` 部署到生产环境（建议使用 Kubernetes）
- 配置 Prometheus 监控 Redis 指标
- 使用 Spring Boot 连接 Cluster 或 Sentinel
- 实现 Redis 数据备份与恢复脚本

需要我为你提供：

- Spring Boot 集成 Cluster 的完整示例？
- Redis 备份脚本（每日 RDB 备份）？
- Kubernetes 部署 YAML？

欢迎继续提问！🚀