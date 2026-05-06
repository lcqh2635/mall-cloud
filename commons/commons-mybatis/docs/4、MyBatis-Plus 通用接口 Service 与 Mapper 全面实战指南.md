当然可以！以下是对 **MyBatis-Plus 通用 Service 层接口（`IService<T>`）** 和 **通用 Mapper 接口（`BaseMapper<T>`）** 的全面、实战级使用示例，涵盖所有常见、实用的持久化操作，每个示例都带有**详细中文注释**，可直接用于实际项目开发。

---

# ✅ MyBatis-Plus 通用接口 —— Service 与 Mapper 全面实战指南

> 💡 为什么使用通用接口？
> - ✅ **无需手写 SQL**：单表 CRUD 开箱即用
> - ✅ **链式调用 + Lambda 条件构造器**：安全高效
> - ✅ **内置分页、批量操作、逻辑删除、乐观锁支持**
> - ✅ **减少样板代码，提升开发效率**

---

## 📌 一、准备工作

### 1. 实体类 User.java

```java
@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
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

### 2. Mapper 接口（继承 BaseMapper）

```java
public interface UserMapper extends BaseMapper<User> {
    // 可自定义扩展方法（如多表关联查询）
}
```

---

### 3. Service 接口（继承 IService）

```java
public interface IUserService extends IService<User> {
    // 可自定义业务方法
}
```

---

### 4. Service 实现类（继承 ServiceImpl）

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    // 实现自定义方法
}
```

---

## 📌 二、通用 Mapper 接口（BaseMapper<T>）常用方法示例

> `BaseMapper` 提供了 30+ 个单表操作方法，以下是**最常用、最具实战价值**的示例：

---

### 1. 插入操作

```java
// 插入单条记录（自动填充 createTime/updateTime/version 等）
User user = new User();
user.setName("张三").setAge(20).setEmail("zhangsan@example.com");
int rows = userMapper.insert(user); // 返回影响行数

System.out.println("插入成功，主键ID：" + user.getId()); // 主键自动回填
```

---

### 2. 根据主键查询

```java
// 根据 ID 查询（逻辑删除字段自动过滤）
User user = userMapper.selectById(1L);

// 根据多个 ID 批量查询
List<Long> ids = Arrays.asList(1L, 2L, 3L);
List<User> users = userMapper.selectBatchIds(ids);
```

---

### 3. 根据条件查询（Wrapper）

```java
// 查询 name = '张三' 的用户
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .eq(User::getName, "张三");

User user = userMapper.selectOne(wrapper); // 查询单条（超过1条会报错）

// 查询多条
List<User> userList = userMapper.selectList(wrapper);

// 查询总数
Long count = userMapper.selectCount(wrapper);
```

---

### 4. 分页查询

```java
// 构造分页对象（当前页1，每页10条）
Page<User> page = new Page<>(1, 10);

// 构造查询条件
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .eq(User::getStatus, 1)
    .orderByDesc(User::getCreateTime);

// 执行分页查询
IPage<User> result = userMapper.selectPage(page, wrapper);

System.out.println("总记录数：" + result.getTotal());
System.out.println("当前页数据：" + result.getRecords());
```

---

### 5. 更新操作

```java
// 根据主键更新（只更新非空字段）
User user = new User();
user.setId(1L);
user.setName("张三丰"); // 只更新 name 字段
user.setAge(null);      // 不更新 age（因为为 null）

int rows = userMapper.updateById(user); // UPDATE ... SET name=? WHERE id=?

// 根据条件更新（使用 UpdateWrapper）
LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
    .set(User::getStatus, 0)           // 设置 status = 0
    .eq(User::getAge, 60);             // 条件：age = 60

int rows2 = userMapper.update(null, updateWrapper); // UPDATE ... SET status=0 WHERE age=60
```

---

### 6. 删除操作

```java
// 根据主键删除（逻辑删除：UPDATE SET deleted=1）
int rows = userMapper.deleteById(1L);

// 根据多个主键批量删除（逻辑删除）
List<Long> ids = Arrays.asList(1L, 2L, 3L);
int rows2 = userMapper.deleteBatchIds(ids);

// 根据条件删除（物理删除！慎用！）
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .lt(User::getAge, 18); // 删除年龄小于18的用户

int rows3 = userMapper.delete(wrapper); // 物理删除！
```

> ⚠️ 注意：若配置了逻辑删除（`@TableLogic`），`deleteById` 等方法会转为更新 `deleted` 字段，而非物理删除。

---

### 7. 自定义 SELECT 字段（避免查大字段）

```java
// 只查询 id, name, age 字段
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .select(User::getId, User::getName, User::getAge)
    .eq(User::getStatus, 1);

List<User> users = userMapper.selectList(wrapper);
```

---

## 📌 三、通用 Service 接口（IService<T>）常用方法示例

> `IService` 在 `BaseMapper` 基础上封装了更丰富的业务方法，更适合在 Service 层调用。

---

### 1. [插入操作](https://baomidou.com/guides/data-interface/#save)（支持批量）

```java
// 插入单条
// 假设有一个 User 实体对象
User user = new User();
user.setName("John Doe");
user.setEmail("john.doe@example.com");
// INSERT INTO user (name, email) VALUES ('John Doe', 'john.doe@example.com')
boolean success = userService.save(user); // 返回 boolean

// 批量插入（性能优化版，避免循环 insert）
// 假设有一组 User 实体对象
List<User> users = Arrays.asList(
        new User("Alice", "alice@example.com"),
        new User("Bob", "bob@example.com"),
        new User("Charlie", "charlie@example.com")
);
// 使用默认批次大小进行批量插入
INSERT INTO user (name, email) VALUES
('Alice', 'alice@example.com'),
('Bob', 'bob@example.com'),
('Charlie', 'charlie@example.com')
boolean result = userService.saveBatch(users); // 调用 saveBatch 方法，默认批次大小
```

---

### 1. saveOrUpdate 操作（支持批量）

```java
// 根据实体对象的主键 ID 进行判断，存在则更新记录，否则插入记录。

// 假设有一个 User 实体对象，其中 id 是 TableId 注解的属性
User user = new User();
user.setId(1);
user.setName("John Doe");
user.setEmail("john.doe@example.com");
boolean result = userService.saveOrUpdate(user); // 调用 saveOrUpdate 方法
// 生成的 SQL（假设 id 为 1 的记录已存在）:
UPDATE user SET name = 'John Doe', email = 'john.doe@example.com' WHERE id = 1
// 生成的 SQL（假设 id 为 1 的记录不存在）:
INSERT INTO user (id, name, email) VALUES (1, 'John Doe', 'john.doe@example.com')


// 假设有一组 User 实体对象，每个对象都有 id 属性
List<User> users = Arrays.asList(
        new User(1, "Alice", "alice@example.com"),
        new User(2, "Bob", "bob@example.com"),
        new User(3, "Charlie", "charlie@example.com")
);
// 使用默认批次大小进行批量修改插入
boolean result = userService.saveOrUpdateBatch(users); // 调用 saveOrUpdateBatch 方法，默认批次大小
// 生成的 SQL（假设 id 为 1 和 2 的记录已存在，id 为 3 的记录不存在）:
UPDATE user SET name = 'Alice', email = 'alice@example.com' WHERE id = 1
UPDATE user SET name = 'Bob', email = 'bob@example.com' WHERE id = 2
INSERT INTO user (id, name, email) VALUES (3, 'Charlie', 'charlie@example.com')

```

---

### 2. [更新操作](https://baomidou.com/guides/data-interface/#update)（支持条件更新 + 批量）

```java
// 根据主键更新（支持自动填充 + 乐观锁）
User user = new User();
user.setId(1);
user.setEmail("updated.email@example.com");
// UPDATE user SET email = 'updated.email@example.com' WHERE id = 1
boolean success = userService.updateById(user); // 自动处理版本校验

// 假设有一组 User 实体对象，批量更新
List<User> users = Arrays.asList(
        new User(1, null, "new.email1@example.com"),
        new User(2, null, "new.email2@example.com")
);
// UPDATE user SET email = 'new.email1@example.com' WHERE id = 1
// UPDATE user SET email = 'new.email2@example.com' WHERE id = 2
boolean result = userService.updateBatchById(users); // 调用 updateBatchById 方法，默认批次大小
// 调用 updateBatchById 方法，指定批次大小
// -- 第一批次
UPDATE user SET email = 'new.email1@example.com' WHERE id = 1
// -- 第二批次
UPDATE user SET email = 'new.email2@example.com' WHERE id = 2
boolean result = userService.updateBatchById(users, 1); 

// 条件更新，只设置条件，不使用 set 设置更新字段
LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate(User.class)
    .eq(User::getStatus, 0)
    .lt(User::getAge, 18);
// UPDATE user SET name = 'Updated Name' WHERE age < 18
boolean updateSuccess = userService.update(user, wrapper);

// 批量更新（根据实体列表，每个实体按主键更新）
List<User> updateList = Arrays.asList(
    new User().setId(1L).setName("张三").setAge(20),
    new User().setId(2L).setName("李四").setAge(25)
);

boolean batchUpdate = userService.updateBatchById(updateList); // 按主键批量更新
```

---

### 3. 查询操作（增强版）

```java
// 查询单个
// 根据指定条件查询符合条件的记录，参考 https://baomidou.com/guides/data-interface/#get
// 假设要查询 ID 为 1 的用户     SELECT * FROM user WHERE id = 1
User user = userService.getById(1); // 调用 getById 方法
if (user != null) {
    System.out.println("User found: " + user);
} else {
    System.out.println("User not found.");
}
// 根据 Wrapper，查询一条记录。结果集，如果是多个会抛出异常，随机取一条加上限制条件 wrapper.last("LIMIT 1")
// SELECT * FROM user WHERE name = '张三'
User user = userService.getOne(
    Wrappers.lambdaQuery(User.class).eq(User::getName, "张三"),
    false // 是否抛异常（true=超过1条抛异常，false=取第一条）
);


// 查询列表，参考 https://baomidou.com/guides/data-interface/#list
// 查询所有用户   SELECT * FROM user
List<User> users = userService.list(); // 调用 list 方法
for (User user : users) {
    System.out.println("User: " + user);
}
// 假设有一个 QueryWrapper 对象，设置查询条件为 age > 25 并且 status = 1
// SELECT * FROM user WHERE age > 25 AND status = 1
List<User> users = userService.list(
    Wrappers.lambdaQuery(User.class).gt(User::getAge, 25).eq(User::getStatus, 1)
);
// 假设有一组 ID 列表，批量查询用户
// SELECT * FROM user WHERE id IN (1, 2, 3)
List<Integer> ids = Arrays.asList(1, 2, 3);
Collection<User> users = userService.listByIds(ids); // 调用 listByIds 方法


// 自 3.4.3.2 开始,返回值从 int 修改为 long
// 查询总数，参考 https://baomidou.com/guides/data-interface/#count
// 查询用户表中的总记录数
// SELECT COUNT(*) FROM user
Long totalUsers = userService.count(); // 调用 count 方法
// 根据 Wrapper 条件，查询总记录数
// SELECT COUNT(*) FROM user WHERE age >= 18
Long count = userService.count(
        Wrappers.lambdaQuery(User.class).ge(User::getAge, 18)
);
```

---

### 4. [分页查询](https://baomidou.com/guides/data-interface/#page)（推荐在 Service 层使用）

```java
// 构造分页对象
// 假设要进行无条件的分页查询，每页显示10条记录，查询第1页
IPage<User> page = new Page<>(1, 10);
// SELECT * FROM user LIMIT 10 OFFSET 0
IPage<User> userPage = userService.page(page); // 调用 page 方法

// 构造条件
// 假设有一个 LambdaQueryWrapper 对象，设置查询条件为 age > 25，进行有条件的分页查询
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .gt(User::getAge, 25)
    .eq(User::getStatus, 1);

// 执行分页（返回 IPage，含 records, total, pages 等）
// SELECT * FROM user WHERE age > 25 and status = 1 LIMIT 10 OFFSET 0
IPage<User> result = userService.page(page, wrapper);
List<User> userList = result.getRecords();
long total = result.getTotal();

// 或者使用自定义分页方法（更灵活）
public IPage<User> searchUsers(String name, Integer status, Integer current, Integer size) {
    IPage<User> page = new Page<>(current, size);
    LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
        .like(StringUtils.isNotBlank(name), User::getName, name)
        .eq(status != null, User::getStatus, status)
        .orderByDesc(User::getCreateTime);

    return this.page(page, wrapper);
}
```

---

### 5. [删除操作](https://baomidou.com/guides/data-interface/#remove)（安全封装）

```java
// 根据主键删除（逻辑删除）
boolean deleted = userService.removeById(1L);

// 根据多个主键删除（逻辑删除）
List<Long> ids = Arrays.asList(1L, 2L, 3L);
boolean batchDeleted = userService.removeByIds(ids);

// 根据条件删除
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
    .isNull(User::getEmail); // 删除没有邮箱的用户

boolean conditionDeleted = userService.remove(wrapper);
```

> ✅ 推荐：在 Service 层统一处理删除逻辑，避免 Controller 直接调用 Mapper。

---

### 6. [链式调用](https://baomidou.com/guides/data-interface/#chain)（LambdaQueryChainWrapper / LambdaUpdateChainWrapper）

```java
// 查询链式调用（等价于 Wrapper，但更流畅）
List<User> users = userService.lambdaQuery()
    .eq(User::getStatus, 1)
    .ge(User::getAge, 18)
    .orderByDesc(User::getCreateTime)
    .list();

// 更新链式调用
boolean updated = userService.lambdaUpdate()
    .set(User::getStatus, 0)
    .lt(User::getAge, 18)
    .update();
```

---

### 7. 批量操作（性能优化）

```java
// 批量插入（推荐用于 > 100 条数据）
List<User> bigList = generateBigUserList(); // 假设有1000条
boolean saved = userService.saveBatch(bigList, 1000); // 分批提交，每批1000条

// 批量更新（按主键）
boolean updated = userService.updateBatchById(bigList, 500); // 每批500条

// 批量删除（按主键）
boolean deleted = userService.removeByIds(idList, 200); // 每批200条
```

> ✅ `saveBatch` / `updateBatchById` / `removeByIds` 第二个参数是批次大小，避免一次性提交过多导致内存溢出或事务超时。

---

## 📌 四、Service 层自定义方法封装示例（实战推荐）

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 根据用户名模糊查询 + 分页
     */
    @Override
    public IPage<User> searchByName(String name, Page<User> page) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
            .like(StringUtils.isNotBlank(name), User::getName, name)
            .eq(User::getStatus, 1)
            .orderByDesc(User::getCreateTime);

        return this.page(page, wrapper);
    }

    /**
     * 启用多个用户（批量更新状态）
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
     * 安全更新用户信息（带乐观锁）
     */
    @Override
    public boolean updateUserSafely(Long id, String name, Integer version) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setVersion(version); // 设置当前版本号

        return this.updateById(user); // 自动处理版本校验
    }

    /**
     * 统计各年龄段用户数量
     */
    @Override
    public List<Map<String, Object>> countByAgeGroup() {
        return this.listMaps(
            Wrappers.lambdaQuery(User.class)
                .select("CASE WHEN age < 18 THEN '未成年' " +
                        "WHEN age BETWEEN 18 AND 60 THEN '成年' " +
                        "ELSE '老年' END as age_group, COUNT(*) as count")
                .groupBy("age_group")
        );
    }
}
```

---

## 📌 五、Controller 层调用示例（完整流程）

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    public Result<IPage<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {

        Page<User> page = new Page<>(current, size);
        IPage<User> result = userService.searchByName(name, page);

        return Result.success(result);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<String> addUser(@RequestBody User user) {
        boolean saved = userService.save(user);
        return saved ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 批量启用用户
     */
    @PutMapping("/enable")
    public Result<String> enableUsers(@RequestBody List<Long> ids) {
        boolean success = userService.enableUsers(ids);
        return success ? Result.success("启用成功") : Result.error("启用失败");
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.removeById(id);
        return deleted ? Result.success("删除成功") : Result.error("删除失败");
    }
}
```

---

## 📌 六、注意事项与最佳实践

### ✅ 最佳实践：

1. **优先使用 Service 层方法**（如 `save`, `updateById`, `page`），而非直接调用 Mapper。
2. **批量操作指定批次大小**，避免 OOM 或长事务。
3. **更新操作优先使用 `updateById(entity)`**，以便触发乐观锁和自动填充。
4. **查询时使用 `.select(字段)` 避免查大字段**（如 text/blob）。
5. **动态条件使用条件函数**：`.eq(condition, column, value)`。
6. **分页查询在 Service 层封装**，便于统一处理权限、租户等逻辑。

---

### ⚠️ 注意事项：

1. **`selectOne` 超过1条会抛异常**，不确定唯一性时用 `list().get(0)` 或 `getOne(wrapper, false)`。
2. **逻辑删除后 `selectById` 查不到数据**，需使用 `selectOne(new QueryWrapper().eq("id", id).last("OR deleted=1"))` 查询已删除数据。
3. **`saveBatch` 默认批次大小是 1000**，大数据量建议手动指定。
4. **`update(null, wrapper)` 不触发自动填充和乐观锁**，优先使用 `update(entity, wrapper)`。
5. **复杂查询（多表关联、子查询）建议写 XML 或 @Select 注解**，保持 Wrapper 用于单表。

---

## ✅ 七、总结对比表

| 操作类型       | Mapper 方法                 | Service 方法                  | 推荐使用场景             |
|----------------|-----------------------------|-------------------------------|--------------------------|
| 插入单条       | `insert(entity)`            | `save(entity)`                | 通用                     |
| 批量插入       | 循环调用 insert             | `saveBatch(list, batchSize)`  | 数据量 > 100             |
| 更新（按主键） | `updateById(entity)`        | `updateById(entity)`          | 触发填充/乐观锁          |
| 更新（按条件） | `update(null, wrapper)`     | `update(null, wrapper)`       | 批量更新状态等           |
| 查询单条       | `selectById(id)`            | `getById(id)`                 | 通用                     |
| 查询列表       | `selectList(wrapper)`       | `list(wrapper)`               | 通用                     |
| 分页查询       | `selectPage(page, wrapper)` | `page(page, wrapper)`         | 推荐在 Service 层使用    |
| 删除（逻辑）   | `deleteById(id)`            | `removeById(id)`              | 通用                     |
| 批量删除       | `deleteBatchIds(ids)`       | `removeByIds(ids, batchSize)` | 数据量大时指定批次       |

---

📌 **官方文档参考：**
- BaseMapper: https://baomidou.com/pages/49cc81/
- IService: https://baomidou.com/pages/49cc81/#iservice-crud-封装

---

通过以上详尽示例，你可以在实际项目中高效、规范地使用 MyBatis-Plus 的通用 Mapper 和 Service 接口，大幅提升开发效率与代码质量！

> 💡 提示：结合 `BaseEntity` + `LambdaQueryWrapper` + `IService`，可实现 90% 以上单表操作零 SQL 开发！复杂场景再补充 XML 或自定义 SQL。