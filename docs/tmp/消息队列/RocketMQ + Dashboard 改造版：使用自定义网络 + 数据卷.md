非常好！你提出的需求非常专业 —— 在生产级或团队协作开发环境中，**共享网络（自定义网络）** 和 **数据卷（Volume）** 是容器编排的最佳实践，它能带来：

✅ **服务发现更稳定**（通过容器名通信，不依赖 link）  
✅ **数据持久化更规范**（Volume 管理比 bind mount 更可控）  
✅ **网络隔离更安全**（自定义网络避免与宿主机或其他容器冲突）  
✅ **便于扩展集群**（如未来加 Slave Broker、多 NameServer）  
✅ **符合 Docker 生产部署规范**

---

# 🚀 RocketMQ + Dashboard 改造版：使用自定义网络 + 数据卷（Docker & Docker Compose）

> ✅ 适用于开发、测试、准生产环境  
> ✅ 支持一键部署、数据持久化、服务稳定互联  
> ✅ 基于 RocketMQ 5.1.4 + Dashboard 最新版

---

## 🧩 一、改造目标

| 组件          | 改造内容 |
|---------------|----------|
| **网络**      | 创建自定义 bridge 网络 `rocketmq-net`，所有容器加入该网络 |
| **数据卷**    | 为 NameServer、Broker 创建命名 Volume，替代 bind mount |
| **配置管理**  | 保留 `broker.conf` bind mount（便于修改），其他数据用 Volume |
| **服务发现**  | 容器间通过服务名通信（如 `namesrv:9876`），不再使用 `--link` |

---

## 🐳 二、方式一：纯 Docker 方式（改造版）

### 1. 创建自定义网络

```bash
# 创建 bridge 网络
docker network create rocketmq-net
# 查看
docker network inspect rocketmq-net
```

### 2. 创建命名数据卷

```bash
docker volume create rocketmq-namesrv-logs
docker volume create rocketmq-broker-logs
docker volume create rocketmq-broker-store
```

> ✅ 数据卷路径默认在 `/var/lib/docker/volumes/`，可通过 `docker volume inspect` 查看

### 3. 创建配置目录和 `broker.conf`

```bash
mkdir -p ~/rocketmq/config/broker
```

📄 `~/rocketmq/config/broker/broker.conf`：

```properties
brokerName = broker-a
namesrvAddr = namesrv:9876
brokerIP1 = 0.0.0.0
listenPort = 10911
storePathRootDir = /home/rocketmq/store
storePathCommitLog = /home/rocketmq/store/commitlog
storePathConsumeQueue = /home/rocketmq/store/consumequeue
storePathIndex = /home/rocketmq/store/index
deleteWhen = 04
fileReservedTime = 72
diskMaxUsedSpaceRatio = 85
autoCreateTopicEnable = false
autoCreateSubscriptionGroup = true
flushDiskType = ASYNC_FLUSH
brokerRole = ASYNC_MASTER
```

> ⚠️ 注意：`namesrvAddr = namesrv:9876` —— 使用容器服务名

---

### 4. 启动 NameServer（加入网络，使用 Volume）

```bash
docker run -d \
  --name rocketmq-namesrv \
  --network rocketmq-net \
  --hostname namesrv \
  -p 9876:9876 \
  -v rocketmq-namesrv-logs:/home/rocketmq/logs \
  -e "JAVA_OPT_EXT=-Xms256m -Xmx256m" \
  --restart=always \
  apache/rocketmq:latest \
  sh mqnamesrv
```

---

### 5. 启动 Broker（加入网络，使用 Volume + 配置挂载）

```bash
docker run -d \
  --name rocketmq-broker \
  --network rocketmq-net \
  --hostname broker \
  -p 10911:10911 \
  -p 10909:10909 \
  -v rocketmq-broker-logs:/home/rocketmq/logs \
  -v rocketmq-broker-store:/home/rocketmq/store \
  -v ~/rocketmq/config/broker/broker.conf:/home/rocketmq/rocketmq-5.1.4/conf/broker.conf \
  -e "JAVA_OPT_EXT=-Xms512m -Xmx512m" \
  --restart=always \
  apache/rocketmq:5.1.4 \
  sh mqbroker -c /home/rocketmq/rocketmq-5.1.4/conf/broker.conf
```

---

### 6. 启动 RocketMQ Dashboard（加入同一网络）

```bash
docker run -d \
  --name rocketmq-dashboard \
  --network rocketmq-net \
  -p 8080:8080 \
  -e "JAVA_OPTS=-Drocketmq.namesrv.addr=namesrv:9876 -Dcom.rocketmq.sendMessageWithVIPChannel=false" \
  --restart=always \
  apacherocketmq/rocketmq-dashboard:latest
```

> ✅ 不再需要 `--link`，因为都在 `rmq-net` 网络内，可通过 `namesrv` 主机名解析

---

### 7. 验证网络连通性

```bash
# 进入 Dashboard 容器，测试是否能 ping 通 namesrv
docker exec -it rmq-dashboard ping namesrv

# 查看网络详情
docker network inspect rocketmq-net
```

---

## 🐳 三、方式二：Docker Compose 方式（改造版 —— 推荐！）

> ✅ 所有配置集中管理，一键部署，生产就绪

### 1. 创建项目目录

```bash
mkdir -p ~/rocketmq-compose-v2
cd ~/rocketmq-compose-v2
mkdir -p config/broker
```

### 2. 创建 `broker.conf`

📄 `~/rocketmq-compose-v2/config/broker/broker.conf`（内容同上）：

```properties
brokerName = broker-a
namesrvAddr = namesrv:9876
brokerIP1 = 0.0.0.0
listenPort = 10911
storePathRootDir = /home/rocketmq/store
storePathCommitLog = /home/rocketmq/store/commitlog
storePathConsumeQueue = /home/rocketmq/store/consumequeue
storePathIndex = /home/rocketmq/store/index
deleteWhen = 04
fileReservedTime = 72
diskMaxUsedSpaceRatio = 85
autoCreateTopicEnable = false
autoCreateSubscriptionGroup = true
flushDiskType = ASYNC_FLUSH
brokerRole = ASYNC_MASTER
```

---

### 3. 创建 `docker-compose.yml`

📄 `~/rocketmq-compose-v2/docker-compose.yml`

```yaml
version: '3.8'

services:
  # ========== NameServer ==========
  namesrv:
    image: apache/rocketmq:latest
    container_name: rocketmq-namesrv
    hostname: namesrv
    ports:
      - 9876:9876
    volumes:
      - rocketmq-namesrv-logs:/home/rocketmq/logs
    environment:
      - JAVA_OPT_EXT=-Xms256m -Xmx256m -Xmn128m
    restart: always
    networks:
      - rocketmq-net
    command: sh mqnamesrv
    
  # ========== Broker ==========
  broker:
    image: apache/rocketmq:latest
    container_name: rocketmq-broker
    ports:
      - 10909:10909
      - 10911:10911
      - 10912:10912
    volumes:
      - rocketmq-broker-logs:/home/rocketmq/logs
      - rocketmq-broker-store:/home/rocketmq/store
      - ./config/broker/broker.conf:/home/rocketmq/rocketmq-5.1.4/conf/broker.conf
    environment:
      - NAMESRV_ADDR=rmqnamesrv:9876
    restart: always
    depends_on:
      - namesrv
    networks:
      - rocketmq-net
    command: sh mqbroker -c /home/rocketmq/rocketmq-5.1.4/conf/broker.conf

  # ========== Proxy ==========
  proxy:
    image: apache/rocketmq:latest
    container_name: rocketmq-proxy
    networks:
      - rocketmq-net
    depends_on:
      - namesrv
      - broker
    ports:
      - 8080:8080
      - 8081:8081
    restart: on-failure
    environment:
      - NAMESRV_ADDR=rmqnamesrv:9876
    command: sh mqproxy
  
  # ========== Dashboard ==========
  dashboard:
    image: apacherocketmq/rocketmq-dashboard:latest
    container_name: rocketmq-dashboard
    ports:
      - 8080:8080
    environment:
      - JAVA_OPTS=-Drocketmq.namesrv.addr=namesrv:9876 -Dcom.rocketmq.sendMessageWithVIPChannel=false
    restart: always
    depends_on:
      - namesrv
    networks:
      - rocketmq-net

# ========== 自定义网络 ==========
networks:
  rocketmq:
    name: rocketmq-net
    driver: bridge

# ========== 命名数据卷 ==========
volumes:
  rocketmq-namesrv-logs:
    name: rocketmq-namesrv-logs
  rocketmq-broker-logs:
    name: rocketmq-broker-logs
  rocketmq-broker-store:
    name: rocketmq-broker-store
```

---

### 4. 启动服务

```bash
cd ~/rocketmq-compose-v2
docker-compose up -d
```

---

### 5. 查看服务状态和网络

```bash
docker-compose ps
docker network inspect rocketmq-net
docker volume ls | grep rocketmq
```

---

### 6. 访问 Dashboard

👉 http://localhost:8080

---

## 🧪 四、验证部署是否成功

### 1. 进入 Broker 容器发送测试消息

```bash
docker exec -it rmq-broker bash

cd /home/rocketmq/rocketmq-5.1.4

# 发送消息
bin/tools.sh org.apache.rocketmq.example.quickstart.Producer

# 消费消息（新开终端）
bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer
```

> ✅ 如果看到 “SendResult” 和 “Receive New Messages”，说明网络、数据卷、配置全部正常！

---

### 2. 查看 Volume 数据（可选）

```bash
# 查看 Volume 挂载点
docker volume inspect rocketmq-broker-store

# 进入 Volume 数据目录（Linux 示例）
sudo ls /var/lib/docker/volumes/rmq-broker-store/_data
```

---

## 🛠 五、常用运维命令

### 查看日志

```bash
docker logs -f rocketmq-namesrv
docker logs -f rocketmq-broker
docker logs -f rocketmq-dashboard
```

### 进入容器

```bash
docker exec -it rocketmq-broker bash
```

### 扩展：增加 Slave Broker（示例）

在 `docker-compose.yml` 中添加：

```yaml
  broker-slave:
    image: apache/rocketmq:5.1.4
    container_name: rocketmq-broker-slave
    hostname: broker-slave
    ports:
      - "10921:10911"
      - "10919:10909"
    volumes:
      - rocketmq-broker-slave-logs:/home/rocketmq/logs
      - rocketmq-broker-slave-store:/home/rocketmq/store
      - ./config/broker-slave.conf:/home/rocketmq/rocketmq-5.1.4/conf/broker.conf
    environment:
      - JAVA_OPT_EXT=-Xms512m -Xmx512m
    command: sh mqbroker -c /home/rocketmq/rocketmq-5.1.4/conf/broker.conf
    restart: always
    depends_on:
      - namesrv
    networks:
      - rocketmq-net
```

并创建对应的 Volume 和配置文件（设置 `brokerRole=SLAVE`）。

---

## 🧹 六、停止与清理（安全版）

### 停止并删除容器、网络、Volume（谨慎操作！）

```bash
cd ~/rocketmq-compose-v2
docker-compose down

# 删除网络（如果未在 compose 中定义，需手动删）
docker network rm rocketmq-net

# 删除 Volume（⚠️ 数据将丢失！）
docker volume rm rocketmq-namesrv-logs rocketmq-broker-logs rocketmq-broker-store
```

---

## ✅ 七、为什么推荐此改造版？

| 特性 | 原始版 | 改造版 |
|------|--------|--------|
| 网络管理 | 使用默认 bridge 或 link | ✅ 自定义网络，服务发现稳定 |
| 数据管理 | bind mount 宿主机目录 | ✅ 命名 Volume，Docker 统一管理 |
| 可移植性 | 路径依赖宿主机 | ✅ Volume + 网络，跨机器可复用 |
| 扩展性 | 难以加节点 | ✅ 轻松扩展 Slave、多 NameServer |
| 生产就绪 | ❌ 仅适合个人测试 | ✅ 符合生产部署规范 |

---

## 📌 总结

你现在拥有一套：

✅ **基于自定义网络 + 命名数据卷** 的 RocketMQ 部署方案  
✅ **服务名稳定解析**，不依赖 IP 或 link  
✅ **数据持久化规范管理**，Volume 可备份、迁移  
✅ **配置文件外挂**，便于调试和版本控制  
✅ **一键部署**，适合团队共享、CI/CD 集成

---

🎯 **下一步建议**：

1. 在 Spring Boot 项目中连接 `127.0.0.1:9876` 测试消息收发
2. 在 Dashboard 中创建 Topic、查看消息轨迹
3. 学习事务消息、延迟消息实战
4. 尝试扩展为 2 NameServer + 1 Master 1 Slave 架构

如需我为你提供：

- Spring Boot + RocketMQ 事务消息完整示例
- 多节点集群部署方案
- Prometheus + Grafana 监控集成
- Kubernetes 部署 YAML

欢迎随时提问！

祝你开发顺利，架构能力再上一层楼！🚀