// stores/modules/app.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用配置store
 * - 包含主题、布局等全局配置
 * - 持久化到sessionStorage
 */
export const useAppStore = defineStore(
    'app',
    () => {
        // 主题模式 'light' | 'dark'
        const theme = ref<'light' | 'dark'>('light')
        // 侧边栏是否折叠
        const sidebarCollapsed = ref(false)
        // 是否显示设置面板
        const showSettings = ref(false)

        // 切换主题
        function toggleTheme() {
            theme.value = theme.value === 'light' ? 'dark' : 'light'
        }

        // 切换侧边栏状态
        function toggleSidebar() {
            sidebarCollapsed.value = !sidebarCollapsed.value
        }

        return {
            theme,
            sidebarCollapsed,
            showSettings,
            toggleTheme,
            toggleSidebar
        }
    },
    {
        persist: {
            key: 'vue3-admin-app',
            storage: sessionStorage, // 使用sessionStorage而不是localStorage
            pick: ['theme', 'sidebarCollapsed'] // 只持久化这两个状态
        }
    }
)