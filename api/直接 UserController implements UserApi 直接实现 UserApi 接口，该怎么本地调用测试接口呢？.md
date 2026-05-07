这是一个非常好的问题，而且：

> ❗ 你已经开始真正理解“API 契约分离”架构了。

很多人第一次看到：

```java
public class UserController implements UserApi
```

都会有同一个疑问：

> “那我本地怎么调接口？接口路径去哪了？”

实际上：

> ✅ 完全不影响本地测试
> ✅ 完全不影响 Swagger/OpenAPI
> ✅ 完全不影响 Postman 调试

因为：

```java
@HttpExchange
@GetExchange
@PostExchange
```

本身就已经定义了 Spring MVC 映射。

---

# 一、核心原理（非常重要）

当你：

```java
@RestController
public class UserController implements UserApi
```

时：

Spring 会：

> ✅ 继承接口上的 Mapping 元数据

也就是说：

```java
@GetExchange("/users/{id}")
```

会自动成为：

```http
GET /users/{id}
```

接口。

---

# 二、你实际上已经拥有接口了

例如：

---

## UserApi.java

```java
@HttpExchange("/users")
public interface UserApi {

    @GetExchange("/{id}")
    UserResponse getById(@PathVariable Long id);

}
```

---

## UserController.java

```java
@RestController
public class UserController implements UserApi {

    @Override
    public UserResponse getById(Long id) {
        return new UserResponse();
    }
}
```

---

那么：

Spring MVC 实际会注册：

```http
GET /users/{id}
```

接口。

---

# 三、本地测试方式（完全正常）

---

# 方式1：浏览器访问（GET）

例如：

```http
http://localhost:8080/users/1
```

---

# 方式2：curl

```bash
curl http://localhost:8080/users/1
```

---

# 方式3：Postman

直接：

```http
GET http://localhost:8080/users/1
```

即可。

---

# 四、POST 接口测试（示例）

---

## UserApi.java

```java
@PostExchange
UserResponse create(@RequestBody UserCreateRequest request);
```

---

实际接口：

```http
POST /users
```

---

# Postman 请求：

```json
{
  "username": "admin",
  "password": "123456",
  "email": "admin@test.com"
}
```

---

# 五、你可能遇到的一个坑（重点）

有些 Spring 版本：

```java
@GetExchange
@PostExchange
```

只适用于：

```java
Http Interface Client
```

而：

```java
@RestController implements Interface
```

不一定自动继承。

---

# 六、企业里最稳的做法（我强烈推荐）

这才是真正推荐的方式：

---

# 七、推荐写法（重点）

## 不要直接使用：

```java
@GetExchange
```

而是：

---

# API 接口只定义“普通 Spring MVC 注解”

## UserApi.java

```java
package com.example.api.user.client;

import org.springframework.web.bind.annotation.*;

@RequestMapping("/users")
public interface UserApi {

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    UserResponse getById(@PathVariable Long id);

    /**
     * 创建用户
     */
    @PostMapping
    UserResponse create(@RequestBody UserCreateRequest request);
}
```

---

# 八、Controller 实现

## UserController.java

```java
@RestController
public class UserController implements UserApi {

    @Override
    public UserResponse getById(Long id) {

        UserResponse response = new UserResponse();
        response.setId(id);

        return response;
    }

    @Override
    public UserResponse create(UserCreateRequest request) {

        UserResponse response = new UserResponse();

        return response;
    }
}
```

---

# 九、然后消费者使用 HttpExchange（重点）

注意：

> ❗ HttpExchange Client 不一定必须定义在接口上。

你可以：

---

## 单独定义客户端接口（推荐）

```java
@HttpExchange("/users")
public interface UserRemoteApi {

    @GetExchange("/{id}")
    UserResponse getById(@PathVariable Long id);

    @PostExchange
    UserResponse create(@RequestBody UserCreateRequest request);
}
```

---

# 十、这才是企业里更推荐的结构（重点）

---

# 推荐最终结构

```plaintext
user-api
│
├── controller-api
│     └── UserApi.java
│
├── remote-api
│     └── UserRemoteApi.java
│
├── dto
```

---

# 十一、为什么这么设计（非常关键）

因为：

---

## Spring MVC 注解

适合：

```java
@RestController
```

---

## HttpExchange 注解

适合：

```java
RestClient
WebClient
```

---

# 十二、很多人误区（重点）

很多人以为：

```java
@HttpExchange
```

既是：

* Controller Mapping
* 又是 Client Mapping

但实际上：

> ❗ 它更偏客户端接口定义。

---

# 十三、最佳实践（我最推荐）

---

# 方案一（推荐，最稳）

## Controller API

```java
@RequestMapping
@GetMapping
@PostMapping
```

---

## Remote API

```java
@HttpExchange
@GetExchange
@PostExchange
```

---

# 十四、最终推荐结构（非常重要）

这是我真正推荐你的：

```plaintext
user-api
│
├── dto
│
├── controller
│     └── UserApi.java
│
├── remote
│     └── UserRemoteApi.java
```

---

# 十五、职责清晰（重点）

| 类型            | 作用             |
| ------------- | -------------- |
| UserApi       | Controller 契约  |
| UserRemoteApi | HTTP Client 契约 |

---

# 十六、如果你嫌两个接口重复怎么办？（重点）

企业里一般：

---

## 小项目

直接：

```java
Controller implements UserApi
```

即可。

---

## 大项目（推荐）

分离：

```plaintext
Controller API
Remote API
```

因为：

* Swagger
* 网关
* 权限
* 版本化

都会更清晰。

---

# 十七、我的最终建议（结合你当前阶段）

你现在：

* Spring Boot 4
* HttpExchange
* 微服务
* 电商项目

我建议：

---

# 当前阶段最推荐

## 使用：

```plaintext
UserApi
```

定义：

```java
@RequestMapping
@GetMapping
```

---

## 然后：

```plaintext
UserRemoteApi
```

定义：

```java
@HttpExchange
```

---

# 十八、最终结论（直接说）

## 你之前那个：

```java
Controller implements UserApi
```

是可以正常本地测试的。

但：

> ❗ 真正企业级、更稳定、更清晰的方案：

应该是：

```plaintext
Controller API
+
Remote API
```

分离。
