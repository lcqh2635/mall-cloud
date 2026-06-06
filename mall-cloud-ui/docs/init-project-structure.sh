#!/bin/bash
# ==============================================================================
# 脚本名称: init-project-structure.sh
# 功能描述：一次性脚手架脚本，仅在项目初始化或重构目录时使用
# 适用系统：Linux
# 作者：龙茶清欢 (优化版)
# 版本：2.0.0
# 使用方法：chmod +x scripts/init-project-structure.sh && ./scripts/init-project-structure.sh
# ==============================================================================
set -euo pipefail

echo "🚀 Initializing Vue3 + Vite + TS project structure..."

# ========== 根目录 ==========
echo ">>> 创建项目基础目录..."
mkdir -vp docs plugins tests scripts/{deploy,utils} mocks/{handlers,data}
touch scripts/init-project-structure.sh
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