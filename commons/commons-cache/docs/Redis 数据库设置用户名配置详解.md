# Redis 用户名配置详解

## 📋 简短回答

**可以设置用户名，但需要 Redis 6.0+ 版本**，通过 **ACL（Access Control List）** 功能实现。[[2]][[6]]

---

## 🔍 版本差异对比

| Redis 版本  | 用户名支持 | 认证方式                         | 权限控制                      |
|-----------|-------|------------------------------|---------------------------|
| **< 6.0** | ❌ 不支持 | 仅密码 (`AUTH <password>`)      | 全局权限，无细分                  |
| **≥ 6.0** | ✅ 支持  | `AUTH <username> <password>` | 细粒度 ACL 权限控制 [[11]][[15]] |

---

## ⚙️ 配置方法

### 方法一：通过 `redis.conf` 配置文件（推荐）

```conf
# ============ ACL 用户配置 ============

# 1. 设置默认用户密码（兼容旧版本）
requirepass your_default_password

# 2. 创建新用户：只读用户，只能访问 cache:* 开头的 key
user readonly_user on >readonly123 ~cache:* +@read -@dangerous

# 3. 创建新用户：应用服务用户，完整权限但限制键范围
user app_service on >App@2024Secure! ~app:* ~session:* +@all -@admin -FLUSHALL -CONFIG

# 4. 创建新用户：无需密码（仅内网可信环境）
user internal_monitor on nopass ~metrics:* +INFO +PING +CLIENT

# 5. 禁用危险命令类别
# +@all 表示所有命令，-@dangerous 排除危险命令
```

**关键参数说明** [[12]][[17]]：

| 参数           | 含义              | 示例                    |
|--------------|-----------------|-----------------------|
| `on` / `off` | 启用/禁用用户         | `user alice on`       |
| `>password`  | 设置密码（明文）        | `>MyP@ss123`          |
| `#hash`      | 设置密码（SHA256 哈希） | `#2d9c75...`          |
| `~pattern`   | 允许访问的 key 模式    | `~user:* ~order:*`    |
| `+@category` | 允许的命令类别         | `+@read +@write`      |
| `-@category` | 禁止的命令类别         | `-@dangerous -@admin` |
| `nopass`     | 免密码登录           | `user monitor nopass` |

---

### 方法二：运行时动态配置（`redis-cli`）

```bash
# 1. 连接 Redis（先以默认用户登录）
redis-cli -a your_default_password

# 2. 创建新用户
127.0.0.1:6379> ACL SETUSER app_user on >SecureP@ss2024 ~app:* +@all -@dangerous
OK

# 3. 查看用户列表
127.0.0.1:6379> ACL LIST
1) "user default on nopass ~* &* +@all"
2) "user app_user on #2d9c75... ~app:* resetchannels -@all +@all -@dangerous"

# 4. 查看指定用户详情
127.0.0.1:6379> ACL GETUSER app_user
1) "flags"
2) 1) "on"
3) "passwords"
4) 1) "2d9c75273d72b32df726fb545c8a4edc719f0a95a6fd993950b10c474ad9c927"
5) "commands"
6) "-@dangerous +@all"
7) "keys"
8) "~app:*"

# 5. 保存 ACL 配置到文件（需配置 aclfile 指令）
127.0.0.1:6379> ACL SAVE
OK
```

---

### 方法三：外部 ACL 文件（生产环境推荐）

**1. 在 `redis.conf` 中指定 ACL 文件**：

```conf
# 启用外部 ACL 文件（与直接配置互斥）
aclfile /etc/redis/users.acl
```

**2. 创建 `/etc/redis/users.acl`**：

```conf
# users.acl - Redis ACL 配置文件

# 默认用户（建议禁用或限制）
user default off

# 微服务应用用户
user order-service on >Order@2026! ~order:* ~user:profile:* +@read +@write +@transaction -FLUSHALL -CONFIG -DEBUG

# 只读分析用户
user analytics-readonly on >Analytics#Read! ~stats:* ~report:* +@read -@write -@admin

# 运维监控用户
user ops-monitor on >Monitor@Ops! ~* +INFO +PING +CLIENT|LIST +SLOWLOG +CONFIG|GET -@dangerous

# 缓存服务专用用户
user cache-service on >Cache@Svc2026! ~cache:* ~session:* +GET +SET +DEL +EXPIRE +MGET +MSET -KEYS -FLUSHDB
```

**3. 加载配置**：

```bash
# 重启 Redis 或动态加载
redis-cli ACL LOAD
```

---

## 🔐 客户端连接示例

### Java (Spring Boot + Lettuce/Jedis)

```java
// application.yml
spring:
data:
redis:
host:localhost
port:6379
username:order-service      #Redis 6.0+支持
password:Order@2026!
database:0

// 或代码配置
RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
config.

setHostName("localhost");
config.

setPort(6379);
config.

setUsername("order-service");  // ✅ 用户名
config.

setPassword("Order@2026!");     // ✅ 密码
```

### Python (redis-py)

```python
import redis

# Redis 6.0+ 连接方式
r = redis.Redis(
    host='localhost',
    port=6379,
    username='order-service',  # ✅ 用户名
    password='Order@2026!',     # ✅ 密码
    decode_responses=True
)

# 测试连接
print(r.ping())  # True 表示成功
```

### Node.js (ioredis)

```javascript
const Redis = require('ioredis');

const redis = new Redis({
    host: 'localhost',
    port: 6379,
    username: 'order-service',  // ✅ 用户名
    password: 'Order@2026!',     // ✅ 密码
});

redis.on('connect', () => {
    console.log('✅ Redis connected with ACL user');
});
```

---

## 🛡️ 安全最佳实践

1. **密码强度**：使用 `ACL GENPASS` 生成强密码 [[12]]
   ```bash
   127.0.0.1:6379> ACL GENPASS
   "a7f3c9e2b1d4f8a6c3e5b7d9f1a3c5e7b9d1f3a5c7e9b1d3f5a7c9e1b3d5f7a9"
   ```

2. **最小权限原则**：只授予必要命令和 key 范围
   ```conf
   # ❌ 不推荐：全权限
   user app on >pwd ~* +@all
   
   # ✅ 推荐：最小权限
   user app on >pwd ~app:* +GET +SET +DEL +EXPIRE -FLUSHALL -CONFIG
   ```

3. **禁用默认用户或限制权限** [[7]][[16]]
   ```conf
   # 禁用默认用户，强制使用命名用户
   user default off
   
   # 或限制默认用户仅用于管理
   user default on >Admin@Strong! ~* +@admin +@dangerous
   ```

4. **定期轮换密码**：结合 `ACL SETUSER` 动态更新

5. **日志审计**：开启 `loglevel verbose` 记录 ACL 相关操作

---

## ⚠️ 常见问题排查

| 问题                                         | 可能原因             | 解决方案                               |
|--------------------------------------------|------------------|------------------------------------|
| `WRONGPASS invalid username-password pair` | 用户名/密码错误 或 用户被禁用 | 检查 `ACL GETUSER <name>` 确认状态 [[7]] |
| `NOPERM this user has no permissions`      | 用户权限不足           | 检查 `+@category` 和 `~pattern` 配置    |
| `ACL command not found`                    | Redis 版本 < 6.0   | 升级 Redis 到 6.0+ [[2]]              |
| 配置不生效                                      | 未重启或未 `ACL SAVE` | 执行 `ACL SAVE` 或重启服务                |

---

## 📦 您的微服务项目建议配置

结合您的 `mall-cloud` 项目，推荐如下 ACL 策略：

```conf
# /etc/redis/users.acl

# 禁用默认用户
user default off

# 订单服务用户 - 只能操作 order:* 和 user:profile:*
user order-service on >Order@2026!Secure ~order:* ~user:profile:* +@read +@write +@transaction -FLUSHALL -CONFIG -DEBUG -KEYS

# 用户服务用户
user user-service on >User@2026!Secure ~user:* ~auth:* +@all -@dangerous -FLUSHDB

# 只读报表服务
user report-readonly on >Report@Read! ~report:* ~stats:* +@read -@write -@admin

# 缓存专用用户（高频短连接）
user cache-service on >Cache@2026! ~cache:* ~session:* +GET +SET +DEL +EXPIRE +MGET +MSET -KEYS -SCAN

# 运维监控用户
user ops-monitor on >Ops@Monitor! ~* +INFO +PING +CLIENT|LIST +SLOWLOG +CONFIG|GET -@dangerous
```

> 💡 **提示**：确保您的客户端（如 Spring Boot、MyBatis-Flex）已升级到支持 Redis 6.0+ ACL 的版本，并在连接配置中正确传入
`username` 参数。

如需进一步了解 ACL
命令类别或键权限细节，可参考 [Redis 官方 ACL 文档](https://redis.io/docs/management/security/acl/) [[12]][[18]]。