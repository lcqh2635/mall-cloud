PostgreSQL 的 `UPDATE` 语句用于**修改表中已有数据**，功能强大且灵活。以下是 **全面详尽的 `UPDATE` 用法示例**，涵盖基础到高级场景，均附带中文注释说明。

---

### 1. 基本更新：修改单条记录
```sql
-- 将 id 为 1 的用户邮箱更新为新地址
UPDATE users 
SET email = 'zhangsan_new@email.com' 
WHERE id = 1;
```

---

### 2. 更新多个字段
```sql
-- 同时更新用户的姓名、年龄和城市
UPDATE users 
SET name = '张三丰', age = 35, city = '西安'
WHERE id = 1;
```

---

### 3. 使用表达式更新字段
```sql
-- 年龄加 1（适用于生日更新）
UPDATE users 
SET age = age + 1 
WHERE id = 2;

-- 邮箱转为小写
UPDATE users 
SET email = LOWER(email) 
WHERE domain = 'COMPANY.COM';
```

---

### 4. 使用字符串拼接更新
```sql
-- 在用户名后添加后缀
UPDATE users 
SET name = name || '（已验证）' 
WHERE is_verified = TRUE;
```

---

### 5. 使用 DEFAULT 设置默认值
```sql
-- 将 profile 字段重置为默认值
UPDATE users 
SET profile = DEFAULT 
WHERE id = 5;
```

---

### 6. 使用函数更新数据
```sql
-- 更新最后登录时间为当前时间
UPDATE users 
SET last_login = NOW() 
WHERE id = 10;

-- 使用字符串函数截取或替换
UPDATE users 
SET email = REPLACE(email, 'old.com', 'new.com') 
WHERE email LIKE '%old.com';
```

---

### 7. 使用子查询更新字段
```sql
-- 将用户的 city 更新为其最近一次订单的发货城市
UPDATE users u
SET city = (
    SELECT o.ship_city 
    FROM orders o 
    WHERE o.user_id = u.id 
    ORDER BY o.order_date DESC 
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 FROM orders WHERE user_id = u.id
);
```

---

### 8. 多表关联更新（使用 FROM）
```sql
-- 根据 orders 表更新 users 表的 last_order_date
UPDATE users u
SET last_order_date = o.order_date,
    last_order_amount = o.amount
FROM orders o
WHERE u.id = o.user_id 
  AND o.order_date = (
      SELECT MAX(order_date) 
      FROM orders 
      WHERE user_id = u.id
  );
```

> PostgreSQL 支持 `UPDATE ... FROM` 实现多表关联更新。

---

### 9. 使用 RETURNING 返回更新后的数据
```sql
-- 更新并返回用户的新信息（便于审计或调试）
UPDATE users 
SET age = 30, updated_at = NOW() 
WHERE id = 1 
RETURNING id, name, age, updated_at;
```

> `RETURNING` 是 PostgreSQL 强大特性，避免额外查询。

---

### 10. 使用 CTE（公共表表达式）进行复杂更新
```sql
-- 先计算每个用户的平均订单金额，再更新 users 表
WITH avg_order AS (
    SELECT user_id, AVG(amount) AS avg_amt
    FROM orders 
    GROUP BY user_id
)
UPDATE users u
SET avg_order_amount = ao.avg_amt
FROM avg_order ao
WHERE u.id = ao.user_id;
```

---

### 11. 批量更新：根据条件批量修改
```sql
-- 将所有北京用户的城市改为“北京市”
UPDATE users 
SET city = '北京市' 
WHERE city = '北京';

-- 将未验证用户的 is_active 设置为 false
UPDATE users 
SET is_active = FALSE 
WHERE is_verified = FALSE;
```

---

### 12. 使用 CASE 表达式进行条件更新
```sql
-- 根据年龄段设置用户等级
UPDATE users 
SET level = CASE 
    WHEN age < 18 THEN '少年'
    WHEN age BETWEEN 18 AND 35 THEN '青年'
    WHEN age BETWEEN 36 AND 55 THEN '中年'
    ELSE '老年'
END;
```

---

### 13. 更新 JSON/JSONB 字段
```sql
-- 更新 profile 中的 phone 字段
UPDATE users 
SET profile = jsonb_set(profile, '{phone}', '"13800138000"')
WHERE id = 1;

-- 添加新的 JSON 字段
UPDATE users 
SET profile = profile || '{"vip": true, "level": 5}'::jsonb
WHERE id IN (1, 2, 3);
```

---

### 14. 更新数组字段
```sql
-- 向 hobbies 数组中添加新爱好
UPDATE users 
SET hobbies = array_append(hobbies, '摄影')
WHERE id = 1;

-- 删除数组中的某个元素
UPDATE users 
SET hobbies = array_remove(hobbies, '抽烟')
WHERE '抽烟' = ANY(hobbies);
```

---

### 15. 使用 LIMIT 限制更新数量（PostgreSQL 支持）
```sql
-- 仅更新最早注册的 5 个用户（谨慎使用！）
UPDATE users 
SET is_premium = TRUE 
WHERE is_premium = FALSE 
ORDER BY created_at ASC 
LIMIT 5;
```

> ⚠️ 注意：`LIMIT` 在 `UPDATE` 中存在，但可能影响可预测性，建议配合 `ORDER BY` 使用。

---

### 16. 使用 EXISTS 条件更新
```sql
-- 为有订单的用户设置 is_active = TRUE
UPDATE users u
SET is_active = TRUE
WHERE EXISTS (
    SELECT 1 FROM orders o WHERE o.user_id = u.id
);
```

---

### 17. 使用 IN 和子查询更新
```sql
-- 更新属于高价值客户组的用户标签
UPDATE users 
SET tags = tags || ARRAY['高价值']
WHERE id IN (
    SELECT user_id 
    FROM orders 
    GROUP BY user_id 
    HAVING SUM(amount) > 10000
);
```

---

### 18. 使用 LATERAL 进行高级更新
```sql
-- 为每个用户更新其最近订单的金额
UPDATE users u
SET last_order_amount = latest.amount
FROM LATERAL (
    SELECT amount 
    FROM orders 
    WHERE user_id = u.id 
    ORDER BY order_date DESC 
    LIMIT 1
) AS latest;
```

---

### 19. 使用触发器（Trigger）自动处理更新
```sql
-- 创建触发器：更新时自动设置 updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

---

### 20. 使用 RETURNING + CTE 记录更新日志
```sql
-- 更新用户并记录到审计表
WITH updated AS (
    UPDATE users 
    SET email = 'new@email.com', updated_at = NOW()
    WHERE id = 100
    RETURNING id, name, email, updated_at
)
INSERT INTO audit_log (table_name, record_id, action, details)
SELECT 'users', id, 'UPDATE', row_to_json(updated)::jsonb
FROM updated;
```

---

### 21. 条件更新：避免无意义更新
```sql
-- 只有当新值与旧值不同时才更新（减少日志和触发器触发）
UPDATE users 
SET last_login = '2025-04-05 10:00:00'
WHERE id = 1 
  AND last_login != '2025-04-05 10:00:00';
```

---

### 22. 使用序列（Sequence）更新字段
```sql
-- 为每个用户分配一个唯一的编号（如会员号）
UPDATE users 
SET member_no = nextval('member_seq')
WHERE member_no IS NULL;
```

---

### 23. 更新时使用窗口函数（间接方式）
```sql
-- 为每个城市的用户按年龄排序并分配排名
WITH ranked AS (
    SELECT id, 
           RANK() OVER (PARTITION BY city ORDER BY age DESC) AS age_rank
    FROM users
)
UPDATE users u
SET city_age_rank = r.age_rank
FROM ranked r
WHERE u.id = r.id;
```

---

### 24. 使用 COPY 或批量导入后更新（结合使用）
```sql
-- 先导入临时表，再更新主表
UPDATE users u
SET import_status = '成功', import_time = NOW()
FROM temp_import t
WHERE u.id = t.user_id;
```

---

### 总结：PostgreSQL UPDATE 核心功能

| 功能 | 说明 |
|------|------|
| `SET column = value` | 基础字段更新 |
| `WHERE` 条件 | 精确控制更新范围 |
| `FROM table` | 多表关联更新 |
| `RETURNING` | 返回更新后数据 |
| `ON CONFLICT` | 不适用（仅 INSERT 支持） |
| `CTE + UPDATE` | 复杂逻辑更新 |
| `JSON/数组更新` | 支持结构化数据 |
| `LIMIT` | 限制更新行数（PostgreSQL 特有） |

---

### ✅ 最佳实践建议

1. **始终使用 WHERE 条件**：避免误更新全表。
2. **先 `SELECT` 验证条件**：确保 WHERE 正确。
3. **使用事务包装 UPDATE**：便于出错回滚。
4. **关键更新使用 `RETURNING`**：获取结果更高效。
5. **避免无条件更新**：如 `UPDATE users SET col = val`。
6. **批量更新优先使用 `FROM` 或 `IN (SELECT)`**：性能优于逐条更新。
7. **使用触发器自动维护 `updated_at`**：保证时间字段准确。

---

PostgreSQL 的 `UPDATE` 语句不仅支持基础修改，还提供 **多表关联、RETURNING、CTE、JSON、数组、条件更新** 等高级功能，是实现数据维护和业务逻辑的核心工具。合理使用可确保数据一致性与操作高效性。