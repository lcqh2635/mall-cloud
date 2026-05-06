

在MySQL中，以下是一些常用的关键字，按照作用对象划分如下：

1. SELECT：从数据库中选择需要检索的行数据。
2. INSERT：将新数据插入到数据库表中的行。
3. UPDATE：更新数据库表中的行数据。
4. DELETE：从数据库表中删除行数据。
5. WHERE：指定条件，筛选满足条件的数据行或数据列。
6. ORDER BY：按照指定的列对查询结果进行排序。
7. ASC：用于按升序对查询结果进行排序。
8. DESC：用于按降序对查询结果进行排序。
9. LIKE：模糊匹配数据行或数据列。
10. LIMIT：用于限制查询结果的数量。
11. OFFSET：配合LIMIT使用，用于指定查询结果的偏移量。
12. COUNT：用于计算满足条件的记录数。
13. DISTINCT：用于返回唯一的结果集，排除重复的值。
14. IN：判断某个列的值是否在一组指定的值中。
15. BETWEEN：判断某个列的值是否在一个范围之间。
16. EXISTS：检查子查询是否返回结果。
17. NOT：用于否定条件或操作符。

16. AS：用于为列或表起别名，便于查询结果的理解和引用。
17. NULL：表示一个字段没有值，可以用于条件判断和数据处理。
18. IS NULL：用于判断一个字段是否为空。
19. UNION：用于合并多个查询结果集。
20. ON：用于指定连接条件。
21. JOIN：用于连接多个表进行查询。
22. HAVING：用于过滤分组后的结果。
23. GROUP BY：用于按照一个或多个列对查询结果进行分组。
24. AVG：用于计算某列的平均值。
25. SUM：用于计算某列的总和。
26. MAX：用于计算某列的最大值。
27. MIN：用于计算某列的最小值。

这些是MySQL中常用的关键字，可以通过组合和使用它们进行各种复杂的查询和操作。



1. **AS：用于为列或表起别名，便于查询结果的理解和引用。**

   以下是 `AS` 的用法示例：

   1. 为列起别名：

   ```sql
   SELECT column_name AS alias_name
   FROM table_name;
   ```

   在上面的示例中，`column_name` 是您要选择的列名，`alias_name` 是您为该列定义的别名。通过使用别名，您可以在查询结果中更清晰地标识和引用该列。

   2. 为表起别名：

   ```sql
   SELECT column_name
   FROM table_name AS alias_name;
   ```

   在上面的示例中，`table_name` 是您要选择的表名，`alias_name` 是您为该表定义的别名。通过使用别名，您可以更简洁地引用该表，并且在复杂的查询中，可以避免表名冲突。

   3. 为计算列起别名：

   ```sql
   SELECT expression AS alias_name
   FROM table_name;
   ```

   在上面的示例中，`expression` 是一个计算出的值或表达式，`alias_name` 是您为该计算列定义的别名。通过为计算列指定别名，可以使查询结果更易读，并且可以方便地引用该计算列。

   注意事项：
   - **使用 `AS` 关键字是可选的，您也可以省略它，直接为列或表指定别名**。
   - 别名通常用双引号或方括号括起来，以避免与 SQL 保留字冲突。
   - **别名在查询中只是临时的，不会修改原始列或表的名称**。

   示例：

   假设我们有一个名为 `employees` 的表，其中包含列 `first_name` 和 `last_name`。我们可以使用别名来简化查询和结果的理解：

   ```sql
   SELECT first_name AS "First Name", last_name AS "Last Name"
   FROM employees;
   ```

   这将选择 `first_name` 列，并用别名 `"First Name"` 表示；同时选择 `last_name` 列，并用别名 `"Last Name"` 表示。

   

1. **EXISTS：检查子查询是否返回结果。**

   在 SQL 中，`EXISTS` 是一个用于检查子查询是否返回结果的逻辑运算符。它主要用于判断某个条件下是否存在满足条件的记录。

   `EXISTS` 的基本语法如下：

   ```sql
   SELECT column1, column2, ...
   FROM table
   WHERE EXISTS (subquery);
   ```

   在上述示例中，`column1, column2, ...` 是要选择的列名，`table` 是要查询的表名，`subquery` 是一个子查询。

   `EXISTS` 子句中的子查询将为每一行执行一次，并返回一个结果集。如果子查询至少返回一行结果，则 `EXISTS` 返回 `TRUE`；否则，返回 `FALSE`。

   以下是一个使用 `EXISTS` 的示例：

   ```sql
   SELECT name, age
   FROM students
   WHERE EXISTS (
       SELECT *
       FROM scores
       WHERE scores.student_id = students.id
       AND scores.subject = 'Math'
   );
   ```

   上述示例中，我们从 `students` 表中选择学生的姓名和年龄，但只返回那些在 `scores` 表中存在数学成绩的学生。子查询检查 `scores` 表是否存在满足条件的记录，如果存在，则返回相应的学生信息。

   请注意，`EXISTS` 子查询中的 `SELECT` 语句通常使用 `*` 或者其他列名，并通过与外部查询相关联的条件进行筛选。

   `EXISTS` 的结果不依赖于子查询的实际返回值或列的选择，只要子查询返回一个或多个结果行，`EXISTS` 就会返回 `TRUE`。

   

2. **IN：用于在 WHERE 子句中指定多个值。**

   在 SQL 中，`IN` 运算符用于在 `WHERE` 子句中指定多个值。

   以下是 `IN` 运算符的用法示例：

   ```sql
   SELECT column_name
   FROM table_name
   WHERE column_name IN (value1, value2, value3, ...);
   ```

   在上面的示例中，`column_name` 是您要筛选的列名，`(value1, value2, value3, ...)` 是包含多个值的列表。查询将返回匹配这些值之一的行。

   例如，假设我们有一个名为 `customers` 的表，其中包含列 `customer_id` 和 `country`。我们可以使用 `IN` 运算符来选择 `country` 列中包含多个特定国家的行：

   ```sql
   SELECT customer_id, country
   FROM customers
   WHERE country IN ('USA', 'Canada', 'Mexico');
   ```

   上述示例将选择在 `'USA'`、`'Canada'` 或 `'Mexico'` 中的任何一个国家的行，并返回相应的 `customer_id` 和 `country` 值。

   `IN` 运算符还可以与子查询一起使用，以从子查询的结果集中选择特定的值。

   例如，假设我们有一个名为 `orders` 的表，其中包含列 `order_id` 和 `customer_id`。我们可以使用子查询和 `IN` 运算符来选择与特定客户相关的订单：

   ```sql
   SELECT order_id, order_date
   FROM orders
   WHERE customer_id IN (SELECT customer_id FROM customers WHERE country = 'USA');
   ```

   上述示例将选择与居住在美国的客户相关联的订单，并返回相应的 `order_id` 和 `order_date` 值。

   

3. **LIKE：用于在 WHERE 子句中进行模糊匹配。**

   在 SQL 中，`LIKE` 关键字用于在 `WHERE` 子句中进行模糊匹配。

   以下是 `LIKE` 关键字的用法示例：

   ```sql
   SELECT column_name
   FROM table_name
   WHERE column_name LIKE pattern;
   ```

   在上面的示例中，`column_name` 是您要筛选的列名，`pattern` 是用于模糊匹配的模式。查询将返回与模式匹配的行。

   `LIKE` 关键字通常与通配符一起使用，以在模式中表示不确定的部分。下面是两种常用的通配符：

   - `%`：表示任意字符序列（包括空字符序列）。
   - `_`：表示任意单个字符。

   例如，假设我们有一个名为 `employees` 的表，其中包含列 `first_name` 和 `last_name`。我们可以使用 `LIKE` 关键字进行模糊匹配来选择以特定字母开头或结尾的姓名：

   - 以 `'J'` 开头的姓名：

   ```sql
   SELECT first_name, last_name
   FROM employees
   WHERE first_name LIKE 'J%';
   ```

   上述示例将选择以 `'J'` 开头的 `first_name` 列的值，并返回相应的 `first_name` 和 `last_name`。

   - 以 `'son'` 结尾的姓名：

   ```sql
   SELECT first_name, last_name
   FROM employees
   WHERE last_name LIKE '%son';
   ```

   上述示例将选择以 `'son'` 结尾的 `last_name` 列的值，并返回相应的 `first_name` 和 `last_name`。

   您还可以在模式中使用多个通配符来进行更复杂的模糊匹配。例如，使用 `%` 和 `_` 的组合来匹配特定字符序列的位置和长度。

   

4. **`LIMIT` 关键字用于限制查询结果的数量，而 `OFFSET` 关键字则配合 `LIMIT` 使用，用于指定查询结果的偏移量。**

   以下是 `LIMIT` 和 `OFFSET` 关键字的用法示例：

   ```sql
   SELECT column_name
   FROM table_name
   LIMIT number_of_rows;
   ```

   在上述示例中，`column_name` 是您要查询的列名，`table_name` 是要查询的表名，`number_of_rows` 是要返回的行数。查询将返回最多指定数量的行。

   例如，假设我们有一个名为 `products` 的表，其中包含列 `product_id`、`product_name` 和 `price`。我们可以使用 `LIMIT` 关键字来限制查询结果的数量：

   ```sql
   SELECT product_id, product_name, price
   FROM products
   LIMIT 5;
   ```

   上述示例将返回 `products` 表中的前 5 行，并列出每行的 `product_id`、`product_name` 和 `price`。

   如果您想要跳过前面的一些行并返回接下来的行，可以结合使用 `LIMIT` 和 `OFFSET` 关键字：

   ```sql
   SELECT product_id, product_name, price
   FROM products
   OFFSET offset_value
   LIMIT number_of_rows;
   ```

   在上述示例中，`offset_value` 是要跳过的行数。查询将返回从指定偏移量开始的指定行数。

   例如：

   ```sql
   SELECT product_id, product_name, price
   FROM products
   OFFSET 10
   LIMIT 5;
   ```

   上述示例将从 `products` 表中的第 11 行开始，返回接下来的 5 行，并列出每行的 `product_id`、`product_name` 和 `price`。

   使用 `LIMIT` 和 `OFFSET` 关键字可以很方便地分页查询结果，例如在网页上展示分页数据。

   

5. **IS NULL：用于判断一个字段是否为空。**

   在 SQL 中，`IS NULL` 是用于判断一个字段是否为空的操作符。

   以下是 `IS NULL` 的用法示例：

   ```sql
   SELECT column_name
   FROM table_name
   WHERE column_name IS NULL;
   ```

   在上述示例中，`column_name` 是您要查询的列名，`table_name` 是要查询的表名。查询将返回满足条件的行，即指定列中的值为空。

   例如，假设我们有一个名为 `employees` 的表，其中包含列 `employee_id`、`first_name` 和 `last_name`。我们可以使用 `IS NULL` 来查找没有填写 `last_name` 的员工：

   ```sql
   SELECT employee_id, first_name
   FROM employees
   WHERE last_name IS NULL;
   ```

   上述示例将返回 `employees` 表中 `last_name` 为空的员工的 `employee_id` 和 `first_name`。

   另外，还可以使用 `IS NOT NULL` 操作符来查找不为空的值，它与 `IS NULL` 正好相反。示例如下：

   ```sql
   SELECT employee_id, first_name
   FROM employees
   WHERE last_name IS NOT NULL;
   ```

   上述示例将返回 `employees` 表中 `last_name` 不为空的员工的 `employee_id` 和 `first_name`。

   使用 `IS NULL` 和 `IS NOT NULL` 可以判断字段是否为空，这在数据过滤和条件筛选中非常有用。

   

6. **JOIN：用于将多个表连接起来进行联合查询。**

   在 SQL 中，`JOIN` 是用于将多个表连接起来进行联合查询的操作，它允许您根据列之间的关系来获取相关联的数据。

   以下是 `JOIN` 的用法示例：

   ```sql
   SELECT column_name(s)
   FROM table1
   JOIN table2 ON table1.column_name = table2.column_name;
   ```

   在上述示例中，`column_name(s)` 是您要查询的列名，`table1` 和 `table2` 是要连接的两个表名，`column_name` 是用于连接两个表的列。

   `JOIN` 可以有不同的类型，包括 `INNER JOIN`、`LEFT JOIN`、`RIGHT JOIN`、`FULL JOIN` 等。如果不显示指定连接类型，默认将是INNER JOIN 内连接。这里给出一些常见的 `JOIN` 类型及其用法：

   1. `INNER JOIN`：返回两个表中符合连接条件的匹配行。

      ```sql
      SELECT column_name(s)
      FROM table1
      INNER JOIN table2 ON table1.column_name = table2.column_name;
      ```

   2. `LEFT JOIN`：返回左表中的所有行，以及与右表匹配的行。

      ```sql
      SELECT column_name(s)
      FROM table1
      LEFT JOIN table2 ON table1.column_name = table2.column_name;
      ```

   3. `RIGHT JOIN`：返回右表中的所有行，以及与左表匹配的行。

      ```sql
      SELECT column_name(s)
      FROM table1
      RIGHT JOIN table2 ON table1.column_name = table2.column_name;
      ```

   4. `FULL JOIN`：返回左表和右表中的所有行，不论是否匹配。

      ```sql
      SELECT column_name(s)
      FROM table1
      FULL JOIN table2 ON table1.column_name = table2.column_name;
      ```

   在 `ON` 子句中，您可以指定连接条件，以便确定如何将两个表关联起来。连接条件通常基于列之间的相等条件。

   例如，假设我们有两个表 `orders` 和 `customers`，它们通过一个共同的列 `customer_id` 关联。我们可以使用 `JOIN` 进行联合查询：

   ```sql
   SELECT orders.order_id, customers.customer_name
   FROM orders
   JOIN customers ON orders.customer_id = customers.customer_id;
   ```

   上述示例将返回符合连接条件的 `orders` 和 `customers` 表中的数据，即根据 `customer_id` 关联两个表，并返回 `order_id` 和 `customer_name`。

   这只是 `JOIN` 的基本用法。在实际应用中，根据需要可能还会用到更复杂的连接操作，如多表联接、使用别名等。了解和灵活运用 `JOIN` 可以帮助您处理复杂的数据查询需求。

   

7. **UNION：用于合并多个查询结果集。**

   在 SQL 中，`UNION` 是用于合并多个查询结果集的操作符。它可以将两个或多个具有相同列数和数据类型的查询结果合并成一个结果集。

   `UNION` 的基本语法如下：

   ```sql
   SELECT column1, column2, ...
   FROM table1
   WHERE condition1
   UNION
   SELECT column1, column2, ...
   FROM table2
   WHERE condition2;
   ```

   在上述示例中，`column1, column2, ...` 是要选择的列名，`table1` 和 `table2` 是要查询的表名，`condition1` 和 `condition2` 是可选的筛选条件。每个 `SELECT` 语句表示一个单独的查询。

   **`UNION` 操作要求两个查询结果的列数和数据类型必须相同**。它会将两个查询结果的行按顺序合并，并去重最终的结果集。去重是指如果存在重复的行，`UNION` 只会返回一次。

   请注意，**`UNION` 默认会执行去重操作**，如果您希望包含重复的行，请使用 `UNION ALL`。`UNION ALL` 不会去除重复的行，只是简单地将多个结果集合并在一起。

   以下是一个使用 `UNION` 的示例：

   ```sql
   SELECT name, age
   FROM students
   WHERE age >= 20
   UNION
   SELECT name, age
   FROM employees
   WHERE age >= 30;
   ```

   上述示例中，我们从 `students` 表和 `employees` 表中选择满足年龄条件的姓名和年龄，并将两个结果集合并在一起。最终的结果集将包含满足条件的学生和员工的信息。

   `UNION` 可以用于合并任意数量的查询结果集，只需按照需要添加更多的 `SELECT` 语句即可。

   

8. **GROUP BY：用于按照一个或多个列对查询结果进行分组。**

   在 SQL 中，`GROUP BY` 用于按照一个或多个列对查询结果进行分组。它将具有相同值的行分组在一起，并允许您对每个组应用聚合函数（如 `SUM`、`COUNT`、`AVG` 等）获取分组级别的汇总数据。

   `GROUP BY` 的基本语法如下：

   ```sql
   SELECT column1, column2, ..., aggregate_function(column)
   FROM table
   WHERE condition
   GROUP BY column1, column2, ...;
   ```

   在上述示例中，`column1, column2, ...` 是要选择的列名，`table` 是要查询的表名，`condition` 是可选的筛选条件。通过 `GROUP BY` 子句指定要分组的列。

   以下是一个使用 `GROUP BY` 的示例：

   ```sql
   SELECT department, AVG(salary) as average_salary
   FROM employees
   GROUP BY department;
   ```

   上述示例中，我们从 `employees` 表中选择部门和平均薪资，并按照部门进行分组。`AVG(salary)` 是对薪资列应用平均函数的示例。结果将返回每个部门的平均薪资。

   `GROUP BY` 子句可以包含多个列，这样就可以按照多个列的组合进行分组。

   以下是一个使用多个列的 `GROUP BY` 示例：

   ```sql
   SELECT department, country, COUNT(*) as employee_count
   FROM employees
   GROUP BY department, country;
   ```

   上述示例中，我们将员工表按照部门和国家进行分组，并使用 `COUNT(*)` 函数计算每个部门和国家的员工数量。

   在 `GROUP BY` 子句后面，您可以使用聚合函数来对每个组进行汇总操作。常见的聚合函数包括 `SUM`、`COUNT`、`AVG`、`MIN`、`MAX` 等。

   请注意，在 `SELECT` 语句中，除了被分组的列和聚合函数外，其他列都必须在 `GROUP BY` 子句中列出或使用聚合函数进行处理。

#### 其他关键字扩展

在MySQL中，`SET`语句用于设置会话级别或全局级别的系统变量的值。系统变量控制着MySQL服务器的行为和配置。下面是关于`SET`语句、`GLOBAL`和`DECLARE`的特点和用法的说明：

1. `SET`语句：`SET`语句用于设置**会话级别**的系统变量的值。会话级别的变量只对当前会话有效，并在会话结束时重置为默认值。`SET`语句的一般语法如下：

   ````sql
   SET variable_name = value;
   ```

   例如，要设置会话的`max_connections`变量为100，可以使用以下语句：

   ````sql
   SET max_connections = 100;
   ```

2. `GLOBAL`关键字：`GLOBAL`关键字用于设置全局级别的系统变量的值。全局级别的变量对所有连接到MySQL服务器的会话都有效，并在服务器重启后仍保持设置的值。`GLOBAL`关键字可以与`SET`语句一起使用，以设置全局级别的变量。例如：

   ````sql
   SET GLOBAL max_connections = 100;
   ```

   请注意，设置全局级别的变量可能需要超级用户权限。

3. `DECLARE`语句：`DECLARE`语句用于在存储过程或函数中声明局部变量。它允许在存储过程或函数中定义一个变量，并为其指定类型。`DECLARE`语句的一般语法如下：

   ````sql
   DECLARE variable_name datatype [DEFAULT initial_value];
   ```
   
   例如，要在一个存储过程中声明一个整型变量`count`并初始化为0，可以使用以下语句：
   
   ````sql
   DECLARE count INT DEFAULT 0;
   ```
   
   这样，在存储过程中就可以使用`count`变量进行计算和操作。

总结一下，`SET`语句用于设置会话级别的系统变量的值，`GLOBAL`关键字用于设置全局级别的系统变量的值（需要超级用户权限），而`DECLARE`语句用于在存储过程或函数中声明局部变量。这些功能可以根据需要来控制和配置MySQL服务器的行为和操作。

