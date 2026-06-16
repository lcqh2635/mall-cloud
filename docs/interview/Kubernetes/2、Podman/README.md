这是一份为您定制的 **Podman 完全使用手册与指南**。内容涵盖了从基础介绍、与 Docker 的对比，到详细的命令速查表及进阶使用，力求结构清晰、内容详实。
---
# Podman 完全使用手册与指南
## 第一章：Podman 基本介绍
**Podman (Pod Manager)** 是一个用于开发、管理和运行 OCI (Open Container Initiative) 容器和容器镜像的轻量级开源工具。它由 Red Hat 主导开发，旨在成为 Docker 的直接替代品。
### 核心特性：
1. **无守护进程**：与 Docker 依赖 `dockerd` 后台守护进程不同，Podman 直接通过 API 与容器运行时交互。这意味着容器作为当前用户的子进程运行，提高了系统的稳定性（不会因为守护进程崩溃导致所有容器宕机）。
2. **Rootless 模式**：非特权用户可以在不拥有 root 权限的情况下运行容器，极大地提升了系统的安全性。
3. **Pod 原生支持**：Podman 原生支持 Kubernetes 中的 "Pod" 概念，允许将多个容器组在一个 Pod 中共享网络和存储命名空间，与 K8s 高度对齐。
4. **兼容 Docker CLI**：Podman 的命令行接口与 Docker 几乎完全一致。大部分情况下，只需将命令中的 `docker` 替换为 `podman` 即可（如 `podman run` 替代 `docker run`）。
5. **Systemd 集成**：Podman 可以轻松生成 systemd 服务文件，使得容器可以作为系统服务进行管理。
---
## 第二章：Podman vs Docker 深度对比
| 特性 | Docker | Podman |
| :--- | :--- | :--- |
| **架构模式** | 客户端-服务器 (C/S)，依赖 `dockerd` 守护进程 | 无守护进程，直接调用容器运行时 |
| **运行权限** | 默认需要 Root 权限运行守护进程 | 默认支持 Rootless（非特权用户运行） |
| **容器生命周期** | 依赖守护进程，守护进程崩溃容器可能受影响 | 容器作为用户子进程运行，相互独立 |
| **Pod 概念** | 不支持原生 Pod（依赖 K8s 或 Swarm） | 原生支持 Pod 概念，方便向 K8s 迁移 |
| **编排工具** | Docker Compose (原生) | 支持 `podman-compose` 及部分 `docker-compose` |
| **CLI 兼容性** | 原生 `docker` 命令 | 完美兼容大部分 `docker` 命令 |
| **Systemd 集成**| 需要手动编写服务文件或使用第三方工具 | 原生提供 `podman generate systemd` (或 Quadlet) |
| **安全性** | 守护进程以 root 运行，存在提权风险 | 默认 rootless，安全性更高 |
**总结**：
如果你需要一个成熟、生态丰富且团队已经非常熟悉的工具，Docker 是首选；如果你更看重**安全性、无守护进程的稳定性、以及与 Kubernetes 的无缝衔接**，Podman 是更好的选择。
---
## 第三章：安装与基础配置
### 1. 安装 (Linux)
*   **RHEL / CentOS / Fedora**: `sudo dnf install podman`
*   **Ubuntu / Debian**: `sudo apt-get update && sudo apt-get install podman`
*   **Arch Linux**: `sudo pacman -S podman`
### 2. Mac / Windows 安装
Mac 和 Windows 无法原生运行 Linux 容器，Podman 提供了一个虚拟机来运行：
*   **Mac**: `brew install podman`
*   **Windows**: 通过 [Podman 官网](https://podman.io/) 下载安装包
*   初始化虚拟机：`podman machine init` && `podman machine start`
### 3. 配置 Docker 别名（可选）
为了让习惯 Docker 的用户无缝切换，可以在 `~/.bashrc` 或 `~/.zshrc` 中添加：
```bash
alias docker=podman
alias docker-compose=podman-compose
```
---
## 第四章：常用命令速查手册
Podman 命令几乎与 Docker 1:1 对齐。以下按功能分类整理。
### 1. 镜像管理
| 命令 | 说明 | 示例 |
| :--- | :--- | :--- |
| `podman search <镜像名>` | 在仓库中搜索镜像 | `podman search nginx` |
| `podman pull <镜像名>:<标签>` | 拉取镜像到本地 | `podman pull nginx:alpine` |
| `podman images` / `podman image ls` | 列出本地所有镜像 | `podman images` |
| `podman rmi <镜像ID/名称>` | 删除本地镜像 | `podman rmi nginx:alpine` |
| `podman build -t <名称:标签> <路径>`| 根据 Containerfile/Dockerfile 构建镜像 | `podman build -t myapp:1.0 .` |
| `podman tag <原镜像> <新镜像>` | 为镜像打标签 | `podman tag myapp:1.0 myapp:latest` |
| `podman push <镜像名>` | 推送镜像到仓库 | `podman push myrepo/myapp:1.0` |
| `podman save -o <文件名.tar> <镜像>`| 导出镜像为 tar 压缩包 | `podman save -o nginx.tar nginx:alpine`|
| `podman load -i <文件名.tar>` | 导入 tar 镜像包 | `podman load -i nginx.tar` |
| `podman inspect <镜像名>` | 查看镜像的详细元数据 | `podman inspect nginx:alpine` |
### 2. 容器管理
| 命令 | 说明 | 示例 |
| :--- | :--- | :--- |
| `podman run [选项] <镜像> [命令]` | 创建并启动容器 | 见下方常用参数组合 |
| `podman ps` | 列出正在运行的容器 | `podman ps` |
| `podman ps -a` | 列出所有容器（包括已停止） | `podman ps -a` |
| `podman start <容器名/ID>` | 启动已存在的容器 | `podman start my_container` |
| `podman stop <容器名/ID>` | 停止运行中的容器 | `podman stop my_container` |
| `podman restart <容器名/ID>` | 重启容器 | `podman restart my_container` |
| `podman rm <容器名/ID>` | 删除已停止的容器 | `podman rm my_container` |
| `podman rm -f <容器名/ID>` | 强制删除运行中的容器 | `podman rm -f my_container` |
| `podman exec -it <容器> <命令>` | 在运行中的容器内执行命令 | `podman exec -it my_web /bin/sh` |
| `podman logs <容器名>` | 查看容器日志 | `podman logs -f my_web` (`-f`持续监听) |
| `podman stats` | 查看容器资源占用 (CPU/MEM) | `podman stats` |
| `podman top <容器名>` | 查看容器内进程 | `podman top my_web` |
| `podman cp <源> <目标>` | 容器与宿主机之间拷贝文件 | `podman cp ./app.conf my_web:/etc/` |
**`podman run` 常用参数速记：**
*   `-d`：后台运行
*   `-it`：交互式终端 (常用于进入容器 bash/sh)
*   `--name <名称>`：指定容器名称
*   `-p <宿主端口>:<容器端口>`：端口映射
*   `-v <宿主路径>:<容器路径>`：挂载数据卷/目录
*   `--rm`：容器退出后自动删除
*   `-e <KEY=VALUE>`：设置环境变量
*   `--network <网络名>`：指定加入的网络
### 3. Pod 管理 (Podman 专属特性)
Pod 允许将多个容器放在同一个命名空间中，共享网络和存储。
| 命令 | 说明 | 示例 |
| :--- | :--- | :--- |
| `podman pod create --name <名称>` | 创建一个新 Pod | `podman pod create --name mypod` |
| `podman pod ls` | 列出所有 Pod | `podman pod ls` |
| `podman run -d --pod <名称> <镜像>` | 将新容器加入指定 Pod | `podman run -d --pod mypod nginx` |
| `podman pod start <名称>` | 启动 Pod 内所有容器 | `podman pod start mypod` |
| `podman pod stop <名称>` | 停止 Pod | `podman pod stop mypod` |
| `podman pod rm <名称>` | 删除 Pod（需先停止） | `podman pod rm mypod` |
### 4. 网络与数据卷
**网络**
*   `podman network ls`：列出网络
*   `podman network create <网络名>`：创建自定义网络
*   `podman network rm <网络名>`：删除网络
*   `podman network connect <网络名> <容器名>`：将容器连入网络
    **数据卷**
*   `podman volume create <卷名>`：创建数据卷
*   `podman volume ls`：列出数据卷
*   `podman volume inspect <卷名>`：查看数据卷详情
*   `podman volume rm <卷名>`：删除数据卷
### 5. 系统维护
*   `podman system df`：查看镜像、容器、卷占用的磁盘空间
*   `podman system prune`：清理所有未使用的资源（悬挂镜像、停止的容器等）
*   `podman system prune -a`：深度清理（包括未被容器引用的镜像）
---
## 第五章：进阶使用与最佳实践
### 1. 生成 Systemd 服务 (容器自启动)
Podman 的一大优势是可以轻松将容器变为系统服务。自从 Podman 4.4 起，官方推荐使用 **Quadlets** 替代原生命令，但传统的生成方式依然可用。
**传统方式**：
```bash
# 1. 创建并启动一个容器
podman run -d --name web_server -p 8080:80 nginx
# 2. 生成 systemd 服务文件
podman generate systemd --name web_server --new --files
# 3. 将文件移动到 systemd 目录并重载
sudo cp container-web_server.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now container-web_server
```
### 2. 使用 Podman Compose
虽然 Podman 原生不包含 Compose，但可以通过安装 `podman-compose` 来解析 `docker-compose.yml` 文件。
*   安装：`pip3 install podman-compose`
*   运行：`podman-compose up -d`
    *(注：Podman 现在也支持通过开启兼容 Socket 来直接使用原生的 `docker-compose`)*
### 3. Rootless 容器配置
非特权用户运行容器时，可能会遇到端口或权限限制。
1. **端口限制**：Linux 默认非 root 用户不能绑定 1024 以下的端口。可以通过修改 `sysctl` 解决：
   `echo "net.ipv4.ip_unprivileged_port_start=80" | sudo tee -a /etc/sysctl.conf` 然后 `sudo sysctl -p`。
2. **Subuid/Subgid**：确保系统为当前用户分配了 UID/GID 映射。可以通过 `usermod --add-subuids 100000-165535 --add-subgids 100000-165535 <username>` 来设置。
---
## 第六章：常见问题 (FAQ)
**Q1: 我可以直接使用 `docker-compose.yml` 吗？**
A: 可以。你可以使用 `podman-compose` 工具，或者直接安装原生的 `docker-compose`，然后通过执行 `systemctl --user enable --now podman.socket` 开启 Podman 的 Docker API 兼容接口，即可让 `docker-compose` 将命令发送给 Podman 执行。
**Q2: 为什么我的容器在重启系统后没有自动启动？**
A: Podman 没有守护进程，不会像 Docker 那样设置 `--restart=always` 就自动开机启动。你需要使用 Systemd 集成（参考第五章）或者使用 `podman-update` 配合 systemd unit 来实现开机自启。
**Q3: Podman 能拉取 Docker Hub 的镜像吗？**
A: 完全可以。Podman 默认使用 Docker Hub 作为公共注册表。执行 `podman pull ubuntu` 会自动从 Docker Hub 拉取。
**Q4: Rootless 模式下挂载目录没有权限怎么办？**
A: 这是由于宿主机 UID 和容器内 UID 不匹配造成的。可以使用 `podman unshare chown -R <UID>:<GID> <宿主机路径>` 来调整宿主机目录的映射权限，或者使用 `:Z` 标签（针对 SELinux 系统）如 `-v /host/path:/container/path:Z`。

```bash
podman run -d \
  --name my_web_app \
  --hostname my_web_app \
  --restart=unless-stopped \
  -p 8080:80 \
  -p 8443:443/udp \
  -e TZ=Asia/Shanghai \
  -e DB_PORT=5432 \
  -v /opt/app/conf/nginx.conf:/etc/nginx/nginx.conf:ro,Z \
  -v app_data:/var/www/html:Z \
  nginx:alpine
```