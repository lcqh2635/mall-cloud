#!/usr/bin/env bash
# ==========================================================
# 中间件集群统一编排启动脚本
# 说明：按依赖顺序拉起各子 compose 文件，结合健康检查保障服务就绪
# ==========================================================

# 启用严格模式：遇错立即退出、未定义变量报错、管道失败传递错误码
set -euo pipefail

# 加载根目录 .env 环境变量（若存在则自动导出至当前 Shell 环境）
if [[ -f .env ]]; then
  set -a  # 开启自动导出所有变量至环境
  source .env
  set +a  # 关闭自动导出
fi

# 定义基础命令与路径变量
COMPOSE_CMD="docker compose"  # Docker Compose 命令别名（兼容 V2 规范）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"  # 获取脚本所在绝对路径
PROJECT_NAME="infra"  # 统一项目前缀，确保所有文件共享同一网络与卷上下文

echo "🚀 开始按序启动微服务中间件集群..."

# ---------------- 阶段 1：启动核心数据层（PG + Redis） ----------------
echo "📦 [1/4] 启动数据层..."
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/00-networks.yaml" -f "$SCRIPT_DIR/01-volumes.yaml" -f "$SCRIPT_DIR/db/compose.yaml" up -d
# 等待 PG 与 Redis 健康检查通过（需 Docker Compose V2.20+）
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/db/compose.yaml" wait postgres --timeout=60
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/db/compose.yaml" wait redis --timeout=30
echo "✅ 数据层已就绪"

# ---------------- 阶段 2：启动 Nacos（强依赖 PG 连接） ----------------
echo "📦 [2/4] 启动 Nacos 注册配置中心..."
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/00-networks.yaml" -f "$SCRIPT_DIR/01-volumes.yaml" -f "$SCRIPT_DIR/middleware/nacos.yaml" up -d
# 等待 Nacos 控制台 HTTP 端口可达
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/middleware/nacos.yaml" wait nacos --timeout=90
echo "✅ Nacos 已就绪（访问 http://localhost:${NACOS_HTTP}/nacos）"

# ---------------- 阶段 3：启动 RocketMQ（依赖 NameServer 路由） ----------------
echo "📦 [3/4] 启动 RocketMQ 消息队列..."
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/00-networks.yaml" -f "$SCRIPT_DIR/01-volumes.yaml" -f "$SCRIPT_DIR/middleware/rocketmq.yaml" up -d
# 等待 NameServer 路由端口监听
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/middleware/rocketmq.yaml" wait rocketmq-namesrv --timeout=40
# 等待 Broker 消息收发端口监听
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/middleware/rocketmq.yaml" wait rocketmq-broker --timeout=60
echo "✅ RocketMQ 已就绪"

# ---------------- 阶段 4：启动独立中间件（Sentinel / Seata） ----------------
echo "📦 [4/4] 启动辅助中间件..."
$COMPOSE_CMD -p $PROJECT_NAME -f "$SCRIPT_DIR/00-networks.yaml" -f "$SCRIPT_DIR/01-volumes.yaml" -f "$SCRIPT_DIR/middleware/sentinel.yaml" -f "$SCRIPT_DIR/middleware/seata.yaml" up -d
echo "✅ Sentinel 与 Seata 已就绪"

echo "🎉 所有中间件集群启动完成！"
echo "📊 可通过以下命令查看实时状态与日志："
echo "   docker compose -p $PROJECT_NAME ps"
echo "   docker compose -p $PROJECT_NAME logs -f nacos"