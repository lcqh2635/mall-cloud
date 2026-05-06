太好了！你使用的是 **Fedora 工作站**，并且已经安装了强大的数据库客户端 **DataGrip**，接下来我们来详细指导你如何在 Fedora 上安装和使用 **PostgreSQL**。

我们将提供两种方式：

1. **本地原生安装（直接安装到系统）**
2. **容器化安装（使用 Podman 或 Docker）**

两种方式各有优势：
- **本地安装**：性能好、适合长期开发、可作为默认数据库服务。
- **容器安装**：隔离性好、环境干净、易于管理多个版本、适合临时测试或项目隔离。

---

## ✅ 一、方式一：本地安装 PostgreSQL（推荐用于主开发环境）

### 步骤 1：更新系统包
```bash
sudo dnf update --refresh -y
```

### 步骤 2：安装 PostgreSQL 服务器和客户端
Fedora 默认仓库中包含 PostgreSQL，推荐安装最新稳定版（如 PostgreSQL 15 或 16）。

```bash
# 安装 PostgreSQL（以 PostgreSQL 15 为例）
# 参考 fedora 官方文档 https://docs.fedoraproject.org/zh_CN/quick-docs/postgresql/
sudo dnf install postgresql-server postgresql-contrib -y
```

> 💡 提示：你可以将 `15` 替换为 `16` 如果你的系统支持（Fedora 39+ 支持）。

### 步骤 3：初始化数据库集群
```bash
# 安装后，需要使用初始数据填充数据库。可以使用以下命令完成数据库初始化。它创建配置文件 postgresql.conf 和 pg_hba.conf
sudo postgresql-setup --initdb --unit postgresql
```

### 步骤 4：启动并设置开机自启
```bash
# 要手动启动 postgresql 服务器，请运行
sudo systemctl start postgresql
# 默认情况下，postgresql 服务器未运行且处于禁用状态。要将其设置为在启动时启动，请运行：
sudo systemctl enable postgresql
# 查看 PostgreSQL 服务状态
systemctl status postgresql
# PostgreSQL 在端口 5432（或您在 postgresql.conf 中设置的任何其他内容）上运行。在 firewalld 中，您可以像这样打开它：
# firewall-cmd --zone=public --list-ports | grep 5432
sudo firewall-cmd --permanent --add-port=5432/tcp
sudo firewall-cmd --reload
```

### 步骤 5：切换到 `postgres` 系统用户并进入数据库
PostgreSQL 安装后会创建一个系统用户 `postgres`，用于管理数据库。

```bash
# -u postgres 指定要切换到的用户为 postgres
# postgres 这个用户是数据库的超级管理员账户（对应数据库内的 postgres 角色）
# 它拥有对数据库数据目录（如 /var/lib/pgsql/data）的完全访问权限
# 使用 psql 进入 PostgreSQL 数据库环境（免密登录）
# psql 是 PostgreSQL 的官方命令行客户端工具，它是你与 PostgreSQL 数据库进行交互最直接、最强大、最常用的工具之一。
# 所以这命令的作用就是：以 postgres 用户的身份，使用 psql 进入 PostgreSQL 环境，并且用 sudo 提权来实现切换。
# 不推荐长期留在 postgres 用户下操作，避免误操作或权限滥用。 
sudo -u postgres psql
# 默认 postgres 管理员密码为空，此处设置为 postgres，需要先执行上面的命令进入 PostgreSQL shell 交互环境
ALTER USER postgres WITH PASSWORD 'postgres';
```

psql 是 PostgreSQL 的官方命令行客户端工具，它是你与 PostgreSQL 数据库进行交互最直接、最强大、最常用的工具之一。
进入 PostgreSQL shell：
```bash
psql
```

你会看到类似提示：
```
psql (15.3)
Type "help" for help.

postgres=#
```

不要在项目、应用中使用 postgres 超级用户，创建专用用户，如 devuser，并限制权限
### 步骤 6：创建新用户（角色）和数据库（建议不要用默认 postgres 用户开发）
```sql
-- 创建一个用于开发的用户（如 yourname）
CREATE USER devuser WITH PASSWORD 'devpass' CREATEDB;

-- 创建一个数据库，并归属该用户
CREATE DATABASE myappdb OWNER devuser;

-- 退出
\q
```
保持 postgres 用户仅用于管理，本地维护用 sudo -u postgres psql，不对外暴露、不写在项目、应用配置中

然后退出 `postgres` 用户：
```bash
exit
```

### 步骤 7：配置远程访问（可选，用于 DataGrip 连接）

编辑配置文件允许 TCP/IP 连接：

```bash
sudo cat /var/lib/pgsql/data/postgresql.conf
# 如果你担心误操作，可以先备份
sudo cp /var/lib/pgsql/data/postgresql.conf /var/lib/pgsql/data/postgresql.conf.bak
# 直接通过使用 sed 命令（Stream Editor）一行搞定
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" /var/lib/pgsql/data/postgresql.conf
# 验证是否修改成功，必须使用 sudo 管理员权限。下面两个命令二选一
sudo grep "listen_addresses" /var/lib/pgsql/data/postgresql.conf
sudo cat /var/lib/pgsql/data/postgresql.conf | grep "listen_addresses"
```

#### 配置客户端认证（允许你的用户登录）

编辑：
```bash
sudo cat /var/lib/pgsql/data/pg_hba.conf
# 如果你担心误操作，可以先备份
sudo cp /var/lib/pgsql/data/pg_hba.conf /var/lib/pgsql/data/pg_hba.conf.bak
# 修改 pg_hba.conf 允许本地密码访问，添加一行（例如允许所有本地 IP 使用密码登录）
sudo sed -i 's/^host    all             all             127\.0\.0\.1\/32            ident$/host    all             all             127.0.0.1\/32            md5/' /var/lib/pgsql/data/pg_hba.conf
# 0.0.0.0/0 表示允许来自任何 IP 的连接，仅限测试环境、内网调试、但绝不推荐用于生产环境。绝对不要用 0.0.0.0/0 直接暴露数据库！
# 127.0.0.1/32 仅限仅本机（localhost）的连接，本地开发、生产环境（推荐）
# 生产环境建议限制 IP 范围，如 192.168.1.0/24
# sudo sh -c "echo 'host    all             all             0.0.0.0/0               md5' >> /var/lib/pgsql/data/pg_hba.conf"
sudo sh -c "echo 'host    all             all             127.0.0.1/32               md5' >> /var/lib/pgsql/data/pg_hba.conf"
# 或者指定数据库和用户
sudo sh -c "echo 'host    myappdb             devuser             127.0.0.1/32               md5' >> /var/lib/pgsql/data/pg_hba.conf"
# 重启 PostgreSQL 服务
sudo systemctl restart postgresql
```

在文件末尾添加一行（允许 devuser 通过密码从本地连接）：
```conf
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    myappdb         devuser         127.0.0.1/32            md5
```

> `md5` 表示密码认证。

### 步骤 8：重启服务
```bash
sudo systemctl restart postgresql

# 指定用户和数据库
psql -U devuser -d myappdb -h localhost -p 5432
```

---

## ✅ 二、方式二：使用 Podman 或 Docker 安装 PostgreSQL（推荐用于隔离环境）

Podman 是 Fedora 默认的容器工具（无守护进程，更安全），用法几乎与 Docker 兼容。

### 步骤 1：确保 Podman 已安装（Fedora 默认自带）
```bash
podman --version
```

如果没有安装：
```bash
sudo dnf install podman -y
```

> 如果你更习惯 Docker，也可以安装 Docker Engine（见附录）。

---

### 步骤 2：拉取并运行 PostgreSQL 容器

```bash
podman run -d \
  --name postgres-dev \
  -e POSTGRES_USER=devuser \
  -e POSTGRES_PASSWORD=devpass \
  -e POSTGRES_DB=myappdb \
  -p 5432:5432 \
  -v postgres-data:/var/lib/postgresql/data \
  --restart unless-stopped \
  docker.io/postgres:15
```

#### 参数说明：
| 参数 | 说明 |
|------|------|
| `-d` | 后台运行 |
| `--name` | 容器名称 |
| `-e POSTGRES_USER` | 创建初始用户 |
| `-e POSTGRES_PASSWORD` | 用户密码 |
| `-e POSTGRES_DB` | 启动时创建的数据库 |
| `-p 5432:5432` | 映射主机 5432 端口 |
| `-v` | 数据持久化（使用命名卷） |
| `--restart` | 开机自启（需配合 systemd 生成服务） |

---

### 步骤 3：验证容器运行
```bash
podman ps
```

你应该看到 `postgres-dev` 正在运行。

---

### 步骤 4：（可选）生成 systemd 服务实现开机自启

Podman 支持将容器生成 systemd 服务：

```bash
podman generate systemd --name postgres-dev --files --new
```

输出会生成一个 `.service` 文件，如 `container-postgres-dev.service`

移动到用户服务目录：
```bash
mkdir -p ~/.config/systemd/user
mv container-postgres-dev.service ~/.config/systemd/user/
```

启用并启动服务：
```bash
systemctl --user daemon-reload
systemctl --user enable container-postgres-dev.service
systemctl --user start container-postgres-dev.service

# 设置开机自启（需启用 linger）
loginctl enable-linger $USER
```

> 💡 这样即使你重启系统，PostgreSQL 容器也会自动启动。

---

## ✅ 三、使用 DataGrip 连接 PostgreSQL

无论你使用 **本地安装** 还是 **容器安装**，只要 PostgreSQL 监听了 `5432` 端口，DataGrip 都可以连接。

### 步骤 1：打开 DataGrip → New Project → Database → New Datasource → PostgreSQL

填写以下信息：

| 字段 | 值 |
|------|-----|
| Host | `localhost` |
| Port | `5432` |
| Database | `myappdb` |
| User | `devuser` |
| Password | `devpass` |

### 步骤 2：点击 “Test Connection”
如果显示绿色对勾 ✅，说明连接成功！

### 步骤 3：保存数据源名称（如 `MyApp DB`）

现在你就可以：
- 浏览表
- 写 SQL 查询
- 设计数据库结构
- 导出数据等

---

## ✅ 四、两种方式对比总结

| 对比项 | 本地安装 | 容器安装（Podman/Docker） |
|--------|----------|-----------------------------|
| 安装难度 | 简单 | 简单 |
| 性能 | 更高 | 略低（几乎无感） |
| 数据持久化 | `/var/lib/pgsql/data` | 使用卷 `postgres-data` |
| 多版本支持 | 需模块切换 | 可同时运行多个版本 |
| 清理方便性 | 需手动卸载 | `podman rm -f postgres-dev` 即可 |
| 开机自启 | `systemctl enable postgresql` | 需生成 systemd 服务 |
| 推荐场景 | 主开发环境、生产类测试 | 项目隔离、学习、临时环境 |

---

## ✅ 五、附录：安装 Docker（可选）

如果你更习惯 Docker：

```bash
# 添加 Docker 官方仓库
sudo dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo

# 安装 Docker Engine
sudo dnf install docker-ce docker-ce-cli containerd.io -y

# 启动并启用
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户加入 docker 组（避免每次用 sudo）
sudo usermod -aG docker $USER
```

> 注销重新登录后生效。

之后你可以用 `docker` 命令替换 `podman`，其余命令完全一致。

---

## ✅ 六、后续建议

1. **推荐你先用容器方式快速上手**，避免污染系统环境。
2. 熟悉后可以切换到本地安装，作为长期开发数据库。
3. 在 DataGrip 中练习：
    - 创建表
    - 插入 JSONB 数据
    - 使用 `EXPLAIN` 分析查询
    - 写窗口函数

---

## 需要我为你提供什么？

我可以继续为你提供：
- Spring Boot + PostgreSQL 的 `application.yml` 配置
- DataGrip 快捷键和技巧
- PostgreSQL 常用命令速查表
- 如何备份和恢复数据库

随时告诉我你的下一步目标！🚀