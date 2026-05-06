# ✅ Knife4j 详解 —— 企业级 Swagger 增强 UI 工具

---

## 一、Knife4j 是什么？

**Knife4j** 是一个基于 **Swagger/OpenAPI 3** 的**增强型 API 文档 UI 工具**，它在 SpringDoc OpenAPI（Swagger 3）的基础上，提供了：

- ✅ 更美观、更现代化的 UI 界面
- ✅ 更强大的调试功能（如参数缓存、离线文档、导出 Markdown/HTML）
- ✅ 更好的中文支持和企业级功能（如生产环境关闭、权限控制、分组授权）
- ✅ 支持微服务网关聚合文档（Spring Cloud Gateway / Zuul）
- ✅ 支持 OpenAPI 3.0 规范（完全兼容 SpringDoc）

> 🔹 项目地址：https://github.com/xiaoymin/knife4j
> 🔹 官网文档：https://doc.xiaominfo.com/

---

## 二、Knife4j 有什么作用？

| 功能 | 说明 |
|------|------|
| 🎨 **增强 UI 界面** | 比原生 Swagger UI 更美观、布局更合理、操作更便捷 |
| 🧪 **增强调试能力** | 支持参数缓存、历史请求、导出请求、Mock 数据 |
| 📄 **文档导出** | 支持导出 HTML、Markdown、Word、OpenAPI JSON/YAML |
| 🔐 **权限控制** | 支持文档访问权限、生产环境关闭、分组鉴权 |
| 🌐 **微服务聚合** | 支持 Spring Cloud Gateway 聚合多个服务的 API 文档 |
| 🌍 **多语言支持** | 完善的中文支持，适合国内团队 |
| 🧩 **插件扩展** | 支持自定义插件、主题、菜单等 |

---

## 三、Knife4j 和 SpringDoc OpenAPI 有什么区别？

| 对比项 | SpringDoc OpenAPI | Knife4j |
|--------|-------------------|---------|
| 核心功能 | 提供 OpenAPI 3 规范实现 + 基础 UI | 在 SpringDoc 基础上提供**增强 UI + 企业功能** |
| UI 界面 | 原生 Swagger UI（简约但功能基础） | 现代化 UI，左侧菜单分组清晰，支持暗黑模式 |
| 调试功能 | 基础 Try it out | 支持参数缓存、历史记录、导出请求、Mock |
| 文档导出 | 无内置导出功能 | 支持导出 HTML/Markdown/Word/OpenAPI |
| 权限控制 | 需自行扩展 | 内置 enable、basicAuth、分组鉴权等 |
| 微服务支持 | 需手动聚合 | 提供 `knife4j-gateway` 自动聚合网关下游服务 |
| 中文支持 | 一般 | 完善，界面、注解、提示均为中文友好 |
| 适用场景 | 快速集成、轻量项目 | 企业级项目、中大型团队、需文档管理 |

> ✅ **简单说：SpringDoc 是“引擎”，Knife4j 是“豪华仪表盘”**  
> ✅ **两者不是替代关系，而是增强关系 —— Knife4j 依赖 SpringDoc**

---

## 四、如何在 Spring Boot 3 项目中使用 Knife4j？

### ✅ 步骤 1：添加 Maven 依赖

```xml
<!-- Knife4j 增强 UI（核心）-->
<dependency>
<groupId>com.github.xiaoymin</groupId>
<artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
<version>4.5.0</version>
</dependency>
```

> ⚠️ 注意：`knife4j-openapi3-ui` 适用于 OpenAPI 3（Spring Boot 3），不要使用旧版 `knife4j-spring-ui`。

---

### ✅ 步骤 2：配置 application.yml

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html  # 保留原生路径（可选）
  api-docs:
    path: /v3/api-docs

# Knife4j 专属配置
knife4j:
  enable: true              # 是否启用 Knife4j（生产环境可设为 false）
  basic-auth:
    enable: false           # 是否开启基础认证（账号密码访问）
    username: admin         # 认证用户名
    password: 123456        # 认证密码
  setting:
    language: zh-CN         # 界面语言（中文）
    cache: true             # 是否缓存参数
    enableFooter: false     # 是否显示底部
    enableFooterCustom: false
    footerCustomContent: "企业 API 文档系统"
    enableSearch: true      # 是否启用搜索
    enableOpenApi: true     # 是否显示 OpenAPI 规范按钮
    enableSwaggerModels: true # 是否显示 Schemas 模型
```

---

### ✅ 步骤 3：配置类（可选，用于全局设置）

```java
package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 全局配置（标题、版本、联系人等）
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("企业用户管理系统 API 文档")
                .version("v2.0.0")
                .description("提供用户管理、权限控制、操作日志等核心功能")
                .termsOfService("https://www.example.com/terms")
                .contact(new Contact()
                    .name("API支持团队")
                    .email("api-support@example.com")
                    .url("https://www.example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

---

### ✅ 步骤 4：Controller 示例（带注解）

```java
package com.example.demo.controller;

import com.example.demo.entity.dto.UserCreateDTO;
import com.example.demo.entity.vo.UserVO;
import com.example.demo.service.IUserService;
import com.example.demo.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户管理控制器
 * 使用 Knife4j + SpringDoc 注解生成专业 API 文档
 */
@Tag(name = "👨‍💼 用户管理模块", description = "提供用户增删改查、状态管理等核心功能")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 分页查询用户列表
     */
    @Operation(
        summary = "分页查询用户",
        description = "支持按姓名、年龄范围、状态筛选，返回分页数据"
    )
    @ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @Content(schema = @Schema(implementation = Result.class))
    )
    @GetMapping
    public Result<?> listUsers(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        // 模拟返回
        return Result.success("查询成功");
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "用户创建请求体",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserCreateDTO.class),
            examples = {
                @ExampleObject(
                    name = "创建成人用户",
                    value = """
                            {
                              "name": "张三",
                              "age": 25,
                              "email": "zhangsan@example.com",
                              "status": 1
                            }
                            """
                )
            }
        )
    )
    @PostMapping
    public Result<String> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        // 模拟创建
        return Result.success("用户创建成功");
    }

    /**
     * 根据ID获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @Parameter(name = "id", description = "用户ID", required = true, example = "123456789012345678")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        // 模拟查询
        UserVO vo = new UserVO();
        vo.setId(id);
        vo.setName("张三");
        vo.setAge(25);
        return Result.success(vo);
    }

    @Operation(summary = "普通body请求")
    @PostMapping("/body")
    public ResponseEntity<FileResp> body(@Valid @RequestBody FileResp fileResp){
        return ResponseEntity.ok(fileResp);
    }

    @Operation(summary = "普通body请求+Param+Header+Path")
    @Parameters({
            @Parameter(name = "id",description = "文件id",in = ParameterIn.PATH),
            @Parameter(name = "token",description = "请求token",required = true,in = ParameterIn.HEADER),
            @Parameter(name = "name",description = "文件名称",required = true,in=ParameterIn.QUERY)
    })
    @PostMapping("/bodyParamHeaderPath/{id}")
    public ResponseEntity<FileResp> bodyParamHeaderPath(@PathVariable("id") String id, @RequestHeader("token") String token, @RequestParam("name") String name, @Valid @RequestBody FileResp fileResp){
        fileResp.setName(fileResp.getName()+",receiveName:"+name+",token:"+token+",pathID:"+id);
        return ResponseEntity.ok(fileResp);
    }
}
```

---

### ✅ 步骤 5：DTO 示例（带 Schema 注解）

```java
package com.example.demo.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 用户创建 DTO
 * 使用 @Schema 注解生成字段说明
 */
@Data
@Schema(description = "用户创建请求参数")
public class UserCreateDTO {

    @Schema(description = "用户名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String name;

    @Schema(description = "年龄", example = "25", minimum = "0", maximum = "150")
    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    private Integer age;

    @Schema(description = "邮箱", example = "zhangsan@example.com", format = "email")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "状态：0=禁用，1=启用", example = "1", allowableValues = {"0", "1"})
    @NotNull(message = "状态不能为空")
    private Integer status;
}
```

---

### ✅ 步骤 6：VO 示例（返回对象）

```java
package com.example.demo.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象（返回给前端）
 */
@Data
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID", example = "123456789012345678")
    private Long id;

    @Schema(description = "用户名", example = "张三")
    private String name;

    @Schema(description = "年龄", example = "25")
    private Integer age;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "创建时间", example = "2025-04-01T10:00:00")
    private LocalDateTime createTime;
}
```

---

## 五、访问 Knife4j UI

启动项目后，访问以下地址：

🔗 **Knife4j 增强 UI**：http://localhost:8080/doc.html  
🔗 原生 Swagger UI（保留）：http://localhost:8080/swagger-ui/index.html  
🔗 OpenAPI JSON：http://localhost:8080/v3/api-docs

> ✅ 推荐使用 `/doc.html` —— 这是 Knife4j 的专属入口，功能最全！

---

## 六、Knife4j 界面功能亮点（实际开发价值）

### 1. 🧭 导航清晰
- 左侧按 `@Tag` 分组，支持折叠/展开
- 支持搜索 API

### 2. 🧪 调试增强
- 参数自动缓存（刷新页面不丢失）
- 支持“调试面板”固定在底部
- 支持“离线调试”（无网络也可调试）

### 3. 📥 文档导出
- 导出 HTML（单页/多页）
- 导出 Markdown
- 导出 OpenAPI JSON/YAML
- 导出 Word（企业常用）

### 4. 🔐 权限与安全
- 支持 Basic Auth 登录访问
- 生产环境一键关闭（`knife4j.enable=false`）
- 支持分组鉴权（不同角色看到不同 API）

### 5. 🌗 主题切换
- 支持亮色/暗黑模式
- 支持自定义主题色

---

## 七、生产环境安全配置

```yaml
# application-prod.yml
knife4j:
  enable: false  # 生产环境关闭 Knife4j
  production: true # 开启生产环境模式（即使 enable=true 也会屏蔽部分功能）

springdoc:
  api-docs:
    enabled: false # 生产环境可关闭 OpenAPI 接口
```

或通过代码动态控制：

```java
@Profile("!prod")
@Configuration
public class Knife4jConfig {
    // 仅在非生产环境加载
}
```

---

## 八、[微服务网关聚合（Spring Cloud Gateway）](https://doc.xiaominfo.com/docs/middleware-sources/spring-cloud-gateway/spring-gateway-introduction)

如使用微服务，可在网关项目添加：

```xml
<!-- https://doc.xiaominfo.com/docs/quick-start/start-knife4j-version#4%E7%BD%91%E5%85%B3%E8%81%9A%E5%90%88 -->
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-gateway-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```

配置路由：

```yaml
knife4j:
  gateway:
    discover:
      enabled: true # 开启服务发现
    strategies:
      - service-name: user-service
        context-path: /user
      - service-name: order-service
        context-path: /order
```

访问网关地址 `/doc.html` 即可看到所有服务的聚合文档！

---

## 九、常见问题 FAQ

### Q1：访问 /doc.html 404？
→ 检查是否添加了 `knife4j-openapi3-ui` 依赖，且版本匹配 Spring Boot 3。

### Q2：界面没有增强效果？
→ 清除浏览器缓存，或检查是否访问了 `/swagger-ui.html` 而非 `/doc.html`。

### Q3：如何自定义界面标题/Logo？
→ 使用 `knife4j.setting` 配置，或通过 `OpenAPI` Bean 自定义。

### Q4：如何支持国际化？
→ `knife4j.setting.language=zh-CN/en-US`

---

## ✅ 十、总结：为什么企业项目推荐 Knife4j？

| 优势 | 说明 |
|------|------|
| 💼 **企业级功能** | 导出、权限、聚合、安全控制一应俱全 |
| 🎯 **提升协作效率** | 前端、测试、产品都能轻松理解和调试 API |
| 🛡️ **安全可控** | 生产环境一键关闭，支持 Basic Auth |
| 🧩 **无缝集成** | 完全兼容 SpringDoc，零侵入、零改造 |
| 🌐 **微服务友好** | 网关自动聚合，分布式架构无忧 |
| 🆓 **开源免费** | MIT 协议，可商用，社区活跃 |

---

📌 **推荐架构**：

```
前端/测试/产品
       ↓
http://localhost:8080/doc.html （Knife4j 增强 UI）
       ↓
SpringDoc OpenAPI （生成 OpenAPI 3.0 规范）
       ↓
Spring Boot 3 + MyBatis-Plus + Spring Security
```

---

通过以上配置和示例，你可以在 Spring Boot 3 项目中快速集成 Knife4j，打造**专业、安全、高效**的企业级 API 文档系统！

> 💡 提示：从今天起，告别原生 Swagger UI，拥抱 Knife4j，让 API 文档成为团队协作的加速器！🚀