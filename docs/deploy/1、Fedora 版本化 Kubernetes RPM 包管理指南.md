# 📘 Fedora 版本化 Kubernetes RPM 包管理指南
> 基于 Fedora 官方文档整理 · 适配 Fedora 41-43+ · 专注版本冲突处理与生命周期管理

---

## 📋 文档概览

| 项目 | 说明 |
|------|------|
| **核心主题** | Fedora 41+ 引入的**版本化 Kubernetes RPM 包**机制、安装、升级与退役策略 |
| **关键变化** | `kubernetes` → `kubernetes1.xx`（如 `kubernetes1.31`），**主版本作为包名一部分** |
| **适用场景** | 在 Fedora 上使用 `dnf` 管理 Kubernetes 组件的全生命周期 |
| **文档来源** | [Fedora Docs: Versioned Kubernetes RPMs on Fedora](https://docs.fedoraproject.org/) |

> 💡 **一句话总结**：  
> 从 Fedora 41 开始，Kubernetes 采用**版本化 RPM 包**（如 `kubernetes1.31`），不同主版本因安装相同文件而**互斥**，升级需使用 `dnf swap` 或 `remove+install`，**补丁版本仍可用 `dnf update` 直接升级**。

---

## 🔍 一、为什么引入「版本化 RPM」？

### 🔄 传统非版本化包的问题
```bash
# ❌ 旧方式：所有版本共用包名
dnf install kubernetes-kubelet
dnf upgrade kubernetes-kubelet  # 自动升级到仓库最新版

# ⚠️ 问题：
# 1. 无法并行安装多个版本（测试/灰度场景受限）
# 2. 升级不可控，可能意外跳到不兼容的大版本
# 3. 依赖管理混乱（CRI-O/cri-tools 版本难以匹配）
```

### ✅ 版本化包的核心优势
```bash
# ✅ 新方式：版本嵌入包名
dnf install kubernetes1.31-kubelet
dnf install kubernetes1.32-kubelet  # 与 1.31 共存？❌ 不行，但可明确选择

# ✅ 优势：
# 1. 版本显式声明，依赖关系清晰
# 2. 升级需主动执行，避免意外变更
# 3. 与 CRI-O/cri-tools 版本严格绑定（1:1:1 原则）
# 4. 支持多版本并行测试（通过不同安装路径/容器隔离）
```

### ⚔️ 版本冲突机制（关键理解）
```
📦 kubernetes1.31-kubelet 与 kubernetes1.32-kubelet 都包含：
   /usr/bin/kubelet
   /usr/lib/systemd/system/kubelet.service
   /usr/share/doc/kubernetes/...

🔒 RPM 检测到「相同文件路径」→ 标记为冲突包（Conflicts）
→ dnf 拒绝同时安装，升级时必须先移除旧版本
```

> 📌 **重要结论**：
> - **补丁版本升级**（1.31.2 → 1.31.3）：✅ `dnf update` 直接生效
> - **主版本升级**（1.31 → 1.32）：❌ 不能直接 `dnf upgrade`，需用 `swap` 或 `remove+install`

---

## 🧩 二、Kubernetes RPM 包详解（Fedora 41+）

### 📦 包清单与用途对照表

| RPM 包名 | 包含组件 | 核心用途 | 安装建议 |
|---------|---------|---------|---------|
| **`kubernetes1.xx`** | `kubelet` | 节点运行时代理，管理 Pod 生命周期 | ✅ **每个节点必装** |
| **`kubernetes1.xx-kubeadm`** | `kubeadm` | 集群初始化工具，简化部署流程 | ✅ 推荐所有节点安装（`kubeadm join` 需要） |
| **`kubernetes1.xx-client`** | `kubectl` | 命令行客户端，操作集群资源 | ✅ 控制平面节点必装；开发机可选装 |
| **`kubernetes1.xx-systemd`** | `kube-apiserver`<br>`kube-controller-manager`<br>`kube-scheduler`<br>`kube-proxy` | 控制平面组件的 systemd 服务定义 | ⚠️ **通常不需要**（kubeadm 以 Static Pod 方式管理这些组件） |

### 🔧 安装场景推荐

#### 场景 1：标准 kubeadm 集群（✅ 推荐）
```bash
# 所有节点（控制平面 + 工作节点）
sudo dnf install -y \
  kubernetes1.31 \
  kubernetes1.31-kubeadm \
  kubernetes1.31-client

# 💡 说明：
# - kubelet + kubeadm + kubectl 三件套满足 99% 场景
# - kubernetes-systemd 包无需安装（kubeadm 自动以 Pod 形式部署控制平面组件）
```

#### 场景 2：手动部署集群（如学习 "Kubernetes The Hard Way"）
```bash
# 控制平面节点
sudo dnf install -y \
  kubernetes1.31 \
  kubernetes1.31-client \
  kubernetes1.31-systemd

# 工作节点
sudo dnf install -y \
  kubernetes1.31 \
  kubernetes1.31-systemd  # 仅需 kube-proxy 的 systemd 配置

# ⚠️ 注意：手动部署需自行管理组件启动顺序、证书、配置等，复杂度高，仅建议学习使用
```

#### 场景 3：仅管理集群的开发/运维机
```bash
# 只需安装 kubectl，无需 kubelet/kubeadm
sudo dnf install -y kubernetes1.31-client

# 配置 kubeconfig 后即可远程管理集群
export KUBECONFIG=/path/to/admin.conf
kubectl get nodes
```

---

## ⚙️ 三、安装与升级策略（核心实操）

### 🚀 首次安装（以 1.31 为例）
```bash
# 1. 查询可用版本
dnf list kubernetes1.?? --showduplicates

# 2. 安装匹配版本套件（含 CRI-O/cri-tools）
sudo dnf install -y \
  kubernetes1.31 \
  kubernetes1.31-kubeadm \
  kubernetes1.31-client \
  cri-o1.31 \
  cri-tools1.31

# 3. 验证安装
kubelet --version    # v1.31.x
kubeadm version      # v1.31.x
kubectl version --client  # v1.31.x
```

### 🔄 升级策略详解（⚠️ 重点）

#### ✅ 补丁版本升级（安全、推荐）
```bash
# 场景：1.31.2 → 1.31.3（仅修复 bug/安全漏洞）
# ✅ 直接使用 dnf update，自动处理依赖

sudo dnf update -y 'kubernetes1.31*' 'cri-o1.31*' 'cri-tools1.31*'

# 重启相关服务
sudo systemctl restart crio kubelet

# 验证
kubectl version --short
```

#### ⚠️ 主版本升级（需谨慎规划）
```bash
# 场景：1.31 → 1.32（新特性 + 可能不兼容变更）
# ❌ 不能直接 dnf upgrade（包冲突）
# ✅ 两种方案：

# 方案 A：remove + install（清晰可控，推荐）
sudo dnf remove -y kubernetes1.31* cri-o1.31* cri-tools1.31*
sudo dnf install -y kubernetes1.32* cri-o1.32* cri-tools1.32*

# 方案 B：dnf swap（简洁，但可能安装额外依赖）
sudo dnf swap 'kubernetes1.31*' 'kubernetes1.32*' \
              'cri-o1.31*' 'cri-o1.32*' \
              'cri-tools1.31*' 'cri-tools1.32*'

# 升级后必须执行：
sudo kubeadm upgrade plan          # 确认可升级版本
sudo kubeadm upgrade apply v1.32.0  # 执行集群升级
sudo systemctl daemon-reload
sudo systemctl restart kubelet crio
```

### 🔐 升级前检查清单（生产环境必备）
```bash
# 1. 备份关键配置
sudo cp -a /etc/kubernetes /backup/k8s-etc-$(date +%F)
sudo cp -a /var/lib/kubelet /backup/kubelet-data-$(date +%F)
sudo cp -a /etc/crio /backup/crio-config-$(date +%F)

# 2. 检查集群健康状态
kubectl get nodes
kubectl get pods -A | grep -v Running
kubectl cluster-info dump | grep -i error

# 3. 查阅目标版本变更日志
# 🔗 https://github.com/kubernetes/kubernetes/blob/master/CHANGELOG/CHANGELOG-1.32.md

# 4. 在测试环境先行验证（强烈建议）
```

---

## 📅 四、版本生命周期与退役政策

### 🔄 Kubernetes 官方支持周期
```
发布节奏：每 4 个月发布一个新小版本（如 1.31 → 1.32）
支持周期：每个小版本支持 12 个月
          ↓
          3 个月宽限期 + 9 个月关键修复

示例：
- v1.31 发布：2024-08
- 停止关键修复：2025-08
- 完全退役：2025-10（+2 个月缓冲）
```

### 🗑️ Fedora RPM 退役流程
```
当 Kubernetes 某版本上游终止支持后：
1. +2 个月：从 Rawhide（开发版）仓库移除对应 RPM
2. +1 个月：从稳定版 Fedora（41/42/43）仓库移除
3. 用户需提前升级到受支持版本，否则 `dnf update` 将失败
```

🔗 查询支持状态：
- Kubernetes：https://kubernetes.io/releases/patch-releases/
- Fedora 包：https://src.fedoraproject.org/rpms/kubernetes1.31

### 🛡️ 防止意外升级/退役影响
```bash
# 1. 使用 versionlock 锁定当前版本（允许补丁更新）
sudo dnf install -y python3-dnf-plugin-versionlock
sudo dnf versionlock add kubernetes1.31* cri-o1.31* cri-tools1.31*

# 2. 定期检查锁定状态
dnf versionlock list
dnf check-update | grep kubernetes  # 应无主版本更新提示

# 3. 设置监控告警（契合你的运维习惯）
# 示例：cron 每日检查可用更新
0 2 * * * /usr/bin/dnf check-update --quiet | grep kubernetes1.31 && \
  /usr/bin/logger "⚠️ Kubernetes 1.31 有新补丁可用"
```

---

## 🐧 五、Fedora 43 专属适配要点

### ✅ 环境预检（执行安装前必跑）
```bash
# 1. 确认 Go 版本约束（影响新 Kubernetes 版本可用性）
go version  # Fedora 43 默认 Go 1.23+
# 💡 如果 Kubernetes 1.33 需要 Go 1.24，而 Fedora 43 尚未提供，则该版本暂不可用

# 2. 验证 cgroups v2 + systemd 驱动
stat -fc %T /sys/fs/cgroup/  # 应输出: cgroup2fs
grep cgroupDriver /var/lib/kubelet/config.yaml  # 应为: systemd

# 3. 检查网络模块加载
lsmod | grep -E 'br_netfilter|overlay'
sysctl net.bridge.bridge-nf-call-iptables net.ipv4.ip_forward  # 两项应为 1
```

### ⚠️ 已知问题与解决方案
| 问题 | 原因 | 解决方案 |
|------|------|---------|
| `dnf install kubernetes1.32` 失败，提示 `conflicts with kubernetes1.31` | 未先移除旧版本 | 按「三-2」使用 `remove+install` 或 `swap` |
| 升级后 kubelet 启动失败 | 配置文件格式变更 / CRI 接口调整 | 检查 `/var/log/journal` 日志 + 对比新旧 config.yaml |
| `kubeadm upgrade` 报错 `CRI version mismatch` | CRI-O 未同步升级 | 确保 `cri-o1.xx` 与 `kubernetes1.xx` 主版本一致 |
| 补丁升级后节点 `NotReady` | kubelet 配置缓存未刷新 | `sudo systemctl daemon-reload && sudo systemctl restart kubelet` |

---

## 💡 六、给你的专属建议（结合你的背景）

> 你当前：Fedora 43 + 银行保险行业 + 人工核保系统 + 关注构建规范/可维护性/安全性

### ✅ 工程化实践建议（契合你的开发习惯）

#### 1. 版本管理策略（参考 Maven Profile 思路）
```bash
# 项目根目录创建 .k8s-version 文件（类似 .java-version）
echo "kubernetes1.31" > .k8s-version

# CI/CD 脚本中读取并安装匹配版本
K8S_VER=$(cat .k8s-version)
sudo dnf install -y ${K8S_VER}* cri-o${K8S_VER#kubernetes}* cri-tools${K8S_VER#kubernetes}*

# ✅ 优势：
# - 团队环境一致性保障
# - 升级时只需修改 .k8s-version 文件
# - 与你的 Maven Profile 环境隔离理念一致
```

#### 2. 配置即代码（契合你的日志/测试规范重视）
```bash
# 将 CRI-O/kubelet 配置纳入 Git 管理
mkdir -p infra/k8s/fedora43/{crio,kubelet}

# 示例：infra/k8s/fedora43/crio/99-bank-prod.conf
[crio.runtime]
cgroup_manager = "systemd"
log_driver = "journald"
# 金融环境：禁用非必要特权
default_privileged = false
# 镜像安全：仅允许内部仓库
[crio.image]
registries = ["registry.internal.bank.com"]

# 使用 Ansible 部署（参考你的 PowerJob 任务思路）
- name: Deploy hardened CRI-O config
  copy:
    src: infra/k8s/fedora43/crio/
    dest: /etc/crio/crio.conf.d/
    owner: root
    mode: '0644'
  notify: reload crio
```

#### 3. 升级流程标准化（契合你对可维护性的重视）
```mermaid
graph LR
    A[收到升级通知] --> B[查阅 CHANGELOG + 评估影响]
    B --> C[测试环境验证]
    C --> D[备份配置 + 数据]
    D --> E[执行 dnf swap/remove+install]
    E --> F[kubeadm upgrade apply]
    F --> G[滚动重启节点 + 验证业务]
    G --> H[更新 .k8s-version + 提交变更]
```

### 🔐 金融行业特别加固建议
```bash
# 1. 镜像签名验证（防止恶意镜像）
# /etc/crio/crio.conf.d/99-security.conf
[crio.signature]
policy_path = "/etc/containers/policy.json"
signature_path = "/etc/containers/signatures"

# 2. 审计关键操作（满足合规要求）
sudo auditctl -w /usr/bin/kubelet -p x -k k8s-exec
sudo auditctl -w /etc/kubernetes -p wa -k k8s-config-change

# 3. 网络策略默认拒绝（零信任）
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
  namespace: nuclear-underwriting  # 你的核保系统命名空间
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
EOF
```

### 🎯 本地开发 vs 生产环境分离建议
| 环境 | 推荐方案 | 理由 |
|------|---------|------|
| **本地开发** | Kind + Podman 驱动 | 快速启停、不影响主机 RPM、专注业务逻辑 |
| **测试环境** | 虚拟机 + 本文档方案 | 模拟真实部署流程、验证升级脚本 |
| **预生产/生产** | 物理机/专有云 + 严格加固 | 满足金融合规、高性能、高可用要求 |

---

## 📎 附录：关键命令速查

### 🔍 诊断与验证
```bash
# 查看已安装 Kubernetes 版本
rpm -qa | grep '^kubernetes1\.' | sort

# 检查包冲突
rpm -q --conflicts kubernetes1.31-kubelet

# 验证组件协同
crictl version && kubelet --version && kubeadm version

# 检查升级可行性
dnf check-update 'kubernetes1.31*' --quiet
```

### 🔄 服务管理
```bash
# 重载配置（修改 drop-in 后）
sudo systemctl daemon-reload
sudo systemctl reload crio    # CRI-O 热重载
sudo systemctl restart kubelet  # kubelet 需重启

# 查看依赖关系
systemctl list-dependencies kubelet.service
```

### 🗑️ 清理与回滚
```bash
# 移除旧版本（保留配置）
sudo dnf remove -y kubernetes1.30* cri-o1.30*

# 紧急回滚（升级失败时）
sudo dnf swap 'kubernetes1.32*' 'kubernetes1.31*'
sudo kubeadm upgrade apply v1.31.3  # 回退集群版本
```

---

## 🌟 最后总结：三大核心原则

```
✅ 原则 1：版本显式声明
   - 安装/升级时始终指定主版本：kubernetes1.31* 而非 kubernetes*
   - 在 .k8s-version 或 CI 配置中固化版本，保障环境一致性

✅ 原则 2：升级分而治之
   - 补丁升级：dnf update（自动）
   - 主版本升级：dnf swap/remove+install + kubeadm upgrade（手动）
   - 升级前必备份、必测试、必查日志

✅ 原则 3：配置持久化 + 安全加固
   - 自定义配置放入 drop-in 目录（/etc/crio/crio.conf.d/）
   - 金融环境：镜像签名 + 网络策略 + 审计日志 + SELinux 策略
```

> 💡 **给你的行动建议**：  
> 1️⃣ 立即在本地创建 `.k8s-version` 文件，固化当前开发环境版本  
> 2️⃣ 将 CRI-O/kubelet 的 drop-in 配置纳入你的项目仓库（参考 Maven 多环境配置）  
> 3️⃣ 编写一个 `upgrade-k8s.sh` 脚本，封装 `dnf swap + kubeadm upgrade + 验证` 流程

需要我为你：
- ✅ 生成一份「Fedora 43 + Kubernetes 1.31」的**完整安装+升级自动化脚本**？
- ✅ 提供适配你**人工核保系统**的 NetworkPolicy + ResourceQuota 安全模板？
- ✅ 整理一份「金融级 Kubernetes 集群」的**合规检查清单**（等保/银监要求）？

随时告诉我，我可以继续为你定制 👇