import { watch } from 'vue'
import { useSettingsStore } from '@/stores/modules/settings.store.ts'

// 主题颜色映射
const themeColors = {
    blue: {
        primary: '#3b82f6',
        primaryLight: '#93c5fd',
        primaryDark: '#1d4ed8'
    },
    green: {
        primary: '#10b981',
        primaryLight: '#6ee7b7',
        primaryDark: '#047857'
    },
    red: {
        primary: '#ef4444',
        primaryLight: '#fca5a5',
        primaryDark: '#b91c1c'
    },
    purple: {
        primary: '#8b5cf6',
        primaryLight: '#c4b5fd',
        primaryDark: '#6d28d9'
    },
    orange: {
        primary: '#f97316',
        primaryLight: '#fdba74',
        primaryDark: '#c2410c'
    },
    teal: {
        primary: '#14b8a6',
        primaryLight: '#5eead4',
        primaryDark: '#0d9488'
    },
    pink: {
        primary: '#ec4899',
        primaryLight: '#f9a8d4',
        primaryDark: '#be185d'
    }
}

export const useTheme = () => {
    const settingsStore = useSettingsStore()

    // 应用主题颜色
    const applyTheme = () => {
        const colors = themeColors[settingsStore.themeColor]
        document.documentElement.style.setProperty('--color-primary', colors.primary)
        document.documentElement.style.setProperty('--color-primary-light', colors.primaryLight)
        document.documentElement.style.setProperty('--color-primary-dark', colors.primaryDark)
    }

    // 应用暗黑模式
    const applyDarkMode = () => {
        if (settingsStore.darkMode) {
            document.documentElement.classList.add('dark')
        } else {
            document.documentElement.classList.remove('dark')
        }
    }

    // 初始化应用主题
    applyTheme()
    applyDarkMode()

    // 监听主题和暗黑模式变化
    watch(() => settingsStore.themeColor, applyTheme)
    watch(() => settingsStore.darkMode, applyDarkMode)
}