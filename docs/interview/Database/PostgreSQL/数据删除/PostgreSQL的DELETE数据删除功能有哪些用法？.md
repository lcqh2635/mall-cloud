PostgreSQL 的数据删除功能主要通过 `DELETE` 语句实现，用于从数据库表中删除一行或多行数据。以下是 **全面且详细的 `DELETE` 用法示例**，涵盖基础到高级场景，均附带中文注释说明。

---

### 1. 基本删除：删除满足条件的记录
```sql
-- 删除 id 为 5 的用户
DELETE FROM users WHERE id = 5;
```

---

### 2. 删除多条记录（使用 IN）
```sql
-- 删除 id 在指定列表中的用户
DELETE FROM users WHERE id IN (1, 2, 3);
```

---

### 3. 使用范围条件删除（BETWEEN）
```sql
-- 删除年龄在 10 到 15 岁之间的用户
DELETE FROM users WHERE age BETWEEN 10 AND 15;
```

---

### 4. 模糊匹配删除（LIKE）
```sql
-- 删除邮箱包含 'spam' 的用户
DELETE FROM users WHERE email LIKE '%spam%';
```

---

### 5. 删除空值（NULL）记录
```sql
-- 删除邮箱为空的用户
DELETE FROM users WHERE email IS NULL;
```

---

### 6. 删除非空值记录
```sql
-- 删除邮箱不为空的用户（谨慎使用！）
DELETE FROM users WHERE email IS NOT NULL;
```

---

### 7. 使用逻辑运算符组合条件
```sql
-- 删除北京地区且年龄小于 18 的用户
DELETE FROM users WHERE city = '北京' AND age < 18;

-- 删除非管理员或注册时间早于 2020 年的用户
DELETE FROM users WHERE NOT is_admin OR created_at < '2020-01-01';
```

---

### 8. 删除所有数据（清空表）
```sql
-- 删除表中所有记录（表结构保留）
DELETE FROM users;
```

> ⚠️ 注意：此操作不会重置自增 ID，且可能较慢（尤其大表）。建议使用 `TRUNCATE` 替代（见下文）。

---

### 9. 使用 LIMIT 限制删除数量（PostgreSQL 支持）
```sql
-- 仅删除一条满足条件的记录（可用于调试或分批删除）
DELETE FROM users WHERE age < 18 LIMIT 1;
```

> 💡 适用于需逐步删除的场景，避免误删大量数据。

---

### 10. 使用 RETURNING 返回被删除的数据
```sql
-- 删除用户并返回被删除的姓名和邮箱（便于日志或审计）
DELETE FROM users WHERE id = 10 
RETURNING name, email, deleted_at;
```

> `RETURNING` 是 PostgreSQL 强大特性，可用于获取删除前的数据。

---

### 11. 使用子查询定位要删除的记录
```sql
-- 删除没有订单记录的用户
DELETE FROM users 
WHERE id NOT IN (
    SELECT DISTINCT user_id FROM orders WHERE user_id IS NOT NULL
);
```

---

### 12. 使用 EXISTS 删除关联数据
```sql
-- 删除有异常订单的用户（存在金额为负的订单）
DELETE FROM users u
WHERE EXISTS (
    SELECT 1 FROM orders o 
    WHERE o.user_id = u.id AND o.amount < 0
);
```

---

### 13. 联合多表删除（使用 USING）
```sql
-- 删除 orders 表中属于已注销用户（users 表中已删除）的订单
DELETE FROM orders 
USING users 
WHERE orders.user_id = users.id 
  AND users.status = 'deleted';
```

> `USING` 允许在 DELETE 中引用其他表进行条件判断。

---

### 14. 使用 CTE（公共表表达式）进行复杂删除
```sql
-- 先找出要删除的用户，再执行删除并返回结果
WITH deleted_users AS (
    DELETE FROM users 
    WHERE last_login < '2020-01-01' 
    RETURNING id, name, email
)
SELECT '已删除用户: ' || name FROM deleted_users;
```

> 适合需要记录删除日志或链式操作的场景。

---

### 15. 使用 LATERAL 进行高级删除（较少见，但可行）
```sql
-- 示例：删除每个城市中最老的一位用户（按 id 排序）
DELETE FROM users u
USING (
    SELECT city, MIN(id) AS min_id 
    FROM users 
    GROUP BY city
) oldest_users
WHERE u.city = oldest_users.city 
  AND u.id = oldest_users.min_id;
```

---

### 16. 使用 TRUNCATE 快速清空表（替代 DELETE）
```sql
-- 快速清空 users 表（比 DELETE 快，且重置自增 ID）
TRUNCATE TABLE users;

-- 同时清空多个表
TRUNCATE TABLE users, orders, logs;

-- 清空并重置外键关联表（需 CASCADE）
TRUNCATE TABLE users CASCADE;
```

> ✅ `TRUNCATE` 优势：速度快、不逐行删除、可重置序列。  
> ❌ 限制：不能带 WHERE 条件，不能触发 DELETE 触发器（除非指定）。

---

### 17. 使用 DELETE FROM ... WHERE ... IN (SELECT ...) 的安全写法
```sql
-- 安全删除：确保子查询不会因 NULL 导致意外
DELETE FROM users 
WHERE id IN (
    SELECT user_id FROM login_attempts 
    WHERE attempt_time < NOW() - INTERVAL '30 days'
      AND success = false
);
```

---

### 18. 防止误删：使用事务（Transaction）
```sql
BEGIN;

DELETE FROM users WHERE age < 10;

-- 检查影响行数
GET DIAGNOSTICS row_count = ROW_COUNT;
RAISE NOTICE '将删除 % 行', row_count;

-- 确认无误后提交，否则 ROLLBACK
-- COMMIT;
-- ROLLBACK;
```

> 建议在生产环境删除前使用事务，避免不可逆错误。

---

### 19. 删除重复数据（保留一条）
```sql
-- 删除 users 表中 email 重复的记录，只保留每组中 id 最小的一条
DELETE FROM users u1
USING users u2
WHERE u1.email = u2.email 
  AND u1.id > u2.id;
```

---

### 20. 使用触发器（Trigger）记录删除日志
```sql
-- 创建触发器函数：将删除的用户记录插入日志表
CREATE OR REPLACE FUNCTION log_deleted_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO user_deletion_log (user_id, name, deleted_at)
    VALUES (OLD.id, OLD.name, NOW());
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER trigger_log_delete
    BEFORE DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION log_deleted_user();
```

> 删除时自动记录日志，便于审计和恢复。

---

### 21. 条件删除 + RETURNING + CTE 实现审计删除
```sql
-- 删除用户并记录到审计表
WITH deleted AS (
    DELETE FROM users 
    WHERE id = 100 
    RETURNING id, name, email, '手动删除' AS reason
)
INSERT INTO audit_log (table_name, record_id, action, details)
SELECT 'users', id, 'DELETE', jsonb_build_object('name', name, 'email', email, 'reason', reason)
FROM deleted;
```

---

### 总结：PostgreSQL 删除功能对比

| 方法 | 适用场景 | 是否可带 WHERE | 是否触发触发器 | 速度 | 是否重置序列 |
|------|----------|----------------|------------------|------|----------------|
| `DELETE FROM ...` | 精确删除部分数据 | ✅ | ✅ | 慢 | ❌ |
| `TRUNCATE TABLE` | 清空整个表 | ❌ | ❌（默认） | 快 | ✅ |
| `DELETE ... RETURNING` | 需要获取删除数据 | ✅ | ✅ | 慢 | ❌ |
| `USING` / `EXISTS` | 多表关联删除 | ✅ | ✅ | 中 | ❌ |

---

### 🛡️ 安全建议

1. **始终在 WHERE 条件中测试 SELECT**：先 `SELECT * FROM table WHERE ...` 确认范围。
2. **使用事务包装 DELETE**：便于出错回滚。
3. **备份重要数据**：删除前导出。
4. **避免在生产环境直接执行无 WHERE 的 DELETE**。
5. **使用 RETURNING 和日志表进行审计**。

---

PostgreSQL 的删除功能不仅强大，还支持高度灵活的条件控制、审计和性能优化，合理使用可确保数据安全与操作可控。