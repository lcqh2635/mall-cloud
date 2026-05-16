import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LayoutType } from '@/types/layout'

export const useLayoutStore = defineStore('layout', () => {
    // 从 localStorage 读取保存的布局
    const savedLayout = localStorage.getItem('layout') as LayoutType | null
    const layoutType = ref<LayoutType>(savedLayout || 'default')

    const layoutComponent = computed(() => {
        return defineAsyncComponent(() => import(`@/layouts/${layoutType.value.charAt(0).toUpperCase() + layoutType.value.slice(1)}Layout.vue`))
    })

    function setLayout(type: LayoutType) {
        layoutType.value = type
        // 保存到 localStorage
        localStorage.setItem('layout', type)
    }

    return {
        layoutType,
        layoutComponent,
        setLayout
    }
})