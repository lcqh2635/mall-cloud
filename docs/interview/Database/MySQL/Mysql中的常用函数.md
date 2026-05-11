MySQL中有很多常用函数，用于在查询和处理数据时进行各种操作和计算。以下是一些常见的MySQL函数及其使用示例：

1. `COUNT()`: 用于计算指定列或表中的行数。
   示例：
   ````mysql
   SELECT COUNT(*) FROM table_name;
   ```

2. `SUM()`: 用于计算指定列的总和。
   示例：
   ````mysql
   SELECT SUM(column_name) FROM table_name;
   ```

3. `AVG()`: 用于计算指定列的平均值。
   示例：
   ````mysql
   SELECT AVG(column_name) FROM table_name;
   ```

4. `MAX()`: 用于获取指定列的最大值。
   示例：
   ````mysql
   SELECT MAX(column_name) FROM table_name;
   ```

5. `MIN()`: 用于获取指定列的最小值。
   示例：
   ````mysql
   SELECT MIN(column_name) FROM table_name;
   ```

6. `UPPER()`: 用于将字符串转换为大写。
   示例：
   
   ````mysql
   SELECT UPPER(column_name) FROM table_name;
   ```
   
7. `LOWER()`: 用于将字符串转换为小写。
   示例：
   ````mysql
   SELECT LOWER(column_name) FROM table_name;
   ```

8. `CONCAT()`: 用于将多个字符串连接在一起。
   示例：
   ````mysql
   SELECT CONCAT(column1, ' ', column2) FROM table_name;
   ```

9. `SUBSTRING()`: 用于提取字符串的子串。
   示例：
   ````mysql
   SELECT SUBSTRING(column_name, start_index, length) FROM table_name;
   ```

10. `DATE_FORMAT()`: 用于将日期格式化为指定的字符串格式。
    示例：
    ```mysql
    SELECT DATE_FORMAT(date_column, '%Y-%m-%d') FROM table_name;
    ```

11. `ROUND()`: 用于对数值进行四舍五入。
    示例：
    ```mysql
    SELECT ROUND(column_name, decimal_places) FROM table_name;
    ```

12. `TRIM()`: 用于去除字符串两端的空格或指定字符。
    示例：
    ```mysql
    SELECT TRIM('   example   ') FROM table_name; -- 去除空格
    SELECT TRIM('.,!example!.,', '.,!') FROM table_name; -- 去除指定字符
    ```

13. `DATE()`: 用于提取日期或日期时间字段的日期部分。
    示例：
    ```mysql
    SELECT DATE(date_column) FROM table_name;
    ```

14. `CURDATE()`: 用于获取当前日期。
    示例：
    ```mysql
    SELECT CURDATE() FROM table FROM table_name;
    ```

15. `NOW()`: 用于获取当前日期和时间。
    示例：
    ```mysql
    SELECT NOW() FROM table_name;
    ```

16. `IFNULL()`: 用于判断某个值是否为NULL，并在为NULL时返回替代值。
    示例：
    ```mysql
    SELECT IFNULL(column_name, 'N/A') FROM table_name;
    ```

17. `CASE`: 用于在查询中实现条件逻辑。
    示例：
    ```mysql
    SELECT column_name,
           CASE
             WHEN condition1 THEN result1
             WHEN condition2 THEN result2
             ELSE result3
           END
    FROM table_name;
    ```

18. `COALESCE()`: 用于返回参数列表中的第一个非NULL值。
    示例：
    
    ```mysql
    SELECT COALESCE(column1, column2, 'N/A') FROM table_name;
    ```
    
19. `GROUP_CONCAT()`: 用于将多行数据按组合并为一个字符串。
    示例：
    ```mysql
    SELECT GROUP_CONCAT(column_name SEPARATOR ', ') FROM table_name GROUP BY group_column;
    ```

20. `DATE_ADD()`: 用于对日期进行加法操作。
    示例：
    ```mysql
    SELECT DATE_ADD(date_column, INTERVAL 1 DAY) FROM table_name;
    ```

21. `DATE_SUB()`: 用于对日期进行减法操作。
    示例：
    
    ```mysql
    SELECT DATE_SUB(date_column, INTERVAL 1 DAY) FROM table_name;
    ```
    
22. `RAND()`: 用于生成一个随机数。
    示例：
    
    ```mysql
    SELECT RAND() FROM table_name;
    ```

这只是一些MySQL中的常见函数，还有很多其他函数可以用于不同的操作和计算。你可以根据具体的需求和情况选择适合的函数进行使用。

#### MySQL中的时间日期函数

MySQL中的时间日期函数有很多，以下是一些常用的时间日期函数：

1. `NOW()`: 返回当前日期和时间。
   示例：SELECT NOW();

2. `CURDATE()`: 返回当前日期。
   示例：SELECT CURDATE();

3. `CURTIME()`: 返回当前时间。
   示例：SELECT CURTIME();

4. `DATE()`: 提取日期或日期时间字段的日期部分。
   示例：SELECT DATE(date_column) FROM table_name;

5. `TIME()`: 提取日期或日期时间字段的时间部分。
   示例：SELECT TIME(date_column) FROM table_name;

6. `YEAR()`: 提取日期或日期时间字段的年份。
   示例：SELECT YEAR(date_column) FROM table_name;

7. `MONTH()`: 提取日期或日期时间字段的月份。
   示例：SELECT MONTH(date_column) FROM table_name;

8. `DAY()`: 提取日期或日期时间字段的天数。
   示例：SELECT DAY(date_column) FROM table_name;

9. `HOUR()`: 提取日期或日期时间字段的小时数。
   示例：SELECT HOUR(date_column) FROM table_name;

10. `MINUTE()`: 提取日期或日期时间字段的分钟数。
    示例：SELECT MINUTE(date_column) FROM table_name;

11. `SECOND()`: 提取日期或日期时间字段的秒数。
    示例：SELECT SECOND(date_column) FROM table_name;

12. `DATE_FORMAT()`: 格式化日期或日期时间字段的显示方式。
    示例：SELECT DATE_FORMAT(date_column, '%Y-%m-%d') FROM table_name;

13. `TIMESTAMP()`: 将日期时间值转换为UNIX时间戳。
    示例：SELECT UNIX_TIMESTAMP(date_column) FROM table_name;

14. `UNIX_TIMESTAMP()`: 返回当前日期和时间的UNIX时间戳。
    示例：SELECT UNIX_TIMESTAMP();

15. `STR_TO_DATE()`: 将字符串转换为日期。
    示例：SELECT STR_TO_DATE('2023-10-06', '%Y-%m-%d');

16. `DATE_ADD()`: 对日期进行加法操作。
    示例：SELECT DATE_ADD(date_column, INTERVAL 1 DAY) FROM table_name;

17. `DATE_SUB()`: 对日期进行减法操作。
    示例：SELECT DATE_SUB(date_column, INTERVAL 1 DAY) FROM table_name;

18. `DATEDIFF()`: 计算两个日期之间的天数差。
    示例：SELECT DATEDIFF(date1, date2) FROM table_name;

19. `DATE_FORMAT()`: 格式化日期或日期时间字段的显示方式。
    示例：SELECT DATE_FORMAT(date_column, '%Y-%m-%d %H:%i:%s') FROM table_name;

20. `WEEK()`: 返回日期的周数。
    示例：SELECT WEEK(date_column) FROM table_name;

这些函数可以帮助你在MySQL中对时间日期数据进行操作和计算。如果你有具体的需求或问题，欢迎提出，我会尽力帮助你。

#### MySQL中的流程控制函数

MySQL中的流程控制函数主要包括条件判断和循环控制函数。以下是一些常用的流程控制函数和示例：

1. IF函数：根据条件判断返回不同的结果。**类似三元表达式**
   示例：
   
   ````sql
   SELECT IF(condition, true_value, false_value);
   ```
   
2. CASE函数：根据条件进行多重选择。**类似 Switch 表达式**
   示例：
   
   ````sql
   # CASE语句在MySQL中确实没有专门的字段名称，它返回的结果可以作为一个匿名的列。当你使用CASE语句进行SELECT查询时，可以使用 AS 给这个匿名列指定一个别名，
   
   # 搜索形式，CASE语句后面不跟具体的字段或表达式，而是直接根据条件进行判断，并返回相应的结果。
   SELECT 
     CASE 
       WHEN condition1 THEN result1
       WHEN condition2 THEN result2
       ELSE result3
     END AS column_name
   FROM table_name;
   
   # 简单形式，CASE语句后面跟随一个字段或表达式，然后根据字段或表达式的值进行匹配判断，并返回相应的结果。
   SELECT 
     CASE column_name
       WHEN value1 THEN result1
       WHEN value2 THEN result2
       ELSE result3
     END AS column_name
   FROM table_name;
   
3. IFNULL函数：判断字段值是否为NULL，如果是NULL则返回指定的默认值 default_value。 如果不是NULL则返回自己 column_name
   示例：
   
   ````sql
   SELECT IFNULL(column_name, default_value) FROM table_name;
   ```
   
4. COALESCE函数：返回参数列表中第一个非NULL的值。
   示例：
   ````sql
   SELECT COALESCE(value1, value2, value3) FROM table_name;
   ```

5. NULLIF函数：如果两个参数相等，则返回NULL，否则返回第一个参数值。
   示例：
   ````sql
   SELECT NULLIF(value1, value2) FROM table_name;
   ```

6. LEAST函数：返回参数列表中的最小值。
   示例：
   ````sql
   SELECT LEAST(value1, value2, value3) FROM table_name;
   ```

7. GREATEST函数：返回参数列表中的最大值。
   示例：
   ````sql
   SELECT GREATEST(value1, value2, value3) FROM table_name;
   ```

8. WHILE循环：在满足条件的情况下循环执行一段代码块。
   示例：
   ````sql
   SET @counter = 0;
   WHILE @counter < 10 DO
     -- 执行的代码块
     SET @counter = @counter + 1;
   END WHILE;
   ```

这些流程控制函数可以帮助你在MySQL中实现条件判断和循环控制的逻辑。请根据你的具体需求选择合适的函数并参考示例进行使用。如果你有进一步的问题，欢迎继续提问！