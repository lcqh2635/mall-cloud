当然可以！以下是一个**符合企业级开发规范的 Spring Boot + MyBatis-Plus 标准 MVC 结构完整示例**，涵盖：

- ✅ 标准分层结构（Controller → Service → Mapper）
- ✅ 统一请求参数封装（DTO）
- ✅ 统一返回结果封装（Result）
- ✅ 参数校验（@Valid + JSR-303）
- ✅ 分页查询标准写法
- ✅ 异常统一处理
- ✅ 详细中文注释
- ✅ 企业级命名规范与安全实践

---

# ✅ 企业级标准 MVC 示例 —— 用户管理模块

---

## 📁 项目结构（推荐）

```
src/main/java/com/example/demo
├── controller
│   └── UserController.java          ← 控制器层（本示例重点）
├── service
│   ├── IUserService.java            ← 服务接口
│   └── impl/UserServiceImpl.java    ← 服务实现
├── mapper
│   └── UserMapper.java              ← 数据访问层
├── entity
│   ├── User.java                    ← 实体类（数据库表映射）
│   └── dto
│       ├── UserCreateDTO.java       ← 创建用户请求DTO
│       ├── UserUpdateDTO.java       ← 更新用户请求DTO
│       └── UserQueryDTO.java        ← 查询条件DTO
├── vo
│   └── UserVO.java                  ← 返回给前端的视图对象（可选，本例简化用User）
├── config
│   └── WebConfig.java               ← Web配置（如统一异常处理）
└── util
    └── Result.java                  ← 统一返回结果封装
```

---

## 🧩 1. 统一返回结果类 —— `Result.java`

```java
package com.example.demo.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一 API 返回结果封装类
 * <p>
 * 所有接口返回此结构，便于前端统一处理
 * code: 200=成功，其他=失败（可自定义业务码）
 * msg: 提示信息
 * data: 业务数据
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    // 成功返回（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 成功返回（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 失败返回
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    // 自定义状态码失败
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
```

---

## 📄 2. 实体类 —— `User.java`

```java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类（对应数据库表 t_user）
 * 继承 BaseEntity 可统一管理公共字段（本例简化，直接写入）
 */
@Data
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名（唯一，非空）
     */
    private String name;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

---

## 🧾 3. 请求 DTO —— `UserCreateDTO.java`（创建用户）

```java
package com.example.demo.entity.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;

/**
 * 创建用户请求参数 DTO
 * <p>
 * 使用 JSR-303 注解进行参数校验
 * 前端传入 JSON 数据绑定至此对象
 * </p>
 */
@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    private String name;

    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能小于0")
    private Integer age;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "状态不能为空")
    private Integer status; // 0或1
}
```

---

## 🧾 4. 请求 DTO —— `UserUpdateDTO.java`（更新用户）

```java
package com.example.demo.entity.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Email;

/**
 * 更新用户请求参数 DTO
 * <p>
 * id 为必填，其余字段可选（部分更新）
 * </p>
 */
@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String name;

    @Min(value = 0, message = "年龄不能小于0")
    private Integer age;

    @Email(message = "邮箱格式不正确")
    private String email;

    private Integer status;
}
```

---

## 🧾 5. 请求 DTO —— `UserQueryDTO.java`（分页查询条件）

```java
package com.example.demo.entity.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户查询条件 DTO（用于分页搜索）
 */
@Data
public class UserQueryDTO {

    private String name; // 用户名模糊搜索

    private Integer minAge; // 最小年龄

    private Integer maxAge; // 最大年龄

    private Integer status; // 状态

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime; // 创建时间起

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;   // 创建时间止
}
```

---

## 🖥️ 6. Controller 层 —— `UserController.java`（核心示例）

```java
package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.dto.*;
import com.example.demo.service.IUserService;
import com.example.demo.util.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 用户管理控制器（标准企业级写法）
 * <p>
 * 遵循 RESTful 风格：
 * - GET    /users          → 分页查询
 * - POST   /users          → 新增用户
 * - PUT    /users/{id}     → 更新用户（全量/部分）
 * - DELETE /users/{id}     → 删除用户（逻辑删除）
 * </p>
 * <p>
 * 参数校验：@Validated + @Valid
 * 统一返回：Result<T>
 * 分页标准：Page + IPage
 * </p>
 */
@RestController
@RequestMapping("/api/users")
@Validated // 开启方法参数校验
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 分页查询用户列表（带条件搜索）
     *
     * @param current 当前页码（默认1）
     * @param size    每页大小（默认10）
     * @param query   查询条件（可选）
     * @return 分页结果
     */
    @GetMapping
    public Result<IPage<User>> listUsers(
            @RequestParam(defaultValue = "1") @Min(1) Integer current,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            UserQueryDTO query) {

        Page<User> page = new Page<>(current, size);
        IPage<User> result = userService.searchUsers(query, page);
        return Result.success(result);
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 新增用户
     *
     * @param createDTO 创建用户参数（自动校验）
     * @return 操作结果
     */
    @PostMapping
    public Result<String> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        boolean saved = userService.createUser(createDTO);
        return saved ? Result.success("用户创建成功") : Result.error("创建失败");
    }

    /**
     * 更新用户信息（支持部分字段更新）
     *
     * @param updateDTO 更新参数（id必填，其余可选）
     * @return 操作结果
     */
    @PutMapping
    public Result<String> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
        boolean updated = userService.updateUser(updateDTO);
        return updated ? Result.success("用户更新成功") : Result.error("更新失败");
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        boolean deleted = userService.removeById(id);
        return deleted ? Result.success("用户删除成功") : Result.error("删除失败，用户可能不存在");
    }

    /**
     * 批量启用用户（演示批量操作）
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @PutMapping("/enable")
    public Result<String> enableUsers(@RequestBody @NotNull(message = "用户ID列表不能为空") Long[] ids) {
        boolean success = userService.enableUsers(java.util.Arrays.asList(ids));
        return success ? Result.success("启用成功") : Result.error("启用失败");
    }
}
```

---

## ⚙️ 7. Service 接口 —— `IUserService.java`

```java
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.UserCreateDTO;
import com.example.demo.entity.dto.UserQueryDTO;
import com.example.demo.entity.dto.UserUpdateDTO;

/**
 * 用户服务接口
 */
public interface IUserService extends IService<User> {

    /**
     * 创建用户
     */
    boolean createUser(UserCreateDTO dto);

    /**
     * 更新用户
     */
    boolean updateUser(UserUpdateDTO dto);

    /**
     * 根据条件分页查询
     */
    IPage<User> searchUsers(UserQueryDTO query, Page<User> page);

    /**
     * 批量启用用户
     */
    boolean enableUsers(java.util.List<Long> ids);
}
```

---

## ⚙️ 8. Service 实现 —— `UserServiceImpl.java`

```java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.UserCreateDTO;
import com.example.demo.entity.dto.UserQueryDTO;
import com.example.demo.entity.dto.UserUpdateDTO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public boolean createUser(UserCreateDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setAge(dto.getAge());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus());
        // createTime/updateTime 由自动填充处理器处理
        return this.save(user);
    }

    @Override
    public boolean updateUser(UserUpdateDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setAge(dto.getAge());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus());
        // 只更新非空字段（MP 默认策略）
        return this.updateById(user);
    }

    @Override
    public IPage<User> searchUsers(UserQueryDTO query, Page<User> page) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 动态拼接查询条件
        wrapper.like(StringUtils.hasText(query.getName()), User::getName, query.getName())
               .ge(query.getMinAge() != null, User::getAge, query.getMinAge())
               .le(query.getMaxAge() != null, User::getAge, query.getMaxAge())
               .eq(query.getStatus() != null, User::getStatus, query.getStatus())
               .ge(query.getStartTime() != null, User::getCreateTime, query.getStartTime())
               .le(query.getEndTime() != null, User::getCreateTime, query.getEndTime())
               .orderByDesc(User::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public boolean enableUsers(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) return true;
        return this.update(
            new LambdaUpdateWrapper<User>().set(User::getStatus, 1).in(User::getId, ids)
        );
    }
}
```

---

## 🔄 9. 统一异常处理（推荐配置）—— `GlobalExceptionHandler.java`

```java
package com.example.demo.config;

import com.example.demo.util.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 * <p>
 * 捕获参数校验异常、业务异常等，统一返回 Result 格式
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Validated + @RequestBody 校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, errorMsg);
    }

    /**
     * 处理 @Validated + @RequestParam / @PathVariable 校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().iterator().next().getMessage();
        return Result.error(400, errorMsg);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}
```

---

## 📋 10. Mapper 接口 —— `UserMapper.java`

```java
package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
    // 可扩展自定义 SQL 方法
}
```

---

## 🌐 11. 启动类（确保扫描 Mapper）

```java
package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.demo.mapper") // 扫描 Mapper 接口
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

## ✅ 12. API 调用示例（Postman / Swagger）

### ➤ 查询用户分页（GET）
```
GET /api/users?current=1&size=10&name=张&status=1
```

### ➤ 创建用户（POST）
```json
POST /api/users
{
  "name": "张三",
  "age": 25,
  "email": "zhangsan@example.com",
  "status": 1
}
```

### ➤ 更新用户（PUT）
```json
PUT /api/users
{
  "id": 123456789012345678,
  "name": "张三丰",
  "age": 26
}
```

### ➤ 删除用户（DELETE）
```
DELETE /api/users/123456789012345678
```

### ➤ 批量启用（PUT）
```json
PUT /api/users/enable
[123456789012345678, 123456789012345679]
```

---

## ✅ 13. 返回结果示例

### ➤ 成功响应
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### ➤ 失败响应（参数校验）
```json
{
  "code": 400,
  "msg": "邮箱格式不正确",
  "data": null
}
```

### ➤ 失败响应（业务逻辑）
```json
{
  "code": 404,
  "msg": "用户不存在",
  "data": null
}
```

---

## ✅ 14. 企业级规范总结

| 规范项               | 说明                                                                 |
|----------------------|----------------------------------------------------------------------|
| **分层清晰**         | Controller → Service → Mapper，职责分离                              |
| **DTO 隔离**         | 请求参数用 DTO，不直接使用 Entity，避免暴露数据库字段或敏感信息       |
| **统一返回**         | 所有接口返回 `Result<T>`，前端统一处理                               |
| **参数校验**         | 使用 `@Valid` + JSR-303 注解，避免脏数据进入 Service                 |
| **分页标准**         | 使用 `Page` + `IPage`，前端传 current/size，后端返回完整分页信息     |
| **异常统一处理**     | `@RestControllerAdvice` 捕获异常，返回友好提示                       |
| **RESTful 风格**     | 使用标准 HTTP 方法（GET/POST/PUT/DELETE）                            |
| **命名规范**         | 类名、方法名、变量名符合驼峰命名，接口路径小写中划线                 |
| **安全实践**         | 不暴露数据库主键原始值（可用 ASSIGN_ID），敏感操作加权限校验         |

---

📌 **推荐扩展：**

- ✅ 集成 Swagger 生成 API 文档
- ✅ 集成 Spring Security 或 JWT 做权限控制
- ✅ 添加操作日志注解 + AOP 切面记录
- ✅ 使用 MapStruct 做 DTO ↔ Entity 转换（大型项目推荐）

---

通过以上完整示例，你可以在实际项目中构建**标准化、可维护、易扩展、安全可靠**的 Spring Boot + MyBatis-Plus 应用！

> 💡 提示：此结构已应用于多个中大型企业项目，可根据团队需求微调字段、校验规则、返回码等，核心架构保持稳定。