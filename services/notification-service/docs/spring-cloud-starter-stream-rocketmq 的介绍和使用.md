你提到的依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
</dependency>
```

是 **Spring Cloud Alibaba** 项目中用于集成 **RocketMQ + Spring Cloud Stream** 的官方 Starter。

---

# 📌 一、最新版本是什么？

> ✅ 截止 2025 年 4 月，**最新稳定版本是 `2022.0.0.0`（对应 Spring Cloud Alibaba 2022.0.0.0）**

### 📦 Maven 依赖（推荐版本）

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
    <version>2022.0.0.0</version>
</dependency>
```

> ⚠️ 注意：
> - 此版本基于 Spring Boot 3.x + Spring Cloud 2022.0.x（即 Spring Cloud “2022.0” 版本代号）
> - 如果你使用的是 Spring Boot 2.x，请使用 `2.2.9.RELEASE` 或 `2021.0.5.0`
> - 版本兼容性请参考：https://sca.aliyun.com/zh-cn/docs/next/user-guide/version-description

---

# 🎯 二、spring-cloud-starter-stream-rocketmq 有什么作用？

它基于 **Spring Cloud Stream** 抽象，让你以“声明式、函数式、配置驱动”的方式操作 RocketMQ，而无需直接调用 RocketMQ 原生 API。

### ✅ 核心价值：

| 功能 | 说明 |
|------|------|
| **屏蔽底层细节** | 不需要关心 Producer/Consumer、连接、序列化等 |
| **统一编程模型** | 使用 `@Input` / `@Output` / `Function` 编程模型 |
| **配置驱动** | 通过 `application.yml` 配置 Topic、Group、Tag、消费模式等 |
| **支持多种消息类型** | 普通消息、事务消息、顺序消息、延迟消息（部分需配置） |
| **无缝集成 Spring Boot** | 自动装配、健康检查、指标监控、Actuator 集成 |

> 🧩 类比：就像 MyBatis 屏蔽了 JDBC，Spring Cloud Stream 屏蔽了 RocketMQ 原生 API。

---

# 🚀 三、详细使用文档 + 完整示例（含中文注释）

下面我将为你提供一个**完整可运行的 Spring Boot 项目示例**，涵盖：

1. 普通消息发送与接收
2. 顺序消息
3. 事务消息（重点！）
4. 延迟消息（通过属性配置）
5. 消息 Tag 过滤

---

## 📁 项目结构

```
src/main/java/com/example/demo/
├── DemoApplication.java          # 启动类
├── config/
│   └── RocketMQStreamConfig.java # Stream 配置（可选）
├── service/
│   └── OrderService.java         # 业务服务（发送消息）
└── listener/
    ├── NormalMessageListener.java
    ├── OrderlyMessageListener.java
    ├── TransactionMessageListener.java
    └── DelayMessageListener.java
```

---

## 🧩 第一步：添加依赖 + 配置

### 1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.5</version> <!-- 或 3.2.x -->
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>rocketmq-stream-demo</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
        <spring-cloud-alibaba.version>2022.0.0.0</spring-cloud-alibaba.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Web 依赖（非必须，用于测试接口） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- RocketMQ Stream Starter -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
        </dependency>

        <!-- Lombok（可选） -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### 2. application.yml（核心配置）

```yaml
server:
  port: 8080

spring:
  application:
    name: rocketmq-stream-demo

  cloud:
    stream:
      # 绑定器配置（支持多绑定器）
      binders:
        rocketmq-binder:
          type: rocketmq
          environment:
            spring:
              cloud:
                stream:
                  rocketmq:
                    binder:
                      # NameServer 地址
                      name-server: 127.0.0.1:9876
                      # 可选：access-key / secret-key（阿里云需要）

      # 绑定输入输出通道
      bindings:
        # ========== 普通消息 ==========
        normal-output:
          destination: NormalTopic          # Topic 名称
          binder: rocketmq-binder
          content-type: application/json
        normal-input:
          destination: NormalTopic
          group: normal-consumer-group      # 消费者组
          binder: rocketmq-binder

        # ========== 顺序消息 ==========
        orderly-output:
          destination: OrderlyTopic
          binder: rocketmq-binder
        orderly-input:
          destination: OrderlyTopic
          group: orderly-consumer-group
          binder: rocketmq-binder

        # ========== 事务消息 ==========
        transactional-output:
          destination: TransactionTopic
          binder: rocketmq-binder
        transactional-input:
          destination: TransactionTopic
          group: transaction-consumer-group
          binder: rocketmq-binder

        # ========== 延迟消息 ==========
        delay-output:
          destination: DelayTopic
          binder: rocketmq-binder
        delay-input:
          destination: DelayTopic
          group: delay-consumer-group
          binder: rocketmq-binder

      # RocketMQ 特定配置
      rocketmq:
        bindings:
          # 普通消息：无需特殊配置
          normal-output:
            producer:
              # 同步发送（默认）
              send-type: sync
          normal-input:
            consumer:
              # 集群消费（默认）
              message-model: clustering

          # 顺序消息：指定分区选择器（按 orderId 一致性 Hash）
          orderly-output:
            producer:
              send-type: sync
              # 开启顺序消息（局部顺序）
              orderly: true
              # 分区选择器 Bean 名称（见下文）
              selector-expression: selectQueueByKey

          # 事务消息：必须配置 transactional: true
          transactional-output:
            producer:
              send-type: transactional
              # 事务监听器 Bean 名称（见下文）
              transaction-listener: transactionListener

          # 延迟消息：通过消息头设置延迟等级
          delay-output:
            producer:
              send-type: sync

          # 所有消费者配置
          normal-input:
            consumer:
              message-model: clustering
          orderly-input:
            consumer:
              message-model: clustering
          transactional-input:
            consumer:
              message-model: clustering
          delay-input:
            consumer:
              message-model: clustering

# 自定义分区选择器（用于顺序消息）
custom:
  queue:
    selector:
      bean-name: selectQueueByKey
```

---

## 🧩 第二步：编写消息监听器（消费者）

### 1. 普通消息监听器

```java
package com.example.demo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 普通消息监听器
 * 监听 NormalTopic，消费者组 normal-consumer-group
 */
@Component
@Slf4j
public class NormalMessageListener {

    @StreamListener("normal-input")
    public void handleNormalMessage(@Payload String message) {
        log.info("✅ [普通消息] 收到消息: {}", message);
        // 业务逻辑：如发邮件、记日志、更新缓存等
    }
}
```

---

### 2. 顺序消息监听器

```java
package com.example.demo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 顺序消息监听器
 * 保证同一 orderId 的消息按发送顺序消费
 */
@Component
@Slf4j
public class OrderlyMessageListener {

    @StreamListener("orderly-input")
    public void handleOrderlyMessage(@Payload String message) {
        log.info("✅ [顺序消息] 收到消息: {}", message);
        // 业务逻辑：如订单状态变更（创建 → 付款 → 发货）
    }
}
```

---

### 3. 事务消息监听器（重点！）

```java
package com.example.demo.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

/**
 * 事务消息消费者
 * 注意：事务消息的“事务”在生产者端控制，消费者只需正常消费
 */
@Component
@Slf4j
public class TransactionMessageListener {

    @StreamListener("transactional-input")
    public void handleTransactionMessage(@Payload String message) {
        log.info("✅ [事务消息] 收到消息: {}", message);
        // 业务逻辑：如增加积分、发通知、创建物流单
        // 注意：必须保证幂等性！
    }
}
```

---

### 4. 延迟消息监听器

```java
package com.example.demo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 延迟消息监听器
 * 消息将在指定延迟时间后被消费
 */
@Component
@Slf4j
public class DelayMessageListener {

    @StreamListener("delay-input")
    public void handleDelayMessage(@Payload String message) {
        log.info("✅ [延迟消息] 收到消息: {}", message);
        // 业务逻辑：如订单超时关闭、优惠券过期提醒
    }
}
```

---

## 🧩 第三步：编写消息发送服务（生产者）

```java
package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * RocketMQ 消息发送服务
 * 使用 StreamBridge 动态发送消息
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final StreamBridge streamBridge;

    // ========== 1. 发送普通消息 ==========
    public void sendNormalMessage(String content) {
        boolean sent = streamBridge.send("normal-output", content);
        if (sent) {
            log.info("📤 [普通消息] 已发送: {}", content);
        } else {
            log.error("❌ [普通消息] 发送失败: {}", content);
        }
    }

    // ========== 2. 发送顺序消息 ==========
    public void sendOrderlyMessage(String orderId, String content) {
        Message<String> message = MessageBuilder
                .withPayload(content)
                .setHeader("ORDER_ID", orderId) // 设置分区键
                .build();

        boolean sent = streamBridge.send("orderly-output", message);
        if (sent) {
            log.info("📤 [顺序消息] orderId={} 已发送: {}", orderId, content);
        } else {
            log.error("❌ [顺序消息] orderId={} 发送失败: {}", orderId, content);
        }
    }

    // ========== 3. 发送事务消息（重点！）==========
    public void sendTransactionalMessage(String orderId, String content) {
        Message<String> message = MessageBuilder
                .withPayload(content)
                .setHeader("ORDER_ID", orderId)
                .build();

        boolean sent = streamBridge.send("transactional-output", message);
        if (sent) {
            log.info("📤 [事务消息] orderId={} 已发送（事务待提交）", orderId);
        } else {
            log.error("❌ [事务消息] orderId={} 发送失败", orderId);
        }
    }

    // ========== 4. 发送延迟消息 ==========
    public void sendDelayMessage(String content, int delayLevel) {
        // RocketMQ 延迟等级：1=1s, 2=5s, 3=10s, 4=30s, 5=1m, ..., 18=2h
        Message<String> message = MessageBuilder
                .withPayload(content)
                .setHeader("DELAY", delayLevel) // 设置延迟等级
                .build();

        boolean sent = streamBridge.send("delay-output", message);
        if (sent) {
            log.info("📤 [延迟消息] {}秒后消费: {}", getDelaySeconds(delayLevel), content);
        } else {
            log.error("❌ [延迟消息] 发送失败: {}", content);
        }
    }

    // 延迟等级转秒数（仅用于日志）
    private int getDelaySeconds(int level) {
        int[] delays = {1, 5, 10, 30, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 900, 1800, 3600, 7200};
        return level >= 1 && level <= 18 ? delays[level - 1] : 0;
    }
}
```

---

## 🧩 第四步：配置顺序消息分区选择器（Bean）

```java
package com.example.demo.config;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;

/**
 * RocketMQ 顺序消息分区选择器
 * 根据 ORDER_ID 选择固定的 MessageQueue，保证同一订单的消息进入同一队列
 */
@Configuration
public class RocketMQStreamConfig {

    @Bean("selectQueueByKey")
    public MessageQueueSelector selectQueueByKey() {
        return new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                String orderId = (String) arg; // 来自 MessageBuilder.setHeader("ORDER_ID", ...)
                if (orderId == null) {
                    return mqs.get(0);
                }
                // 一致性 Hash，确保同一 orderId 总是选同一个队列
                int index = Math.abs(orderId.hashCode()) % mqs.size();
                return mqs.get(index);
            }
        };
    }
}
```

---

## 🧩 第五步：配置事务消息监听器（Bean）

```java
package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 事务消息监听器
 * 实现二阶段提交：本地事务执行 + 事务状态回查
 */
@Component("transactionListener")
@Slf4j
public class TransactionListener implements RocketMQLocalTransactionListener {

    /**
     * 执行本地事务（第一阶段）
     * @param message 消息体
     * @param o 附加参数（本例未用）
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        try {
            String orderId = (String) message.getHeaders().get("ORDER_ID");
            log.info("beginTransaction: orderId={}", orderId);

            // 🎯 模拟本地事务：如扣减库存、更新订单状态
            boolean success = simulateLocalTransaction(orderId);

            if (success) {
                log.info("✅ 本地事务成功，提交消息: orderId={}", orderId);
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                log.warn("❌ 本地事务失败，回滚消息: orderId={}", orderId);
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (Exception e) {
            log.error("executeLocalTransaction 异常", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 事务状态回查（第二阶段，Broker 定时调用）
     * @param message 消息体
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        String orderId = (String) message.getHeaders().get("ORDER_ID");
        log.info("checkLocalTransaction: orderId={}", orderId);

        // 🎯 模拟查询本地事务状态（如查询订单是否支付成功）
        boolean success = checkTransactionStatus(orderId);

        if (success) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    // 模拟本地事务（如数据库操作）
    private boolean simulateLocalTransaction(String orderId) {
        // 假设 orderId 为 "fail" 时失败
        return !Objects.equals(orderId, "fail");
    }

    // 模拟事务状态查询
    private boolean checkTransactionStatus(String orderId) {
        // 简化：假设所有订单最终都成功（实际应查数据库）
        return true;
    }
}
```

---

## 🧩 第六步：启动类

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

## 🧪 第七步：测试 Controller（可选）

```java
package com.example.demo.controller;

import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final OrderService orderService;

    @GetMapping("/send/normal")
    public String sendNormal(@RequestParam String msg) {
        orderService.sendNormalMessage(msg);
        return "普通消息已发送: " + msg;
    }

    @GetMapping("/send/orderly")
    public String sendOrderly(@RequestParam String orderId, @RequestParam String msg) {
        orderService.sendOrderlyMessage(orderId, msg);
        return "顺序消息已发送: orderId=" + orderId + ", msg=" + msg;
    }

    @GetMapping("/send/transaction")
    public String sendTransaction(@RequestParam String orderId, @RequestParam String msg) {
        orderService.sendTransactionalMessage(orderId, msg);
        return "事务消息已发送（待本地事务提交）: orderId=" + orderId;
    }

    @GetMapping("/send/delay")
    public String sendDelay(@RequestParam String msg, @RequestParam(defaultValue = "3") int level) {
        orderService.sendDelayMessage(msg, level);
        return "延迟消息已发送（" + level + "级延迟）: " + msg;
    }
}
```

---

## ✅ 第八步：启动 & 测试

1. 启动 RocketMQ（NameServer + Broker + Dashboard）
2. 启动 Spring Boot 应用
3. 访问：

```bash
# 发送普通消息
curl "http://localhost:8080/send/normal?msg=HelloNormal"

# 发送顺序消息（相同 orderId 保证顺序）
curl "http://localhost:8080/send/orderly?orderId=1001&msg=Step1"
curl "http://localhost:8080/send/orderly?orderId=1001&msg=Step2"

# 发送事务消息
curl "http://localhost:8080/send/transaction?orderId=2001&msg=PaySuccess"

# 发送延迟消息（等级3=10秒后消费）
curl "http://localhost:8080/send/delay?msg=TimeoutClose&level=3"
```

4. 查看控制台日志 + RocketMQ Dashboard，验证消息是否按预期发送和消费

---

## 📌 九、注意事项 & 最佳实践

1. **事务消息必须实现 `RocketMQLocalTransactionListener`**
2. **顺序消息必须配置 `selector-expression` 和分区选择器 Bean**
3. **延迟消息通过 Header 设置 `DELAY`，值为 1~18 的整数**
4. **所有消费者必须保证幂等性**（网络重试、消费重试可能导致重复）
5. **生产环境建议关闭自动创建 Topic**：`autoCreateTopicEnable=false`
6. **监控：集成 Actuator + Prometheus，监控消息堆积、消费延迟**

---

## ✅ 总结

你现在拥有：

✅ 一个完整的 Spring Cloud Stream + RocketMQ 集成项目  
✅ 支持普通、顺序、事务、延迟消息  
✅ 详细中文注释 + 可运行示例  
✅ 生产级最佳实践

---

🎯 **下一步建议**：

- 在 Dashboard 中观察消息轨迹
- 实现死信队列 + 人工补偿
- 集成 Sleuth + Zipkin 实现全链路追踪
- 配置消费重试 + 死信 Topic

如需我为你提供：

- 死信队列处理示例
- 消息幂等性实现方案
- 与 Spring Cloud Sleuth 集成
- Kubernetes 部署配置

欢迎随时提问！

祝你开发顺利，架构能力再上新台阶！🚀