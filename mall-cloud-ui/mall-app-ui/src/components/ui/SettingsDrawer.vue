<script lang="ts" setup>
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/modules/settings.store.ts'
import ThemeColorPicker from './ThemeColorPicker.vue'

const settingsStore = useSettingsStore()

const visible = computed(() => settingsStore.settingsDrawerVisible)
const currentLayout = computed(() => settingsStore.layout)
const darkMode = computed(() => settingsStore.darkMode)

const layouts = [
  {
    value: 'classic',
    label: '经典布局',
    previewClass: 'bg-gradient-to-r from-blue-500 to-blue-300'
  },
  {
    value: 'horizontal',
    label: '水平布局',
    previewClass: 'bg-gradient-to-r from-green-500 to-green-300'
  },
  {
    value: 'sidebar',
    label: '侧边栏布局',
    previewClass: 'bg-gradient-to-r from-purple-500 to-purple-300'
  }
]

const closeDrawer = () => {
  settingsStore.settingsDrawerVisible = false
}

const switchLayout = (layout: 'classic' | 'horizontal' | 'sidebar') => {
  settingsStore.switchLayout(layout)
}

const toggleDarkMode = () => {
  settingsStore.toggleDarkMode()
}
</script>

<template>
  <transition name="slide-fade">
    <div
        v-if="visible"
        class="fixed inset-0 overflow-hidden z-50"
        @click.self="closeDrawer"
    >
      <div class="absolute inset-y-0 right-0 max-w-full flex">
        <div class="relative w-screen max-w-md">
          <div class="h-full flex flex-col bg-white dark:bg-gray-800 shadow-xl">
            <!-- 抽屉头部 -->
            <div class="px-4 py-6 bg-primary text-white">
              <div class="flex items-center justify-between">
                <h2 class="text-xl font-bold">主题设置</h2>
                <button @click="closeDrawer" class="p-2 rounded-full hover:bg-primary-dark">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                  </svg>
                </button>
              </div>
            </div>

            <!-- 抽屉内容 -->
            <div class="flex-1 overflow-y-auto p-6">
              <!-- 布局选择 -->
              <div class="mb-8">
                <h3 class="text-lg font-medium mb-4 dark:text-white">布局样式</h3>
                <div class="grid grid-cols-3 gap-4">
                  <button
                      v-for="layout in layouts"
                      :key="layout.value"
                      @click="switchLayout(layout.value)"
                      class="p-4 border rounded-lg transition-all"
                      :class="{
                      'border-primary shadow-md': currentLayout === layout.value,
                      'border-gray-200 hover:border-primary': currentLayout !== layout.value,
                      'dark:border-gray-600': currentLayout !== layout.value
                    }"
                  >
                    <div class="flex flex-col items-center">
                      <div class="w-full h-12 mb-2" :class="layout.previewClass"></div>
                      <span class="text-sm dark:text-white">{{ layout.label }}</span>
                    </div>
                  </button>
                </div>
              </div>

              <!-- 主题颜色选择 -->
              <div class="mb-8">
                <h3 class="text-lg font-medium mb-4 dark:text-white">主题颜色</h3>
                <ThemeColorPicker />
              </div>

              <!-- 暗黑模式切换 -->
              <div class="flex items-center justify-between">
                <span class="text-gray-700 dark:text-white">暗黑模式</span>
                <button
                    @click="toggleDarkMode"
                    class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors"
                    :class="darkMode ? 'bg-primary' : 'bg-gray-200'"
                >
                  <span
                      class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
                      :class="darkMode ? 'translate-x-6' : 'translate-x-1'"
                  ></span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped lang="scss">
/* 抽屉动画效果 */
.slide-fade-enter-active, .slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from, .slide-fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-to, .slide-fade-leave-from {
  opacity: 1;
}

/* 背景遮罩 */
.slide-fade-enter-active::before, .slide-fade-leave-active::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  transition: opacity 0.3s ease;
}

.slide-fade-enter-from::before, .slide-fade-leave-to::before {
  opacity: 0;
}

.slide-fade-enter-to::before, .slide-fade-leave-from::before {
  opacity: 1;
}
</style>