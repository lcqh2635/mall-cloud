# 📘 Fedora 版本化 CRI-O 与 CRI-Tools RPM 配置指南
> 基于 Fedora 官方文档整理 · 适配 Fedora 41-43+ · 专注版本匹配与配置持久化

---

## 📋 文档概览

| 项目 | 说明 |
|------|------|
| **核心主题** | CRI-O / CRI-Tools 版本化 RPM 包的安装、配置与维护策略 |
| **关键原则** | **版本严格匹配**：CRI-O/CRI-Tools 主版本必须与 Kubernetes 一致 |
| **适用场景** | 在 Fedora 上使用 `kubeadm` 部署 Kubernetes 集群 |
| **文档来源** | [Fedora Docs: Versioned CRI-O and CRI-Tools RPMs](https://docs.fedoraproject.org/) |

> 💡 **一句话总结**：  
> 如果你安装了 `kubernetes1.31`，就必须安装 `cri-o1.31` + `cri-tools1.31`，**主版本（minor version）必须完全一致**。

---

## 🔑 一、核心概念解析

### 🔄 什么是 CRI（Container Runtime Interface）？
```
┌─────────────────┐
│    kubelet      │  ← Kubernetes 节点代理
└────────┬────────┘
         │ CRI gRPC 接口
         ▼
┌─────────────────┐
│  CRI 实现 (CRI-O)│  ← 容器运行时插件
└────────┬────────┘
         │ OCI 标准
         ▼
┌─────────────────┐
│  容器引擎 (runc) │  ← 实际创建/管理容器
└─────────────────┘
```

| 组件 | 作用 | 是否必需 |
|------|------|---------|
| **CRI-O** | Kubernetes 专用的轻量级 CRI 实现，专注运行 K8s Pod | ✅ kubeadm 部署推荐 |
| **CRI-Tools** | 命令行调试工具（`crictl`/`critest`），用于与 CRI 交互 | ✅ kubeadm init 必需 |
| **containerd** | 通用容器运行时，也可作为 CRI 实现（替代 CRI-O） | ⚪ 可选方案 |

### 🎯 为什么需要「版本化 RPM」？
```bash
# ❌ 非版本化包（已不推荐）
dnf install kubernetes-kubelet cri-o cri-tools
# ⚠️ 问题：版本可能不匹配，升级时易产生依赖冲突

# ✅ 版本化包（官方推荐）
dnf install kubernetes1.31-kubelet cri-o1.31 cri-tools1.31
# ✅ 优势：版本锁定、依赖清晰、升级可控
```

---

## 🔢 二、版本匹配规则（⚠️ 重点）

### 📐 匹配原则
```
Kubernetes 版本 : CRI-O 版本 : CRI-Tools 版本 = 1 : 1 : 1
                    ↓ 主版本必须一致 ↓
              kubernetes1.31 + cri-o1.31 + cri-tools1.31
```

### 🔍 如何查询可用版本？
```bash
# 查看 Fedora 仓库中可用的 Kubernetes 版本
dnf list kubernetes1.?? --showduplicates

# 示例输出（Fedora 41）：
# kubernetes1.29.x86_64  1.29.11-2.fc41  updates
# kubernetes1.30.x86_64  1.30.7-1.fc41   updates  
# kubernetes1.31.x86_64  1.31.3-1.fc41   updates  ← 当前推荐
# kubernetes1.32.x86_64  1.32.0-1.fc41   updates

# 验证 CRI-O 版本匹配
dnf list cri-o1.?? --showduplicates
dnf list cri-tools1.?? --showduplicates
```

### 🛠️ 完整安装命令示例（以 1.31 为例）
```bash
# 一键安装匹配版本套件
sudo dnf install -y \
  kubernetes1.31 \
  kubernetes1.31-kubeadm \
  kubernetes1.31-client \
  cri-o1.31 \
  cri-tools1.31 \
  containernetworking-plugins

# 验证安装结果
kubelet --version      # v1.31.x
crio --version         # v1.31.x  
crictl --version       # v1.31.x
```

### ⚠️ 常见版本错误及后果
| 错误组合 | 可能后果 | 错误日志示例 |
|---------|---------|-------------|
| `kubernetes1.31 + cri-o1.30` | CRI 接口不兼容，kubelet 启动失败 | `runtime network not ready: NetworkPluginNotReady` |
| `cri-o1.31 + cri-tools1.30` | `crictl` 命令部分功能异常 | `CRI version mismatch` 警告 |
| 混用版本化/非版本化包 | 依赖冲突，`dnf update` 失败 | `package kubernetes-kubelet conflicts with kubernetes1.31-kubelet` |

---

## ⚙️ 三、CRI-O 配置管理（持久化关键）

### 📁 配置文件层级
```
/etc/crio/
├── crio.conf              # 主配置文件（⚠️ rpm 管理，升级可能覆盖）
└── crio.conf.d/           # ✅ drop-in 目录（用户自定义，升级保留）
    ├── 00-default.conf    # 默认片段（如有）
    ├── 10-custom.conf     # 用户自定义（推荐命名规范）
    └── 99-override.conf   # 高优先级覆盖（字母序最后生效）
```

### 🔀 配置合并规则
```
优先级从低到高：
1. /etc/crio/crio.conf（基础配置）
2. /etc/crio/crio.conf.d/*.conf（按文件名升序合并）
3. 命令行参数（最高优先级，不推荐）

✅ 最佳实践：所有自定义配置放入 crio.conf.d/，使用 99- 前缀确保最高优先级
```

### 🛠️ 配置持久化实操（以 Fedora 43 + cgroups v2 为例）

#### 步骤 1：创建 drop-in 配置目录（如不存在）
```bash
sudo mkdir -p /etc/crio/crio.conf.d/
```

#### 步骤 2：编写自定义配置片段
```bash
# 示例：/etc/crio/crio.conf.d/99-fedora43.conf
# 适配 Fedora 43 cgroups v2 + 优化日志 + 配置镜像加速

[crio.runtime]
# ✅ 强制使用 systemd cgroup 驱动（Fedora 43 必需）
cgroup_manager = "systemd"

# ✅ 配置日志驱动（推荐 journald 便于集中管理）
log_driver = "journald"
log_size_max = -1  # 不限制单文件大小，由 journald 轮转

# ✅ 配置国内镜像加速（根据实际网络选择）
[crio.image]
default_transport = "docker://"
insecure_registries = []
# 阿里云镜像加速（替换为你的专属地址）
# registries = ["registry.cn-hangzhou.aliyuncs.com"]

# ✅ 配置 Pod sandbox 镜像（加速节点初始化）
pause_image = "registry.k8s.io/pause:3.9"
pause_image_auth_file = ""
pause_command = "/pause"

# ✅ 启用额外日志（调试阶段）
[crio.runtime.runtimes.runc]
runtime_path = "/usr/bin/runc"
runtime_type = "oci"
runtime_root = "/run/runc"
privileged_without_host_devices = false
allowed_annotations = []
```

#### 步骤 3：验证配置生效
```bash
# 1. 重载 CRI-O 配置
sudo systemctl restart crio

# 2. 检查实际生效配置（关键！）
sudo crio config --config /etc/crio/crio.conf | grep -A2 -B2 "cgroup_manager"
# ✅ 预期输出: cgroup_manager = "systemd"

# 3. 验证 crictl 连接正常
sudo crictl info | grep -E "cgroupManager|status"
# ✅ 预期: "cgroupManager": "systemd", "status": "ready"
```

#### 步骤 4：备份配置（升级前必备）
```bash
# 备份用户自定义配置
sudo cp -a /etc/crio/crio.conf.d/ /backup/crio-conf.d-$(date +%F)

# 记录当前版本（便于回滚）
rpm -q cri-o1.31 > /backup/crio-version-$(date +%F).txt
```

---

## 🧰 四、CRI-Tools 使用指南

### 🔧 核心命令速查
```bash
# 1. 验证运行时连接
crictl version
# ✅ 预期: RuntimeName: crio, RuntimeVersion: v1.31.x

# 2. 查看节点容器列表
crictl ps -a                    # 所有容器
crictl ps --state running     # 仅运行中
crictl ps --label io.kubernetes.container.name=POD  # 仅 sandbox

# 3. 调试 Pod 问题
crictl pods                   # 列出所有 Pod sandbox
crictl inspectp <POD_ID>     # 查看 Pod 详情
crictl logs <CONTAINER_ID>   # 查看容器日志（替代 docker logs）

# 4. 镜像管理
crictl images               # 列出镜像
crictl pull nginx:alpine    # 拉取镜像
crictl rmi <IMAGE_ID>       # 删除镜像

# 5. 执行命令调试
crictl exec -it <CONTAINER_ID> sh
```

### ⚙️ crictl 配置（可选但推荐）
```bash
# 创建默认配置文件，避免每次指定 --runtime-endpoint
sudo tee /etc/crictl.yaml > /dev/null << 'EOF'
runtime-endpoint: unix:///var/run/crio/crio.sock
image-endpoint: unix:///var/run/crio/crio.sock
timeout: 10
debug: false
pull-image-on-create: false
EOF

# 验证
crictl info | head -5  # 应无需额外参数即可连接
```

### 🔍 排查 CRI 连接问题
```bash
# 问题：crictl 报错 "rpc error: code = Unimplemented desc = unknown service"
# 原因：CRI-O 未启动 / 套接字路径错误 / 权限不足

# 排查步骤：
# 1. 检查 CRI-O 状态
systemctl status crio

# 2. 验证套接字文件存在
ls -la /var/run/crio/crio.sock

# 3. 检查用户权限（crictl 通常需 root 或 crio 组）
sudo usermod -aG crio $USER  # 添加当前用户到 crio 组
# ⚠️ 需重新登录生效

# 4. 手动指定端点测试
crictl --runtime-endpoint unix:///var/run/crio/crio.sock version
```

---

## 🔄 五、升级与退役策略

### 📅 版本退役规则
```
当 Kubernetes 某个主版本（如 1.29）被上游社区终止支持（EOL）后：
→ Fedora 仓库将同步移除 kubernetes1.29* / cri-o1.29* / cri-tools1.29*
→ 用户需提前规划升级到受支持版本
```

🔗 查询 Kubernetes 支持周期：[Release History](https://kubernetes.io/releases/patch-releases/)

### 📦 安全升级流程（以小版本升级为例）
```bash
# 场景：从 1.31.2 升级到 1.31.3（补丁版本，安全）

# 1. 备份关键配置（必备！）
sudo cp -a /etc/crio/crio.conf.d/ /backup/crio-conf-$(date +%F)
sudo cp -a /var/lib/kubelet/config.yaml /backup/kubelet-config-$(date +%F)

# 2. 查看可用更新
dnf list --upgrades | grep -E 'kubernetes1.31|cri-o1.31|cri-tools1.31'

# 3. 执行升级（建议逐个组件）
sudo dnf upgrade -y cri-o1.31 cri-tools1.31
sudo systemctl restart crio

sudo dnf upgrade -y kubernetes1.31-kubelet kubernetes1.31-client
sudo systemctl daemon-reload
sudo systemctl restart kubelet

# 4. 验证集群健康
kubectl get nodes
kubectl get pods -A | grep -v Running

# ⚠️ 注意：主版本升级（1.31 → 1.32）需使用 kubeadm upgrade，不可直接 dnf upgrade！
```

### 🔒 防止意外升级（生产环境推荐）
```bash
# 1. 安装 versionlock 插件
sudo dnf install -y python3-dnf-plugin-versionlock

# 2. 锁定当前版本（允许补丁更新）
sudo dnf versionlock add kubernetes1.31* cri-o1.31* cri-tools1.31*

# 3. 验证锁定
dnf versionlock list
# 输出: kubernetes1.31-kubelet-1.31.3-2.fc41.x86_64

# 4. 临时解锁升级
dnf versionlock delete kubernetes1.31-kubelet
```

---

## 🐧 六、Fedora 43 专属适配清单

### ✅ 预检命令（执行前必跑）
```bash
# 1. 确认 cgroups 版本（必须为 v2）
stat -fc %T /sys/fs/cgroup/  # 输出: cgroup2fs

# 2. 检查内核模块
lsmod | grep -E 'br_netfilter|overlay'  # 网络插件依赖

# 3. 验证 sysctl 参数
sysctl net.bridge.bridge-nf-call-iptables net.ipv4.ip_forward
# ✅ 两项均应为 1

# 4. 确认 CRI-O 配置兼容 cgroups v2
grep cgroup_manager /etc/crio/crio.conf /etc/crio/crio.conf.d/*.conf 2>/dev/null
# ✅ 应输出: cgroup_manager = "systemd"
```

### ⚠️ 已知兼容性问题及解决方案
| 问题现象 | 根本原因 | 解决方案 |
|---------|---------|---------|
| `crio: failed to create containerd task: cgroups: unable to load path` | CRI-O 未正确识别 cgroups v2 | 确保 `cgroup_manager = "systemd"` + 重启 crio |
| `crictl: permission denied` | 套接字权限限制 | `sudo usermod -aG crio $USER` + 重新登录 |
| `kubelet: CRI connection failed` | CRI-O 套接字路径不匹配 | 检查 kubelet config.yaml 中 `containerRuntimeEndpoint` 与 crio 实际路径一致 |
| `Pod 网络不通` | firewalld 未放行 + 缺少桥接过滤 | 按前文指南配置 `/etc/sysctl.d/k8s.conf` + 临时 disable firewalld 测试 |

---

## 📎 附录：关键路径与命令速查

### 🔑 配置文件路径
```
/etc/crio/crio.conf                    # CRI-O 主配置（⚠️ rpm 管理）
/etc/crio/crio.conf.d/                 # ✅ 用户自定义 drop-in 目录
/etc/crictl.yaml                       # crictl 默认连接配置
/var/run/crio/crio.sock               # CRI 套接字（kubelet/crictl 连接点）
```

### 🔧 诊断命令
```bash
# CRI-O 状态
systemctl status crio
crio config --config /etc/crio/crio.conf | grep -E 'cgroup_manager|log_driver'

# 连接测试
crictl version
crictl info | jq '.status.runtimeConditions[] | select(.message != "")'

# kubelet 与 CRI 协同
journalctl -u kubelet -n 20 --no-pager | grep -i cri
kubectl describe node $(hostname) | grep -A5 'ContainerRuntime'
```

### 🔄 服务管理
```bash
# 重载配置（修改 crio.conf.d 后）
sudo systemctl reload crio    # 热重载（推荐）
# 或
sudo systemctl restart crio   # 完全重启

# 查看服务依赖
systemctl list-dependencies kubelet.service | grep crio
```

---

## 💡 七、给你的专属建议（结合你的背景）

> 你当前：Fedora 43 + 银行保险行业 + 人工核保系统 + 关注构建规范与可维护性

### ✅ 配置管理最佳实践（契合你的工程习惯）
```bash
# 1. 将 CRI-O drop-in 配置纳入版本控制
mkdir -p ~/k8s-infra/crio-config
cp /etc/crio/crio.conf.d/99-fedora43.conf ~/k8s-infra/crio-config/
# 添加 .gitignore 排除敏感信息，提交到内部仓库

# 2. 使用 Ansible/Shell 脚本标准化部署（参考你的 Maven Profile 思路）
# 示例：roles/crio/tasks/main.yml
- name: Deploy CRI-O drop-in config
  template:
    src: 99-fedora43.conf.j2
    dest: /etc/crio/crio.conf.d/99-fedora43.conf
    owner: root
    mode: '0644'
  notify: restart crio

# 3. 配置审计与变更追踪（契合银行合规要求）
sudo auditctl -w /etc/crio/crio.conf.d/ -p wa -k crio-config-change
# 后续可通过 ausearch -k crio-config-change 审计变更
```

### 🎯 本地开发环境建议
| 场景 | 推荐方案 | 理由 |
|------|---------|------|
| **学习 CRI-O 底层机制** | 按本文档手动配置 + 启用 debug 日志 | 深入理解 kubelet-CRI 交互 |
| **开发测试核保系统** | Kind + Podman 驱动 | 快速启停、隔离性好、不影响主机 CRI-O |
| **模拟生产部署流程** | 虚拟机内按文档完整部署 | 安全隔离、可快照回滚、贴近真实环境 |

### 🔐 安全加固建议（金融行业特别关注）
```bash
# 1. 限制 CRI-O 可访问的镜像仓库（防止恶意镜像）
# /etc/crio/crio.conf.d/99-security.conf
[crio.image]
registries = ["registry.internal.bank.com", "registry.k8s.io"]

# 2. 启用 SELinux 策略（不推荐完全禁用）
# 使用 audit2allow 生成自定义策略
sudo ausearch -m avc -ts recent | audit2allow -M crio-custom
sudo semodule -i crio-custom.pp

# 3. 配置日志审计（满足合规要求）
# /etc/crio/crio.conf.d/99-audit.conf
[crio.runtime]
log_to_journald = true
[crio.runtime.logging]
log_size_max = 104857600  # 100MB 轮转
log_max_files = 5
```

---

> 🌟 **最后总结**：  
> 1️⃣ **版本匹配是红线**：`kubernetes1.xx` + `cri-o1.xx` + `cri-tools1.xx` 必须主版本一致  
> 2️⃣ **配置持久化靠 drop-in**：所有自定义放入 `/etc/crio/crio.conf.d/`，避免升级丢失  
> 3️⃣ **Fedora 43 必配 systemd 驱动**：`cgroup_manager = "systemd"` 是 cgroups v2 的硬性要求  
> 4️⃣ **调试首选 crictl**：比 `docker ps` 更贴近 K8s 实际运行时状态

需要我为你：
- ✅ 生成一份「Fedora 43 + CRI-O 1.31」的**完整 drop-in 配置模板**（含镜像加速/日志/安全加固）？
- ✅ 提供适配你**人工核保系统**的 Kubernetes Deployment + Service YAML 示例？
- ✅ 整理一份「CRI-O 配置变更审计」的**合规检查脚本**？

随时告诉我，我可以继续为你定制 👇