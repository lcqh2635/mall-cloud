在 Spring Boot 项目中，**数据校验（Validation）** 是保证接口输入合法、提升系统健壮性的关键环节。Java 的 Bean Validation（如 `jakarta.validation`）提供了一系列常用的注解，结合 `@Valid` 或 `@Validated` 可以实现自动校验。

---

## ✅ 一、常用 Validation 注解一览

| 注解 | 作用 | 适用类型 |
|------|------|---------|
| `@NotNull` | 不能为 `null` | 任意对象 |
| `@NotBlank` | 不能为 `null` 或 空白字符串（含空格） | 字符串 |
| `@NotEmpty` | 不能为 `null` 或 空（集合、数组、字符串等） | 集合、数组、字符串 |
| `@Size` | 限制大小（长度、元素个数） | 字符串、集合、数组 |
| `@Min` / `@Max` | 数值最小/最大值 | 数值类型 |
| `@DecimalMin` / `@DecimalMax` | 小数最小/最大值（支持字符串） | BigDecimal、Double 等 |
| `@Email` | 邮箱格式校验 | 字符串 |
| `@Pattern` | 正则表达式匹配 | 字符串 |
| `@Future` / `@Past` | 时间必须是将来/过去 | 日期类型 |
| `@Positive` / `@Negative` | 正数/负数 | 数值 |
| `@PositiveOrZero` / `@NegativeOrZero` | 非负/非正 | 数值 |

---

## ✅ 二、详细使用示例（含中文注释）

### 示例实体类：用户注册信息 `UserRegisterDTO`

```java
package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户注册信息 DTO（数据传输对象）
 * 用于接收前端注册请求参数，并进行自动校验
 */
@Getter
@Setter
public class UserRegisterDTO {

    // ==================== 字符串类校验 ====================

    /**
     * 用户名不能为空，且长度在 3-20 之间
     * @NotBlank 用于字符串，自动 trim 后判断是否为空
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 {min} 到 {max} 个字符之间")
    private String username;

    /**
     * 邮箱格式必须正确，且可为空（非必填）
     * @Email 校验邮箱格式（如 user@example.com）
     */
    @Email(message = "邮箱格式不正确", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private String email;

    /**
     * 手机号必须符合中国大陆手机号格式
     * 使用正则表达式校验
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    // ==================== 数值类校验 ====================

    /**
     * 年龄必须在 18 到 120 之间
     * @Min 和 @Max 适用于整数类型
     */
    @Min(value = 18, message = "年龄不能小于 {value}")
    @Max(value = 120, message = "年龄不能大于 {value}")
    private Integer age;

    /**
     * 账户余额必须大于 0
     * @Positive 表示严格大于 0
     */
    @Positive(message = "账户余额必须为正数")
    private BigDecimal balance;

    /**
     * 折扣率范围 0.0 到 1.0（含边界）
     * @DecimalMin/@DecimalMax 支持小数比较
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "折扣率不能小于 {value}")
    @DecimalMax(value = "1.0", inclusive = true, message = "折扣率不能大于 {value}")
    private Double discount;

    // ==================== 集合与数组类校验 ====================

    /**
     * 兴趣爱好列表不能为空，且至少选 1 项，最多 5 项
     * @NotEmpty 确保集合不为 null 且不为空
     * @Size 限制集合大小
     */
    @NotEmpty(message = "兴趣爱好不能为空")
    @Size(min = 1, max = 5, message = "兴趣爱好数量必须在 {min} 到 {max} 之间")
    private List<String> hobbies;

    /**
     * 用户标签数组，最多允许 3 个
     */
    @Size(max = 3, message = "标签数量不能超过 {max} 个")
    private String[] tags;

    // ==================== 时间类校验 ====================

    /**
     * 生日必须是过去的时间
     * @Past 表示日期必须在过去（不包含现在）
     */
    @Past(message = "生日必须是过去的时间")
    private LocalDate birthday;

    /**
     * 会员到期时间必须是将来的时间
     * @Future 表示日期必须在将来
     */
    @Future(message = "会员到期时间必须是将来的时间")
    private LocalDate membershipExpiryDate;

    // ==================== 对象引用校验 ====================

    /**
     * 地址信息为嵌套对象，需使用 @Valid 启用级联校验
     */
    @Valid
    private AddressDTO address;

    // ==================== Getters and Setters ====================
}
```

---

### 嵌套对象示例：`AddressDTO`

```java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 地址信息 DTO
 * 用于级联校验（被 @Valid 引用）
 */
@Getter
@Setter
public class AddressDTO {

    @NotBlank(message = "省不能为空")
    private String province;

    @NotBlank(message = "市不能为空")
    private String city;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址不能超过 {max} 个字符")
    private String detail;

    // Getters and Setters
}
```

---

## ✅ 三、Controller 使用示例

```java
@RestController
@RequestMapping("/api/register")
public class UserController {

    @PostMapping
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterDTO dto,
                                          BindingResult result) {
        if (result.hasErrors()) {
            // 获取所有错误信息（实际项目中建议使用全局异常处理）
            String errorMsg = result.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body("注册失败：" + errorMsg);
        }
        return ResponseEntity.ok("注册成功！");
    }
}
```

> ✅ 实际项目中建议使用 **全局异常处理器** 捕获 `MethodArgumentNotValidException`，避免每个接口都写 `BindingResult`

---

## ✅ 四、各注解使用场景总结

| 注解 | 最佳使用场景 |
|------|-------------|
| `@NotBlank` | 用户名、密码、昵称等非空字符串 |
| `@NotNull` | 包装类字段（如 Long、Integer）非空判断 |
| `@NotEmpty` | 集合、数组、字符串必须有内容 |
| `@Size` | 限制字符串长度、集合大小、数组长度 |
| `@Min/@Max` | 年龄、数量、页码等整数范围控制 |
| `@DecimalMin/@DecimalMax` | 金额、折扣、税率等小数范围 |
| `@Email` | 邮箱字段校验 |
| `@Pattern` | 手机号、身份证、验证码等格式校验 |
| `@Future/@Past` | 生日、有效期、预约时间等时间约束 |
| `@Positive` | 金额、数量等必须为正 |
| `@Valid` | 嵌套对象的级联校验 |

---

## ✅ 五、注意事项

1. **仅对对象类型有效**：`@NotBlank`、`@Size` 等对 `String` 有效，对 `int` 无效。
2. **基本类型默认值问题**：避免使用 `int`、`long`，建议用 `Integer`、`Long`，否则 `@NotNull` 无法生效。
3. **配合 `@Validated` 使用分组校验**：可实现“新增”和“更新”不同校验规则。
4. **国际化支持**：`message = "{key}"` 可从 `messages.properties` 加载多语言提示。
5. **性能影响小**：校验在请求解析后、进入方法前完成，开销极小。

---

## ✅ 六、测试示例（JSON 请求）

```json
POST /api/register
Content-Type: application/json

{
  "username": "ab",
  "email": "invalid-email",
  "phone": "12345678901",
  "age": 15,
  "balance": -100,
  "discount": 1.5,
  "hobbies": [],
  "tags": ["tag1", "tag2", "tag3", "tag4"],
  "birthday": "2025-01-01",
  "membershipExpiryDate": "2024-01-01",
  "address": {
    "province": "",
    "city": "北京",
    "detail": ""
  }
}
```

**返回错误示例：**
```
注册失败：用户名长度必须在 3 到 20 个字符之间, 邮箱格式不正确, 手机号格式不正确, 年龄不能小于 18, 账户余额必须为正数, 折扣率不能大于 1.0, 兴趣爱好不能为空, 标签数量不能超过 3 个, 生日必须是过去的时间, 会员到期时间必须是将来的时间, 省不能为空, 详细地址不能为空
```

---

## ✅ 总结

这些注解是 Spring Boot 开发中 **最常用、最实用的数据校验工具**，合理使用可以：

- ✅ 减少手动 `if` 判断
- ✅ 提升代码可读性
- ✅ 实现前后端协同校验
- ✅ 支持国际化和统一异常处理

建议在项目中结合 **全局异常处理 + 国际化 + `@Valid`** 构建完整的输入校验体系。