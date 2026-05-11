PostgreSQL 的 `SELECT` 语句是 SQL 中最核心的数据查询功能，用于从数据库中检索数据。以下是 PostgreSQL 中 `SELECT` 语句的**多种用法和详细中文注释示例**，涵盖基础到高级用法。

---

### 1. 基本查询：查询所有列
```sql
-- 从 users 表中查询所有列的所有数据
SELECT * FROM users;
```

---

### 2. 查询指定列
```sql
-- 仅查询用户的姓名和邮箱
SELECT name, email FROM users;
```

---

### 3. 使用 WHERE 条件筛选数据
```sql
-- 查询年龄大于 25 的用户
SELECT name, age FROM users WHERE age > 25;
```

---

### 4. 使用逻辑运算符（AND、OR、NOT）
```sql
-- 查询年龄大于 20 且城市为北京的用户
SELECT * FROM users WHERE age > 20 AND city = '北京';

-- 查询年龄小于 18 或城市为上海的用户
SELECT * FROM users WHERE age < 18 OR city = '上海';

-- 查询非管理员用户
SELECT * FROM users WHERE NOT is_admin;
```

---

### 5. 使用 IN 运算符匹配多个值
```sql
-- 查询城市为北京、上海或广州的用户
SELECT * FROM users WHERE city IN ('北京', '上海', '广州');
```

---

### 6. 使用 BETWEEN 范围查询
```sql
-- 查询年龄在 20 到 30 之间的用户（包含边界）
SELECT * FROM users WHERE age BETWEEN 20 AND 30;
```

---

### 7. 使用 LIKE 进行模糊匹配
```sql
-- 查询姓名以 '张' 开头的用户（% 表示任意字符序列）
SELECT * FROM users WHERE name LIKE '张%';

-- 查询邮箱包含 'gmail' 的用户
SELECT * FROM users WHERE email LIKE '%gmail%';

-- 查询姓名第二个字为 '小' 的用户（_ 表示单个字符）
SELECT * FROM users WHERE name LIKE '_小%';
```

---

### 8. 使用 ILIKE 实现不区分大小写的模糊匹配（PostgreSQL 特有）
```sql
-- 不区分大小写地查询邮箱包含 'GMAIL' 的用户
SELECT * FROM users WHERE email ILIKE '%gmail%';
```

---

### 9. 排序：ORDER BY
```sql
-- 按年龄升序排列用户
SELECT * FROM users ORDER BY age ASC;

-- 按姓名降序排列
SELECT * FROM users ORDER BY name DESC;

-- 多列排序：先按城市升序，再按年龄降序
SELECT * FROM users ORDER BY city ASC, age DESC;
```

---

### 10. 限制返回行数：LIMIT 和 OFFSET
```sql
-- 只返回前 5 条记录
SELECT * FROM users LIMIT 5;

-- 跳过前 10 条，返回接下来的 5 条（常用于分页）
SELECT * FROM users LIMIT 5 OFFSET 10;
```

---

### 11. 去重：DISTINCT
```sql
-- 查询所有不同的城市（去重）
SELECT DISTINCT city FROM users;
```

---

### 12. 别名：AS
```sql
-- 为列起别名
SELECT name AS 姓名, email AS 邮箱 FROM users;

-- 为表起别名（在复杂查询中常用）
SELECT u.name, o.order_date 
FROM users AS u 
JOIN orders AS o ON u.id = o.user_id;
```

---

### 13. 聚合函数（COUNT、SUM、AVG、MAX、MIN）
```sql
-- 统计用户总数
SELECT COUNT(*) AS 用户总数 FROM users;

-- 计算平均年龄
SELECT AVG(age) AS 平均年龄 FROM users;

-- 查询最大年龄
SELECT MAX(age) AS 最大年龄 FROM users;

-- 查询最小年龄
SELECT MIN(age) AS 最小年龄 FROM users;

-- 计算订单总金额
SELECT SUM(amount) AS 总金额 FROM orders;
```

---

### 14. 分组查询：GROUP BY
```sql
-- 按城市分组，统计每个城市的用户数量
SELECT city, COUNT(*) AS 人数 FROM users GROUP BY city;

-- 按城市分组，计算每个城市的平均年龄
SELECT city, AVG(age) AS 平均年龄 FROM users GROUP BY city;
```

---

### 15. 分组后筛选：HAVING
```sql
-- 查询用户数量大于 2 的城市
SELECT city, COUNT(*) AS 人数 
FROM users 
GROUP BY city 
HAVING COUNT(*) > 2;
```

> 注意：`HAVING` 用于对聚合结果进行筛选，而 `WHERE` 用于行级筛选。

---

### 16. 多表连接查询（JOIN）
```sql
-- 内连接：只返回匹配的记录
SELECT u.name, o.order_date, o.amount
FROM users u
INNER JOIN orders o ON u.id = o.user_id;

-- 左外连接：返回左表所有记录，右表无匹配则为 NULL
SELECT u.name, o.order_date
FROM users u
LEFT JOIN orders o ON u.id = o.user_id;

-- 右外连接：返回右表所有记录
SELECT u.name, o.order_date
FROM users u
RIGHT JOIN orders o ON u.id = o.user_id;

-- 全外连接：返回两个表的所有记录
SELECT u.name, o.order_date
FROM users u
FULL OUTER JOIN orders o ON u.id = o.user_id;
```

---

### 17. 子查询（Subquery）
```sql
-- 查询订单金额高于平均值的用户
SELECT name FROM users 
WHERE id IN (
    SELECT user_id FROM orders 
    WHERE amount > (SELECT AVG(amount) FROM orders)
);

-- 在 SELECT 中使用子查询（标量子查询）
SELECT name, 
       (SELECT AVG(amount) FROM orders WHERE user_id = users.id) AS 平均订单金额
FROM users;
```

---

### 18. 使用 CASE 表达式进行条件判断
```sql
-- 根据年龄分组显示年龄段
SELECT name, age,
       CASE 
           WHEN age < 18 THEN '未成年'
           WHEN age BETWEEN 18 AND 60 THEN '成年'
           ELSE '老年'
       END AS 年龄段
FROM users;
```

---

### 19. 使用窗口函数（Window Functions）
```sql
-- 为每个用户计算其订单金额的排名
SELECT user_id, amount,
       RANK() OVER (ORDER BY amount DESC) AS 排名
FROM orders;

-- 按用户分组计算每个订单在其用户内的累计金额
SELECT user_id, order_date, amount,
       SUM(amount) OVER (PARTITION BY user_id ORDER BY order_date) AS 累计金额
FROM orders;
```

---

### 20. 使用 CTE（Common Table Expression）提高可读性
```sql
-- 使用 CTE 查询订单金额前 3 的用户信息
WITH top_users AS (
    SELECT user_id, SUM(amount) AS total_amount
    FROM orders
    GROUP BY user_id
    ORDER BY total_amount DESC
    LIMIT 3
)
SELECT u.name, u.email, tu.total_amount
FROM users u
JOIN top_users tu ON u.id = tu.user_id;
```

---

### 21. 使用 UNION 合并多个查询结果
```sql
-- 合并北京和上海的用户列表（去重）
SELECT name, city FROM users WHERE city = '北京'
UNION
SELECT name, city FROM users WHERE city = '上海';

-- 使用 UNION ALL 保留重复项
SELECT name FROM users WHERE age < 20
UNION ALL
SELECT name FROM users WHERE city = '北京';
```

---

### 22. 使用 EXISTS 检查子查询是否存在结果
```sql
-- 查询有订单的用户
SELECT name FROM users u
WHERE EXISTS (
    SELECT 1 FROM orders o WHERE o.user_id = u.id
);
```

---

### 23. 使用数组和 JSON 查询（PostgreSQL 高级特性）
```sql
-- 假设 users 表有 hobbies 字段为 TEXT[] 数组类型
SELECT name, hobbies FROM users WHERE '读书' = ANY(hobbies);

-- 假设 profile 字段为 JSON 类型
SELECT name, profile->>'phone' AS 手机号 
FROM users 
WHERE profile->>'city' = '北京';
```

---

### 24. 使用 LATERAL 关键字进行横向连接
```sql
-- 对每个用户获取其最近的一笔订单
SELECT u.name, latest_order.amount, latest_order.order_date
FROM users u
CROSS JOIN LATERAL (
    SELECT amount, order_date
    FROM orders o
    WHERE o.user_id = u.id
    ORDER BY order_date DESC
    LIMIT 1
) AS latest_order;
```

---

### 25. 使用 VALUES 构造临时数据集
```sql
-- 构造一个临时表格进行查询
SELECT * FROM (
    VALUES ('张三', 25, '北京'),
           ('李四', 30, '上海')
) AS temp(name, age, city);
```

---

### 26. 使用 EXPLAIN 查看查询执行计划（调试用）
```sql
-- 查看查询的执行计划，用于性能优化
EXPLAIN SELECT * FROM users WHERE age > 30;
```

---

### 总结

PostgreSQL 的 `SELECT` 语句功能极其丰富，支持：

- 基础查询、条件筛选、排序、分页
- 聚合、分组、去重
- 多表连接、子查询、CTE
- 窗口函数、CASE 表达式
- 高级特性：JSON、数组、LATERAL、UNION 等

这些功能使得 PostgreSQL 能够处理从简单查询到复杂数据分析的各种场景。

> 💡 提示：合理使用索引、避免 `SELECT *`、优化 WHERE 条件可显著提升查询性能。