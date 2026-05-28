<script lang="ts" setup>
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/modules/settings.store.ts'

const settingsStore = useSettingsStore()

const currentTheme = computed(() => settingsStore.themeColor)

const colors = [
  { value: 'blue', label: '蓝色', bgClass: 'bg-blue-500' },
  { value: 'green', label: '绿色', bgClass: 'bg-green-500' },
  { value: 'red', label: '红色', bgClass: 'bg-red-500' },
  { value: 'purple', label: '紫色', bgClass: 'bg-purple-500' },
  { value: 'orange', label: '橙色', bgClass: 'bg-orange-500' },
  { value: 'teal', label: '青色', bgClass: 'bg-teal-500' },
  { value: 'pink', label: '粉色', bgClass: 'bg-pink-500' }
]

const switchTheme = (color: string) => {
  settingsStore.switchTheme(color as any)
}
</script>

<template>
  <div class="flex flex-wrap gap-3">
    <button
        v-for="color in colors"
        :key="color.value"
        @click="switchTheme(color.value)"
        class="w-8 h-8 rounded-full flex items-center justify-center transition-transform hover:scale-110"
        :class="color.bgClass"
        :aria-label="color.label"
    >
      <svg
          v-if="color.value === currentTheme"
          class="w-4 h-4 text-white"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
      </svg>
    </button>
  </div>
</template>