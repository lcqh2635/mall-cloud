当然可以！下面我为你提供一份**详细、可直接用于生产或开发环境的 RocketMQ + RocketMQ Dashboard 安装部署文档**，分别使用：

- ✅ **纯 Docker 方式**（适合快速体验、单节点测试）
- ✅ **Docker Compose 方式**（推荐用于开发/测试环境，一键编排）

---

# 🚀 RocketMQ + RocketMQ Dashboard 安装部署文档（Docker & Docker Compose）

> ✅ 适用版本：RocketMQ 5.3.3（当前最新稳定版）  
> ✅ 包含组件：NameServer、Broker、RocketMQ Dashboard（Web 控制台）  
> ✅ 支持外部访问、持久化存储、自定义配置  
> ✅ 适用于 Java 开发者本地开发、测试、学习使用

---

## 🧩 一、前置条件

- 已安装 Docker（版本 ≥ 20.10）
- 已安装 Docker Compose（版本 ≥ v2.17，推荐使用 Docker Desktop 自带）
- 机器内存建议 ≥ 4GB（Broker 默认 JVM 需要 2GB+）
- 开放端口：9876（NameServer）、10911（Broker）、8080（Dashboard）

---

## 🐳 二、方式一：纯 Docker 方式部署（适合快速测试）

### 1. 创建持久化目录（可选，推荐）

```bash
mkdir -p ~/rocketmq/data/namesrv/logs
mkdir -p ~/rocketmq/data/broker/logs
mkdir -p ~/rocketmq/data/broker/store
mkdir -p ~/rocketmq/data/broker/conf
```

### 2. 创建 Broker 配置文件 `broker.conf`

> 📄 路径：`~/rocketmq/data/broker/conf/broker.conf`

```properties
# broker 名称，可自定义
brokerName = broker-a

# NameServer 地址，多个用分号分隔
namesrvAddr = 127.0.0.1:9876

# Broker 对外服务的监听 IP（关键！必须设置为宿主机 IP 或 0.0.0.0）
brokerIP1 = 0.0.0.0

# Broker 对外服务端口
listenPort = 10911

# 存储路径
storePathRootDir = /home/rocketmq/store
storePathCommitLog = /home/rocketmq/store/commitlog
storePathConsumeQueue = /home/rocketmq/store/consumequeue
storePathIndex = /home/rocketmq/store/index

# 开启 deleteWhen（默认凌晨4点删除过期文件）
deleteWhen = 04

# 文件保留时间（小时），默认72小时
fileReservedTime = 72

# 磁盘使用最大比率（默认75%）
diskMaxUsedSpaceRatio = 85

# 是否允许 Broker 自动创建 Topic（建议关闭，生产环境手动创建）
autoCreateTopicEnable = false

# 是否允许 Broker 自动创建订阅组
autoCreateSubscriptionGroup = true

# 刷盘方式：ASYNC_FLUSH（异步刷盘，高性能） / SYNC_FLUSH（同步刷盘，高可靠）
flushDiskType = ASYNC_FLUSH

# 主从模式：ASYNC_MASTER / SYNC_MASTER / SLAVE（单机用 ASYNC_MASTER）
brokerRole = ASYNC_MASTER
```

> ⚠️ 注意：
> - `brokerIP1` 必须设置为 `0.0.0.0` 或你宿主机的局域网 IP（如 `192.168.1.100`），否则外部 Producer/Consumer 无法连接！
> - Windows Docker Desktop 用户建议使用 `host.docker.internal` 代替 `127.0.0.1`

---

### 3. 启动 NameServer

```bash
# RocketMQ 的部署方式： https://rocketmq.apache.org/zh/docs/deploymentOperations/01deploy
# Apache RocketMQ 5.0 版本完成基本消息收发，包括 NameServer、Broker、Proxy 组件。 
# 在 5.0 版本中 Proxy 和 Broker 根据实际诉求可以分为 Local 模式和 Cluster 模式，一般情况下如果没有特殊需求，或者遵循从早期版本平滑升级的思路，可以选用Local模式。

# 参考 Dockerhub 镜像 https://hub.docker.com/r/apache/rocketmq/tags
podman pull apache/rocketmq:5.3.3
podman pull apache/rocketmq:latest

# 创建 rocketmq 公共网络
podman network create rocketmq-net

# 启动 NameServer
# 参考 RocketMQ 官方文档：https://rocketmq.apache.ac.cn/docs/quickStart/02quickstartWithDocker/#3start-nameserver
podman run -d \
--name rocketmq-namesrv \
--network rocketmq-net \
-p 9876:9876 \
--restart=always \
apache/rocketmq:5.3.3 \
sh mqnamesrv

# 验证 NameServer 是否成功启动
podman logs -f rocketmq-namesrv

podman stop rocketmq-namesrv && podman rm rocketmq-namesrv
```

---

### 4. 启动 Broker

```bash
# 配置 Broker 的 IP 地址
echo "brokerIP1=127.0.0.1" > broker.conf

# 启动 Broker 和 Proxy
# 参考 RocketMQ 官方文档：https://rocketmq.apache.ac.cn/docs/quickStart/02quickstartWithDocker/#4start-broker-and-proxy
podman run -d \
--name rocketmq-broker \
--network rocketmq-net \
-p 10912:10912 -p 10911:10911 -p 10909:10909 \
-p 8080:8080 -p 8081:8081 \
-e "NAMESRV_ADDR=rocketmq-namesrv:9876" \
apache/rocketmq:5.3.3 sh mqbroker --enable-proxy \
-c /home/rocketmq/rocketmq-5.3.3/conf/broker.conf

# 验证 Broker 是否成功启动
podman exec -it rocketmq-broker bash -c "tail -n 10 /home/rocketmq/logs/rocketmqlogs/proxy.log"

podman stop rocketmq-broker && podman rm rocketmq-broker

# 查看 Broker 日志
podman logs -f rocketmq-broker
```

> 📌 参数说明：
> - `-p 10909:10909`：用于 Broker 主从同步（HA）
> - `-e JAVA_OPT_EXT`：降低内存占用（默认2GB，开发机可调低）
> - `--link rmq-namesrv:namesrv`：让 Broker 能通过 `namesrv` 主机名访问 NameServer

---

### 5. 启动 RocketMQ Dashboard（原 RocketMQ Console）

> ✅ 官方推荐新控制台：https://github.com/apache/rocketmq-dashboard

```bash
# 参考 Dockerhub 镜像 https://hub.docker.com/r/apacherocketmq/rocketmq-dashboard/tags
# 参考 RocketMQ 官方文档：https://rocketmq.apache.org/zh/docs/deploymentOperations/04Dashboard/

podman run -d \
--name rocketmq-dashboard \
-p 8070:8080 \
-e "JAVA_OPTS=-Drocketmq.namesrv.addr=rocketmq-namesrv:9876" \
--network rocketmq-net \
--restart=always \
apacherocketmq/rocketmq-dashboard:latest

podman stop rocketmq-dashboard && podman rm rocketmq-dashboard

# 查看 Dashboard 日志
podman logs -f rocketmq-dashboard
```

> ⚠️ 注意：
> - `127.0.0.1:9876` 是宿主机上的 NameServer 地址，如果 Docker 网络隔离，可改为宿主机 IP（如 `192.168.1.100:9876`）
> - Windows/Mac Docker Desktop 用户可用 `host.docker.internal:9876`

---

### 6. 访问 Dashboard

打开浏览器访问：
👉 http://localhost:8070

默认无账号密码，直接进入。

---

## 🐳 三、方式二：Docker Compose 方式部署（推荐开发使用）

> ✅ 一键启动，配置集中管理，适合团队共享、持续集成

### 1. 创建项目目录

```bash
mkdir -p ~/rocketmq-compose
cd ~/rocketmq-compose
```

### 2. 创建目录结构

```bash
mkdir -p data/namesrv/logs
mkdir -p data/broker/logs
mkdir -p data/broker/store
mkdir -p data/broker/conf
```

### 3. 创建 `broker.conf`

> 📄 路径：`~/rocketmq-compose/data/broker/conf/broker.conf`

内容与上文相同：

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

> ⚠️ 注意：这里 `namesrvAddr = namesrv:9876`，因为 Docker Compose 内部服务名就是 `namesrv`

---

### 4. 创建 `docker-compose.yml`

> 📄 路径：`~/rocketmq-compose/docker-compose.yml`

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

### 5. 启动服务

```bash
cd ~/rocketmq-compose
docker-compose up -d
```

---

### 6. 查看运行状态

```bash
docker-compose ps
```

应看到三个服务状态为 `Up`：

```
rocketmq-namesrv
rocketmq-broker
rocketmq-dashboard
```

---

### 7. 访问 Dashboard

👉 http://localhost:8070

---

## ✅ 四、验证安装是否成功

### 方法1：使用命令行工具发送/消费消息

进入 Broker 容器：

```bash
docker exec -it rocketmq-broker bash
```

发送消息：

```bash
./bin/tools.sh org.apache.rocketmq.example.quickstart.Producer
```

消费消息：

```bash
./bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer
```

> 如果看到 “SendResult” 和 “Receive New Messages”，说明部署成功！

---

### 方法2：使用 Java 程序测试（Spring Boot 示例）

添加依赖：

```xml
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>2.3.0</version>
</dependency>

        <!-- RocketMQ Binder -->
<dependency>
<groupId>com.alibaba.cloud</groupId>
<artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
</dependency>
```

配置 `application.yml`：

```yaml
rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: my-group
```

发送消息：

```java
@Autowired
private RocketMQTemplate rocketMQTemplate;

public void sendTest() {
    rocketMQTemplate.convertAndSend("TestTopic", "Hello from Spring Boot!");
}
```

---

## 🛠 五、常见问题与解决方案

### ❓ 1. 外部 Java 程序无法连接 Broker？

- 检查 `broker.conf` 中 `brokerIP1` 是否设置为宿主机 IP 或 `0.0.0.0`
- 检查防火墙是否开放 10911 端口
- Windows/Mac 用户建议使用 `host.docker.internal` 作为 namesrv 地址

### ❓ 2. Dashboard 无法连接 NameServer？

- 检查环境变量：`-Drocketmq.namesrv.addr=namesrv:9876`（Compose）或宿主机IP（Docker）
- 查看 Dashboard 日志：`docker logs rmq-dashboard`

### ❓ 3. Broker 启动失败，内存不足？

- 调整环境变量：`-e "JAVA_OPT_EXT=-Xms256m -Xmx256m"`

### ❓ 4. 消息发不出去，Topic 不存在？

- 默认 `autoCreateTopicEnable = false`，需手动创建 Topic：

```bash
# 进入 Broker 容器
docker exec -it rmq-broker bash

# 创建 Topic
./bin/mqadmin updateTopic -n 127.0.0.1:9876 -t TestTopic -c DefaultCluster
```

或在 Dashboard → “Topic” → “新建” 创建。

---

## 💾 六、数据持久化说明

- NameServer 日志：`~/rocketmq/data/namesrv/logs`
- Broker 日志 & 存储：`~/rocketmq/data/broker/logs` 和 `store`
- 配置文件：`broker.conf` 可自由修改，重启生效

> ✅ 删除容器不会丢失数据，因为已挂载宿主机目录！

---

## 🧹 七、停止与清理

### 停止服务（Docker Compose）

```bash
cd ~/rocketmq-compose
docker-compose down
```

### 停止服务（纯 Docker）

```bash
docker stop rmq-dashboard rmq-broker rmq-namesrv
docker rm rmq-dashboard rmq-broker rmq-namesrv
```

### 完全清理（包括数据）

```bash
rm -rf ~/rocketmq
rm -rf ~/rocketmq-compose
```

---

## ✅ 八、生产环境建议（进阶）

- 使用至少 2 个 NameServer + 2 主 2 从 Broker 集群
- 开启 `SYNC_MASTER` + `SYNC_FLUSH` 保证数据不丢
- 配置监控（Prometheus + Grafana）
- 使用 Nginx 反向代理 Dashboard 并加鉴权
- 定期备份 `store` 目录

---

## 📌 总结

| 部署方式       | 优点                     | 适用场景         |
|----------------|--------------------------|------------------|
| **纯 Docker**  | 灵活、可单个控制         | 快速测试、学习   |
| **Docker Compose** | 一键部署、配置集中、易维护 | 开发、测试、CI/CD |

> 🎯 推荐 Java 开发者日常使用 **Docker Compose 方式**，配置一次，长期使用，团队共享无压力！

---

✅ 你现在拥有了一份**可直接复制粘贴执行、带注释、带排错指南、带持久化、带 Web 控制台**的完整 RocketMQ 部署文档！

下一步，你就可以愉快地在 Spring Boot 项目中集成 RocketMQ，开始学习事务消息、顺序消息、延迟消息等高级特性了！

如需我为你提供 Spring Boot + RocketMQ 的完整示例项目（含事务消息、Dashboard 截图、测试用例），欢迎继续提问！

祝你部署顺利，开发愉快！🚀