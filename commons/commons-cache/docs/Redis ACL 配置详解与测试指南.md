# Redis ACL 配置详解与测试指南

## 📝 配置语句逐段解析

```conf
user order-service on >Order@2026! ~order:* ~user:profile:* +@read +@write +@transaction -FLUSHALL -CONFIG -DEBUG
│        │          │            │             │              │          │           │           │           │
│        │          │            │             │              │          │           │           │           └─ 禁止 DEBUG 命令
│        │          │            │             │              │          │           │           └───────────── 禁止 CONFIG 命令
│        │          │            │             │              │          │           └───────────────────────── 禁止 FLUSHALL 命令
│        │          │            │             │              │          └───────────────────────────────────── 允许事务相关命令（MULTI/EXEC等）
│        │          │            │             │              └──────────────────────────────────────────────── 允许写操作命令（SET/DEL等）
│        │          │            │             └──────────────────────────────────────────────────────────────── 允许读操作命令（GET/HGET等）
│        │          │            └────────────────────────────────────────────────────────────────────────────── 允许访问的 Key 模式：user:profile:*
│        │          └─────────────────────────────────────────────────────────────────────────────────────────── 允许访问的 Key 模式：order:*
│        └────────────────────────────────────────────────────────────────────────────────────────────────────── 用户密码：Order@2026!
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────── 用户名：order-service
```

## 📋 完整配置说明表

| 配置项               | 含义       | 详细说明                         |
|-------------------|----------|------------------------------|
| `user`            | 关键字      | 声明这是一个用户配置                   |
| `order-service`   | 用户名      | 用于订单服务的 Redis 账号             |
| `on`              | 启用状态     | 启用该用户（`off` 表示禁用）            |
| `>Order@2026!`    | 密码       | 明文密码（`>` 表示明文，`#` 表示哈希）      |
| `~order:*`        | Key 权限 1 | 允许访问所有以 `order:` 开头的键        |
| `~user:profile:*` | Key 权限 2 | 允许访问所有以 `user:profile:` 开头的键 |
| `+@read`          | 命令类别     | 允许所有读操作（GET, HGET, LRANGE 等） |
| `+@write`         | 命令类别     | 允许所有写操作（SET, HSET, LPUSH 等）  |
| `+@transaction`   | 命令类别     | 允许事务命令（MULTI, EXEC, WATCH 等） |
| `-FLUSHALL`       | 禁止命令     | 禁止清空所有数据库                    |
| `-CONFIG`         | 禁止命令     | 禁止修改 Redis 配置                |
| `-DEBUG`          | 禁止命令     | 禁止调试命令                       |

## 🔧 配置生效步骤

### 方法一：通过配置文件（推荐）

**1. 编辑 `redis.conf`**：

```conf
# 在 redis.conf 末尾添加
user order-service on >Order@2026! ~order:* ~user:profile:* +@read +@write +@transaction -FLUSHALL -CONFIG -DEBUG

# 或者使用外部 ACL 文件（更安全）
aclfile /etc/redis/users.acl
```

**2. 创建 `/etc/redis/users.acl`**：

```conf
user order-service on >Order@2026! ~order:* ~user:profile:* +@read +@write +@transaction -FLUSHALL -CONFIG -DEBUG
```

**3. 重启 Redis**：

```bash
sudo systemctl restart redis
```

### 方法二：运行时动态配置

```bash
# 1. 以管理员身份连接 Redis
redis-cli -a your_admin_password

# 2. 创建用户
127.0.0.1:6379> ACL SETUSER order-service on >Order@2026! ~order:* ~user:profile:* +@read +@write +@transaction -FLUSHALL -CONFIG -DEBUG
OK

# 3. 保存配置到 ACL 文件（需要配置 aclfile）
127.0.0.1:6379> ACL SAVE
OK

# 4. 验证用户创建成功
127.0.0.1:6379> ACL GETUSER order-service
1) "flags"
2) 1) "on"
3) "passwords"
4) 1) "9d8f7e6c5b4a3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1b0c9d8e"
5) "commands"
6) "-@all +@read +@write +@transaction -FLUSHALL -CONFIG -DEBUG"
7) "keys"
8) 1) "~order:*"
   2) "~user:profile:*"
9) "channels"
10) "~*"
```

## 🧪 测试方法

### 测试 1：使用 redis-cli 连接测试

```bash
# ✅ 正确连接（应该成功）
redis-cli -u redis://order-service:Order@2026!@localhost:6379

# 或者分步连接
redis-cli -h localhost -p 6379
AUTH order-service Order@2026!

# 测试 1：允许的操作 - 设置 order 相关的键
127.0.0.1:6379> SET order:123 '{"id":123,"status":"paid"}'
OK  ✅ 成功

# 测试 2：允许的操作 - 获取 order 相关的键
127.0.0.1:6379> GET order:123
"{\"id\":123,\"status\":\"paid\"}"  ✅ 成功

# 测试 3：允许的操作 - 设置 user:profile 相关的键
127.0.0.1:6379> HSET user:profile:456 name "张三" age 25
(integer) 2  ✅ 成功

# 测试 4：允许的操作 - 获取 user:profile 相关的键
127.0.0.1:6379> HGETALL user:profile:456
1) "name"
2) "张三"
3) "age"
4) "25"  ✅ 成功

# 测试 5：允许的操作 - 事务操作
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379> SET order:789 "test"
QUEUED
127.0.0.1:6379> EXEC
1) OK  ✅ 成功

# ❌ 禁止的操作 - 访问不允许的键
127.0.0.1:6379> SET product:999 "iPhone"
(error) NOPERM this user has no permissions to access one of the keys used as arguments  ❌ 失败（预期）

# ❌ 禁止的操作 - 执行 FLUSHALL
127.0.0.1:6379> FLUSHALL
(error) NOPERM this user has no permissions to run the 'flushall' command  ❌ 失败（预期）

# ❌ 禁止的操作 - 修改配置
127.0.0.1:6379> CONFIG SET maxmemory 1gb
(error) NOPERM this user has no permissions to run the 'config' command  ❌ 失败（预期）

# ❌ 禁止的操作 - 访问其他用户的键
127.0.0.1:6379> GET cart:user:100
(error) NOPERM this user has no permissions to access one of the keys used as arguments  ❌ 失败（预期）

# 退出
127.0.0.1:6379> QUIT
```

### 测试 2：使用 Spring Boot 测试

**1. 添加测试依赖**（`pom.xml`）：

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-test</artifactId>
<scope>test</scope>
</dependency>
```

**2. 配置测试环境**（`application-test.yml`）：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      username: order-service
      password: Order@2026!
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

**3. 创建测试类**：

```java
package com.mallcloud.order.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RedisACLTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试 1：允许的操作 - 设置和获取 order:* 键
     */
    @Test
    public void testOrderKeyAccess_ShouldSuccess() {
        // 设置订单数据
        stringRedisTemplate.opsForValue().set("order:1001", "{\"id\":1001,\"amount\":99.99}");

        // 获取订单数据
        String orderData = stringRedisTemplate.opsForValue().get("order:1001");

        assertNotNull(orderData);
        assertTrue(orderData.contains("1001"));

        // 清理
        stringRedisTemplate.delete("order:1001");
    }

    /**
     * 测试 2：允许的操作 - 设置和获取 user:profile:* 键
     */
    @Test
    public void testUserProfileKeyAccess_ShouldSuccess() {
        // 设置用户资料
        redisTemplate.opsForHash().put("user:profile:2001", "name", "李四");
        redisTemplate.opsForHash().put("user:profile:2001", "email", "lisi@example.com");

        // 获取用户资料
        String name = (String) redisTemplate.opsForHash().get("user:profile:2001", "name");
        String email = (String) redisTemplate.opsForHash().get("user:profile:2001", "email");

        assertEquals("李四", name);
        assertEquals("lisi@example.com", email);

        // 清理
        redisTemplate.delete("user:profile:2001");
    }

    /**
     * 测试 3：允许的操作 - 事务操作
     */
    @Test
    public void testTransaction_ShouldSuccess() {
        // 执行事务
        redisTemplate.executePipelined(connection -> {
            connection.multi();
            connection.set("order:batch:1".getBytes(), "item1".getBytes());
            connection.set("order:batch:2".getBytes(), "item2".getBytes());
            connection.exec();
            return null;
        });

        // 验证
        String item1 = stringRedisTemplate.opsForValue().get("order:batch:1");
        String item2 = stringRedisTemplate.opsForValue().get("order:batch:2");

        assertEquals("item1", item1);
        assertEquals("item2", item2);

        // 清理
        stringRedisTemplate.delete("order:batch:1", "order:batch:2");
    }

    /**
     * 测试 4：禁止的操作 - 访问不允许的键（应该抛出异常）
     */
    @Test
    public void testForbiddenKeyAccess_ShouldFail() {
        assertThrows(Exception.class, () -> {
            // 尝试访问 product:* 键（不在允许范围内）
            stringRedisTemplate.opsForValue().set("product:999", "iPhone");
        });
    }

    /**
     * 测试 5：禁止的操作 - 执行危险命令（应该抛出异常）
     */
    @Test
    public void testForbiddenCommand_ShouldFail() {
        assertThrows(Exception.class, () -> {
            // 尝试执行 FLUSHALL（被禁止）
            stringRedisTemplate.execute(connection -> {
                connection.flushAll();
                return null;
            });
        });
    }
}
```

### 测试 3：使用 Python 测试

```python
import redis
import pytest

# 创建 Redis 连接
redis_client = redis.Redis(
    host='localhost',
    port=6379,
    username='order-service',
    password='Order@2026!',
    decode_responses=True
)

def test_allowed_operations():
    """测试允许的操作"""
    # 设置 order 键
    redis_client.set('order:1001', '{"id":1001,"status":"paid"}')
    assert redis_client.get('order:1001') == '{"id":1001,"status":"paid"}'
    
    # 设置 user:profile 键
    redis_client.hset('user:profile:2001', mapping={'name': '王五', 'age': 30})
    assert redis_client.hget('user:profile:2001', 'name') == '王五'
    
    # 清理
    redis_client.delete('order:1001', 'user:profile:2001')
    print("✅ 允许的操作测试通过")

def test_forbidden_key():
    """测试禁止访问的键"""
    try:
        redis_client.set('product:999', 'iPhone')
        assert False, "应该抛出权限异常"
    except redis.exceptions.NoPermissionError as e:
        print(f"✅ 禁止的键访问被正确拦截: {e}")

def test_forbidden_command():
    """测试禁止的命令"""
    try:
        redis_client.flushall()
        assert False, "应该抛出权限异常"
    except redis.exceptions.NoPermissionError as e:
        print(f"✅ 禁止的命令被正确拦截: {e}")

if __name__ == '__main__':
    test_allowed_operations()
    test_forbidden_key()
    test_forbidden_command()
```

## 📊 测试结果总结表

| 测试场景    | 操作                                | 预期结果 | 实际结果      |
|---------|-----------------------------------|------|-----------|
| ✅ 允许的键  | `SET order:123 "data"`            | 成功   | OK        |
| ✅ 允许的键  | `GET order:123`                   | 成功   | 返回数据      |
| ✅ 允许的键  | `HSET user:profile:456 name "张三"` | 成功   | OK        |
| ✅ 事务操作  | `MULTI/EXEC`                      | 成功   | OK        |
| ❌ 禁止的键  | `SET product:999 "iPhone"`        | 失败   | NOPERM 错误 |
| ❌ 禁止的命令 | `FLUSHALL`                        | 失败   | NOPERM 错误 |
| ❌ 禁止的命令 | `CONFIG SET`                      | 失败   | NOPERM 错误 |
| ❌ 禁止的命令 | `DEBUG`                           | 失败   | NOPERM 错误 |

## 🎯 实际应用场景

这个配置适用于 **订单微服务**，具体场景：

1. **订单服务** 需要读写订单数据（`order:*`）
2. **订单服务** 需要读取用户资料（`user:profile:*`）用于展示订单详情
3. **禁止危险操作** 防止误操作清空数据库或修改配置
4. **权限隔离** 订单服务无法访问购物车（`cart:*`）、商品（`product:*`）等其他服务的数据

## 🔍 监控与审计

```bash
# 查看用户使用情况
127.0.0.1:6379> ACL LOG
1) 1) "count"
   2) (integer) 0
   3) "reason"
   4) "command"
   5) "context"
   6) "object"
   7) "username"
   8) "order-service"
   9) "age-seconds"
  10) (integer) 0

# 查看当前连接的用户
127.0.0.1:6379> CLIENT LIST
id=12 addr=127.0.0.1:54321 laddr=127.0.0.1:6379 fd=8 name= age=3600 idle=0 flags=N db=0 sub=0 psub=0 ssub=0 multi=-1 qbuf=26 qbuf-free=32742 argv-mem=10 obl=0 oll=0 omem=0 tot-mem=61466 events=r cmd=auth user=order-service redir=-1 resp=2
```

通过以上测试，您可以全面验证 ACL 配置是否正确生效！