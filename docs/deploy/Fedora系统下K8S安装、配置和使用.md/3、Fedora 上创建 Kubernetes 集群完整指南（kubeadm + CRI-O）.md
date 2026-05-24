# 📘 Fedora 上创建 Kubernetes 集群完整指南（kubeadm + CRI-O）
> 基于 Fedora 官方文档整理 · 适配 Fedora 41-43+ · 单节点学习/实验环境专用

---

## 📋 文档概览

| 项目 | 说明 |
|------|------|
| **适用系统** | Fedora 41/42/43/rawhide（本文针对你的 **Fedora 43** 优化） |
| **集群类型** | 单节点单机器集群（控制平面 + 工作节点合一） |
| **使用场景** | ✅ 学习探索 ✅ 本地开发测试 ✅ 技术验证 ❌ 非生产环境 |
| **核心组件** | kubeadm + kubelet + kubectl + CRI-O + Flannel |
| **文档来源** | [Fedora Docs: Creating a Kubernetes cluster on Fedora](https://docs.fedoraproject.org/) |

> 💡 **重要提示**：本文档讨论的 Kubernetes RPM 包来自 Fedora 官方仓库，**非第三方源**，安全性与兼容性有保障。

---

## 🚀 一、快速开始：完整执行流程（复制即用）

> ⚠️ 以下命令按顺序执行，每步均有详细说明，建议先阅读再操作

```bash
# ========== 阶段 1：系统预准备 ==========
# 1. 更新系统（可选但推荐）
sudo dnf update -y
# 💡 如有内核更新，建议重启：sudo reboot now

# 2. 禁用 swap（kubeadm 强制要求，Fedora 默认使用 zram）
sudo systemctl stop swap-create@zram0
sudo dnf remove -y zram-generator-defaults
# ⚠️ 必须重启使 swap 完全失效
sudo reboot now

# 3. 【可选】SELinux 处理（生产建议启用，学习可临时关闭）
# 临时关闭（重启失效）：
sudo setenforce 0
# 永久关闭（不推荐）：编辑 /etc/selinux/config 设 SELINUX=permissive

# 4. 禁用防火墙（学习简化，生产需配置规则）
sudo systemctl disable --now firewalld

# 5. 安装网络基础包
sudo dnf install -y iptables iproute-tc

# 6. 配置内核模块（桥接过滤 + overlay）
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

# 7. 配置 sysctl 网络参数（持久化）
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system  # 立即生效

# 8. 验证配置
lsmod | grep -E 'br_netfilter|overlay'
sysctl net.bridge.bridge-nf-call-iptables net.bridge.bridge-nf-call-ip6tables net.ipv4.ip_forward
# ✅ 预期输出：三项均为 1
```

```bash
# ========== 阶段 2：安装容器运行时（CRI-O） ==========
# 9. 查看可用 Kubernetes 版本（选择与你需求匹配的版本）
dnf list kubernetes1.??  # 示例输出：1.30/1.31/1.32

# 10. 安装 CRI-O（版本必须与 Kubernetes 主版本一致！）
# 以 1.31 为例：
sudo dnf install -y cri-o1.31 containernetworking-plugins

# 11. 启动并启用 CRI-O
sudo systemctl enable --now crio

# 12. 验证 CRI-O 状态
crictl info | head -20
# ✅ 检查 "cgroupManager": "systemd"（Fedora 43 cgroups v2 必需）
```

```bash
# ========== 阶段 3：安装 Kubernetes 组件 ==========
# 13. 安装 kubeadm/kubelet/kubectl（版本化包推荐）
sudo dnf install -y kubernetes1.31 kubernetes1.31-kubeadm kubernetes1.31-client

# 14. 预拉取系统镜像（可选，加速后续初始化）
sudo kubeadm config images pull

# 15. 启动 kubelet（此时会进入 crash loop，正常现象）
sudo systemctl enable --now kubelet
systemctl status kubelet --no-pager  # 应显示 activating/auto-restart
```

```bash
# ========== 阶段 4：初始化集群 ==========
# 16. 执行 kubeadm init（指定 Pod 网段，与 CNI 插件匹配）
sudo kubeadm init --pod-network-cidr=10.244.0.0/16

# ✅ 成功输出示例：
# "Your Kubernetes control-plane has initialized successfully!"

# 17. 配置 kubectl 权限（普通用户可用）
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# 🔁 或 root 用户临时方案：
# export KUBECONFIG=/etc/kubernetes/admin.conf

# 18. 允许控制平面节点调度应用 Pod（单节点必需）
kubectl taint nodes --all node-role.kubernetes.io/control-plane-

# 19. 安装 CNI 网络插件（以 Flannel 为例）
kubectl apply -f https://github.com/coreos/flannel/raw/master/Documentation/kube-flannel.yml

# 20. 验证集群状态
kubectl get nodes          # 应显示 Ready
kubectl get pods -A        # 所有 Pod 应为 Running 状态
```

---

## ⚠️ 二、关键注意事项（避坑指南）

### 🔑 1. 版本匹配原则（极易踩坑！）
| 组件 | 版本要求 | 验证命令 |
|------|---------|---------|
| **CRI-O** | 主版本必须与 Kubernetes 一致 | `crio --version` |
| **Kubernetes RPM** | 使用 `kubernetes1.xx` 版本化包 | `rpm -qa \| grep kubernetes` |
| **kubelet + kubeadm + kubectl** | 三者版本必须完全一致 | `kubelet --version` |

```bash
# ✅ 正确示例（全部 1.31）：
dnf install cri-o1.31 kubernetes1.31 kubernetes1.31-kubeadm kubernetes1.31-client

# ❌ 错误示例（版本混用）：
dnf install cri-o1.30 kubernetes1.31 ...  # ⚠️ 可能导致 CRI 连接失败
```

### 🔐 2. Fedora 43 专属适配要点
```bash
# (1) cgroups v2 强制要求 systemd 驱动
# ✅ CRI-O 默认已适配，验证：
crio config | grep cgroup_manager  # 应输出: cgroup_manager = "systemd"

# ✅ kubelet config.yaml 必须指定：
grep cgroupDriver /var/lib/kubelet/config.yaml  # 应输出: cgroupDriver: systemd

# (2) Podman 与 CRI-O 共存建议
# 💡 两者可共存，但 kubelet 只连接 CRI-O 套接字
# 检查 kubelet 配置：
grep containerRuntimeEndpoint /var/lib/kubelet/config.yaml
# ✅ 应指向: unix:///var/run/crio/crio.sock

# (3) 内核参数持久化验证
# 重启后检查是否仍生效：
cat /proc/sys/net/bridge/bridge-nf-call-iptables  # 应为 1
```

### 🛡️ 3. 安全与生产环境建议（学习阶段可简化）
| 配置项 | 学习/实验环境 | 生产环境建议 |
|--------|-------------|-------------|
| **SELinux** | 可临时 `setenforce 0` | ✅ 保持 enforcing + 配置策略 |
| **Firewalld** | 可禁用简化调试 | ✅ 开放必要端口（6443/10250/2379 等） |
| **Swap** | 建议禁用（kubeadm 默认要求） | ⚠️ 如必须启用，需加 `--fail-swap-on=false` |
| **网络插件** | Flannel（简单） | Calico/Cilium（功能更强） |

🔗 生产防火墙规则参考：[Kubernetes 端口与协议](https://kubernetes.io/docs/reference/networking/ports-and-protocols/)

---

## 🔧 三、常见问题与解决方案

### ❌ 问题 1：kubelet 启动失败（你之前遇到的）
```
failed to load kubelet config file: open /var/lib/kubelet/config.yaml: no such file
```
✅ **解决方案**：
```bash
# kubeadm init 成功后会自动生成该文件
# 如手动安装，需先创建最小化配置（参考前文指南）
# 验证生成：
ls -la /var/lib/kubelet/config.yaml
```

### ❌ 问题 2：CoreDNS 处于 CrashLoopBackOff
```bash
kubectl get pods -n kube-system | grep coredns
# coredns-xxx   0/1   CrashLoopBackOff
```
✅ **两种解决方案**：

#### 方案 A：修改 CoreDNS 配置（推荐）
```bash
# 1. 编辑 ConfigMap
kubectl edit configmap coredns -n kube-system

# 2. 找到并修改这一行：
#    forward . /etc/resolv.conf
# 改为你的实际 DNS（如 114.114.114.114 或 8.8.8.8）：
#    forward . 114.114.114.114

# 3. 保存退出，CoreDNS 会自动重启
```

#### 方案 B：禁用 systemd-resolved 存根监听（根治）
```bash
# 创建配置覆盖
sudo mkdir -p /etc/systemd/resolved.conf.d/
cat <<EOF | sudo tee /etc/systemd/resolved.conf.d/stub-listener.conf
[Resolve]
DNSStubListener=no
EOF

# 重启网络解析服务
sudo systemctl restart systemd-resolved

# 重新创建集群（或重启节点）
```

### ❌ 问题 3：节点状态为 NotReady
```bash
kubectl get nodes
# NAME       STATUS     ROLES           AGE   VERSION
# fedora     NotReady   control-plane   5m    v1.31.0
```
✅ **排查步骤**：
```bash
# 1. 检查 CNI 插件是否运行
kubectl get pods -n kube-system | grep -E 'flannel|calico'

# 2. 检查 kubelet 日志
journalctl -u kubelet -n 50 --no-pager | grep -i error

# 3. 检查 CRI-O 状态
systemctl status crio
crictl ps  # 应能列出容器

# 4. 检查网络连通性
ping -c 2 10.244.0.1  # Flannel 默认网关
```

---

## 🔄 四、集群维护与升级

### 🔒 防止意外升级（dnf versionlock）
```bash
# 1. 安装 versionlock 插件
sudo dnf install -y python3-dnf-plugin-versionlock

# 2. 锁定当前版本（以 1.31 为例）
sudo dnf versionlock add kubernetes*-1.31.* cri-o-1.31.*

# 3. 查看锁定列表
dnf versionlock list

# 4. 解锁/删除锁定
dnf versionlock delete kubernetes1.31-kubelet
```

### 📦 升级 Kubernetes 集群（谨慎操作！）
```bash
# 1. 先备份关键配置
sudo cp -a /etc/kubernetes /backup/kubernetes-$(date +%F)
sudo cp -a /var/lib/kubelet /backup/kubelet-$(date +%F)

# 2. 升级 CRI-O（必须先于 k8s）
sudo dnf upgrade -y cri-o1.32  # 升级到目标版本
sudo systemctl restart crio

# 3. 升级 kubeadm
sudo dnf upgrade -y kubernetes1.32-kubeadm

# 4. 执行 kubeadm upgrade
sudo kubeadm upgrade plan  # 查看可升级版本
sudo kubeadm upgrade apply v1.32.0

# 5. 升级 kubelet + kubectl
sudo dnf upgrade -y kubernetes1.32-kubelet kubernetes1.32-client
sudo systemctl daemon-reload
sudo systemctl restart kubelet

# 6. 验证
kubectl version --short
kubectl get nodes
```

> 📚 官方升级指南：[Upgrading kubeadm clusters](https://kubernetes.io/docs/tasks/administer-cluster/kubeadm/kubeadm-upgrade/)

---

## 🧪 五、验证与测试（确认集群可用）

```bash
# ✅ 基础验证清单
echo "=== 节点状态 ==="
kubectl get nodes -o wide

echo -e "\n=== 系统组件 Pod ==="
kubectl get pods -n kube-system

echo -e "\n=== 网络插件状态 ==="
kubectl get pods -n kube-system | grep -E 'flannel|calico|cilium'

echo -e "\n=== 测试部署 Nginx ==="
kubectl create deployment nginx-test --image=nginx:alpine
kubectl scale deployment nginx-test --replicas=2
kubectl get pods -l app=nginx-test

echo -e "\n=== 测试服务暴露 ==="
kubectl expose deployment nginx-test --port=80 --type=NodePort
kubectl get svc nginx-test

echo -e "\n=== 访问测试（获取 NodePort） ==="
NODE_PORT=$(kubectl get svc nginx-test -o jsonpath='{.spec.ports[0].nodePort}')
echo "访问: http://$(hostname -I | awk '{print $1}'):${NODE_PORT}"
curl -s http://localhost:${NODE_PORT} | head -5
```

✅ **预期结果**：
- 所有节点状态为 `Ready`
- `kube-system` 下 Pod 均为 `Running`
- Nginx 测试 Pod 成功调度并运行
- 可通过 NodePort 访问 Nginx 欢迎页

---

## 🗑️ 六、清理与重置（实验结束）

```bash
# ⚠️ 警告：以下操作将彻底删除集群数据！

# 1. 重置 kubeadm（清理控制平面配置）
sudo kubeadm reset -f

# 2. 清理 CNI 网络配置
sudo rm -rf /etc/cni/net.d/*
sudo rm -rf /var/lib/cni/*

# 3. 清理 kubelet 数据
sudo rm -rf /var/lib/kubelet/*
sudo rm -rf /etc/kubernetes/*

# 4. 清理用户配置
rm -rf $HOME/.kube/config

# 5. （可选）重装前清理残留包
sudo dnf remove -y kubernetes1.31* cri-o1.31*

# 6. 重启确保干净状态
sudo reboot now
```

---

## 📎 附录：关键配置文件速查

| 文件路径 | 用途 | 管理方式 |
|---------|------|---------|
| `/var/lib/kubelet/config.yaml` | kubelet 主配置 | kubeadm 生成 / 手动维护 |
| `/etc/kubernetes/admin.conf` | 集群管理员 kubeconfig | kubeadm 生成 |
| `/etc/kubernetes/kubelet.conf` | kubelet 连接 API Server 凭证 | kubeadm 生成 |
| `/etc/cni/net.d/` | CNI 插件配置 | Flannel/Calico 安装时创建 |
| `/etc/systemd/system/kubelet.service.d/override.conf` | 用户自定义 systemd 覆盖 | ✅ 手动创建（安全） |

---

## 💡 七、给你的专属建议（基于你的环境）

> 你当前环境：Fedora 43 + Podman + JDK25 + IDEA + 银行保险项目背景

### ✅ 推荐方案对比
| 方案 | 适用场景 | 优点 | 缺点 |
|------|---------|------|------|
| **本文档方案**（kubeadm+CRI-O） | 深入学习 k8s 原理/节点运维 | 完整掌握底层配置、贴近生产 | 配置复杂、调试成本高 |
| **Kind + Podman** | 本地开发测试（如博客项目） | 5 分钟启动、隔离性好、不影响主机 | 无法学习节点级配置 |
| **Minikube + Podman** | 中等复杂度实验 | 插件丰富、支持多节点模拟 | 资源占用略高 |

### 🎯 针对你的场景建议：
1. **如果想深入理解 Kubernetes 运维** → 按本文档完整走一遍，重点掌握：
    - systemd drop-in 配置管理（避免升级丢失）
    - CRI-O 与 kubelet 的协同机制
    - cgroups v2 + systemd 驱动的配置要点

2. **如果只是为博客项目提供测试环境** → 直接使用 Kind：
   ```bash
   # 10 分钟搞定，专注业务开发
   kind create cluster --name blog-dev --image kindest/node:v1.31.0
   kubectl apply -f your-springboot-deployment.yaml
   ```

3. **混合方案**（推荐⭐）：
    - 主机用 **Kind 集群** 做日常开发测试
    - 单独用 **虚拟机/容器** 按本文档搭建「学习专用集群」，练习故障排查与升级

---

> 🌟 **最后提醒**：  
> 你在银行保险行业工作，若未来需将本地集群经验迁移到生产环境，请务必：
> 1. 重新评估安全策略（SELinux/防火墙/审计）
> 2. 使用企业级 CNI（Calico/Cilium）+ Ingress 控制器
> 3. 集成日志/监控（Prometheus + Loki + Grafana）
> 4. 遵循公司合规要求（镜像签名、网络策略、RBAC）

需要我为你：
- ✅ 生成一份「Fedora 43 + kubeadm + CRI-O」的**一键初始化脚本**？
- ✅ 提供适配你**人工核保系统**的 Kubernetes 部署 YAML 模板？
- ✅ 整理一份**生产环境安全检查清单**？

随时告诉我，我可以继续为你定制 👇