#!/bin/bash
# ==============================================================================
# 脚本名称: init-project-structure.sh
# 功能描述：一次性脚手架脚本，仅在项目初始化或重构目录时使用
# 适用系统：Linux
# 作者：龙茶清欢 (优化版)
# 版本：2.0.0
# 使用方法：chmod +x scripts/init-project-structure.sh && ./scripts/init-project-structure.sh
# ==============================================================================

# 确保脚本在遇到错误时立即退出
set -euo pipefail

# =========================================================
# 🎯 核心优化：自动定位并切换到项目根目录
# =========================================================
# 1. 获取当前脚本所在的绝对目录 (即 scripts/)
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# 2. 切换到脚本所在目录的上一级 (即项目根目录)
cd "$SCRIPT_DIR/.." || exit

# 3. 安全校验：检查当前目录是否存在 package.json，以确认我们真的在根目录
if [ ! -f "package.json" ]; then
    echo "❌ 错误：未找到 package.json。请确认此脚本位于项目的 scripts/ 目录下。"
    exit 1
fi

# 4. 打印当前工作目录，方便确认
echo "✅ 工作目录已自动切换至: $(pwd)"
echo "🚀 开始初始化 Vue3 + Vite + TS 项目结构..."
echo "---------------------------------------------------"

# =========================================================
# 📂 目录与文件创建逻辑 (保持不变)
# =========================================================

echo "🚀 Initializing Vue3 + Vite + TS project structure..."

# ========== 根目录 ==========
echo ">>> 创建项目基础目录..."
mkdir -vp docs plugins tests scripts/{deploy,utils} mocks/{handlers,data}
touch docs/README.md
touch plugins/README.md
touch scripts/init-project-structure.sh
touch scripts/{deploy,utils}/README.md
touch mocks/handlers/api.mock.ts
touch mocks/data/data.mock.ts
touch bunfig.toml README.zh-CN.md uno.config.ts vitest.config.ts

# ========== 环境配置 ==========
echo ">>> 创建环境变量文件..."
touch .env .env.development .env.production .env.test
# 注意：请确保 .gitignore 中包含 .env.*.local

# ========== 测试目录 ==========
mkdir -vp tests/{unit/{utils,composables,stores},e2e/{fixtures,pages,specs}}
touch tests/unit/setup.ts
touch tests/unit/utils/format.spec.ts
touch tests/unit/composables/useAuth.spec.ts
touch tests/unit/stores/user.spec.ts

# ========== src 源码 ==========
echo ">>> 创建 src 核心入口..."
mkdir -vp src/{api/{types,modules,enums},assets/{images,fonts,svgs},components,composables,directives,layouts/{modules,components/{Logo,Sidebar,Navbar,AppMain}},locales/modules/{zh-CN,en-US},plugins,router/{guards,modules},stores/modules,styles,types,utils,views/{login,dashboard,user,system}}
# API
touch src/api/{index,request}.ts
touch src/api/types/{user,order}.ts
touch src/api/modules/{user,order}.ts
# Components
touch src/components/index.ts
# Composables
touch src/composables/{index,useAuth}.ts
# Directives
touch src/directives/{index,auth,permission}.ts
# Layouts
touch src/layouts/index.vue
touch src/layouts/modules/{DefaultLayout,MixedLayout,TopNavLayout}.vue
# Locales
touch src/locales/index.ts
touch src/locales/modules/zh-CN/{common,layout,login,dashboard,index}.ts
touch src/locales/modules/en-US/{common,layout,login,dashboard,index}.ts
# Plugins
touch src/plugins/index.ts
# Router
touch src/router/{index,routes,constants}.ts
touch src/router/guards/{auth,permission}.ts
touch src/router/modules/{user,order}.ts
# Stores
touch src/stores/index.ts
touch src/stores/modules/{app,user}.ts
# Styles
touch src/styles/{index,variables}.scss
# Types (全局声明保留 .d.ts)
touch src/types/{api,env,vue-shims}.d.ts
# Utils
touch src/utils/{index,storage,format,validate}.ts
# Views
touch src/views/{dashboard,login,user,system}/index.vue

echo "✅ Project structure initialized successfully!"