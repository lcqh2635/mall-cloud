import {defineStore} from 'pinia'
import {ref, watch} from 'vue'

// 定义布局类型
type LayoutType = 'classic' | 'horizontal' | 'sidebar'

// 定义主题颜色
type ThemeColor = 'blue' | 'green' | 'red' | 'purple' | 'orange' | 'teal' | 'pink'

export const useSettingsStore = defineStore(
    'settings',
    () => {
        // 从 localStorage 加载设置或使用默认值
        const defaultLayout = (localStorage.getItem('layout') as LayoutType) || 'classic'
        const defaultTheme = (localStorage.getItem('theme') as ThemeColor) || 'blue'
        const defaultDarkMode = localStorage.getItem('darkMode') === 'true'

        // 响应式状态 state
        const layout = ref<LayoutType>(defaultLayout)
        const themeColor = ref<ThemeColor>(defaultTheme)
        const darkMode = ref<boolean>(defaultDarkMode)
        const settingsDrawerVisible = ref<boolean>(false)

        // 切换布局
        const switchLayout = (newLayout: LayoutType) => {
            layout.value = newLayout
        }

        // 切换主题颜色
        const switchTheme = (color: ThemeColor) => {
            themeColor.value = color
        }

        // 切换暗黑模式
        const toggleDarkMode = () => {
            darkMode.value = !darkMode.value
        }

        // 持久化设置到 localStorage
        watch([layout, themeColor, darkMode], ([newLayout, newTheme, newDarkMode]) => {
            localStorage.setItem('layout', newLayout)
            localStorage.setItem('theme', newTheme)
            localStorage.setItem('darkMode', String(newDarkMode))
        })

        return {
            layout,
            themeColor,
            darkMode,
            settingsDrawerVisible,
            switchLayout,
            switchTheme,
            toggleDarkMode
        }
    },
    {
        persist: {
            key: 'vue3-admin-settings',
            storage: localStorage,
            pick: ['layout', 'themeColor', 'darkMode']
        }
    }
)