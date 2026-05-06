

### 下面是mysql5.7和8.0 版本设置用户名和密码：

在 MySQL 5.7 和 8.0 版本的中，基本操作都是相同的，8.0及之后的版本主要是对之前的版本进行了性能优化和功能扩展。

**特别注意点：数据库中一个用户需要通过用户名User和主机名Host两个选项进行唯一标识，只要两者有一个不同都代表是不同的用户。**

1. 登录到 MySQL 数据库服务器：
   ```mysql
   # 登录数据库服务器，username为用户名
   mysql -u username -p
   mysql -u username -p 123456 -h localhost -P 3306
   
   # 使用 use 切换到名为 mysql 的数据库
   use mysql;
   
   # 查看数据库管理系统的所有用户关键信息
   select host, user, plugin, authentication_string from user;
   
   # 创建一个数据库用户，这样创建的用户只有数据库的USAGE使用权，也就是登录和连接的权限，没有其他操作权限，这是最基础的权限。
   # username 为用户名User，hostname 为主机名 Host，password 为用户密码
   CREATE USER 'username'@'hostname' IDENTIFIED BY 'password';
   
   # 查看用户的权限
   SHOW GRANTS FOR 'username'@'hostname';
   
   # 对用户进行数据级别授权，database_name.* 表示将权限授予给指定数据库中的所有表
   GRANT SELECT, INSERT, UPDATE, DELETE ON database_name.* TO 'username'@'hostname';
   ```
   
2. 使用以下命令修改用户名或主机：
   ```sql
   # RENAME USER 中的USER指的是数据库管理系统的mysql数据库的user表，该表存放了一个数据库管理系统的所有用户信息
   # 我们可以修改 current_username 和 hostname 这两项，他们分别对应 User、Host
   RENAME USER 'current_username'@'hostname' TO 'new_username'@'hostname';
   
   # 只修改该用户名 User，从 test 改为 test_demo
   RENAME USER 'test'@'localhost' TO 'test_demo'@'localhost';
   
   # 只修改主机 Host，从 localhost 改为 %
   RENAME USER 'test'@'localhost' TO 'test'@'%';
   
   # 同时修改用户名 User 和主机 Host
   RENAME USER 'test'@'localhost' TO 'test_demo'@'%';
   
   # 也可以通过这种方式修改主机，不推荐使用
   UPDATE mysql.`user` SET HOST='%' WHERE USER='test_demo';
   ```
   
3. 使用以下命令修改密码：
   ```sql
   # MySQL 数据库中，IDENTIFIED BY 是一个用于创建或修改用户时的选项，用于指定用户的密码。
   # 修改用户密码，通过 'current_username'@'hostname' 来唯一确定用户，其中current_username代表用户名，hostname代表主机
   ALTER USER 'current_username'@'hostname' IDENTIFIED BY 'new_password';
   
   # 修改用户 'test'@'localhost' 的密码为 123456
   ALTER USER 'test'@'localhost' IDENTIFIED BY '123456';
   ALTER USER 'root'@'localhost' IDENTIFIED BY '479368';
   
   # 修改用户 'root'@'%' 的密码并指定密码加密方式。
   alter user 'root'@'%' identified with mysql_native_password by '123456';
   
   # 创建的数据库默认密码继承root，密码加密方式也继承root。从Mysql8.0开始，密码默认使用 caching_sha2_password 方式加密。
   # 如果我们想使用以前的密码加密方式 mysql_native_password 来加密，我们只需要修改root超级管理员的密码加密方式即可。
   ```
   
4. 刷新权限：
   ```sql
   FLUSH PRIVILEGES;
   ```

执行上述步骤后，你的用户名和密码就会被更新为新的值。

请注意，执行这些操作需要具有适当的权限。如果你没有足够的权限来修改用户名和密码，你可能需要使用具有合适权限的管理员账户进行操作。





```sql
use mysql;
select host, user, authentication_string, plugin from user;

创建的数据库默认密码继承root，密码加密方式也继承root。从Mysql8.0开始，密码默认使用 caching_sha2_password 方式加密。
如果我们想使用以前的密码加密方式 mysql_native_password 来加密，我们只需要修改root超级管理员的密码加密方式即可。

# 进入到数据库管理系统中的mysql数据库。
use mysql

# 查看mysql数据中的所有用户关键信息
select host, user, plugin, authentication_string from user;

# 设置数据库远程连接访问权限
update user set host='%' where user='nacos_config';

# 修改root超级管理员的密码和密码加密方式。
alter user 'root'@'%' identified with mysql_native_password by '你的密码';
alter user 'root'@'localhost' identified with mysql_native_password by '479368';
# 刷新权限
FLUSH PRIVILEGES;


# 普通修改数据库密码模板
alter user 'username'@'localhost' identified with mysql_native_password by 'new_password';

此外，需要特别注意的是，一个数据库尽量只有一个HOST，也就是说，一个数据库，要么它HOST是%，允许远程连接，要么HOST是localhost或者其他IP地址。不要一个数据库同时存在多个HOST配置项，避免访问权限互相影响。例如在配置Nacos使用Mysql数据库做配置持久化时，如果同时存在多个HOST配置项，则HOST为localhost的有效，其他无效，此时，Nacos连接Mysql就会失败。因此，一个数据库只保留一个HOST配置项，避免不必要的麻烦。
```

