# Kubernetes 详解文档

---

## 一、Kubernetes 是什么？

**Kubernetes**（常缩写为 **K8s**，因“K”和“s”之间有8个字母）是一个开源的容器编排平台，最初由 Google 设计，现由 Cloud Native Computing Foundation（CNCF）维护。它用于自动化部署、扩展和管理容器化应用程序。

Kubernetes 的目标是让开发者和运维人员能够轻松地管理跨多个主机的容器化应用，提供高可用性、弹性伸缩、服务发现、负载均衡、滚动更新、自动修复等能力。

---

## 二、Kubernetes 的作用

1. **自动化部署与回滚**  
   自动部署容器化应用，支持一键回滚到历史版本。

2. **弹性伸缩（Horizontal & Vertical）**  
   根据 CPU、内存或自定义指标自动扩展或收缩应用实例数量。

3. **服务发现与负载均衡**  
   为容器提供 DNS 名称或 IP 地址，自动在多个 Pod 间分配流量。

4. **存储编排**  
   挂载本地、云存储或网络存储系统到容器中，支持持久化数据。

5. **自我修复能力**  
   自动重启失败容器、替换不健康节点、确保指定数量的副本始终运行。

6. **密钥与配置管理**  
   通过 Secret 和 ConfigMap 管理敏感信息和配置，避免硬编码。

7. **批处理执行**  
   支持一次性任务（Job）和定时任务（CronJob）。

8. **多环境一致性**  
   在开发、测试、生产环境中提供一致的部署和管理体验。

---

## 三、Kubernetes 核心概念

### 1. Pod
- **最小部署单元**，包含一个或多个共享网络和存储的容器。
- 通常一个 Pod 运行一个主容器，辅助容器用于日志收集、监控等。

### 2. Deployment
- 管理 Pod 副本的声明式更新机制。
- 支持滚动更新、回滚、扩缩容。

### 3. Service
- 为一组 Pod 提供稳定的网络端点（IP + Port）。
- 类型：ClusterIP（集群内访问）、NodePort（节点端口）、LoadBalancer（外部负载均衡）、ExternalName（DNS别名）。

### 4. Namespace
- 逻辑隔离多个团队或项目的资源。
- 默认命名空间：`default`, `kube-system`, `kube-public`。

### 5. ConfigMap & Secret
- **ConfigMap**：存储非敏感配置数据（如环境变量、配置文件）。
- **Secret**：存储敏感数据（如密码、token、密钥），Base64编码。

### 6. Volume
- 数据卷，用于 Pod 内容器间共享数据或持久化存储。
- 类型：emptyDir、hostPath、PersistentVolume（PV）、PersistentVolumeClaim（PVC）等。

### 7. StatefulSet
- 管理有状态应用（如数据库），保证 Pod 有序部署、唯一网络标识、持久化存储。

### 8. DaemonSet
- 确保每个（或部分）节点运行一个 Pod 副本，常用于日志收集、监控代理。

### 9. Job & CronJob
- **Job**：运行一次性任务直到完成。
- **CronJob**：按计划运行 Job（类似 Linux cron）。

### 10. Ingress
- 管理外部访问集群服务的 HTTP/HTTPS 路由规则。
- 通常配合 Ingress Controller（如 Nginx、Traefik）使用。

### 11. Label & Selector
- **Label**：键值对，用于标识资源对象（如 `app=nginx`）。
- **Selector**：根据 Label 筛选资源，用于 Service、Deployment 等关联 Pod。

---

## 四、Kubernetes 核心组件

### 控制平面组件（Master Node）

#### 1. kube-apiserver
- Kubernetes API 入口，所有操作都通过它进行。
- 提供认证、授权、API 版本控制、数据校验。
- 是唯一与 etcd 交互的组件。

#### 2. etcd
- 一致且高可用的键值存储系统，保存集群所有配置数据。
- 所有集群状态变更都持久化到 etcd。

#### 3. kube-scheduler
- 监听新创建的 Pod，为其选择合适的 Node 节点运行。
- 调度策略基于资源需求、亲和性、污点容忍等。

#### 4. kube-controller-manager
- 运行控制器进程，确保集群状态符合期望状态。
- 包括：Node Controller、Replication Controller、Endpoint Controller、Service Account & Token Controllers 等。

#### 5. cloud-controller-manager（云环境）
- 与底层云平台交互（如 AWS、Azure、GCP），管理负载均衡、路由、节点等。

---

### 工作节点组件（Worker Node）

#### 1. kubelet
- 运行在每个节点上的代理，确保容器按 PodSpec 运行。
- 与容器运行时（如 Docker、containerd）交互，管理容器生命周期。

#### 2. kube-proxy
- 维护节点上的网络规则，实现 Service 的虚拟 IP 和负载均衡。
- 支持 iptables、ipvs 模式。

#### 3. 容器运行时（Container Runtime）
- 负责运行容器，如 Docker、containerd、CRI-O。
- Kubernetes 通过 CRI（Container Runtime Interface）与之通信。

---

## 五、Kubernetes 安装教程（单节点 Minikube 或 kubeadm）

> 本教程使用 **kubeadm** 工具部署单节点 Kubernetes 集群（Master + Worker 合一），适用于学习和开发。

---

## 🐧 Ubuntu 22.04 安装 Kubernetes

### 步骤 1：系统准备

```bash
# 关闭 swap（必须）
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# 加载内核模块
sudo modprobe overlay
sudo modprobe br_netfilter

# 配置 sysctl
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

### 步骤 2：安装容器运行时（containerd）

```bash
# 安装 containerd
sudo apt update
sudo apt install -y containerd

# 配置 containerd 使用 systemd cgroup
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/g' /etc/containerd/config.toml

sudo systemctl restart containerd
sudo systemctl enable containerd
```

### 步骤 3：安装 kubeadm、kubelet、kubectl

```bash
# 添加 Kubernetes apt 仓库
sudo apt update
sudo apt install -y apt-transport-https ca-certificates curl

curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

# 安装组件
sudo apt update
sudo apt install -y kubelet kubeadm kubectl

# 锁定版本防止自动更新
sudo apt-mark hold kubelet kubeadm kubectl
```

### 步骤 4：初始化集群

```bash
# 初始化 Master 节点（替换 YOUR_IP 为本机 IP）
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=YOUR_IP

# 配置 kubectl
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# 安装网络插件（Flannel）
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml

# （可选）允许 Master 节点运行 Pod（仅用于单节点测试）
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
```

### 步骤 5：验证安装

```bash
kubectl get nodes
kubectl get pods -A
```

---

## 🍎 Fedora 38/39 安装 Kubernetes

### 步骤 1：系统准备

```bash
# 关闭 swap
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# 加载内核模块
sudo modprobe overlay
sudo modprobe br_netfilter

cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

### 步骤 2：安装 containerd

```bash
sudo dnf install -y containerd

# 配置 containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/g' /etc/containerd/config.toml

sudo systemctl restart containerd
sudo systemctl enable containerd
```

### 步骤 3：添加 Kubernetes 仓库并安装

```bash
# 添加 Kubernetes repo
cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://pkgs.k8s.io/core:/stable:/v1.29/rpm/
enabled=1
gpgcheck=1
gpgkey=https://pkgs.k8s.io/core:/stable:/v1.29/rpm/repodata/repomd.xml.key
exclude=kubelet kubeadm kubectl cri-tools kubernetes-cni
EOF

# 安装组件
sudo dnf install -y kubelet kubeadm kubectl --disableexcludes=kubernetes

# 启动 kubelet
sudo systemctl enable --now kubelet
```

### 步骤 4：初始化集群

```bash
# 初始化（替换 YOUR_IP）
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=YOUR_IP

# 配置 kubectl
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# 安装 Flannel 网络插件
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml

# 允许 Master 运行工作负载
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
```

### 步骤 5：验证

```bash
kubectl get nodes
kubectl get pods -A
```

---

## ✅ 安装后验证

```bash
# 查看节点状态
kubectl get nodes

# 查看所有命名空间的 Pod
kubectl get pods -A

# 查看集群信息
kubectl cluster-info

# 部署测试应用
kubectl create deployment nginx --image=nginx
kubectl expose deployment nginx --port=80 --type=NodePort
kubectl get svc nginx
```

---

## 🚫 常见问题

1. **Pod 无法启动（ImagePullBackOff）**  
   检查镜像名称或配置国内镜像源（如阿里云）。

2. **CoreDNS CrashLoopBackOff**  
   通常为网络插件未安装或 CIDR 冲突，确认 `--pod-network-cidr` 与网络插件匹配。

3. **节点 NotReady**  
   检查 kubelet 状态：`systemctl status kubelet`，查看日志：`journalctl -u kubelet -f`

4. **防火墙干扰**  
   关闭防火墙或放行端口：
   ```bash
   sudo ufw disable          # Ubuntu
   sudo systemctl stop firewalld   # Fedora
   ```

---

## 📚 推荐学习资源

- 官方文档：https://kubernetes.io/zh-cn/docs/home/
- 交互式教程：https://kubernetes.io/zh-cn/docs/tutorials/kubernetes-basics/
- Kubernetes Playground：https://labs.play-with-k8s.com/

---

## ✅ 总结

Kubernetes 是云原生时代的基石，掌握其核心概念和组件是运维和开发人员的必备技能。本教程提供了在 Ubuntu 和 Fedora 上使用 kubeadm 安装单节点集群的完整步骤，适合学习、实验和本地开发环境搭建。

> ⚠️ 生产环境建议使用多节点高可用架构，或使用托管服务（如 EKS、AKS、GKE）。

---

如有问题，欢迎留言交流！Happy K8sing 🚀