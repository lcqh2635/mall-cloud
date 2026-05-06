当然可以！以下是对 MyBatis-Plus 中 **`Wrappers.lambdaQuery()` 和 `Wrappers.lambdaUpdate()`** 的全面、实战级使用示例，涵盖几乎所有常见、实用的条件构造场景，每个示例都带有**详细中文注释**，可直接用于实际项目开发。

---

# ✅ MyBatis-Plus Lambda 条件构造器 —— 全面实战指南

> 💡 为什么推荐 Lambda 方式？
> - ✅ **编译期检查字段名**（避免手写字符串出错）
> - ✅ **支持链式调用，代码清晰**
> - ✅ **避免 SQL 注入**
> - ✅ **IDEA 自动补全 + 重构友好**

---

## 📌 一、准备实体类（User.java）

```java
@Data
@TableName("t_user")
public class User {
    private Long id;
    private String name;
    private Integer age;
    private String email;
    private Integer status; // 0-禁用, 1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

---

## 📌 二、查询条件构造器 —— `Wrappers.lambdaQuery()`

### 1. 基础等值查询

```java
// 查询 name = '张三' 的用户
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .eq(User::getName, "张三");

List<User> users = userMapper.selectList(wrapper);
```

---

### 2. 多条件 AND 查询

```java
// 查询 name = '张三' AND age > 18 AND status = 1 的用户
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .eq(User::getName, "张三")
    .gt(User::getAge, 18)          // 大于
    .eq(User::getStatus, 1);       // 启用状态

List<User> users = userMapper.selectList(wrapper);
```

---

### 3. OR 条件查询

```java
// 查询 name = '张三' OR email LIKE '%@qq.com'
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .eq(User::getName, "张三")
    .or()                          // 开启 OR
    .like(User::getEmail, "@qq.com");

List<User> users = userMapper.selectList(wrapper);
```

---

### 4. 嵌套 AND/OR（复杂组合条件）

```java
// 查询：(name = '张三' AND age > 18) OR (status = 1 AND email IS NOT NULL)
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .and(i -> i.eq(User::getName, "张三").gt(User::getAge, 18))  // 嵌套 AND
    .or(j -> j.eq(User::getStatus, 1).isNotNull(User::getEmail)); // 嵌套 OR

List<User> users = userMapper.selectList(wrapper);
```

---

### 5. 模糊查询（LIKE）

```java
// 查询 name 包含 "三" 的用户（%三%）
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .like(User::getName, "三");

// 查询 name 以 "张" 开头（张%）
wrapper.likeLeft(User::getName, "张");   // 错！应为 likeRight

// 正确写法：
wrapper.likeRight(User::getName, "张");  // name LIKE '张%'

// 查询 name 以 "三" 结尾（%三）
wrapper.likeLeft(User::getName, "三");   // name LIKE '%三'
```

> 📌 注意：`likeLeft` = `%值`，`likeRight` = `值%`

---

### 6. 范围查询（BETWEEN / NOT BETWEEN）

```java
// 查询 age 在 18 到 30 之间的用户
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .between(User::getAge, 18, 30);

// 查询 age 不在 18 到 30 之间的用户
wrapper.notBetween(User::getAge, 18, 30);
```

---

### 7. IN / NOT IN 查询

```java
List<Long> ids = Arrays.asList(1L, 2L, 3L);

// 查询 id IN (1,2,3)
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .in(User::getId, ids);

// 查询 id NOT IN (1,2,3)
wrapper.notIn(User::getId, ids);
```

---

### 8. NULL / NOT NULL 判断

```java
// 查询 email 为 NULL 的用户
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .isNull(User::getEmail);

// 查询 email 不为 NULL 的用户
wrapper.isNotNull(User::getEmail);
```

---

### 9. 日期范围查询

```java
LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59, 59);

// 查询 createTime 在 2024 年内的用户
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .ge(User::getCreateTime, start)  // 大于等于
    .le(User::getCreateTime, end);   // 小于等于
```

---

### 10. 排序（ORDER BY）

```java
// 按 age 降序，再按 createTime 升序
// SELECT * FROM user ORDER BY id ASC, name ASC
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .orderByDesc(User::getAge)
    .orderByAsc(User::getCreateTime);
```

---

### 11. 分组 + 聚合查询（GROUP BY + HAVING）

```java
// 查询每个年龄段人数，并筛选人数 > 1 的组
// SELECT age，count FROM user GROUP BY age HAVING count > 1
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .select("age, COUNT(*) as count")  // 自定义 SELECT
    .groupBy(User::getAge)
    .having("count > {0}", 1);         // HAVING 条件，{0} 占位符防注入

List<Map<String, Object>> list = userMapper.selectMaps(wrapper);
```

---

### 12. 分页查询（结合 Page 对象）

```java
Page<User> page = new Page<>(1, 10); // 第1页，每页10条

LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .eq(User::getStatus, 1)
    .orderByDesc(User::getCreateTime);

IPage<User> result = userMapper.selectPage(page, wrapper);

System.out.println("总记录数：" + result.getTotal());
System.out.println("当前页数据：" + result.getRecords());
```

---

### 13. 子查询（EXISTS / NOT EXISTS）

```java
// 查询存在订单的用户（假设关联 order 表）
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .exists("SELECT 1 FROM t_order WHERE t_order.user_id = t_user.id");

// 查询没有订单的用户
wrapper.notExists("SELECT 1 FROM t_order WHERE t_order.user_id = t_user.id");
```

> ⚠️ 子查询中表别名需注意：MP 默认主表别名为实体对应表名（如 `t_user`）

---

### 14. 条件动态拼接（避免空值条件）

```java
String name = "张三";
Integer minAge = null; // 可能为空
Integer status = 1;

LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .eq(User::getStatus, status)
    .like(StringUtils.isNotBlank(name), User::getName, name)           // 有值才拼接
    .ge(minAge != null, User::getAge, minAge);                         // 有值才拼接

// 等价于：
// if (StringUtils.isNotBlank(name)) wrapper.like(User::getName, name);
// if (minAge != null) wrapper.ge(User::getAge, minAge);
```

✅ **这是实际开发中最推荐的写法！避免无效条件和 SQL 错误。**

---

## 📌 三、更新条件构造器 —— `Wrappers.lambdaUpdate()`

### 1. 基础更新（SET ... WHERE ...）

```java
// UPDATE t_user SET age = 20 WHERE name = '张三'
LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
    .set(User::getAge, 20)
    .eq(User::getName, "张三");

userMapper.update(null, wrapper); // 第一个参数是 entity，传 null 表示只用 wrapper 更新
```

---

### 2. 多字段更新

```java
// UPDATE t_user SET age = 20, status = 1 WHERE id = 1
LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
    .set(User::getAge, 20)
    .set(User::getStatus, 1)
    .eq(User::getId, 1L);

userMapper.update(null, wrapper);
```

---

### 3. 表达式更新（字段自增/拼接等）

```java
// age = age + 1
wrapper.setSql("age = age + 1");

// 或者使用 set 方法 + 表达式（推荐）
wrapper.set("age", "age + 1", true); // 第三个参数 true 表示是原始 SQL 片段

// email = CONCAT(email, '_updated')
wrapper.set("email", "CONCAT(email, '_updated')", true);
```

> ⚠️ 使用 `set(column, value, true)` 时要注意 SQL 注入，确保 value 是安全的表达式。

---

### 4. 条件更新（带 AND/OR）

```java
// UPDATE t_user SET status = 0 WHERE age > 60 AND status = 1
LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
    .set(User::getStatus, 0)
    .gt(User::getAge, 60)
    .eq(User::getStatus, 1);

userMapper.update(null, wrapper);
```

---

### 5. 动态条件更新（避免空值）

```java
Integer newAge = 25;
String newName = null;
Long userId = 1L;

LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
    .set(newAge != null, User::getAge, newAge)      // 有值才更新
    .set(StringUtils.isNotBlank(newName), User::getName, newName)
    .eq(User::getId, userId);

userMapper.update(null, wrapper);
```

✅ **推荐写法：避免更新空值或无效字段。**

---

### 6. 更新 + 乐观锁（自动版本校验）

```java
// 假设 User 有 @Version 注解的 version 字段
// MP 会自动在 WHERE 中加入 version = ?，成功后 version + 1

LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
    .set(User::getName, "张三丰")
    .eq(User::getId, 1L)
    .eq(User::getVersion, 1); // 可不写，MP 自动处理

// 或者更简单：直接使用 updateById / update(entity, wrapper)
User user = new User();
user.setId(1L);
user.setName("张三丰");
user.setVersion(1); // 设置当前版本号

boolean success = userService.updateById(user); // 自动处理乐观锁
```

### 7. setIncrBy 和 setDecrBy（数值加减更新）

```java
// setIncrBy 方法是 MyBatis-Plus 中用于更新操作的高级方法之一，它允许你指定一个字段，
// 并使其在数据库中的值增加指定的数值。这个方法特别适用于需要对数值字段进行增量操作的场景。

// 普通 Wrapper 和 Lambda Wrapper 生成的 SQL 相同
// UPDATE product SET num = num + 1 WHERE id = 1
LambdaUpdateWrapper<Product> wrapper = Wrappers.lambdaUpdate(Product.class)
        // num = num + 1
        .setIncrBy(Product::getNum, 1)
        .eq(User::getVersion, 1); // 可不写，MP 自动处理


// setDecrBy 方法是 MyBatis-Plus 中用于更新操作的高级方法之一，它允许你指定一个字段，
// 并使其在数据库中的值减少指定的数值。这个方法特别适用于需要对数值字段进行减量操作的场景。

// 普通 Wrapper 和 Lambda Wrapper 生成的 SQL 相同
// UPDATE product SET num = num - 1 WHERE id = 1
LambdaUpdateWrapper<Product> wrapper = Wrappers.lambdaUpdate(Product.class)
    .setDecrBy(Product::getNum, 1)
    .eq(User::getVersion, 1); // 可不写，MP 自动处理

boolean success = userService.updateById(user); // 自动处理乐观锁
```

> ✅ 实际开发中，建议使用 `updateById(entity)` 或 `update(entity, wrapper)`，而非裸用 `update(null, wrapper)`，以便自动处理乐观锁和自动填充字段。

---

### 7. 批量更新（不推荐大数据量）

```java
// 更新多个用户的 status = 0，条件是 age > 50
LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
    .set(User::getStatus, 0)
    .gt(User::getAge, 50);

userMapper.update(null, wrapper); // 影响多行
```

> ⚠️ 注意：批量更新不触发自动填充，不触发乐观锁（除非手动指定 version 条件）

---

## 📌 四、高级技巧 & 注意事项

### ✅ 技巧 1：复用 Wrapper（避免重复构造）

```java
public LambdaQueryWrapper<User> buildCommonWrapper() {
    return Wrappers.lambdaQuery(User.class)
        .eq(User::getStatus, 1)
        .ge(User::getAge, 18);
}

// 在多个方法中复用
var wrapper1 = buildCommonWrapper().like(User::getName, "张");
var wrapper2 = buildCommonWrapper().orderByDesc(User::getCreateTime);
```

---

### ✅ 技巧 2：自定义 SELECT 字段（避免查大字段）

```java
// 只查询 id, name, age，避免查 text/blob 字段
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .select(User::getId, User::getName, User::getAge)
    .eq(User::getStatus, 1);
```

---

### ✅ 技巧 3：排除某些字段

```java
// 查询除 email 外的所有字段
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .select(User.class, info -> !info.getColumn().equals("email"))
    .eq(User::getStatus, 1);
```

---

### ⚠️ 注意事项：

1. **Lambda 表达式不能嵌套太深**，否则可能影响性能或可读性。
2. **避免在循环中构造 Wrapper**，应提取到循环外复用。
3. **更新时优先使用 `update(entity, wrapper)`**，以便触发自动填充和乐观锁。
4. **子查询注意表别名**，MP 默认使用表名作为别名。
5. **动态条件务必使用条件函数**（如 `.eq(condition, column, value)`），避免空值拼接。
6. **复杂 SQL 建议写 XML**，Wrapper 适合单表或简单关联。

---

## ✅ 五、完整 Service 层封装示例

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 根据条件分页查询用户
     */
    @Override
    public IPage<User> searchUsers(String name, Integer minAge, Integer status, Page<User> page) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
            .like(StringUtils.isNotBlank(name), User::getName, name)
            .ge(minAge != null, User::getAge, minAge)
            .eq(status != null, User::getStatus, status)
            .orderByDesc(User::getCreateTime);

        return this.page(page, wrapper);
    }

    /**
     * 批量启用用户（根据ID列表）
     */
    @Override
    public boolean enableUsers(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return true;

        LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
            .set(User::getStatus, 1)
            .in(User::getId, ids);

        return this.update(null, wrapper);
    }

    /**
     * 安全更新用户信息（带版本号）
     */
    @Override
    public boolean updateUserSafely(Long id, String name, Integer version) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setVersion(version); // 乐观锁版本

        return this.updateById(user); // 自动处理版本校验
    }
}
```

---

## ✅ 六、总结

| 场景                  | 推荐写法                                                                 |
|-----------------------|--------------------------------------------------------------------------|
| 单表查询              | `Wrappers.lambdaQuery().eq().like().orderBy()`                           |
| 动态条件              | `.eq(condition, column, value)`                                          |
| 分页                  | `baseMapper.selectPage(page, wrapper)`                                   |
| 更新                  | 优先 `updateById(entity)` 或 `update(entity, wrapper)`                   |
| 批量更新              | `update(null, wrapper)`（注意不触发填充/乐观锁）                         |
| 复杂条件              | 使用 `.and(i -> ...).or(j -> ...)` 嵌套                                  |
| 性能优化              | 使用 `.select(字段)` 避免查大字段                                        |

---

📌 **官方文档参考：**
https://baomidou.com/pages/10c804/

---

通过以上详尽示例，你可以在实际项目中灵活、安全、高效地使用 MyBatis-Plus 的 Lambda 条件构造器，大幅提升开发效率与代码质量！

如需更复杂的多表关联查询，建议结合 `@Select` 注解或 XML 编写 SQL，保持 Wrapper 用于单表操作。