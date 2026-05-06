### 查看用户权限

在 MySQL 中，有几种方法可以查看用户的权限。以下是两种常用的方法：

1. 使用 `SHOW GRANTS` 命令：
   登录到 MySQL 数据库服务器后，使用以下命令查看特定用户的权限：

   ```sql
   -- 查看当前用户权限
   SHOW GRANTS FOR CURRENT_USER;
   
   SHOW GRANTS FOR 'username'@'localhost';
   ```
   
   这里的 `'username'@'localhost'` 是你要查询权限的用户名和主机名。你可以根据需要修改为具体的用户名和主机名。
   
   该命令将返回该用户在指定主机上的权限列表。
   
2. 查询 `mysql.user` 表：
   运行以下命令查看所有用户的权限：

   ```sql
   -- host主机、user用户、authentication_string加密密码、plugin密码加密插件
   SELECT user, host, authentication_string FROM mysql.user;
   
   SELECT user, host, authentication_string, plugin FROM mysql.user;
   
   -- MySQL8.0密码加密规则：caching_sha2_password
   ALTER USER 'your_username'@'localhost' IDENTIFIED WITH 'new_plugin';
   -- 使用MySQL5.7密码加密插件进行加密，mysql_native_password
   ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '你的密码';
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '479368';

ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '479368';
   ```
   
   这将返回一个包含所有用户的列表，其中包括用户名、主机以及用于身份验证的加密密码字符串。
   
   此外，还可以查询 `mysql.tables_priv`、`mysql.columns_priv` 和其他类似的表来查看特定用户对特定表或列的权限。

请注意，执行上述操作需要具有适当的权限。如果你没有足够的权限来查看用户权限，你可能需要使用具有合适权限的管理员账户进行操作。

### 添加用户权限

要添加用户权限，你可以使用以下步骤：

1. 登录到 MySQL 数据库服务器，并使用具有足够权限的管理员账户。

2. 创建一个新用户或选择要为其授予权限的现有用户。如果要创建新用户，请运行以下命令：
   ```sql
   -- 创建一个新用户，可以选择设置密码过期
   CREATE USER 'username'@'localhost' IDENTIFIED BY 'password';
   CREATE USER 'username'@'localhost' IDENTIFIED BY 'password' PASSWORD EXPIRE [DEFAULT|NEVER|INTERVAL N DAY];
   
   
   -- 修改用户的密码
   ALTER USER 'username'@'localhost' IDENTIFIED BY 'new_password';
   -- 修改用户的用户名
   ALTER USER 'old_username'@'localhost' IDENTIFIED BY 'password' REPLACE 'new_username'@'localhost';
   -- 修改用户的主机
   ALTER USER 'username'@'old_host' IDENTIFIED BY 'password' REPLACE 'username'@'new_host';
   -- 修改用户的密码过期策略,可以选择`DEFAULT`（默认策略）、`NEVER`（密码永不过期）或`INTERVAL N DAY`（N天后密码过期）。
   ALTER USER 'username'@'localhost' PASSWORD EXPIRE [DEFAULT|NEVER|INTERVAL N DAY];
   -- 执行这些命令需要具有足够的权限，通常需要使用具有适当特权的MySQL用户（如root用户）登录
   
   -- 删除指定的用户
   DROP USER 'username'@'localhost';
   ```
   这里的 `'username'@'localhost'` 是你要创建的用户名和主机名，`'password'` 是密码。你可以更改这些值以适应你的需求。
   
3. 授予用户权限。根据需要，你可以为用户授予不同级别的权限，例如全局权限、数据库权限、表权限等。以下是一些示例命令：

   - 授予用户所有数据库的读取权限：
     ```sql
     GRANT SELECT ON *.* TO 'username'@'localhost';
     ```

   - 授予用户特定数据库的读写权限：
     ```sql
     GRANT SELECT, INSERT, UPDATE, DELETE ON database_name.* TO 'username'@'localhost';
     ```

   - 授予用户对特定表的权限：
     ```sql
     GRANT SELECT, INSERT, UPDATE, DELETE ON database_name.table_name TO 'username'@'localhost';
     
     -- 授予用户对特定表的所有权限， ALL PRIVILEGES。此时用户的权限和root超级管理员几乎相同，只差对权限的操作，root可以操作所有用户的权限，而其他的所有用户则没有这个权限。
     GRANT ALL PRIVILEGES ON database_name.table_name TO 'username'@'localhost';
     ```

   可以通过将权限列表替换为你所需的权限来自定义上述命令。

4. 使用 `FLUSH PRIVILEGES;` 命令刷新权限，使修改生效：
   ```sql
   FLUSH PRIVILEGES;
   ```

请注意，执行上述操作需要具有足够的权限。请使用具有合适权限的管理员账户进行操作。

### 删除用户权限

要删除 MySQL 用户的权限，你可以按照以下步骤进行操作：

1. 登录到 MySQL 数据库服务器，并使用具有足够权限的管理员账户。

2. 选择要删除权限的用户。可以使用以下命令查看当前存在的用户：
   ```sql
   SELECT user, host FROM mysql.user;
   ```
   根据查询结果选择要删除权限的用户。

3. 使用 `REVOKE` 命令撤销用户的权限。以下是一些示例命令：

   - 撤销用户所有数据库的某个权限：
     ```sql
     REVOKE SELECT ON *.* FROM 'username'@'localhost';
     ```

   - 撤销用户特定数据库的某个权限：
     ```sql
     REVOKE SELECT, INSERT, UPDATE, DELETE ON database_name.* FROM 'username'@'localhost';
     ```

   - 撤销用户对特定表的某个权限：
     ```sql
     REVOKE SELECT, INSERT, UPDATE, DELETE ON database_name.table_name FROM 'username'@'localhost';
     
     -- 撤销用户对特定表的所有权限
     REVOKE ALL PRIVILEGES ON database_name.table_name FROM 'username'@'localhost';
     ```

   可以根据需要替换权限列表，将其更改为你要撤销的具体权限。

4. 使用 `FLUSH PRIVILEGES;` 命令刷新权限，使修改生效：
   ```sql
   FLUSH PRIVILEGES;
   ```

请注意，执行上述操作需要具有足够的权限。请使用具有合适权限的管理员账户进行操作。

### 更新用户权限

要修改 MySQL 用户的权限，你可以按照以下步骤进行操作：

1. 登录到 MySQL 数据库服务器，并使用具有足够权限的管理员账户。

2. 选择要修改权限的用户。可以使用以下命令查看当前存在的用户：
   ```sql
   SELECT user, host FROM mysql.user;
   ```
   根据查询结果选择要修改权限的用户。

3. 使用 `REVOKE` 命令撤销用户的权限。以下是一些示例命令：

   - 撤销用户所有数据库的某个权限：
     ```sql
     REVOKE SELECT ON *.* FROM 'username'@'localhost';
     ```

   - 撤销用户特定数据库的某个权限：
     ```sql
     REVOKE SELECT, INSERT, UPDATE, DELETE ON database_name.* FROM 'username'@'localhost';
     ```

   - 撤销用户对特定表的某个权限：
     ```sql
     REVOKE SELECT, INSERT, UPDATE, DELETE ON database_name.table_name FROM 'username'@'localhost';
     ```

   注意：这是为了撤销用户的旧权限，以便在接下来的步骤中重新分配新权限。

4. 使用 `GRANT` 命令授予用户新的权限。以下是一些示例命令：

   - 授予用户所有数据库的新权限：
     ```sql
     GRANT SELECT, INSERT ON *.* TO 'username'@'localhost';
     ```

   - 授予用户特定数据库的新权限：
     ```sql
     GRANT SELECT, INSERT, UPDATE, DELETE ON database_name.* TO 'username'@'localhost';
     ```

   - 授予用户对特定表的新权限：
     ```sql
     GRANT SELECT, INSERT, UPDATE, DELETE ON database_name.table_name TO 'username'@'localhost';
     ```

   可以根据需要替换权限列表，将其更改为你要授予的具体权限。

5. 使用 `FLUSH PRIVILEGES;` 命令刷新权限，使修改生效。
   ```sql
   FLUSH PRIVILEGES;
   ```

请注意，执行上述操作需要具有足够的权限。请使用具有合适权限的管理员账户进行操作。

#### 创建角色并授予权限，MySQL8.0新增

要在MySQL中创建角色并授予权限，你可以按照以下步骤进行操作：

1. 创建角色：

   ````sql
   CREATE ROLE 'role_name';
   CREATE ROLE 'role_admin' 'role_manger' 'role_user';
   
   -- 删除角色
   DROP ROLE 'role_name';
   
   -- 查看角色权限
   SHOW GRANTS FOR 'role_name';

2. 授予权限给角色：

   ````sql
   GRANT permission1, permission2, ... ON database_name.table_name TO 'role_name';
   
   -- 回收角色权限
   REVOKE permission1, permission2, ... ON database_name.table_name FROM 'role_name';
   
   -- 回收用户角色
   REVOKE 'role_name' FROM 'username'@'localhost';
   
   GRANT SELECT, INSERT, UPDATE ON database_name.table_name TO 'role_name';
   GRANT SELECT, INSERT, UPDATE ON database_name.* TO 'role_name';
   GRANT ALL PRIVILEGES ON database_name.* TO 'role_name';

3. 将角色授予用户：

   ````sql
   GRANT 'role_name' TO 'username'@'localhost';
   
   
   使用以上命令，你可以将之前创建的角色授予特定用户。将`role_name`替换为角色名，`username`替换为要授予该角色的用户名，`localhost`表示该用户只能从本地连接到MySQL服务器。如果你希望允许该用户从任何主机连接，请将`localhost`替换为`'%'`。

请注意，执行这些命令需要具有足够的权限，通常需要使用具有适当特权的MySQL用户（如root用户）登录。在授予权限之前，请确保你理解每个命令的含义和影响，并根据需要进行适当的权限管理。

如果你有任何疑问，请随时提问。
