在 Spring Boot 项目中，`@Valid` 和 `@Validated` 是两个用于**数据校验**的关键注解。它们都基于 JSR-380（Bean Validation 2.0）规范，但在功能和使用场景上有明显区别。

---

## ✅ 一、核心作用对比

| 特性 | `@Valid` | `@Validated` |
|------|----------|-------------|
| 来源 | `jakarta.validation.Valid`（标准 JSR） | `org.springframework.validation.annotation.Validated`（Spring 扩展） |
| 是否支持分组校验 | ❌ 不支持 | ✅ 支持 |
| 是否支持方法级别校验（如 service） | ❌ 仅支持嵌套对象 | ✅ 支持方法参数、返回值 |
| 是否支持组合注解 | ✅ 可用于嵌套对象 | ✅ 更灵活 |
| 是否支持 `@Valid` 级联校验 | ✅ 支持 | ✅ 支持 |

---

## ✅ 二、详细区别说明

### 1. `@Valid`：标准 JSR 校验注解

- **作用**：触发 JSR-380 规范的校验逻辑。
- **使用位置**：
    - 方法参数（如 `@RequestBody`）
    - 嵌套对象字段（级联校验）
- **特点**：
    - 是 **Java Bean Validation 的标准注解**，不依赖 Spring。
    - 支持 `@Valid` 嵌套对象自动校验（级联校验）。
    - **不支持分组校验** 和 **方法级别的校验**。

---

### 2. `@Validated`：Spring 扩展校验注解

- **作用**：Spring 提供的增强版校验注解，支持更复杂的校验场景。
- **使用位置**：
    - 类上（启用校验功能）
    - 方法参数上（配合分组）
    - Service 层方法上（AOP 校验）
- **特点**：
    - 是 **Spring 特有的注解**，必须配合 Spring 使用。
    - 支持 **分组校验**（如新增、更新不同规则）。
    - 支持在 **Service 层进行方法参数校验**。
    - 可用于 `@RequestParam`、`@PathVariable` 等非对象参数。

---

## ✅ 三、使用场景总结

| 场景 | 推荐注解 | 说明 |
|------|----------|------|
| 接收 JSON 请求体（`@RequestBody`） | `@Valid` | 最常见用法，配合 DTO 使用 |
| 嵌套对象校验（级联） | `@Valid` | 必须用 `@Valid` 标记嵌套对象 |
| 区分“新增”和“更新”校验规则 | `@Validated + 分组` | 使用分组实现不同校验逻辑 |
| 校验 `@RequestParam`、`@PathVariable` | `@Validated` | `@Valid` 无法直接用于基本类型 |
| Service 层方法参数校验 | `@Validated` | AOP 拦截实现校验 |
| 自定义校验器组合使用 | `@Validated` | 更灵活控制 |

---

## ✅ 四、具体使用示例（含中文注释）

### 📁 项目结构

```
src/main/java/
├── dto/
│   ├── UserDTO.java
│   └── AddressDTO.java
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── validation/
│   ├── CreateGroup.java
│   └── UpdateGroup.java
```

---

### 1. 定义分组接口（用于区分校验场景）

```java
// validation/CreateGroup.java
package com.example.demo.validation;

/**
 * 新增场景校验分组
 */
public interface CreateGroup {
}
```

```java
// validation/UpdateGroup.java
package com.example.demo.validation;

/**
 * 更新场景校验分组
 */
public interface UpdateGroup {
}
```

---

### 2. DTO 对象：`UserDTO.java`

```java
package com.example.demo.dto;

import com.example.demo.validation.CreateGroup;
import com.example.demo.validation.UpdateGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户数据传输对象
 * 包含不同场景下的校验规则
 */
@Getter
@Setter
public class UserDTO {

    // ID 在更新时必须存在，在新增时应为空
    @Null(groups = CreateGroup.class, message = "新增时ID必须为空")
    @NotNull(groups = UpdateGroup.class, message = "更新时ID不能为空")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为 {min} 到 {max} 个字符")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    // 嵌套对象：必须使用 @Valid 实现级联校验
    @Valid
    private AddressDTO address;

    // 其他字段...
}
```

---

### 3. 嵌套对象：`AddressDTO.java`

```java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 地址信息 DTO
 * 用于级联校验
 */
@Getter
@Setter
public class AddressDTO {

    @NotBlank(message = "省不能为空")
    private String province;

    @NotBlank(message = "市不能为空")
    private String city;

    @Size(max = 200, message = "详细地址不能超过 {max} 个字符")
    private String detail;

    // Getters and Setters
}
```

---

### 4. Controller 使用示例

```java
package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.validation.CreateGroup;
import com.example.demo.validation.UpdateGroup;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 演示 @Valid 和 @Validated 的不同使用方式
 */
@RestController
@RequestMapping("/api/users")
@Validated  // 必须加在类上，才能使 @Validated 在方法参数上生效
public class UserController {

    /**
     * 新增用户：使用 CreateGroup 分组校验
     * @param userDTO 校验分组为 CreateGroup
     */
    @PostMapping
    public ResponseEntity<String> createUser(
            @Validated(CreateGroup.class) // 指定校验分组
            @RequestBody UserDTO userDTO) {

        // 校验通过
        return ResponseEntity.ok("用户新增成功: " + userDTO.getUsername());
    }

    /**
     * 更新用户：使用 UpdateGroup 分组校验
     */
    @PutMapping
    public ResponseEntity<String> updateUser(
            @Validated(UpdateGroup.class)
            @RequestBody UserDTO userDTO) {

        return ResponseEntity.ok("用户更新成功: " + userDTO.getUsername());
    }

    /**
     * 根据 ID 查询用户
     * 演示 @Validated 校验 @PathVariable
     */
    @GetMapping("/{id}")
    public ResponseEntity<String> getUserById(
            @PathVariable
            @Min(value = 1L, message = "用户ID必须大于0", groups = UpdateGroup.class)
            Long id) {

        return ResponseEntity.ok("查询用户ID: " + id);
    }

    /**
     * 根据邮箱查询用户
     * 演示 @Validated 校验 @RequestParam
     */
    @GetMapping("/search")
    public ResponseEntity<String> searchUser(
            @RequestParam
            @Email(message = "邮箱格式不正确")
            String email) {

        return ResponseEntity.ok("搜索邮箱: " + email);
    }
}
```

---

### 5. Service 层方法校验（`@Validated` 的高级用法）

```java
package com.example.demo.service;

import com.example.demo.validation.CreateGroup;
import com.example.demo.dto.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

/**
 * 用户服务层
 * 演示在 Service 中使用 @Validated 进行方法参数校验
 */
@Service
@Validated  // 必须加在类上，启用方法校验 AOP
public class UserService {

    /**
     * 保存用户信息
     * 方法参数校验由 Spring AOP 拦截处理
     */
    public void saveUser(@Validated(CreateGroup.class) @Valid UserDTO userDTO) {
        System.out.println("保存用户: " + userDTO.getUsername());
        // 业务逻辑...
    }
}
```

> ⚠️ 注意：`@Validated` 要生效，必须加在类上，且类由 Spring 管理。

---

## ✅ 五、全局异常处理器（处理校验失败）

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        // 处理 @Valid 校验失败
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        // 处理 @Validated 方法参数校验失败（如 @Min on @PathVariable）
    }
}
```

---

## ✅ 六、测试示例

### 1. 新增用户（ID 不能有值）

```json
POST /api/users
{
  "id": 100,
  "username": "test",
  "email": "test@example.com",
  "address": { "province": "北京", "city": "北京", "detail": "中关村" }
}
```

**返回错误：**
```
新增时ID必须为空
```

---

### 2. 更新用户（ID 必须有值）

```json
PUT /api/users
{
  "username": "updated",
  "email": "new@example.com"
}
```

**返回错误：**
```
更新时ID不能为空
```

---

### 3. 查询用户（路径变量校验）

```http
GET /api/users/0
```

**返回错误：**
```
用户ID必须大于0
```

---

## ✅ 七、总结：如何选择？

| 需求 | 使用注解 |
|------|----------|
| 接收 JSON 请求体校验 | ✅ `@Valid` |
| 嵌套对象级联校验 | ✅ `@Valid` |
| 区分“新增”和“更新”规则 | ✅ `@Validated + 分组` |
| 校验 `@RequestParam` / `@PathVariable` | ✅ `@Validated` |
| Service 层方法参数校验 | ✅ `@Validated` |
| 纯标准 JSR 校验（非 Spring） | ✅ `@Valid` |

---

## ✅ 八、最佳实践建议

1. **Controller 方法参数**：优先使用 `@Valid` + `@RequestBody`
2. **需要分组校验**：使用 `@Validated(Group.class)`
3. **非对象参数校验**：使用 `@Validated` + `@Min`, `@Email` 等
4. **嵌套对象**：必须用 `@Valid` 标记
5. **全局异常处理**：统一捕获 `MethodArgumentNotValidException` 和 `ConstraintViolationException`
6. **Service 层校验**：谨慎使用，避免性能开销

---

## 🎯 结论

| 注解 | 适用场景 | 关键词 |
|------|----------|--------|
| `@Valid` | 对象校验、级联校验 | 标准、嵌套、JSON |
| `@Validated` | 分组、方法参数、Service 校验 | Spring、分组、AOP |

> ✅ **简单记：**
> - `@Valid` 用于“**对象内部校验**”
> - `@Validated` 用于“**场景控制和方法级别校验**”

两者可以**协同使用**，构建完整、灵活的校验体系。