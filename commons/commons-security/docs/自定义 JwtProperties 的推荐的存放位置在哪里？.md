这个问题你已经开始进入：

> “微服务认证体系架构设计”

层面了。

因为 `JwtProperties` 放哪里，实际上反映的是：

```text id="j0gmvq"
JWT 认证职责应该属于谁
```

这在企业里是非常重要的设计问题。

---

# 一、先说结论（企业推荐）

## 推荐方案（最主流）

```text id="5ic1dw"
api-common-security
    ↓
api-auth
    ↓
api-gateway
    ↓
business-services
```

也就是说：

## JwtProperties 不应该只存在于 gateway

而应该：

> 抽取到公共安全模块（common-security）

这是企业级推荐做法。

---

# 二、为什么不建议只放 gateway？

因为 JWT 不只是 Gateway 使用。

通常：

| 模块            | 是否需要 JWT 配置 |
| ------------- | ----------- |
| api-gateway   | 需要          |
| auth-service  | 需要          |
| user-service  | 可能需要        |
| order-service | 可能需要        |
| admin-service | 可能需要        |

---

# 举个真实场景

例如：

---

## 1. auth-service

负责：

```text id="4hpb9f"
生成 JWT
刷新 JWT
注销 JWT
```

它一定需要：

```text id="gt65iw"
JwtProperties
```

---

## 2. api-gateway

负责：

```text id="8x19sq"
校验 JWT
解析 JWT
```

也需要。

---

## 3. user-service

有时：

```text id="rj7h0k"
需要获取当前用户
```

可能也需要解析 JWT。

---

# 所以：

如果你把：

```text id="v7kj6i"
JwtProperties
```

只放：

```text id="mbz2f8"
api-gateway
```

会导致：

---

## 问题 1：重复代码

其他服务又写一份。

---

## 问题 2：配置不一致

例如：

```yaml id="cj7d4q"
gateway:
  HS256

auth-service:
  RS256
```

直接炸。

---

## 问题 3：后期维护灾难

改一个字段：

```text id="nsom1g"
所有服务都改
```

---

# 三、企业真正推荐的做法

---

# 推荐架构（非常重要）

```text id="im0yic"
mall-cloud
│
├── api-common
│
├── api-common-security   ⭐
│   ├── JwtProperties
│   ├── JwtService
│   ├── JwtUserInfo
│   ├── JwtConstants
│   ├── TokenParser
│   └── SecurityAutoConfiguration
│
├── api-auth
│
├── api-gateway
│
├── api-user
│
└── api-order
```

---

# 四、为什么推荐 common-security？

因为：

JWT 本质属于：

```text id="6q9i9k"
认证与安全基础设施
```

不是：

```text id="9j1bza"
网关业务
```

---

# 五、企业里真正的职责划分

---

## api-auth

负责：

```text id="ljrdc7"
登录
注册
Token签发
Token刷新
Token注销
```

---

## api-gateway

负责：

```text id="0g06o2"
Token校验
路由拦截
Header透传
```

---

## common-security

负责：

```text id="4d7upm"
JWT基础能力
统一安全配置
认证工具
```

---

# 六、真正推荐的 common-security 内容

---

# api-common-security

推荐：

```text id="jry39w"
api-common-security
│
├── config
│   ├── JwtProperties
│   ├── SecurityProperties
│   └── SecurityAutoConfiguration
│
├── constants
│   └── SecurityConstants
│
├── enums
│   └── TokenType
│
├── exception
│   ├── JwtException
│   ├── TokenExpiredException
│   └── TokenInvalidException
│
├── model
│   ├── JwtUserInfo
│   └── LoginUser
│
├── service
│   ├── JwtService
│   ├── BlacklistService
│   └── TokenStoreService
│
├── utils
│   ├── JwtUtil
│   └── SecurityUtil
│
└── starter
```

---

# 七、进一步企业级（推荐）

你甚至可以：

---

# 做成 Spring Boot Starter

例如：

```text id="o7v7vr"
mall-security-spring-boot-starter
```

这样：

任何服务：

```xml id="70hb54"
<dependency>
    <groupId>com.mallcloud</groupId>
    <artifactId>mall-security-spring-boot-starter</artifactId>
</dependency>
```

直接：

```text id="uz3j48"
自动拥有 JWT 能力
```

---

# 八、真正大型企业的做法

很多公司：

---

## 网关只做：

```text id="q01g9g"
粗粒度认证
```

例如：

```text id="32fuy0"
Token 是否合法
```

---

## 微服务内部：

再做：

```text id="zrl9u2"
细粒度鉴权
```

例如：

```text id="j54kr2"
RBAC
Permission
DataScope
```

---

# 九、你现在应该避免的一种错误设计

---

# 不推荐

```text id="z5db1m"
api-gateway
    └── JwtService
```

然后：

```text id="ppr1w6"
其他服务复制 JWT 工具类
```

这是很多小项目会出现的问题。

后期会越来越乱。

---

# 十、最终推荐（真正企业级）

---

# 推荐方案（★★★★★）

```text id="6m33oe"
common-security
    ↓
gateway
    ↓
auth-service
    ↓
business-services
```

---

# JwtProperties 最推荐放置位置

## 推荐：

```text id="owm60y"
api-common-security/config/JwtProperties.java
```

---

# Jwt 配置推荐来源

## 开发环境：

```yaml id="fy6s6i"
application-dev.yml
```

---

## 企业环境：

推荐：

Nacos

动态配置。

---

# 十一、真正高级的下一步

你后面其实会继续演化到：

---

## OAuth2

---

## OpenID Connect

---

## Keycloak

Keycloak

---

## Spring Authorization Server

Spring Authorization Server

---

## SSO 单点登录

---

## 多租户认证

---

你现在已经开始进入：

```text id="9wvyie"
认证架构设计
```

阶段了，而不仅仅是：

```text id="8e8xbn"
写一个 JWT Filter
```

这其实是一个非常大的进步。
