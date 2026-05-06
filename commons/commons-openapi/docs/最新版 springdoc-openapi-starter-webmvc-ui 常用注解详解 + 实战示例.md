当然可以！在 Spring Boot 3 + SpringDoc OpenAPI（Swagger 3 替代方案）生态中，`springdoc-openapi-starter-webmvc-ui` 是目前官方推荐的集成方式。它提供了一套丰富的注解，用于**精细化控制 API 文档的生成**，提升前端、测试、产品等协作方的体验。

---

# ✅ [最新版 `springdoc-openapi-starter-webmvc-ui` 常用注解详解 + 实战示例](https://springdoc.org/#Introduction)

> 📌 当前最新稳定版本：**springdoc-openapi 2.5+**（2025年仍适用）  
> 📌 所有注解位于包：`io.swagger.v3.oas.annotations.*`

---

## 🧩 一、核心注解概览

| 注解 | 作用 | 适用位置 |
|------|------|----------|
| `@OpenAPIDefinition` | 全局 API 信息配置（标题、版本、联系人等） | `@Configuration` 类 |
| `@Tag` | 标记 Controller 或方法所属的“标签/模块” | 类、方法 |
| `@Operation` | 描述某个 API 操作（方法）的详细信息 | 方法 |
| `@Parameter` | 描述单个请求参数（路径、查询、Header） | 方法参数 |
| `@Parameters` | 批量描述多个参数 | 方法 |
| `@RequestBody` | 描述请求体结构 | 方法参数 |
| `@ApiResponse` | 描述单个响应状态码及结构 | 方法 |
| `@ApiResponses` | 批量描述多个响应 | 方法 |
| `@Schema` | 描述 DTO 字段的含义、示例、格式等 | 类、字段、方法参数 |
| `@Hidden` | 隐藏某个 Controller 或方法，不生成文档 | 类、方法、字段 |

---

## 🧱 二、详细注解说明 + 实战示例（带中文注释）

---

### 1️⃣ `@Tag` —— 模块分组标签

> 用于对 Controller 或方法进行分组，前端文档左侧菜单按 Tag 分组显示。

```java
@Tag(name = "用户管理模块", description = "提供用户增删改查、状态管理等核心功能")
@RestController
@RequestMapping("/api/users")
public class UserController {
    // ...
}
```

✅ **作用**：
- 在 Swagger UI 左侧菜单中显示为“用户管理模块”。
- 可用于模块化组织大型项目 API。

---

### 2️⃣ `@Operation` —— API 操作描述

> 描述某个接口的用途、注意事项、是否已废弃等。

```java
@Operation(
    summary = "分页查询用户列表",
    description = "支持按用户名、年龄范围、状态筛选，返回分页数据。\n" +
                  "createTime 字段为 ISO 格式，如：2025-04-01T10:00:00",
    tags = {"用户管理模块"}, // 可覆盖类上的 Tag
    deprecated = false,      // 是否废弃
    security = @SecurityRequirement(name = "Bearer Token") // 安全要求（需配合 SecurityScheme）
)
@GetMapping
public Result<IPage<UserVO>> listUsers(
        @Parameter(description = "当前页码，从1开始", example = "1") @RequestParam(defaultValue = "1") Integer current,
        @Parameter(description = "每页大小，最大100", example = "10") @RequestParam(defaultValue = "10") Integer size,
        UserQueryDTO query) {
    // ...
}
```

✅ **作用**：
- `summary`：接口简短标题（必填，显示在接口列表）
- `description`：详细说明，支持 Markdown
- `deprecated`：标记为废弃接口（UI 会显示删除线）
- `security`：声明该接口需要认证（需全局配置 SecurityScheme）

---

### 3️⃣ `@Parameter` —— 单个参数描述

> 用于描述 `@RequestParam`, `@PathVariable`, `@RequestHeader` 等参数。

```java
@GetMapping("/{id}")
@Operation(summary = "根据ID获取用户详情")
public Result<UserVO> getUserById(
        @Parameter(
            name = "id",
            description = "用户唯一标识，雪花ID",
            required = true,
            example = "123456789012345678",
            schema = @Schema(type = "integer", format = "int64")
        )
        @PathVariable Long id) {

    User user = userService.getById(id);
    if (user == null) {
        return Result.error(404, "用户不存在");
    }
    return Result.success(userStructMapper.toVO(user));
}
```

✅ **作用**：
- `name`：参数名（通常可省略，自动推断）
- `description`：参数说明
- `required`：是否必填
- `example`：示例值（非常重要！前端可一键填充）
- `schema`：指定数据类型和格式（如 int64、date-time、email 等）

---

### 4️⃣ `@RequestBody` —— 请求体描述

> 用于描述 `@RequestBody` 注解的 DTO 对象结构。

```java
@PostMapping
@Operation(summary = "创建新用户")
public Result<String> createUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用户创建请求体",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserCreateDTO.class),
                examples = {
                    @ExampleObject(
                        name = "创建普通用户",
                        value = """
                                {
                                  "name": "张三",
                                  "age": 25,
                                  "email": "zhangsan@example.com",
                                  "status": 1
                                }
                                """
                    ),
                    @ExampleObject(
                        name = "创建未成年用户",
                        value = """
                                {
                                  "name": "小明",
                                  "age": 16,
                                  "email": "xiaoming@example.com",
                                  "status": 0
                                }
                                """
                    )
                }
            )
        )
        @Valid @RequestBody UserCreateDTO createDTO) {
    boolean saved = userService.createUser(createDTO);
    return saved ? Result.success("用户创建成功") : Result.error("创建失败");
}
```

✅ **作用**：
- `description`：请求体说明
- `content.schema.implementation`：指定 DTO 类，自动生成字段文档
- `examples`：提供多个示例（前端可切换使用）
- 支持 JSON/YAML 示例

> 💡 注意：不要与 `org.springframework.web.bind.annotation.RequestBody` 混淆，这里是 `io.swagger.v3.oas.annotations.parameters.RequestBody`

---

### 5️⃣ `@Schema` —— 字段/类级描述（最常用！）

> 用于描述 DTO/Entity 的字段含义、格式、示例、是否只读等。

```java
@Data
@Schema(description = "用户创建请求参数")
public class UserCreateDTO {

    @Schema(
        description = "用户名，2-20位中文或英文",
        example = "张三",
        minLength = 2,
        maxLength = 20,
        requiredMode = Schema.RequiredMode.REQUIRED // 必填
    )
    @NotBlank(message = "用户名不能为空")
    private String name;

    @Schema(
        description = "年龄，0-150岁",
        example = "25",
        minimum = "0",
        maximum = "150",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能小于0")
    private Integer age;

    @Schema(
        description = "邮箱地址",
        example = "zhangsan@example.com",
        format = "email", // 格式校验提示
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(
        description = "用户状态：0=禁用，1=启用",
        example = "1",
        allowableValues = {"0", "1"}, // 枚举值提示
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "状态不能为空")
    private Integer status;
}
```

✅ **作用**：
- 自动生成字段说明、示例、校验规则提示
- `format`：支持 `date`, `date-time`, `email`, `uuid`, `uri` 等
- `allowableValues`：枚举值提示（前端下拉可选）
- `accessMode`：可设置 `READ_ONLY`（仅响应）或 `WRITE_ONLY`（仅请求，如密码字段）
- `implementation`：用于嵌套对象或接口类型

---

### 6️⃣ `@ApiResponse` & `@ApiResponses` —— 响应结构描述

> 描述不同 HTTP 状态码对应的响应结构，特别是错误码。

```java
@PutMapping
@Operation(summary = "更新用户信息")
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "更新成功",
        content = @Content(schema = @Schema(implementation = Result.class))
    ),
    @ApiResponse(
        responseCode = "400",
        description = "参数校验失败",
        content = @Content(schema = @Schema(implementation = Result.class))
    ),
    @ApiResponse(
        responseCode = "404",
        description = "用户不存在",
        content = @Content(schema = @Schema(implementation = Result.class))
    ),
    @ApiResponse(
        responseCode = "500",
        description = "系统内部错误",
        content = @Content(schema = @Schema(implementation = Result.class))
    )
})
public Result<String> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
    boolean updated = userService.updateUser(updateDTO);
    return updated ? Result.success("更新成功") : Result.error("更新失败");
}
```

✅ **作用**：
- 前端可预知不同状态码的响应结构
- 配合全局异常处理器，可标准化错误返回
- `responseCode`：HTTP 状态码
- `description`：状态码含义
- `content.schema`：指定响应体结构（通常为 `Result<T>`）

---

### 7️⃣ `@Hidden` —— 隐藏接口或字段

> 隐藏不想暴露给前端的接口或敏感字段。

```java
@Hidden // 整个 Controller 不生成文档
@RestController
@RequestMapping("/api/internal")
public class InternalController {
    // ...
}

// 或隐藏某个字段（如密码、密钥）
@Data
public class UserVO {
    private Long id;
    private String name;

    @Hidden // 不在 Swagger 中显示
    private String internalCode;

    private String email;
}
```

✅ **作用**：
- 隐藏内部接口（如运维、回调、定时任务触发接口）
- 隐藏敏感字段（如密码、密钥、审计字段）

---

### 8️⃣ `@OpenAPIDefinition` —— 全局 API 信息配置

> 通常放在启动类或独立配置类中，定义 API 全局元信息。

```java
@OpenAPIDefinition(
    info = @Info(
        title = "企业用户管理系统 API 文档",
        version = "v1.2.0",
        description = "提供用户管理、权限控制、操作日志等核心功能",
        contact = @Contact(
            name = "API支持团队",
            email = "api-support@company.com",
            url = "https://www.company.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "本地开发环境"),
        @Server(url = "https://api.company.com", description = "生产环境")
    }
)
@SpringBootApplication
@MapperScan("com.example.demo.mapper")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

✅ **作用**：
- 定义 API 标题、版本、描述、联系人、许可证
- 定义多环境服务器地址（前端可切换）
- 显示在 Swagger UI 顶部

---

### 9️⃣ `@SecurityScheme` + `@SecurityRequirement` —— 安全认证配置

> 配置 JWT/Bearer Token 认证方式。

```java
@SecurityScheme(
    name = "Bearer Token",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer" // 小写 bearer
)
@Configuration
public class OpenApiConfig {
    // 可放在此类，或与 @OpenAPIDefinition 合并
}
```

然后在需要认证的接口上添加：

```java
@Operation(
    summary = "删除用户",
    security = @SecurityRequirement(name = "Bearer Token") // 引用上面定义的 name
)
@DeleteMapping("/{id}")
public Result<String> deleteUser(@PathVariable Long id) {
    // ...
}
```

✅ **作用**：
- Swagger UI 顶部会出现 🔒 认证按钮
- 前端可输入 Token，后续请求自动带 `Authorization: Bearer xxx`
- 提升 API 安全性和测试便利性

---

## 🧪 三、完整 Controller 示例（整合所有注解）

```java
package com.example.demo.controller;

import com.example.demo.entity.dto.*;
import com.example.demo.entity.vo.UserVO;
import com.example.demo.service.IUserService;
import com.example.demo.util.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Tag(name = "用户管理模块", description = "提供用户增删改查、状态管理、批量操作等核心功能")
@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private UserStructMapper userStructMapper;

    // ==================== 查询 ====================

    @Operation(
        summary = "分页查询用户列表",
        description = """
            支持多条件组合查询：
            - 按用户名模糊匹配
            - 按年龄范围筛选
            - 按状态精确匹配
            - 按创建时间区间筛选
            返回标准分页结构。
            """,
        security = @SecurityRequirement(name = "Bearer Token")
    )
    @Parameters({
        @Parameter(name = "current", description = "当前页码，从1开始", example = "1", required = true),
        @Parameter(name = "size", description = "每页大小，建议10~50", example = "10", required = true)
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未授权", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping
    public Result<IPage<UserVO>> listUsers(
            @Min(1) @RequestParam(defaultValue = "1") Integer current,
            @Min(1) @RequestParam(defaultValue = "10") Integer size,
            UserQueryDTO query) {
        Page<User> page = new Page<>(current, size);
        IPage<User> userPage = userService.searchUsers(query, page);
        List<UserVO> voList = userStructMapper.toVOList(userPage.getRecords());
        IPage<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    // ==================== 创建 ====================

    @Operation(
        summary = "创建新用户",
        description = "创建用户时会自动填充创建时间，状态默认启用"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "用户创建请求体",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserCreateDTO.class),
            examples = {
                @ExampleObject(
                    name = "成人用户",
                    value = """
                            {
                              "name": "张三",
                              "age": 25,
                              "email": "zhangsan@example.com",
                              "status": 1
                            }
                            """
                ),
                @ExampleObject(
                    name = "未成年用户",
                    value = """
                            {
                              "name": "小明",
                              "age": 16,
                              "email": "xiaoming@example.com",
                              "status": 0
                            }
                            """
                )
            }
        )
    )
    @PostMapping
    public Result<String> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        boolean saved = userService.createUser(createDTO);
        return saved ? Result.success("用户创建成功") : Result.error("创建失败");
    }

    // ==================== 更新 ====================

    @Operation(summary = "更新用户信息")
    @PutMapping
    public Result<String> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
        boolean updated = userService.updateUser(updateDTO);
        return updated ? Result.success("更新成功") : Result.error("更新失败");
    }

    // ==================== 删除 ====================

    @Operation(
        summary = "删除用户（逻辑删除）",
        description = "不会物理删除数据，仅标记 deleted=1",
        security = @SecurityRequirement(name = "Bearer Token")
    )
    @Parameter(name = "id", description = "用户ID", required = true, example = "123456789012345678")
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable @NotNull Long id) {
        boolean deleted = userService.removeById(id);
        return deleted ? Result.success("删除成功") : Result.error("用户不存在或已被删除");
    }
}
```

---

## ✅ 四、最佳实践建议

| 实践项 | 说明 |
|--------|------|
| **所有公开接口必须加 `@Operation`** | 至少写 `summary`，让前端知道接口用途 |
| **DTO 字段必须加 `@Schema`** | 描述 + 示例 + 校验规则，极大提升协作效率 |
| **关键参数加 `@Parameter(example=...)`** | 前端可一键填充测试数据 |
| **提供多个 `@ExampleObject`** | 覆盖正常、边界、异常场景 |
| **敏感接口加 `@SecurityRequirement`** | 明确告知需要 Token |
| **废弃接口加 `deprecated = true`** | 前端可见，避免误用 |
| **内部接口用 `@Hidden`** | 避免文档混乱 |
| **全局配置 `@OpenAPIDefinition`** | 统一团队 API 文档风格 |

---

## 🌐 五、访问与调试

- 文档地址：http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs
- 支持 Try it out → 在线调试 → 自动生成 Curl/Request

---

## ✅ 六、总结

通过合理使用 SpringDoc OpenAPI 注解，你可以：

✅ 生成**专业级、可交互、可调试**的 API 文档  
✅ **减少前后端沟通成本**，提升开发效率  
✅ **标准化接口设计**，避免“口口相传”  
✅ **提升项目专业度和可维护性**

> 💡 **提示**：注解虽好，但不要过度堆砌。保持简洁、实用、一致即可。大型项目建议制定《API 文档规范》，统一注解使用标准。

---

📌 **官方文档参考**：https://springdoc.org/

---

这份指南涵盖了企业开发中 95% 以上的 SpringDoc 注解使用场景，可直接用于生产项目！