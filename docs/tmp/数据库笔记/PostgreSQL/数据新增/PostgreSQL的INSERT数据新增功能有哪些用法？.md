PostgreSQL 的 `INSERT` 语句用于向数据库表中**插入新数据**，功能非常丰富，支持多种语法和高级特性。以下是 **全面且详细的 `INSERT` 用法示例**，涵盖基础到高级场景，均附带中文注释说明。

---

### 1. 基本插入：指定列名和值
```sql
-- 向 users 表插入一条新用户记录（推荐写法，明确字段）
INSERT INTO users (name, email, age, city)
VALUES ('张三', 'zhangsan@email.com', 25, '北京');
```

---

### 2. 插入所有列（省略列名）
```sql
-- 按表定义的列顺序插入所有字段值（不推荐，易出错）
INSERT INTO users
VALUES (1, '李四', 'lisi@email.com', 30, '上海', TRUE, NOW());
```

> ⚠️ 注意：必须提供所有列的值，且顺序一致，否则报错。

---

### 3. 插入多条记录（批量插入）
```sql
-- 一次性插入多个用户（高效，减少网络开销）
INSERT INTO users (name, email, age, city)
VALUES 
  ('王五', 'wangwu@email.com', 28, '广州'),
  ('赵六', 'zhaoliu@email.com', 32, '深圳'),
  ('孙七', 'sunqi@email.com', 24, '杭州');
```

---

### 4. 使用 DEFAULT 关键字插入默认值
```sql
-- 对某些字段使用默认值（如 created_at、is_active 等）
INSERT INTO users (name, email, age, city, created_at)
VALUES ('周八', 'zhouba@email.com', 27, '南京', DEFAULT);
```

---

### 5. 插入自增主键（SERIAL 或 IDENTITY）
```sql
-- id 为 SERIAL 或 GENERATED ALWAYS AS IDENTITY 时，使用 DEFAULT
INSERT INTO users (id, name, email)
VALUES (DEFAULT, '吴九', 'wujiu@email.com');
```

> 或者直接省略 id 字段：
```sql
INSERT INTO users (name, email) 
VALUES ('吴九', 'wujiu@email.com');
```

---

### 6. 使用 DEFAULT VALUES 插入全默认值
```sql
-- 如果所有字段都有默认值，可插入一行默认数据
INSERT INTO config_table DEFAULT VALUES;
```

---

### 7. 从查询结果插入数据（INSERT INTO ... SELECT）
```sql
-- 将 temp_users 表中符合条件的用户迁移到 users 表
INSERT INTO users (name, email, age, city)
SELECT name, email, age, city 
FROM temp_users 
WHERE status = 'valid';
```

> 常用于数据迁移、归档、ETL 场景。

---

### 8. 使用 RETURNING 返回插入后的数据
```sql
-- 插入后返回生成的 id 和创建时间（常用于获取自增 ID）
INSERT INTO users (name, email)
VALUES ('郑十', 'zhengshi@email.com')
RETURNING id, name, created_at;
```

> `RETURNING` 是 PostgreSQL 强大特性，避免额外查询。

---

### 9. 使用 CTE（公共表表达式）插入并返回
```sql
-- 先准备数据，再插入并返回结果
WITH new_users AS (
    SELECT '小明' AS name, 'xiaoming@school.com' AS email, 12 AS age
    UNION ALL
    SELECT '小红', 'xiaohong@school.com', 11
)
INSERT INTO users (name, email, age)
SELECT name, email, age FROM new_users
RETURNING id, name, email;
```

---

### 10. 使用序列（Sequence）手动指定 ID
```sql
-- 手动从序列获取下一个值
INSERT INTO users (id, name, email)
VALUES (nextval('users_id_seq'), '刘十一', 'liushiyi@email.com');
```

> 适用于需要控制 ID 生成逻辑的场景。

---

### 11. 插入 JSON 数据（JSON/JSONB 类型）
```sql
-- 向 profile 字段（JSONB 类型）插入结构化数据
INSERT INTO users (name, email, profile)
VALUES (
  '陈十二', 
  'chenshi'er@email.com', 
  '{"phone": "13800138000", "address": "北京市朝阳区", "hobbies": ["读书", "游泳"]}'::jsonb
);
```

---

### 12. 插入数组数据
```sql
-- hobbies 字段为 TEXT[] 类型
INSERT INTO users (name, email, hobbies)
VALUES ('钱十三', 'qiansanshi@email.com', ARRAY['唱歌', '跳舞', '编程']);
```

---

### 13. 条件插入：使用 INSERT ... ON CONFLICT（UPSERT）
```sql
-- 如果 email 冲突，则更新 name 和 updated_at（类似 MySQL 的 ON DUPLICATE KEY UPDATE）
INSERT INTO users (email, name, age)
VALUES ('exist@email.com', '新名字', 30)
ON CONFLICT (email) 
DO UPDATE SET 
    name = EXCLUDED.name,
    age = EXCLUDED.age,
    updated_at = NOW();
```

> `EXCLUDED` 表示尝试插入的那行数据。

---

### 14. 冲突时忽略插入（DO NOTHING）
```sql
-- 如果 email 已存在，则不执行任何操作（静默忽略）
INSERT INTO users (email, name)
VALUES ('test@email.com', '测试用户')
ON CONFLICT (email) DO NOTHING;
```

> 常用于防止重复插入。

---

### 15. 指定冲突的约束名进行处理
```sql
-- 假设有一个唯一约束名为 uk_users_username
INSERT INTO users (username, name)
VALUES ('admin', '管理员')
ON CONFLICT ON CONSTRAINT uk_users_username 
DO UPDATE SET name = EXCLUDED.name;
```

---

### 16. 插入时设置时区或时间戳
```sql
-- 使用带时区的时间
INSERT INTO logs (message, created_at)
VALUES ('系统启动', NOW() AT TIME ZONE 'Asia/Shanghai');
```

---

### 17. 使用 generate_series 生成测试数据
```sql
-- 快速插入 100 条测试用户
INSERT INTO users (name, email)
SELECT 
  '用户' || g AS name,
  'user' || g || '@test.com' AS email
FROM generate_series(1, 100) AS g;
```

---

### 18. 插入时调用函数或表达式
```sql
-- 插入时自动计算字段值
INSERT INTO users (name, email, age, birth_year)
VALUES (
  '吴十五',
  LOWER('WuShiwu@Email.com'),  -- 转小写
  EXTRACT(YEAR FROM NOW()) - 1990,
  1990
);
```

---

### 19. 使用 DEFAULT 和表达式混合
```sql
INSERT INTO users (name, email, age, created_at, is_active)
VALUES (
  '郑十六',
  'zheng16@email.com',
  25,
  NOW(),
  DEFAULT  -- 使用 is_active 的默认值（如 true）
);
```

---

### 20. 插入大对象或二进制数据（BYTEA）
```sql
-- 插入图片或文件的二进制数据
INSERT INTO files (filename, data)
VALUES ('logo.png', decode('89504E470D0A...', 'hex'));
```

> `decode('hex')` 用于将十六进制字符串转为 BYTEA。

---

### 21. 使用规则（Rule）或触发器（Trigger）自动处理插入
```sql
-- 创建触发器：插入时自动设置 created_at
CREATE OR REPLACE FUNCTION set_created_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_created_at
    BEFORE INSERT ON users
    FOR EACH ROW EXECUTE FUNCTION set_created_at();
```

---

### 22. 插入时使用 WITH（CTE）预处理数据
```sql
-- 先计算平均年龄，再插入新用户
WITH avg_age AS (
    SELECT AVG(age) AS avg_val FROM users
)
INSERT INTO users (name, email, age)
SELECT '新用户', 'new@company.com', CEIL(avg_val)
FROM avg_age
RETURNING id, name, age;
```

---

### 23. 使用 COPY 命令快速批量导入（非 INSERT，但属于插入功能）
```sql
-- 从 CSV 文件快速导入数据（高性能）
COPY users (name, email, age) FROM '/path/to/users.csv' WITH (FORMAT csv, HEADER true);
```

> `COPY` 比 `INSERT` 快得多，适合大数据量导入。

---

### 总结：PostgreSQL INSERT 功能对比

| 方法 | 适用场景 | 是否支持 RETURNING | 是否支持条件 | 性能 |
|------|----------|---------------------|----------------|--------|
| `INSERT VALUES` | 单条或多条插入 | ✅ | ❌ | 中 |
| `INSERT SELECT` | 从查询结果插入 | ✅ | ✅ | 高 |
| `INSERT ON CONFLICT` | 防止重复插入（Upsert） | ✅ | ✅ | 中 |
| `COPY` | 大批量导入 | ❌ | ❌ | 极高 |
| `CTE + INSERT` | 复杂逻辑插入 | ✅ | ✅ | 中 |

---

### ✅ 最佳实践建议

1. **始终指定列名**：避免因表结构变化导致错误。
2. **使用 `ON CONFLICT` 替代先查后插**：避免竞态条件。
3. **批量插入用 `VALUES (...), (...), ...`**：减少事务开销。
4. **大容量导入用 `COPY`**：性能远超 INSERT。
5. **关键操作使用 `RETURNING`**：减少查询次数。
6. **生产环境插入前测试**：防止数据类型或约束错误。

---

PostgreSQL 的 `INSERT` 功能不仅支持基础插入，还提供 **Upsert、CTE、RETURNING、JSON、数组、批量插入** 等强大特性，是构建健壮数据写入逻辑的核心工具。