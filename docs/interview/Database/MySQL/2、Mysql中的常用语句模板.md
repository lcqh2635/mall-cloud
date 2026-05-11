

数据库使用规则：

- 数据库名称通常不需要引号或反引号进行引用，除非它包含特殊字符或与MySQL的保留字冲突。
- 在MySQL中，表名称、字段名称通常使用反引号（`）来引用，而不是单引号或双引号。使用反引号是为了区分字段名称与MySQL的保留字或关键字之间的冲突。反引号在MySQL中是可选的，除非字段名称与保留字冲突或包含特殊字符。在大多数情况下，字段名称是不需要引号的，直接写在SQL语句中即可。

#### 哪类SQL可以回滚？

在MySQL中，DDL（数据定义语言）和一些特定的DML（数据操作语言）操作是不可回滚的，而其他DML操作是可以回滚的。

DDL操作（如CREATE、ALTER、DROP、TRUNCATE等）通常会立即生效，并且无法回滚到之前的状态。所以，在执行这些操作之前，需要谨慎考虑和备份数据。

对于DML操作（如INSERT、UPDATE、DELETE等），如果在一个事务中执行，可以使用回滚操作将其还原到事务开始之前的状态。这意味着，如果在事务中执行DML操作后发生了错误或需要撤销更改，可以回滚事务，使数据回到修改前的状态。

需要注意的是，事务的回滚功能只适用于使用支持事务的存储引擎（如InnoDB）的表。对于使用不支持事务的存储引擎（如MyISAM）的表，无法执行回滚操作。

因此，当需要执行可能对数据产生重大影响的操作时，建议在事务中执行，并在必要时使用回滚操作来确保数据的一致性和完整性。



下面是MySQL中常用的语法模板示例：

1. 数据库相关：
   ```sql
   sudo apt install mysql-client-8.0 redis-server
   
   
   
   # 创建数据库
   CREATE DATABASE database_name;
   
   # 删除数据库
   DROP DATABASE database_name;
   
   # 修改数据库名称
   RENAME DATABASE old_database_name TO new_database_name;
   
   # 导入数据库，未登录情况下
   mysql -u username -p database_name < /path/to/dump.sql
   # 已经登录情况下
   mysql database_name < /path/to/dump.sql
   
   # 导出数据库，未登录情况下
   mysqldump -u username -p database_name > /path/to/dump.sql
   # 已经登录情况下
   mysqldump database_name > /path/to/dump.sql
   
   
   # 使用示例:
   DROP DATABASE IF EXISTS `database_name`;
   # 创建数据库时，全局统一设置字符集和校对规则，它将应用于当前数据库的所有表、字段等，成为默认设置
   CREATE DATABASE `database_name` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 
   USE `database_name`;
   ```

2. 数据表相关：

   ```sql
   # 创建数据表
   CREATE TABLE `table_name` (
       column1 datatype,
       column2 datatype,
       ...
   );
   
   # 基于已有的表创建一张新表，会包含数据
   CREATE TABLE new_table_name AS
   SELECT * FROM existing_table_name;
   # 该语句将创建一个新表，并将已有表中的所有列和数据复制到新表中。请注意，新表的定义和数据将完全复制自已有表，包括列名、数据类型、约束等。可以使用以下语句：
   CREATE TABLE new_table_name LIKE existing_table_name;
   # 这将创建一个新表，其结构与已有表相同，但不包含数据。
   
   # 该语句将创建一个新表，只包含已有表中指定字段的结构，但不包含数据。请确保所选字段在已有表中存在，并根据需要调整条件。
   CREATE TABLE new_table_name
   AS
   SELECT column1, column2, column3
   FROM existing_table_name
   WHERE condition;
   
   
   # 删除数据表
   DROP TABLE `table_name`;
   
   # 清空表数据，不可回滚
   # 语句是一个DDL（数据定义语言）语句，它会立即删除表中的所有数据，且无法回滚。如果你只想删除部分数据或者需要回滚操作，可以使用DELETE语句，但DELETE语句会逐行删除数据，效率可能较低。在执行任何删除操作之前，请务必备份重要的数据，以防止意外删除。
   TRUNCATE TABLE table_name;
   # 清空表数据，可以回滚
   DELETE FROM table_name;
   
   # 修改数据表名称
   RENAME TABLE `old_table_name` TO `new_table_name`;
   
   # 查看表结构
   DESCRIBE `table_name`
   DESC `table_name`
   
   # 使用示例:
   DROP TABLE IF EXISTS `table_name`;
   CREATE TABLE `table_name` 
   (
       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
       `order_no` bigint NOT NULL 
       `del_flag` char(1) DEFAULT '0' COMMENT '删除标识 -1: 已删除 0: 正常',
       `create_time` datetime DEFAULT NULL COMMENT '创建时间',
       `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
       `update_time` datetime DEFAULT NULL COMMENT '更新时间',
       `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
        PRIMARY KEY (`id`),
        UNIQUE KEY `unique_flag` (`order_no`)
   ) 
   ENGINE=InnoDB COMMENT='表描述';
   ```

3. 数据行相关：

   ```sql
   # 添加数据行
   INSERT INTO table_name (column1, column2, ...) VALUES (value1, value2, ...);
   
   # 删除数据行
   DELETE FROM table_name WHERE condition;
   
   # 修改数据行
   UPDATE table_name SET column1 = value1, column2 = value2, ... WHERE condition;
   
   # 查询数据行
   SELECT column1, column2, ... FROM table_name WHERE condition;
   
   # 简单的单表查询模板
   SELECT column1, column2, ... 
   FROM table_name 
   WHERE condition... AND / OR / NOT ...
   ORDER BY column1... ASC/DESC ...
   LIMIT offset_value, number_of_rows;
   ```

4. 数据列相关：
   ```sql
   # 增加数据列,指定该列的位置 [FIRST | AFTER column_name] 默认是最后
   ALTER TABLE table_name ADD COLUMN column_name data_type [NULL | NOT NULL] [DEFAULT default_value] COMMENT 'column_comment';
   
   ALTER TABLE t_user ADD COLUMN nick_name VARCHAR(30) [NULL | NOT NULL] [DEFAULT default_value] [FIRST | AFTER column_name] COMMENT 'column_comment';
   
   # 删除数据列
   ALTER TABLE table_name DROP COLUMN column_name;
   
   # 修改数据列，字段名一旦设计好了就不要修改，我们只能修改字段的某些属性
   ALTER TABLE table_name MODIFY COLUMN column_name new_data_type [NULL | NOT NULL] [DEFAULT default_value];
   # 修改字段名称
   ALTER TABLE table_name RENAME COLUMN old_column_name TO new_column_name;
   ```

5. 数据行去重：
   ```sql
   # 可以同时对多个字段去重
   SELECT DISTINCT column_name1, column_name2, ...
   FROM table_name
   WHERE condition;
   ```

6. 对查询结果排序：

   **升序（asc）是默认的排序方式，可以省略不写**

   **order by 子句中的排序是按照其后面的字段顺序依次进行的，如果字段顺序不同，排序结果也会不同。**

   ```sql
   # DESC 降序，从大到小; ASC 升序，从小到大
   SELECT column1, column2, ...
   FROM table_name
   WHERE condition;
   ORDER BY column1 ASC/DESC;
   
   
   # 二级排序，即在第一级排序的基础上再进行第二级排序
   SELECT column1, column2, ...
   FROM table_name
   WHERE condition;
   ORDER BY column1 ASC/DESC column2 ASC/DESC;
   ```

7. 多表查询：

   ```mysql
   # 两张表
   SELECT alias1.column1, alias1.column2, alias2.column3, alias2.column4
   FROM table_name1 alias1, table_name2 alias2
   WHERE alias1.columnX = alias2.columnY;
   # 三张表
   SELECT alias1.column1, alias1.column2, alias2.column3, alias2.column4, alias3.column5
   FROM table_name1 alias1, table_name2 alias2, table_name3 alias3
   WHERE alias1.columnX = alias2.columnY
     AND alias2.columnZ = alias3.columnW;
    
   # 总结：如果需要在多个表上进行别名多表查询，则查询的条件个数至少是表个数减一。两个表则关联条件至少有一个，三个表则关联条件至少有两个
   ```

8. 子查询：

   ```sql
   # 内查询返回一个结果则为单行子查询; 返回多行数据则为多行子查询
   # 单行子查询使用 > >= < <=  = != 等运算符
   # 多行子查询使用 in 
   SELECT column1, column2, ...
   FROM table_name1
   WHERE columnN IN (
       SELECT column_name
       FROM table_name2
       WHERE condition
   );
   
   
   
   # 示例
   SELECT name, age
   FROM t_student ts
   WHERE ts.student_id IN (
       SELECT student_id
       FROM t_score
       WHERE subject = 'Math'
       AND score >= 90
   );
   ```

9. 连接查询：

   ```mysql
   SELECT column1, column2, ...
   FROM table_name1
   JOIN table_name2 ON table_name1.column = table_name2.column
   WHERE conditions;
   
   SELECT tor.order_id, tc.customer_name, tor.order_date
   FROM t_order tor
   JOIN t_customer tc ON tor.customer_id = tc.customer_id
   WHERE tor.order_date >= '2023-01-01';
   ```

10. 分页查询

   ```sql
   # LIMIT 关键字的位置是固定的，应该放在 OFFSET 关键字之前
   # 形式一，OFFSET、LIMIT 分开写。mysql8.x版本新特性
   SELECT column1, column2, ...
   FROM table_name1
   LIMIT number_of_rows OFFSET offset_value;
   
   # 形式二、直接在 LIMIT 中写，mysql5.x版本
   # 这种写法第一个数值一定是偏移量，第二个数值表示限定数量
   SELECT column1, column2, ...
   FROM table_name
   WHERE condition1
     AND condition2
     ...
   ORDER BY column1, column2, ...
   LIMIT offset_value, number_of_rows;
   
   # offset_value 指定偏移量，表示从第几行开始返回结果，number_of_rows 表示返回的行数。
   ```

11.分组

```mysql
# 聚合函数前面的字段通常都是分组字段。这些分组字段被称为分组列。因此，聚合函数前面的字段的数量决定了在GROUP BY子句中需要指定多少个字段进行分组
SELECT column1, column2, ..., aggregate_function(column)
FROM table_name
WHERE condition
GROUP BY column1, column2, ...

# 分组后使用 HAVING 过滤
SELECT column1, column2, ..., aggregate_function(column)
FROM table_name
WHERE condition
GROUP BY column1, column2, ...
HAVING condition;

# 一般情况下，WHERE子句用于在分组前对原始数据进行过滤，而HAVING子句用于在分组后对聚合结果进行过滤。
# 当过滤条件中存在聚合函数时，使用HAVING进行过滤;反之，则使用WHERE进行过滤
# HAVING子句是在SQL中用于在分组后对分组结果进行过滤的条件语句。它通常与GROUP BY子句一起使用，用于筛选出符合特定条件的分组结果。不要单独使用，必须搭配GROUP BY子句一起使用

# 筛选出总销售额大于1000的客户，可以使用以下查询
SELECT customer_id, SUM(amount) as total_amount
FROM sales
WHERE condition
GROUP BY customer_id
HAVING SUM(amount) > 1000;

# WITH ROLLUP的作用是在分组结果中添加总计行，以便更方便地进行数据汇总和分析。
SELECT IFNULL("note", "合计总数") AS note, COUNT(*)
FROM books
GROUP BY note WITH ROLLUP
ORDER BY num DESC;
```

以上是MySQL中常用的语法模板示例，具体语句根据需要进行修改和补充。请注意，在实际使用时，需要根据具体的表名、列名和条件进行相应的替换。

#### 非等值连接、自连接、内连接和外连接

![img](https://s3.51cto.com/oss/201903/18/1c327cd4dcd88941d003719b71a9ad4c.jpg)

```mysql
# 中图，内连接。两表的交集就是关联字段,可以用在 ON 子句中进行条件关联匹配
SELECT column_list
FROM tableA ta
INNER JOIN tableB tb
ON ta.column_name = tb.column_name;

# 左上图，左外连接
SELECT column_list
FROM tableA ta
LEFT JOIN tableB tb
ON ta.column_name = tb.column_name;

# 右上图，右外连接
SELECT column_list
FROM tableA ta
RIGHT JOIN tableB tb
ON ta.column_name = tb.column_name;

# 左中图，
SELECT column_list
FROM tableA ta
LEFT JOIN tableB tb
ON ta.column_name = tb.column_name
WHERE tb.column_name IS NULL;

# 右中图
SELECT column_list
FROM tableA ta
RIGHT JOIN tableB tb
ON ta.column_name = tb.column_name
WHERE ta.column_name IS NULL;

# 左下图，
# 左上 UNION ALL 右中 / 右上 UNION ALL 左中
SELECT column_list
FROM tableA ta
LEFT JOIN tableB tb
ON ta.column_name = tb.column_name;
UNION ALL
SELECT column_list
FROM tableA ta
RIGHT JOIN tableB tb
ON ta.column_name = tb.column_name
WHERE ta.column_name IS NULL;


# 右下图
# 左中 UNION ALL 右中
SELECT column_list
FROM tableA ta
LEFT JOIN tableB tb
ON ta.column_name = tb.column_name
WHERE tb.column_name IS NULL
UNION ALL
SELECT column_list
FROM tableA ta
RIGHT JOIN tableB tb
ON ta.column_name = tb.column_name
WHERE ta.column_name IS NULL;
```

SQL语句模板如下：

1. 非等值连接的SQL语句模板：
```sql
SELECT column_list
FROM table1, table2
WHERE condition1 [non-equi operator] condition2;
```

2. 自连接的SQL语句模板：
```sql
SELECT column_list
FROM table1 t1, table2 t2
WHERE t1.column = t2.column;
```

请注意，上述示例和模板中的"column_list"表示要查询的列，"table1"和"table2"表示要连接的表，"condition"表示关联条件，"non-equi operator"表示非等值比较运算符（如"<"、">"等）等。具体的表名、列名和条件根据实际情况进行替换。

#### UNION和UNION ALL的区别

UNION ALL 是用于合并两个或多个 SELECT 语句的结果集的操作符，与 UNION 不同的是，它会保留所有的行，包括重复的行。

区别如下：

1. UNION 会去除重复的行，而 UNION ALL 不会。如果你希望保留所有行，包括重复的行，可以使用 UNION ALL。
2. UNION 需要对结果集进行去重的操作，**因此在某些情况下可能会比 UNION ALL 操作稍慢一些**。

推荐使用哪个操作符取决于你的需求。如果你需要合并结果集并去除重复的行，可以使用 UNION。如果你希望保留所有行，包括重复的行，可以使用 UNION ALL。根据具体情况选择适合的操作符可以提高查询的效率和准确性。

#### MySQL中的自然连接

MySQL中的自然连接是一种连接操作，它根据两个表之间的相同列名自动进行连接。当两个表中存在相同列名时，自然连接将返回所有相同列名的匹配行，并将这些行组合成一个结果集。**自然连接可以被看作是内连接的一种特殊形式。**

自然连接在MySQL中可以通过使用"JOIN"关键字来实现。下面是一个示例：

```mysql
SELECT *
FROM Table1
NATURAL JOIN Table2;
# 自然连接可以被看作是内连接的一种特殊形式。两表中的所有两同字段都为 ON 的匹配条件
SELECT column_list
FROM tableA ta
INNER JOIN tableB tb
ON ta.column_name = tb.column_name;
```

在这个示例中，自然连接将根据两个表中的相同列名进行匹配，并返回匹配的行。

需要注意的是，自然连接可能会导致列名冲突，因为它会自动匹配相同列名。在处理自然连接结果时，你可能需要对列名进行适当的处理，以避免冲突。

#### MySQL数据库中查询过程分析

在MySQL数据库中，查询一张表的数据的过程可以大致分为以下几个步骤：

1. 解析查询语句：当用户发起查询请求时，MySQL会首先对查询语句进行解析，以确定查询的语法正确性和语义准确性。这个过程包括对表名、列名以及查询条件的解析。

2. 查询优化器：MySQL的查询优化器会根据查询语句和表的统计信息，选择最优的执行计划。执行计划是指确定数据获取方式、连接顺序以及使用哪些索引等决策。

3. 数据加载到内存：一旦选择了执行计划，MySQL会根据执行计划从磁盘读取相应的数据页并加载到内存中。这个过程涉及到磁盘IO操作，包括读取数据文件和索引文件。

4. 执行查询操作：一旦数据加载到内存中，MySQL会根据执行计划逐行或逐块地处理数据。这个过程包括数据过滤、排序、连接等操作，以及应用查询中的函数和操作符。

5. 返回结果：当查询操作完成后，MySQL会将查询结果返回给用户。结果可以是单个值、多个行或者多个表格，具体取决于查询语句的类型和结构。

需要注意的是，MySQL数据库在执行查询过程中会利用缓存机制来提高查询性能。例如，查询结果可以被缓存到内存中，下次相同的查询可以直接从缓存中获取结果，而无需再次执行查询操作。

此外，数据加载到内存的过程中，MySQL也会根据表的索引情况进行IO操作的优化，例如利用索引来加速数据的读取。索引是一种数据结构，可以帮助数据库快速定位和访问特定的数据。

综上所述，查询一张表的数据涉及到查询语句的解析、查询优化、数据加载到内存、执行查询操作以及返回结果等过程。这些步骤的具体执行方式和性能会受到多个因素的影响，包括表的大小、索引的设计、系统配置等。MySQL作为一种关系型数据库管理系统，提供了多种优化手段来提高查询性能，例如适当的索引设计、查询优化器的调整和硬件资源的优化等。

#### MySQL查询语句的子句执行顺序

MySQL查询语句的执行顺序是先执行`FROM`子句，然后是`WHERE`子句，最后是`SELECT`子句。这是一般情况下的执行顺序，不过在实际执行时，MySQL查询优化器可能会对查询进行优化和重排序。

下面是查询语句的一般执行顺序：

1. `FROM`子句：查询从指定的表或表别名中选择数据。这个阶段涉及从表中读取数据并生成一个虚拟的结果集，该结果集包含了所有需要的数据列。

2. `WHERE`子句：在这个阶段，查询会根据`WHERE`子句中的条件对虚拟结果集进行筛选，只保留满足条件的行。

3. `SELECT`子句：在这个阶段，查询会根据`SELECT`子句中指定的列从筛选后的结果集中选择需要返回的数据列。在这一步中，还可以使用聚合函数和其他列操作符来对数据进行处理和计算。

需要注意的是，虽然这是一般情况下的执行顺序，但MySQL查询优化器可能会根据查询的复杂性和索引情况对查询进行优化。这可能包括重新排序操作的顺序以提高查询性能。因此，实际执行的顺序可能会有所不同。

总结起来，MySQL查询的一般执行顺序是`FROM`子句，然后是`WHERE`子句，最后是`SELECT`子句，但实际执行顺序可能会受到查询优化器的干预而有所调整。

#### 多表查询分类

根据用户的要求，可以将多表查询分为两类：等值连接和非等值连接。

1. 等值连接（Equi Join）：等值连接是指通过使用相等的关联条件将两个或多个表中的行连接在一起的查询方式。在等值连接中，关联条件使用相等比较运算符（例如`=`）来比较两个表中的列，只返回满足条件的行。等值连接可以使用内连接（INNER JOIN）或者外连接（LEFT JOIN、RIGHT JOIN）来实现。这种连接方法常用于需要在两个表之间进行精确匹配的情况。

2. 非等值连接（Non-Equi Join）：非等值连接是指通过使用不等于的关联条件将两个或多个表中的行连接在一起的查询方式。在非等值连接中，关联条件使用不等比较运算符（例如`<`、`>`、`<=`、`>=`）来比较两个表中的列，返回满足条件的行。非等值连接常用于需要根据不同条件进行范围匹配或者比较的情况，例如查找某个表中大于或小于另一个表中某个列的值的行。

综上所述，根据关联条件的相等性，MySQL多表查询可以分为等值连接和非等值连接两类。在实际应用中，根据具体的查询需求选择适合的连接方式可以更准确地获取所需的结果。

#### 通过表别名进行多表查询

当使用表别名进行多表查询时，你可以为每个表指定一个别名，然后在查询语句中使用这些别名来引用表和指定关联条件。这种方法可以提高查询语句的可读性和简洁性。

以下是使用表别名进行多表查询的示例和语句模板：

示例：
假设我们有两个表：`customers`（客户表）和`orders`（订单表），它们之间通过`customer_id`列进行关联。我们想要查询客户及其对应的订单信息。

```sql
SELECT c.customer_id, c.customer_name, o.order_id, o.order_date
FROM customers c, orders o
WHERE c.customer_id = o.customer_id;
```

在上面的示例中，我们为`customers`表指定了别名`c`，为`orders`表指定了别名`o`。然后，在`WHERE`子句中，我们使用别名来指定两个表之间的关联条件`c.customer_id = o.customer_id`。通过这样的方式，我们可以同时引用两个表并获取它们之间的关联数据。

语句模板：
以下是使用表别名进行多表查询的语句模板：

```sql
SELECT alias1.column1, alias1.column2, alias2.column3, alias2.column4
FROM table_name1 alias1, table_name2 alias2
WHERE alias1.columnX = alias2.columnY;
```

在上面的语句模板中，你需要将`table_name1`和`table_name2`替换为实际的表名，`alias1`和`alias2`替换为你希望的表别名，`column1`、`column2`、`column3`等替换为你希望选择的列名，`columnX`和`columnY`替换为适当的关联条件。

请记住，使用表别名时要确保别名在查询语句中是唯一的，并且关联条件正确匹配表和列的关系，以获取正确的查询结果。

#### MySQL中，模糊查询和正则匹配哪个效率更高？

在MySQL中，通常情况下模糊查询比正则匹配的效率更高。模糊查询是通过使用通配符（如`%`和`_`）来匹配模式的搜索方式，而正则匹配则是通过使用正则表达式来匹配模式。

模糊查询通常使用`LIKE`操作符，可以在开始、中间或结尾使用通配符，例如：
```mysql
SELECT * FROM table_name WHERE column_name LIKE 'abc%'; -- 匹配以'abc'开头的值
SELECT * FROM table_name WHERE column_name LIKE '%abc%'; -- 匹配包含'abc'的值
SELECT * FROM table_name WHERE column_name LIKE '%abc'; -- 匹配以'abc'结尾的值
```

正则匹配使用`REGEXP`或`RLIKE`操作符，并且可以使用更复杂的模式进行匹配，例如：
```mysql
SELECT * FROM table_name WHERE column_name REGEXP 'abc.*'; -- 匹配以'abc'开头的值
SELECT * FROM table_name WHERE column_name REGEXP '.*abc.*'; -- 匹配包含'abc'的值
SELECT * FROM table_name WHERE column_name REGEXP '.*abc'; -- 匹配以'abc'结尾的值
```

**尽管正则匹配提供了更灵活的模式匹配能力，但它的执行速度通常比模糊查询慢。这是因为正则表达式需要进行更复杂的模式匹配操作，而模糊查询只需简单的通配符匹配。**

因此，在性能要求较高的情况下，如果可以使用模糊查询满足需求，通常会比正则匹配更高效。然而，具体的性能差异取决于数据量、查询条件的复杂程度以及数据库的优化策略等因素。在实际应用中，建议根据具体情况进行测试和评估，以确定最适合的查询方式。

#### MySQL中正则匹配使用

在MySQL中，正则匹配可以使用正则表达式来匹配模式。以下是一些在MySQL中进行正则匹配的常见用法：

1. REGEXP/RLIKE操作符：用于进行正则匹配。
   示例：
   ````sql
   SELECT * FROM table_name WHERE column_name REGEXP 'pattern'; -- 匹配符合正则表达式模式的值
   ```

2. ^和$符号：用于匹配字符串的开头和结尾。
   示例：
   ````sql
   SELECT * FROM table_name WHERE column_name REGEXP '^pattern$'; -- 匹配完全符合正则表达式模式的值
   ```

3. .符号：用于匹配任意单个字符。
   示例：
   
   ````sql
   SELECT * FROM table_name WHERE column_name REGEXP 'a.c'; -- 匹配以'a'开头，以'c'结尾，中间有一个任意字符的值
   ```
   
4. *和+符号：用于匹配前面的字符零次或多次（*）或一次或多次（+）。
   示例：
   ````sql
   SELECT * FROM table_name WHERE column_name REGEXP 'ab*c'; -- 匹配以'a'开头，以'c'结尾，中间有零个或多个'b'的值
   SELECT * FROM table_name WHERE column_name REGEXP 'ab+c'; -- 匹配以'a'开头，以'c'结尾，中间有一个或多个'b'的值
   ```

5. []符号：用于匹配括号内的任意一个字符。
   示例：
   ````sql
   SELECT * FROM table_name WHERE column_name REGEXP '[abc]'; -- 匹配包含'a'、'b'或'c'中任意一个字符的值
   ```

6. [^]符号：用于匹配不在括号内的任意一个字符。
   示例：
   ````sql
   SELECT * FROM table_name WHERE column_name REGEXP '[^abc]'; -- 匹配不包含'a'、'b'或'c'中任意一个字符的值
   ```

这些只是正则表达式的一部分用法，正则表达式还支持更多的元字符和语法。在实际使用中，可以根据需要编写适合的正则表达式来进行匹配。请注意，正则匹配的性能相对较低，因此在大数据量情况下，应谨慎使用并进行性能测试。

#### MySQL数据导入和导出操作

MySQL提供了几个命令行工具来进行数据导入和导出操作。以下是两个常用的命令：

`mysqldump`命令是用于导出数据库为SQL文件的工具，而不是用于导入SQL文件的工具。要导入SQL文件到MySQL数据库，可以使用`mysql`命令。

1. **导出数据**：使用mysqldump命令可以将MySQL数据库中的数据导出到一个文件中。下面是导出数据的示例命令：
   ````mysql
   mysqldump -u username -p database_name > dump.sql
   
   mysqldump -u username -p database_name > /path/to/dump.sql
   ```
   其中，`username`是数据库用户的用户名，`database_name`是要导出数据的数据库名称，`dump.sql`是导出数据保存的文件名。
   
   在执行该命令后，系统会提示输入密码。输入正确的密码后，命令将执行导出操作，并将数据库中的数据保存到`dump.sql`文件中。
   
   导入指定位置的SQL文件
   source /path/to/file.sql;
   
2. **导入数据**：使用mysql命令可以将之前导出的数据文件导入到MySQL数据库中。下面是导入数据的示例命令：
   ````mysql
   mysql -u username -p database_name < dump.sql
   
   mysql -u username -p database_name < /path/to/dump.sql
   ```
   其中，`username`是数据库用户的用户名，`database_name`是要导入数据的目标数据库名称，`dump.sql`是之前导出的数据文件。
   
   类似于导出数据命令，执行导入数据命令后，系统也会提示输入密码。输入正确的密码后，命令将令将执行导入操作，并将数据文件中的数据导入到指定的数据库中。

请注意，以上命令中的`username`需要替换为你实际使用的数据库用户名，`database_name`需要替换为你要导入或导出数据的数据库名称，`dump.sql`是你指定的数据文件名。

此外，还可以使用其他选项和参数来进一步自定义数据导入导出的行为，如限制导出的表、导入前清除目标数据库等。你可以查阅MySQL的官方文档或使用命令行工具的帮助命令（例如 `mysqldump --help` 和 `mysql --help`）了解更多详细信息。

#### 导入SQL文件时可以使用`mysql`命令或`source`命令

导入SQL文件时可以使用`mysql`命令或`source`命令的原因是它们都是MySQL客户端提供的功能，用于执行SQL语句或脚本。

使用`mysql`命令时，可以通过输入`mysql -u username -p`进入MySQL客户端的交互模式，然后逐行执行SQL语句或使用`source`命令加载并执行包含SQL语句的文件。例如，可以使用以下命令在MySQL客户端中执行SQL文件：

```bash
mysql -u username -p
```
然后在MySQL交互模式下执行：
```bash
USE database_name;
source /path/to/dump.sql;
```
这将使用指定的用户名登录MySQL，并打开交互式模式。然后，`USE`语句用于选择要导入数据的目标数据库，`source`命令用于加载并执行包含SQL语句的文件。

另一种方式是直接使用`mysql`命令来导入SQL文件，就像在之前的回答中提到的那样。这种方法可以在命令行中一次性导入整个SQL文件，而无需进入交互模式。

至于为什么可以使用这些方法导入SQL文件，这是因为MySQL客户端提供了这些功能，以便用户可以方便地执行SQL语句或加载并执行SQL文件。这样做可以将SQL文件中的内容导入到指定的数据库中，以便在数据库中创建表、插入数据或执行其他SQL操作。

希望这解答了你的疑问。如果还有其他问题，请随时提问。

### 子查询和连接查询的优劣

子查询和连接查询都是在数据库中进行表之间关联的常见查询方式，它们各有优劣，适用于不同的场景。以下是它们的一些优劣势比较：

子查询的优势：
1. 灵活性高：子查询可以嵌套在其他查询语句中，具有更大的灵活性，可以根据需要进行多层嵌套。
2. 可读性好：子查询通常比连接查询更容易理解和阅读，尤其对于复杂的查询逻辑。
3. 适用于小数据集：当处理的数据集较小时，使用子查询可能会更加高效，因为它只需要执行一次查询。

连接查询的优势：
1. 性能较好：连接查询通常能够利用数据库引擎的优化技术，对查询进行优化，从而提供更好的性能。
2. 可扩展性好：当处理大型数据集时，连接查询通常比子查询更具可扩展性和效率。
3. 操作灵活：连接查询可以使用不同类型的连接，如内连接、外连接和交叉连接，以满足不同的关联需求。

然而，子查询和连接查询也存在一些劣势：

子查询的劣势：
1. 可能较低的性能：当子查询的结果集较大时，可能会导致性能下降，尤其是嵌套多层子查询时。
2. 可读性较差：嵌套多个子查询可能会导致查询语句的可读性下降，难以理解和维护。

连接查询的劣势：
1. 复杂性高：连接查询涉及多个表之间的关联，可能需要在连接条件、过滤条件等方面进行更复杂的处理。
2. 可能产生冗余行：在进行连接查询时，如果没有正确的连接条件，可能会导致结果中出现冗余行。

综上所述，子查询和连接查询都有各自的优势和劣势。在实际使用中，应根据具体的需求、数据规模和性能要求来选择合适的查询方法。通常情况下，**连接查询更适合处理大型数据集和复杂关联查询，而子查询更适用于简单的查询逻辑和小规模数据集。**

#### 为什么子查询性能通常不如连接查询？

子查询和连接查询在性能上的差异通常取决于数据库引擎的优化器以及查询本身的复杂性。以下是一些常见的原因：

1. 数据量大时的性能差异：在大数据量的情况下，子查询往往需要多次执行，可能导致性能下降。而连接查询可以一次性把相关数据一起处理，减少了多次执行的开销。

2. 内部处理方式不同：某些数据库系统在优化连接查询时会使用更高效的方式，比如使用哈希表或者排序合并等技术，而对子查询的优化可能没有那么充分。

3. 优化器的选择：一些数据库引擎会根据查询的复杂度和数据分布来选择不同的执行计划，有时候优化器可能更倾向于选择连接查询而不是子查询。

4. 索引的使用：在一些情况下，数据库系统可能更容易通过索引来优化连接查询而不是子查询。

5. 查询语法的限制：一些数据库系统对子查询的优化支持可能不如连接查询。

虽然子查询的性能通常不如连接查询，但是并非所有情况下都如此。在一些特定的情况下，子查询可能比连接查询更适合，这取决于数据的分布、索引的使用、优化器的选择等因素。

总之，性能差异通常取决于具体的查询和数据库系统的实现，建议在实际应用中进行基准测试，根据实际情况选择合适的查询方式。

### 实际RBAC权限模型使用示例：

```sql
DROP DATABASE IF EXISTS `vue3_blog`;

CREATE DATABASE `vue3_blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `vue3_blog`;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` 
(
	`user_id` bigint NOT NULL COMMENT '用户ID',
	`username` varchar(64) NOT NULL COMMENT '用户名',
	`password` varchar(255) NOT NULL COMMENT '密码',
	`salt` varchar(255) DEFAULT NULL COMMENT '随机盐',
	`phone` varchar(20) DEFAULT NULL COMMENT '手机号',
	`avatar` varchar(255) DEFAULT NULL COMMENT '头像',
	`dept_id` bigint DEFAULT NULL COMMENT '部门ID',
	`lock_flag` char(1) DEFAULT '0' COMMENT '0-正常，9-锁定',
	`del_flag` char(1) DEFAULT '0' COMMENT '0-正常，1-删除',
	`create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
	`update_time` datetime DEFAULT NULL COMMENT '修改时间',
	`update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
 	 PRIMARY KEY (`user_id`),
 	 UNIQUE KEY `user_idx1_username` (`username`)
) 
ENGINE=InnoDB ROW_FORMAT=DYNAMIC COMMENT='用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$RpFJjxYiXdEsAGnWp/8fsOetMuOON96Ntk/Ym2M/RKRyU0GZseaDC', NULL, '17034642999', '', 1, '0', '0', '2018-04-20 07:15:18', '2019-01-31 14:29:07', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` 
(
	`role_id` bigint NOT NULL COMMENT '角色ID',
	`role_name` varchar(64) NOT NULL COMMENT '角色名称',
	`role_code` varchar(64) NOT NULL COMMENT '角色编号',
	`role_desc` varchar(255) DEFAULT NULL COMMENT '角色描述',
	`del_flag` char(1) DEFAULT '0' COMMENT '删除标识（0-正常,1-删除）',
	`create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
	`update_time` datetime DEFAULT NULL COMMENT '修改时间',
	`update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
	 PRIMARY KEY (`role_id`),
	 UNIQUE KEY `unique_role_code` (`role_code`)
) 
ENGINE=InnoDB ROW_FORMAT=DYNAMIC COMMENT='系统角色表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` VALUES (1, '管理员', 'ROLE_ADMIN', '管理员', '0', '2017-10-29 15:45:51', '2018-12-26 14:09:11', NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '普通用户','GENERAL_USER', '普通用户', '0', '2022-03-30 09:59:24', '2022-03-30 09:59:24', 'admin', 'admin');

-- 改写
INSERT INTO `sys_role` VALUES
(1, '管理员', 'ROLE_ADMIN', '管理员', '0', '2017-10-29 15:45:51', '2018-12-26 14:09:11', NULL, NULL),
(2, '普通用户','GENERAL_USER', '普通用户', '0', '2022-03-30 09:59:24', '2022-03-30 09:59:24', 'admin', 'admin');
COMMIT;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` 
(
 	`menu_id` bigint NOT NULL COMMENT '菜单ID',
 	`name` varchar(32) NOT NULL COMMENT '菜单名称',
 	`permission` varchar(32) DEFAULT NULL COMMENT '菜单权限标识',
 	`path` varchar(128) DEFAULT NULL COMMENT '前端URL',
 	`parent_id` bigint DEFAULT NULL COMMENT '父菜单ID',
 	`icon` varchar(32) DEFAULT NULL COMMENT '图标',
 	`sort_order` int NOT NULL DEFAULT '0' COMMENT '排序值',
 	`keep_alive` char(1) DEFAULT '0' COMMENT '0-开启，1- 关闭',
 	`type` char(1) DEFAULT NULL COMMENT '菜单类型 （0菜单 1按钮）',
 	`del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除标记(0--正常 1--删除)',
	`create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
	`update_time` datetime DEFAULT NULL COMMENT '修改时间',
	`update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
 	 PRIMARY KEY (`menu_id`)
)	 
ENGINE=InnoDB ROW_FORMAT=DYNAMIC COMMENT='菜单权限表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` VALUES ('1000', '权限管理', null, '/admin', '-1', 'icon-quanxianguanli', '1', '0', '0', '0', ' ', '2018-09-28 08:29:53', ' ', '2020-03-11 23:58:18');
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` 
(
	`user_id` bigint NOT NULL COMMENT '用户ID',
	`role_id` bigint NOT NULL COMMENT '角色ID',
	 PRIMARY KEY (`user_id`,`role_id`)
) 
ENGINE=InnoDB ROW_FORMAT=DYNAMIC COMMENT='用户角色表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` VALUES (1, 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` 
(
	`role_id` bigint NOT NULL COMMENT '角色ID',
	`menu_id` bigint NOT NULL COMMENT '菜单ID',
	 PRIMARY KEY (`role_id`,`menu_id`)
) 
ENGINE=InnoDB ROW_FORMAT=DYNAMIC COMMENT='角色菜单表';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_menu` VALUES (1, 1000);
INSERT INTO `sys_role_menu` VALUES (1, 1100);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
```

