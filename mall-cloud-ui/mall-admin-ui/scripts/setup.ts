import { existsSync, mkdirSync, writeFileSync } from 'fs'
import { join } from 'path'

const root = process.cwd()

// =========================================================
// 🎯 辅助函数：保证幂等性与静默输出
// =========================================================

/** 安全创建目录（等同 mkdir -vp，但静默） */
function ensureDir(dirPath: string) {
    const fullPath = join(root, dirPath)
    if (!existsSync(fullPath)) {
        mkdirSync(fullPath, { recursive: true })
        return true // 表示创建了
    }
    return false
}

/** 安全创建文件（等同 touch，但静默且不覆盖） */
function ensureFile(filePath: string, content = '') {
    const fullPath = join(root, filePath)
    if (!existsSync(fullPath)) {
        writeFileSync(fullPath, content, 'utf-8')
        return true
    }
    return false
}

// =========================================================
// 📦 定义项目结构数据 (从你的 Shell 脚本提取)
// =========================================================

const DIRS = [
    // 基础目录
    'docs', 'plugins', 'mocks/handlers', 'mocks/data', 'public',
    'scripts/deploy', 'scripts/utils',
    // 测试目录
    'tests/unit/utils', 'tests/unit/composables', 'tests/unit/stores', 'tests/unit/components',
    'tests/e2e/fixtures', 'tests/e2e/pages', 'tests/e2e/specs',
    // src 源码目录
    'src/api/types', 'src/api/modules', 'src/api/enums',
    'src/assets/images', 'src/assets/fonts', 'src/assets/icons',
    'src/components/common', 'src/components/business', 'src/components/global',
    'src/composables', 'src/directives',
    'src/layouts/modules', 'src/layouts/components/Logo', 'src/layouts/components/Sidebar',
    'src/layouts/components/Navbar', 'src/layouts/components/AppMain', 'src/layouts/components/TagsView', 'src/layouts/components/Settings',
    'src/locales/modules/zh-CN', 'src/locales/modules/en-US',
    'src/plugins', 'src/router/guards', 'src/router/modules',
    'src/stores/modules', 'src/styles', 'src/types', 'src/utils', 'src/hooks',
    'src/views/auth', 'src/views/dashboard', 'src/views/user', 'src/views/system', 'src/views/error',
    // 特定视图的 components 目录
    'src/views/login/components', 'src/views/dashboard/components',
    'src/views/user/components', 'src/views/system/components',
]

const FILES = [
    // 根目录
    'bunfig.toml', 'README.zh-CN.md', 'uno.config.ts', 'vitest.config.ts', 'playwright.config.ts',
    // 测试
    'tests/unit/setup.ts',
    'tests/unit/utils/format.spec.ts', 'tests/unit/composables/useAuth.spec.ts', 'tests/unit/stores/user.spec.ts',
    // src 核心
    'src/App.vue', 'src/main.ts',
    // API
    'src/api/index.ts', 'src/api/request.ts', 'src/api/config.ts',
    'src/api/types/user.ts', 'src/api/types/order.ts', 'src/api/types/common.ts',
    'src/api/modules/auth.ts', 'src/api/modules/user.ts', 'src/api/modules/order.ts', 'src/api/modules/system.ts',
    'src/api/enums/http-status.ts', 'src/api/enums/result-code.ts', 'src/api/enums/user-status.ts', 'src/api/enums/menu-type.ts',
    // 组件
    'src/components/index.ts',
    'src/components/common/BaseTable.vue', 'src/components/common/BaseForm.vue', 'src/components/common/BaseSearch.vue', 'src/components/common/BaseDialog.vue',
    'src/components/business/UserSelect.vue', 'src/components/business/DeptTree.vue', 'src/components/business/RoleTag.vue',
    'src/components/global/SvgIcon.vue', 'src/components/global/AppLink.vue',
    // Composables & Hooks
    'src/composables/index.ts', 'src/composables/useAuth.ts', 'src/composables/useTheme.ts', 'src/composables/useTable.ts', 'src/composables/useForm.ts', 'src/composables/useModal.ts',
    'src/hooks/usePagination.ts', 'src/hooks/useDict.ts', 'src/hooks/useDownload.ts', 'src/hooks/useMessage.ts',
    // Directives
    'src/directives/index.ts', 'src/directives/auth.ts', 'src/directives/permission.ts', 'src/directives/loading.ts', 'src/directives/debounce.ts',
    // Layouts (由于层级深，单独列出更清晰)
    'src/layouts/index.ts', 'src/layouts/index.vue',
    'src/layouts/modules/DefaultLayout.vue', 'src/layouts/modules/TopNavLayout.vue', 'src/layouts/modules/MixedLayout.vue',
    'src/layouts/components/Logo/index.vue', 'src/layouts/components/Logo/LogoCollapse.vue',
    'src/layouts/components/Sidebar/index.vue', 'src/layouts/components/Sidebar/SidebarItem.vue', 'src/layouts/components/Sidebar/SidebarMenu.vue',
    'src/layouts/components/Navbar/index.vue', 'src/layouts/components/Navbar/Breadcrumb.vue', 'src/layouts/components/Navbar/Fullscreen.vue', 'src/layouts/components/Navbar/ThemeSwitch.vue', 'src/layouts/components/Navbar/UserDropdown.vue',
    'src/layouts/components/AppMain/index.vue', 'src/layouts/components/AppMain/KeepAliveWrapper.vue',
    'src/layouts/components/TagsView/index.vue', 'src/layouts/components/TagsView/TagItem.vue', 'src/layouts/components/TagsView/ContextMenu.vue',
    'src/layouts/components/Settings/index.vue', 'src/layouts/components/Settings/LayoutSelect.vue', 'src/layouts/components/Settings/ColorPicker.vue', 'src/layouts/components/Settings/SwitchItem.vue',
    // Locales
    'src/locales/index.ts',
    'src/locales/modules/zh-CN/common.ts', 'src/locales/modules/zh-CN/layout.ts', 'src/locales/modules/zh-CN/login.ts', 'src/locales/modules/zh-CN/dashboard.ts', 'src/locales/modules/zh-CN/system.ts', 'src/locales/modules/zh-CN/routes.ts', 'src/locales/modules/zh-CN/index.ts',
    'src/locales/modules/en-US/common.ts', 'src/locales/modules/en-US/layout.ts', 'src/locales/modules/en-US/login.ts', 'src/locales/modules/en-US/dashboard.ts', 'src/locales/modules/en-US/system.ts', 'src/locales/modules/en-US/routes.ts', 'src/locales/modules/en-US/index.ts',
    // Plugins & Router
    'src/plugins/index.ts', 'src/plugins/element-plus.ts', 'src/plugins/permission.ts', 'src/plugins/i18n.ts',
    'src/router/index.ts', 'src/router/routes.ts', 'src/router/constants.ts', 'src/router/types.ts',
    'src/router/guards/auth.ts', 'src/router/guards/permission.ts', 'src/router/guards/progress.ts', 'src/router/guards/title.ts',
    'src/router/modules/dashboard.ts', 'src/router/modules/user.ts', 'src/router/modules/system.ts', 'src/router/modules/login.ts',
    // Stores & Styles
    'src/stores/index.ts', 'src/stores/modules/app.ts', 'src/stores/modules/theme.ts', 'src/stores/modules/user.ts', 'src/stores/modules/permission.ts', 'src/stores/modules/tagsView.ts',
    'src/styles/index.css', 'src/styles/theme.css', 'src/styles/transition.css', 'src/styles/element-override.css', 'src/styles/scrollbar.css',
    // Types & Utils
    'src/types/api.d.ts', 'src/types/env.d.ts', 'src/types/router.d.ts', 'src/types/store.d.ts', 'src/types/global.d.ts',
    'src/utils/index.ts', 'src/utils/storage.ts', 'src/utils/format.ts', 'src/utils/validate.ts', 'src/utils/request.ts', 'src/utils/crypto.ts', 'src/utils/tree.ts',
    // Views
    'src/views/login/index.vue', 'src/views/login/components/LoginForm.vue', 'src/views/login/components/RegisterForm.vue',
    'src/views/dashboard/index.vue', 'src/views/dashboard/components/StatisticCard.vue', 'src/views/dashboard/components/ChartPanel.vue',
    'src/views/user/index.vue', 'src/views/user/components/UserTable.vue', 'src/views/user/components/UserForm.vue',
    'src/views/system/index.vue', 'src/views/system/components/MenuManage.vue', 'src/views/system/components/RoleManage.vue',
    'src/views/error/404.vue', 'src/views/error/403.vue', 'src/views/error/500.vue',
]

// 带内容的特殊文件
const CONTENT_FILES: Record<string, string> = {
    '.env.example': `# API 基础地址\nVITE_API_BASE_URL=http://localhost:8080/api\n# 应用标题\nVITE_APP_TITLE=Admin Pro\n# 是否开启 Mock\nVITE_USE_MOCK=true\n# 应用版本\nVITE_APP_VERSION=1.0.0`
}

// =========================================================
// 🚀 执行初始化
// =========================================================

function initProject() {
    let hasChanges = false

    // 1. 创建目录
    DIRS.forEach(dir => {
        if (ensureDir(dir)) {
            console.log(`✅ 创建目录: ${dir}`)
            hasChanges = true
        }
    })

    // 2. 创建空文件
    FILES.forEach(file => {
        if (ensureFile(file)) {
            console.log(`✅ 生成文件: ${file}`)
            hasChanges = true
        }
    })

    // 3. 创建带内容的文件
    Object.entries(CONTENT_FILES).forEach(([file, content]) => {
        if (ensureFile(file, content)) {
            console.log(`✅ 生成文件: ${file}`)
            hasChanges = true
        }
    })

    // 4. 只有在确实发生变更时才输出总结提示
    if (hasChanges) {
        console.log('\n🚀 项目结构初始化完成！')
        console.log('📋 后续步骤：')
        console.log(' 1. 复制 .env.example 为 .env 并配置环境变量')
        console.log(' 2. 配置 biome.json 和 lefthook.yml')
        console.log(' 3. 运行 bun dev 启动开发服务器')
    }
}

initProject()