要在Manjaro上通过MySQL压缩包安装MySQL，你可以按照以下步骤进行操作：

1. 首先，访问MySQL的官方网站（https://dev.mysql.com/downloads/mysql/）并下载适用于Linux的MySQL压缩包。选择适合你系统的版本（如MySQL Community Server）和操作系统架构（如x86或x64）。

2. 下载完成后，将压缩包解压到你选择的目录中。

3. 打开终端，并使用root权限或具有sudo权限的用户执行以下命令，安装MySQL的依赖项：

   ````shell
   sudo mkdir /usr/local/mysql && cd /usr/local/mysql
   sudo wget https://dev.mysql.com/get/Downloads/MySQL-8.1/mysql-8.1.0-linux-glibc2.28-x86_64.tar.xz
   sudo tar -xf mysql-8.1.0-linux-glibc2.28-x86_64.tar.xz
   
   sudo pacman -Syu
   
   这将确保你的系统处于最新状态，并安装MySQL所需的依赖项。

4. 进入解压后的MySQL目录，执行以下命令以初始化MySQL安装：

   ````shell
   sudo ./bin/mysqld --initialize --user=mysql --basedir=/usr/local/mysql/mysql-8.1.0-linux --datadir=/usr/local/mysql/mysql-8.1.0-linux/data
   ```

   请将`/path/to/mysql/directory`替换为你选择的MySQL安装目录，将`/path/to/mysql/data/directory`替换为你想要存储MySQL数据的目录。

5. 初始化完成后，执行以下命令启动MySQL服务：

   ````shell
   sudo ./bin/mysqld_safe --user=mysql --basedir=/path/to/mysql/directory --datadir=/path/to/mysql/data/directory &
   ```

6. 现在，MySQL服务器应该已经在后台运行。你可以使用以下命令连接到MySQL服务器：

   ````shell
   sudo ./bin/mysql -u root -p
   ```
   
   
   mysql -u username -h localhost -P 3306 -p 123456
   输入你设置的root用户密码，即可登录到MySQL服务器。

这些步骤将帮助你通过MySQL压缩包手动安装MySQL。请注意，手动安装可能需要更多的配置和管理工作，并且不会自动处理依赖项和系统集成，所以使用包管理器是更常见和方便的方法。如果有任何疑问，请随时提问。



配置环境变量

~~~shell
sudo ./bin/mysqld --initialize --user=mysql --basedir=/usr/local/mysql --datadir=/usr/local/mysql/data
```

请将`/path/to/mysql/directory`替换为你选择的MySQL安装目录，将`/path/to/mysql/data/directory`替换为你想要存储MySQL数据的目录。


sudo systemctl start mysqld

sudo systemctl enable mysqld

sudo systemctl disable mysqld
~~~





