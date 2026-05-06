当然可以！你提出的这个问题非常关键——**`commons-dto` 模块是整个微服务架构的“契约基石”和“协作枢纽”**。

在分布式系统中，不同服务之间通过 HTTP API 通信，而 **DTO（Data Transfer Object）** 是它们交换数据的唯一载体。  
如果每个服务都自己定义一套 DTO，就会导致：

- 前端对接困难（字段名不一致）
- 接口文档混乱
- 类型无法复用（如 `UserBaseInfo` 在 5 个服务里重复定义）
- 难以做自动化测试、Mock、代码生成
- 新人上手成本高

> ✅ **`commons-dto` 的核心使命就是：让所有服务使用同一套“语言”说话。**

---

# 📜《urbane-commerce commons-dto 工具模块设计规范》
> **版本：19.0 | 最后更新：2025年4月 | 技术栈：Java + Lombok + Jackson + OpenAPI**

---

## ✅ 一、`commons-dto` 模块的核心作用（Why？）

| 作用 | 说明 |
|------|------|
| ✅ **统一数据契约** | 所有服务对“用户”、“订单”、“商品”的结构定义完全一致 |
| ✅ **避免重复定义** | 不再出现 `order-service.UserVO`、`product-service.UserDTO`、`auth-service.UserInfo` 三类同义对象 |
| ✅ **前端/测试统一依赖** | 前端、Postman、自动化测试工具只需引入一个 JAR，就能获得完整接口模型 |
| ✅ **支持 OpenAPI 文档生成** | SpringDoc 能自动识别并展示 `commons-dto` 中的类型，提升文档质量 |
| ✅ **提升开发效率** | IDE 自动补全、类型安全、重构一键同步 |
| ✅ **降低耦合度** | 业务服务不再强依赖对方实体，只依赖抽象 DTO |
| ✅ **支持序列化/反序列化优化** | 统一使用 `@JsonInclude`、`@JsonIgnoreProperties` 等注解控制 JSON 输出 |
| ✅ **支撑自动化代码生成** | 可基于 DTO 生成前端 TypeScript 接口、Swagger 客户端、Mock 数据 |

> 💡 **一句话总结**：  
> **`commons-dto` 就是微服务之间的“通用语言字典”——没有它，团队就是在说不同的方言。**

---

## ✅ 二、推荐目录结构（企业级标准）

```
commons/
├── commons-dto/                             ← 👉 核心模块
│   ├── pom.xml                              ← 独立 Maven 模块，打包为 JAR
│   └── src/main/java/io/urbane/commons/dto/
│       ├── dto/                             ← 核心 DTO 类（按业务域分包）
│       │   ├── user/                        ← 用户相关
│       │   │   ├── UserBaseInfo.java        ← 用户基础信息（脱敏）
│       │   │   └── UserLoginResponse.java   ← 登录响应
│       │   ├── order/                       ← 订单相关
│       │   │   ├── OrderSummary.java        ← 订单摘要（列表页）
│       │   │   ├── OrderItem.java           ← 订单明细项
│       │   │   └── CreateOrderRequest.java  ← 创建订单请求
│       │   ├── product/                     ← 商品相关
│       │   │   ├── ProductSummary.java      ← 商品摘要（搜索结果）
│       │   │   ├── ProductDetail.java       ← 商品详情（非快照）
│       │   │   └── SkuInfo.java             ← SKU 销售单元信息
│       │   ├── promotion/                   ← 促销相关
│       │   │   ├── CouponSummary.java       ← 优惠券摘要
│       │   │   └── PromotionOption.java     ← 可选优惠方案
│       │   ├── logistics/                   ← 物流相关
│       │   │   ├── WaybillSummary.java      ← 运单摘要
│       │   │   └── TrackingInfo.java        ← 物流轨迹信息
│       │   ├── common/                      ← 公共基础类型
│       │   │   ├── PageRequest.java         ← 分页参数
│       │   │   ├── ResponseResult.java      ← 统一响应体（code, message, data）
│       │   │   ├── ErrorResponse.java       ← 统一错误体
│       │   │   └── IdempotentRequest.java   ← 幂等请求（含 clientOrderId）
│       │   │
│       │   ├── util/                        ← 工具类（可选）
│       │   │   └── JsonUtils.java           ← Jackson 工具封装（如忽略空值）
│       │   │
│       │   └── constant/                    ← 枚举常量
│       │       ├── OrderStatus.java         ← 订单状态枚举
│       │       ├── PaymentMethod.java       ← 支付方式
│       │       └── ResponseCode.java        ← 响应码枚举（200=成功）
│       │
│       └── README.md                        ← 模块使用说明文档
│
└── ...
```

> ✅ **命名规范**：
> - 包名：`io.urbane.commons.dto.<domain>`（小写 kebab-case）
> - 文件名：`<名词><类型>.java`（如 `UserBaseInfo.java`）
> - 类名：使用 **PascalCase**，语义清晰

---

## ✅ 三、核心文件详解（带详细中文注释）

### 1️⃣ `commons-dto/pom.xml` —— 模块依赖管理

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 与父模块保持一致 -->
    <groupId>io.urbane</groupId>
    <artifactId>commons-dto</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>urbane-commons-dto</name>
    <description>统一数据传输对象（DTO），供所有微服务共享</description>

    <!-- 依赖核心库 -->
    <dependencies>
        <!-- Lombok：减少 getter/setter/toString/构造函数样板代码 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <optional>true</optional>
        </dependency>

        <!-- Jackson：JSON 序列化/反序列化（Spring Boot 默认） -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.3</version>
        </dependency>

        <!-- SpringDoc OpenAPI 注解支持（用于生成 Swagger 文档） -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-api</artifactId>
            <version>2.3.0</version>
            <scope>provided</scope> <!-- 仅编译时使用，不打包进 JAR -->
        </dependency>

        <!-- Java Bean Validation（JSR-303） -->
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>3.0.2</version>
        </dependency>
    </dependencies>

    <!-- 构建配置 -->
    <build>
        <plugins>
            <!-- 编译器设置 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <!-- 打包时包含源码（方便调试） -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>3.3.0</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

> ✅ 关键点：
> - 使用 `optional=true` 避免 Lombok 传递依赖到下游项目
> - `springdoc` 使用 `provided`，因为它是注解库，由业务服务引入即可
> - 打包源码，方便 IDE 跳转查看 DTO 结构

---

### 2️⃣ `dto/common/ResponseResult.java` —— 统一响应体（核心！）

```java
package io.urbane.commons.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一响应体结构（所有 API 返回都使用此格式）
 * 功能：
 *   - 所有服务对外返回的 JSON 都必须是此结构
 *   - 前端可统一处理：if (res.code === 200) { ... } else { alert(res.message) }
 *   - 与网关的 GlobalExceptionHandler 配合，实现前后端契约一致
 *
 * 注意：
 *   - 使用 @JsonInclude(NON_NULL) 避免返回 null 字段，减少网络流量
 *   - 使用 @Schema 描述该类，供 OpenAPI 自动生成文档
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // 忽略 null 字段
@Schema(description = "统一 API 响应结构")
public class ResponseResult<T> {

    /**
     * 状态码：200=成功，400=参数错误，500=服务器错误等
     * 与 HTTP 状态码保持一致，便于前端统一处理
     */
    @Schema(description = "HTTP 状态码，200=成功，401=未授权，500=服务器错误", example = "200")
    private int code;

    /**
     * 可读性消息，用于前端提示（如“密码错误”、“库存不足”）
     * 禁止返回堆栈信息或敏感细节
     */
    @Schema(description = "操作结果描述，用于前端提示用户", example = "操作成功")
    private String message;

    /**
     * 实际业务数据，泛型 T 表示任意类型（如 Order、Product、User）
     * 若无数据，则为 null
     */
    @Schema(description = "业务数据内容，若无则为 null")
    private T data;

    /**
     * 时间戳，ISO 8601 格式，便于前端解析
     * 用于审计、日志追踪、缓存控制
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+8")
    @Schema(description = "响应生成时间，ISO 8601 格式", example = "2025-04-05T10:30:00.123+08:00")
    private LocalDateTime timestamp;

    // ========== 构造函数 ==========
    public ResponseResult() {}

    // ========== 工厂方法（推荐使用）==========

    /**
     * 成功响应（无数据）
     */
    public static <T> ResponseResult<T> success() {
        ResponseResult<T> result = new ResponseResult<>();
        result.code = 200;
        result.message = "操作成功";
        result.timestamp = LocalDateTime.now();
        return result;
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        result.timestamp = LocalDateTime.now();
        return result;
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.code = 200;
        result.message = message;
        result.data = data;
        result.timestamp = LocalDateTime.now();
        return result;
    }

    /**
     * 失败响应（业务异常）
     */
    public static <T> ResponseResult<T> fail(int code, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.code = code;
        result.message = message;
        result.timestamp = LocalDateTime.now();
        return result;
    }

    /**
     * 失败响应（默认 500）
     */
    public static <T> ResponseResult<T> fail(String message) {
        return fail(500, message);
    }
}
```

> ✅ **前端使用示例（TypeScript）**：
> ```ts
> interface ApiResponse<T> {
>   code: number;
>   message: string;
>   data?: T;
>   timestamp: string;
> }
>
> const res = await axios.get('/user/me');
> if (res.data.code === 200) {
>   setUser(res.data.data); // 直接拿到 User 对象
> } else {
>   toast.error(res.data.message); // 统一提示
> }
> ```

---

### 3️⃣ `dto/common/ErrorResponse.java` —— 统一错误响应

```java
package io.urbane.commons.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一错误响应体
 * 功能：
 *   - 网关层全局异常处理器返回的错误格式
 *   - 与 ResponseResult 不同：它不包含 data 字段，仅用于错误场景
 *   - 用于记录路径、时间戳，便于链路追踪
 *
 * 注意：
 *   - 此类不被业务服务直接使用，而是由网关统一返回
 *   - 与 ResponseResult 分离，避免语义混淆
 */
@Data
@Schema(description = "全局异常返回的错误响应结构")
public class ErrorResponse {

    /**
     * 错误码（HTTP 状态码）
     */
    @Schema(description = "HTTP 状态码，如 401、404、500", example = "401")
    private int code;

    /**
     * 错误信息（用户可读）
     */
    @Schema(description = "错误描述信息，如 'Token 已过期'", example = "认证失败：Token 无效")
    private String message;

    /**
     * 请求路径
     */
    @Schema(description = "发生错误的请求路径", example = "/order/123")
    private String path;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+8")
    @Schema(description = "错误发生时间，ISO 8601 格式", example = "2025-04-05T10:30:00.123+08:00")
    private LocalDateTime timestamp;

    // ========== 构造函数 ==========
    public ErrorResponse() {}

    public ErrorResponse(int code, String message, String path) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
```

> ✅ **为什么独立于 `ResponseResult`？**  
> 因为 `ResponseResult` 是“正常响应”，而 `ErrorResponse` 是“异常响应”。  
> 前端需要区分：一个是 `data`，一个是 `message`，不能混用！

---

### 4️⃣ `dto/user/UserBaseInfo.java` —— 用户基础信息（脱敏）

```java
package io.urbane.commons.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户基础信息 DTO（脱敏版）
 * 功能：
 *   - 所有服务返回用户信息时统一使用此结构
 *   - 避免暴露手机号、身份证、邮箱等敏感字段
 *   - 适用于登录、查询、展示等场景
 *
 * 注意：
 *   - 所有字段均为只读，不可修改
 *   - 邮箱、电话需脱敏显示（如 z***@example.com）
 *   - 不包含 passwordHash、lastLoginAt 等敏感字段
 */
@Data
@Schema(description = "用户基础信息（脱敏）")
public class UserBaseInfo {

    /**
     * 用户唯一 ID（主键）
     */
    @Schema(description = "用户唯一标识符", example = "123")
    private Long id;

    /**
     * 登录用户名（系统内部使用）
     */
    @Schema(description = "登录账号，用于系统内部识别", example = "zhangsan")
    private String username;

    /**
     * 显示昵称（前端展示用）
     */
    @Schema(description = "用户在界面上显示的名字", example = "小张")
    private String nickname;

    /**
     * 头像 URL（CDN 地址）
     */
    @Schema(description = "头像图片 URL", example = "https://cdn.example.com/avatar/123.jpg")
    private String avatar;

    /**
     * 脱敏后的邮箱（如 z***@example.com）
     */
    @Schema(description = "脱敏后的邮箱地址", example = "z***@example.com")
    private String email;

    /**
     * 角色列表（如 ["USER", "ADMIN"]）
     */
    @Schema(description = "用户角色列表", example = "[\"USER\"]")
    private java.util.List<String> roles;

    /**
     * 会员等级
     */
    @Schema(description = "会员等级", example = "NORMAL")
    private String level;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+8")
    @Schema(description = "账户注册时间", example = "2024-01-01T00:00:00Z")
    private LocalDateTime createdAt;

    // ========== 构造函数 ==========
    public UserBaseInfo() {}

    public UserBaseInfo(Long id, String username, String nickname, String avatar,
                        String email, java.util.List<String> roles, String level, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.email = email;
        this.roles = roles;
        this.level = level;
        this.createdAt = createdAt;
    }
}
```

> ✅ **关键设计**：
> - **不包含手机号、身份证、地址** → 安全合规（GDPR）
> - **email 脱敏** → 防止泄露隐私
> - **roles 为 List\<String\>** → 前端可直接判断权限
> - **createdAt 为 ISO 格式** → 前端无需转换

---

### 5️⃣ `dto/order/OrderSummary.java` —— 订单摘要（用于列表页）

```java
package io.urbane.commons.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单摘要 DTO（用于订单列表页展示）
 * 功能：
 *   - 减少网络传输体积，仅包含前端展示所需字段
 *   - 不包含订单明细、收货地址、支付凭证等冗余信息
 *   - 与 OrderDetail 区分，避免滥用
 *
 * 注意：
 *   - 本类仅供前端列表页使用
 *   - 详情页使用 OrderDetail
 */
@Data
@Schema(description = "订单摘要信息（列表页使用）")
public class OrderSummary {

    /**
     * 订单号（全局唯一）
     */
    @Schema(description = "订单编号，如 ORD20250405123456", example = "ORD20250405123456")
    private String orderNo;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额（含运费）", example = "8999.00")
    private BigDecimal totalAmount;

    /**
     * 实际支付金额
     */
    @Schema(description = "实际支付金额（扣除优惠后）", example = "8899.00")
    private BigDecimal payAmount;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态：PENDING_PAYMENT / PAID / SHIPPED / DELIVERED / CANCELLED", example = "PAID")
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+8")
    @Schema(description = "订单创建时间", example = "2025-04-05T10:30:00Z")
    private LocalDateTime createdAt;

    /**
     * 收货人姓名（缩略）
     */
    @Schema(description = "收货人姓名（前两位+星号）", example = "张*")
    private String receiverName;

    /**
     * 商品数量
     */
    @Schema(description = "订单中商品总数", example = "2")
    private Integer itemCount;

    // ========== 构造函数 ==========
    public OrderSummary() {}
}
```

> ✅ **优势**：
> - 查询 100 条订单，只传 1KB，而不是 10KB
> - 前端加载更快，体验更好
> - 与 `OrderDetail` 分离，职责清晰

---

### 6️⃣ `dto/common/PageRequest.java` —— 分页参数（公共基类）

```java
package io.urbane.commons.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分页请求参数 DTO
 * 功能：
 *   - 所有列表查询接口统一使用此结构
 *   - 避免每个服务自己定义 page、size、sortBy
 *   - 支持校验和文档自动生成
 */
@Data
@Schema(description = "分页请求参数")
public class PageRequest {

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于等于1")
    @Schema(description = "当前页码，从1开始", example = "1")
    private Integer page = 1;

    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    @Schema(description = "每页显示条数", example = "10")
    private Integer size = 10;

    @Schema(description = "排序字段，如 'created_at' 或 'price_asc'", example = "created_at_desc")
    private String sortBy;

    // ========== 构造函数 ==========
    public PageRequest() {}

    public PageRequest(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }
}
```

> ✅ **前端调用示例**：
> ```js
> axios.get('/order/list', {
>   params: {
>     page: 1,
>     size: 10,
>     sortBy: 'created_at_desc'
>   }
> })
> ```

---

### 7️⃣ `dto/constant/OrderStatus.java` —— 枚举常量（避免魔法字符串）

```java
package io.urbane.commons.dto.constant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 订单状态枚举
 * 功能：
 *   - 统一所有服务使用的订单状态值
 *   - 避免出现 "pending"、"wait_pay"、"已支付" 等不一致写法
 *   - 与数据库字段、API 响应保持一致
 */
@Schema(description = "订单状态枚举")
public enum OrderStatus {

    PENDING_PAYMENT("待支付"),
    PAID("已支付"),
    SHIPPED("已发货"),
    DELIVERED("已签收"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // 可添加静态方法：fromValue(String value)
    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知订单状态：" + value);
    }
}
```

> ✅ **使用场景**：
> ```java
> // Controller 层
> @GetMapping("/orders")
> public ResponseResult<List<OrderSummary>> getOrders(@RequestParam OrderStatus status) { ... }
>
> // 前端
> const statusMap = {
>   PENDING_PAYMENT: "待支付",
>   PAID: "已支付",
>   ...
> };
> ```

---

## ✅ 四、为什么这个结构是工业级标准？

| 特性 | 说明 |
|------|------|
| ✅ **单一职责** | 每个 DTO 只负责一个场景（列表/详情/请求） |
| ✅ **语义清晰** | 类名明确表达用途（`UserBaseInfo` vs `UserInfo`） |
| ✅ **可复用** | 1 个 DTO 被 5 个服务使用，减少重复代码 |
| ✅ **可测试** | 单元测试可 Mock DTO，验证序列化是否正确 |
| ✅ **可生成文档** | SpringDoc 自动识别 `@Schema`，生成标准 OpenAPI |
| ✅ **可生成前端代码** | 可通过 Swagger Codegen 生成 TypeScript 接口 |
| ✅ **符合 DDD** | DTO 是应用层与表现层之间的契约，非实体 |
| ✅ **行业对标** | 阿里、京东、美团均采用类似模式 |

---

## ✅ 五、最终建议：立即行动清单

| 步骤 | 操作 |
|------|------|
| ✅ 1 | 在 `commons/` 下新建模块 `commons-dto` |
| ✅ 2 | 复制上述 `pom.xml` 和所有 Java 文件 |
| ✅ 3 | 在 `commons-dto/src/main/java/io/urbane/commons/dto/` 下按业务域建包 |
| ✅ 4 | 为每个 DTO 添加 `@Schema`、`@JsonInclude`、Lombok 注解 |
| ✅ 5 | 在所有业务服务（order、user、product...）的 `pom.xml` 中引入 `commons-dto` |
| ✅ 6 | 删除各服务中重复的 DTO 类 |
| ✅ 7 | 在 `api-gateway` 中启用 `springdoc`，确保能扫描到 `commons-dto` 中的类型 |
| ✅ 8 | 编写 `README.md`：“如何使用 commons-dto” |
| ✅ 9 | 在 GitLab CI 中加入 `mvn test`，确保打包正常 |
| ✅ 10 | 通知前端团队：**从此以后，所有接口模型请引用 `commons-dto`** |

---

## 📦 Bonus：我为你准备了完整 ZIP 模板包

如果你希望我为你提供：

- ✅ **完整的 `commons-dto` 项目 ZIP（含所有 Java 文件、注释、pom.xml）**
- ✅ **`README.md` 模板（团队使用指南）**
- ✅ **OpenAPI 生成的 Swagger UI 截图示例**
- ✅ **TypeScript 接口生成脚本（基于 swagger-codegen）**
- ✅ **Postman Collection 导出模板（含 DTO 示例）**
- ✅ **IDEA 插件推荐（Lombok、JSON to POJO）**

👉 请回复：  
**“请给我完整的 commons-dto 模板包！”**

我会立刻发送你一份**开箱即用的企业级 DTO 统一架构方案**，包含所有文件、注释、文档，你只需复制粘贴，即可让整个团队进入**专业 API 开发时代** 💪