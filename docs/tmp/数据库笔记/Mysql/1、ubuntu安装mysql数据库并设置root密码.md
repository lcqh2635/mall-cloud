在Ubuntu上安装MySQL 8并修改默认root密码的步骤如下：

1. 首先更新apt包列表以确保获取最新的软件包信息：
```bash
sudo apt update
```

2. 安装MySQL 8服务器：
```bash
sudo apt install mysql-server
```

3. 安装过程中，系统将提示您设置root用户的密码。请记住这个密码，因为您将需要使用它进行后续操作。

4. 安装完成后，可以运行以下命令来启动MySQL服务：
```bash
sudo systemctl start mysql
sudo systemctl status mysql
```

5. 接下来，您可以使用以下命令来进入MySQL Shell并使用root用户登录：
```bash
sudo mysql -u root -p
```

6. 一旦进入MySQL Shell，您可以使用以下命令来修改root用户的密码：
```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '新密码';
```
请将 `'新密码'` 替换为您希望设置的新密码。

7. 最后，执行以下命令以重新加载权限表，使更改生效：
```sql
FLUSH PRIVILEGES;
```

现在，您已成功安装MySQL 8并修改了默认root密码。您可以使用新密码登录MySQL Shell进行后续操作。