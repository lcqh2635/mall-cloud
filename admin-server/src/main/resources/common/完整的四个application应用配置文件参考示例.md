好的！下面给出完整的四个配置文件参考示例，以 `user-service` 为例。

---

## application.yaml

```yaml
# ================================================================
# user-service 主配置文件
# 说明：只保留启动必须的基础配置，其余配置托管到 Nacos 配置中心
# 敏感信息全部通过环境变量注入，不写任何明文密码
# ================================================================

server:
  # 服务端口
  port: 8082
  servlet:
    encoding:
      # 请求响应字符编码
      charset: UTF-8
      enabled: true
      force: true
  # Tomcat 配置
  tomcat:
    # 最大连接数
    max-connections: 8192
    # 最大线程数
    threads:
      max: 200
      min-spare: 10
    # 连接超时时间（毫秒）
    connection-timeout: 5000

spring:
  # ==================== 应用基础信息 ====================
  application:
    # 服务名称：注册到 Nacos 的唯一标识，务必与 Nacos 配置文件名保持一致
    name: urbane-user

  # ==================== 环境激活 ====================
  profiles:
    # 默认激活开发环境，生产环境通过启动参数覆盖：--spring.profiles.active=prod
    active: dev

  # ==================== Spring Cloud Nacos 配置 ====================
  cloud:
    # -------------------- 负载均衡配置 --------------------
    loadbalancer:
      nacos:
        # 开启基于 Nacos 权重和元数据的负载均衡策略
        enabled: true
      cache:
        # 开启负载均衡本地缓存，减少频繁请求 Nacos
        enabled: true
        # 缓存过期时间：35 秒（略大于心跳间隔）
        ttl: 35s

    nacos:
      # -------------------- Nacos 服务器连接 --------------------
      # 集群模式示例：192.168.1.1:8848,192.168.1.2:8848,192.168.1.3:8848
      server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
      # Nacos 控制台登录用户名（通过环境变量注入）
      username: ${NACOS_USERNAME:nacos}
      # Nacos 控制台登录密码（通过环境变量注入）
      password: ${NACOS_PASSWORD:nacos}

      # -------------------- 注册中心配置 --------------------
      discovery:
        # 命名空间 ID：用于环境隔离（在 Nacos 控制台创建命名空间后填入 ID）
        # 默认为空表示使用 public 命名空间
        namespace: ${NACOS_DISCOVERY_NAMESPACE:}
        # 服务分组：同一命名空间下进一步隔离，默认使用 DEFAULT_GROUP
        group: ${NACOS_DISCOVERY_GROUP:DEFAULT_GROUP}
        # 是否开启服务注册，false 时服务不会注册到 Nacos（用于本地调试）
        register-enabled: true
        # 心跳间隔（毫秒）：每 5 秒向 Nacos 发送一次心跳
        heart-beat-interval: 5000
        # 心跳超时（毫秒）：超过 15 秒未收到心跳则标记为不健康
        heart-beat-timeout: 15000
        # 服务下线超时（毫秒）：超过 30 秒则从注册列表移除
        ip-delete-timeout: 30000
        # 服务元数据：可用于灰度发布、版本路由等高级功能
        metadata:
          # 服务版本号
          version: ${spring.application.version:1.0.0}
          # 当前激活环境
          env: ${spring.profiles.active:dev}

      # -------------------- 配置中心配置 --------------------
      config:
        # 命名空间 ID：与注册中心保持一致，用于配置隔离
        namespace: ${NACOS_CONFIG_NAMESPACE:}
        # 配置分组
        group: ${NACOS_CONFIG_GROUP:DEFAULT_GROUP}

  # ==================== 配置导入（替代 bootstrap.yaml）====================
  config:
    import:
      # -------------------- 服务专属配置 --------------------
      # 服务主配置文件：urbane-user.yaml（数据库连接、业务参数等）
      - optional:nacos:${spring.application.name}.yaml?group=DEFAULT_GROUP&refreshEnabled=true
      # 服务环境配置文件：urbane-user-dev.yaml / urbane-user-prod.yaml 等
      - optional:nacos:${spring.application.name}-${spring.profiles.active:dev}.yaml?group=DEFAULT_GROUP&refreshEnabled=true

      # -------------------- 公共共享配置（所有服务共享）--------------------
      # 数据库连接池公共配置（HikariCP 参数等）
      - optional:nacos:common-datasource.yaml?group=COMMON_GROUP&refreshEnabled=true
      # Redis 连接池公共配置
      - optional:nacos:common-redis.yaml?group=COMMON_GROUP&refreshEnabled=true
      # MyBatis-Plus 公共配置（分页、逻辑删除、字段填充等）
      - optional:nacos:common-mybatis.yaml?group=COMMON_GROUP&refreshEnabled=true
      # Spring Security + JWT 公共配置（Token 参数、白名单等）
      - optional:nacos:common-security.yaml?group=COMMON_GROUP&refreshEnabled=true
      # 接口文档公共配置（Knife4j / SpringDoc）
      - optional:nacos:common-openapi.yaml?group=COMMON_GROUP&refreshEnabled=true
      # 对象存储公共配置（OSS/MinIO）
      - optional:nacos:common-oss.yaml?group=COMMON_GROUP&refreshEnabled=true

      # -------------------- 按需引入（用户服务不需要，在对应服务中单独引入）--------------------
      # 消息队列配置（订单服务、通知服务等引入）
      # - optional:nacos:common-rocketmq.yaml?group=COMMON_GROUP&refreshEnabled=true
      # 搜索引擎配置（搜索服务引入）
      # - optional:nacos:common-elasticsearch.yaml?group=COMMON_GROUP&refreshEnabled=true

  # ==================== Jackson 序列化配置 ====================
  jackson:
    # 日期时间格式
    date-format: yyyy-MM-dd HH:mm:ss
    # 时区
    time-zone: Asia/Shanghai
    # 序列化配置
    serialization:
      # 序列化时不包含 null 值
      write-null-map-values: false
      # Long 类型序列化为字符串，避免前端精度丢失
      write-bigdecimal-as-plain: true
    deserialization:
      # 反序列化时忽略未知字段
      fail-on-unknown-properties: false

# ==================== Actuator 监控端点 ====================
management:
  endpoints:
    web:
      exposure:
        # 默认暴露所有端点（生产环境在 application-prod.yaml 中收窄）
        include: "*"
      # 监控端点基础路径
      base-path: /actuator
  endpoint:
    health:
      # 显示详细健康信息
      show-details: always
    shutdown:
      # 禁用 shutdown 端点，防止误操作
      enabled: false
  # Spring Boot Admin Client 配置
  boot:
    admin:
      client:
        # Admin Server 地址（各环境在对应 yaml 中覆盖）
        url: ${ADMIN_SERVER_URL:http://localhost:9090}
        instance:
          # 注册时优先使用 IP 而非主机名
          prefer-ip: true
          # 自定义实例名称
          name: ${spring.application.name}

# ==================== 日志基础配置 ====================
logging:
  config: classpath:logback-spring.xml
  level:
    # 根日志级别（各环境在对应 yaml 中覆盖）
    root: INFO
    com.mallcloud: INFO
```

---

## application-dev.yaml

```yaml
# ================================================================
# user-service 开发环境配置
# 说明：开发环境配置，日志详细，可以明文配置，方便本地开发调试
# ================================================================

spring:
  cloud:
    nacos:
      # -------------------- 开发环境 Nacos 连接 --------------------
      # 开发环境直接写本地地址，无需环境变量
      server-addr: localhost:8848
      username: nacos
      password: nacos

      discovery:
        # 开发环境命名空间 ID（在 Nacos 控制台创建 dev 命名空间后填入）
        # 示例：a1b2c3d4-e5f6-7890-abcd-ef1234567890
        namespace: dev-namespace-id
        group: DEFAULT_GROUP
        # 开发环境可以关闭服务注册，纯本地调试时使用
        # register-enabled: false

      config:
        # 开发环境配置命名空间，与注册中心保持一致
        namespace: dev-namespace-id
        group: DEFAULT_GROUP

  # ==================== 开发环境数据库（本地直连，不走 Nacos）====================
  # 开发时如果 Nacos 未启动，可以临时在这里配置数据库，联调时再切换到 Nacos
  # datasource:
  #   url: jdbc:mysql://localhost:3306/mall_user?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
  #   username: root
  #   password: root123

# ==================== 开发环境日志（详细）====================
logging:
  level:
    # 根日志级别
    root: INFO
    # 业务代码日志：DEBUG 级别，方便排查问题
    com.mallcloud: DEBUG
    # 打印 MyBatis SQL 语句，方便调试
    com.mallcloud.**.mapper: DEBUG
    # Spring Cloud 相关日志
    org.springframework.cloud: DEBUG
    # Nacos 客户端日志
    com.alibaba.nacos: WARN
    # Feign 调用日志（打印请求响应详情）
    feign.Logger: DEBUG

# ==================== 开发环境 Admin 监控 ====================
management:
  endpoints:
    web:
      exposure:
        # 开发环境暴露所有端点
        include: "*"
  boot:
    admin:
      client:
        # 本地 Admin Server 地址
        url: http://localhost:9090
        # 开发环境连接失败不影响服务启动
        connect-timeout: 5s
        read-timeout: 5s

# ==================== 开发环境 Feign 配置 ====================
feign:
  client:
    config:
      default:
        # 开发环境打印 Feign 请求响应详情
        logger-level: FULL
        # 连接超时时间（毫秒）
        connect-timeout: 5000
        # 读取超时时间（毫秒）
        read-timeout: 10000
  # 开启断路器
  circuitbreaker:
    enabled: true

# ==================== 开发环境特有配置 ====================
# 开发环境关闭部分功能，加快启动速度
spring:
  # 开发工具热重载
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true
```

---

## application-test.yaml

```yaml
# ================================================================
# user-service 测试环境配置
# 说明：测试环境配置，连接测试服务器，日志适中，接近生产但保留调试能力
# ================================================================

spring:
  cloud:
    nacos:
      # -------------------- 测试环境 Nacos 连接 --------------------
      # 测试环境从环境变量注入，不写明文（Jenkins/GitLab CI 中配置）
      server-addr: ${NACOS_SERVER_ADDR:nacos-test.mallcloud.com:8848}
      username: ${NACOS_USERNAME}
      password: ${NACOS_PASSWORD}

      discovery:
        # 测试环境命名空间 ID
        namespace: ${NACOS_DISCOVERY_NAMESPACE:test-namespace-id}
        group: DEFAULT_GROUP

      config:
        # 测试环境配置命名空间
        namespace: ${NACOS_CONFIG_NAMESPACE:test-namespace-id}
        group: DEFAULT_GROUP

# ==================== 测试环境日志（适中）====================
logging:
  level:
    root: INFO
    # 业务代码保留 INFO，方便查看流程
    com.mallcloud: INFO
    # 测试环境不打印 SQL，减少日志量
    com.mallcloud.**.mapper: INFO
    # Nacos 日志只保留警告
    com.alibaba.nacos: WARN

# ==================== 测试环境 Admin 监控 ====================
management:
  endpoints:
    web:
      exposure:
        # 测试环境适当收窄，保留常用端点
        include: health,info,metrics,env,loggers,mappings
  endpoint:
    health:
      show-details: always
  boot:
    admin:
      client:
        # 测试环境 Admin Server 地址
        url: ${ADMIN_SERVER_URL:http://admin-test.mallcloud.com:9090}
        connect-timeout: 10s
        read-timeout: 10s

# ==================== 测试环境 Feign 配置 ====================
feign:
  client:
    config:
      default:
        # 测试环境打印请求头和响应头，不打印 Body
        logger-level: HEADERS
        connect-timeout: 5000
        read-timeout: 15000
  circuitbreaker:
    enabled: true

# ==================== 测试环境特有配置 ====================
# 测试环境关闭热重载
spring:
  devtools:
    restart:
      enabled: false
```

---

## application-prod.yaml

```yaml
# ================================================================
# user-service 生产环境配置
# 说明：生产环境配置，所有敏感信息必须通过环境变量注入
#       日志精简，端点收窄，安全第一
# ================================================================

spring:
  cloud:
    nacos:
      # -------------------- 生产环境 Nacos 连接 --------------------
      # 生产环境全部从环境变量注入，绝不写任何明文配置
      # 环境变量在 K8s Secret 或运维平台中配置
      server-addr: ${NACOS_SERVER_ADDR}
      username: ${NACOS_USERNAME}
      password: ${NACOS_PASSWORD}

      discovery:
        # 生产环境命名空间 ID（从环境变量注入）
        namespace: ${NACOS_DISCOVERY_NAMESPACE}
        group: DEFAULT_GROUP
        # 生产环境元数据（可用于灰度发布）
        metadata:
          version: ${APP_VERSION:1.0.0}
          env: prod
          # 标记该实例的部署区域（多机房场景）
          region: ${DEPLOY_REGION:cn-hangzhou}

      config:
        # 生产环境配置命名空间（从环境变量注入）
        namespace: ${NACOS_CONFIG_NAMESPACE}
        group: DEFAULT_GROUP

# ==================== 生产环境日志（精简）====================
logging:
  level:
    # 生产环境根日志级别：只打印警告和错误
    root: WARN
    # 业务代码保留 INFO
    com.mallcloud: INFO
    # 生产环境绝不打印 SQL
    com.mallcloud.**.mapper: WARN
    # Nacos、Spring 相关只打印错误
    com.alibaba.nacos: ERROR
    org.springframework: WARN
  # 生产环境日志文件配置
  file:
    # 日志文件路径（挂载到宿主机或持久化存储）
    path: /var/log/mallcloud/${spring.application.name}
    name: ${spring.application.name}.log
  logback:
    rollingpolicy:
      # 单个日志文件最大 100MB
      max-file-size: 100MB
      # 日志保留天数
      max-history: 30
      # 日志总大小上限
      total-size-cap: 10GB

# ==================== 生产环境 Actuator 端点（严格收窄）====================
management:
  endpoints:
    web:
      exposure:
        # 生产环境只暴露必要端点
        # health：健康检查（K8s 存活探针使用）
        # info：服务信息
        # metrics：指标数据（Prometheus 采集）
        # prometheus：Prometheus 格式指标
        include: health,info,metrics,prometheus
  endpoint:
    health:
      # 生产环境不对外显示详细健康信息，防止信息泄露
      show-details: when-authorized
      # 健康检查角色
      roles: ACTUATOR_ADMIN
    # 生产环境禁止动态修改日志级别
    loggers:
      enabled: false
  # Prometheus 指标配置
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      # 添加服务名和环境标签，方便 Grafana 过滤
      application: ${spring.application.name}
      env: prod
  boot:
    admin:
      client:
        # 生产环境 Admin Server 地址（从环境变量注入）
        url: ${ADMIN_SERVER_URL}
        # 生产环境 Admin 认证信息
        username: ${ADMIN_USERNAME}
        password: ${ADMIN_PASSWORD}
        instance:
          prefer-ip: true
          name: ${spring.application.name}
        connect-timeout: 10s
        read-timeout: 10s

# ==================== 生产环境 Feign 配置 ====================
feign:
  client:
    config:
      default:
        # 生产环境不打印请求响应详情，减少性能损耗
        logger-level: NONE
        # 生产环境适当延长超时，避免偶发超时导致熔断
        connect-timeout: 3000
        read-timeout: 10000
  circuitbreaker:
    # 生产环境必须开启断路器
    enabled: true

# ==================== 生产环境特有配置 ====================
spring:
  # 生产环境禁用热重载
  devtools:
    restart:
      enabled: false
    livereload:
      enabled: false

  # 生产环境 MVC 配置
  mvc:
    # 隐藏服务器版本信息，防止信息泄露
    pathmatch:
      use-suffix-pattern: false

server:
  # 生产环境隐藏服务器版本信息
  server-header: ""
  error:
    # 生产环境不返回错误详情，防止信息泄露
    include-message: never
    include-stacktrace: never
    include-binding-errors: never
```

---

## 四个文件职责总结

```
application.yaml          所有环境共享的基础配置
                          服务名、端口、Nacos连接、config.import

application-dev.yaml      开发环境专属
                          本地地址明文配置、SQL日志、热重载、详细端点

application-test.yaml     测试环境专属
                          测试服务器地址、适中日志、部分端点

application-prod.yaml     生产环境专属
                          全部环境变量注入、精简日志、严格收窄端点、安全加固
```