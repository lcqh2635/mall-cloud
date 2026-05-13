<template>
  <!-- 抽屉遮罩层 -->
  <transition name="fade">
    <div
        v-if="modelValue"
        class="fixed inset-0 bg-black/30 dark:bg-black/50 backdrop-blur-xs z-40"
        @click="closeDrawer"
    />
  </transition>

  <!-- 抽屉主体 -->
  <aside
      class="fixed top-0 left-0 h-full z-50 transform transition-transform duration-300 ease-in-out"
      :class="{
      '-translate-x-full': !modelValue,
      'translate-x-0': modelValue,
      'w-64': size === 'md',
      'w-80': size === 'lg',
      'w-96': size === 'xl'
    }"
  >
    <!-- 玻璃效果容器 -->
    <div
        class="absolute inset-0 backdrop-filter backdrop-blur-lg bg-white/80 dark:bg-gray-800/80 border-r border-white/30 dark:border-gray-700/30"
    />

    <!-- 抽屉内容 -->
    <div class="relative h-full flex flex-col">
      <!-- 抽屉头部 -->
      <header class="p-4 border-b border-white/30 dark:border-gray-700/30 flex justify-between items-center">
        <h2 class="text-xl font-bold text-gray-800 dark:text-gray-100">
          <slot name="header">{{ title }}</slot>
        </h2>
        <button
            @click="closeDrawer"
            class="text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition-colors"
            aria-label="关闭抽屉"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </header>

      <!-- 抽屉主体内容 -->
      <main class="flex-1 overflow-y-auto p-4">
        <slot></slot>
      </main>

      <!-- 抽屉底部 -->
      <footer v-if="$slots.footer" class="p-4 border-t border-white/30 dark:border-gray-700/30">
        <slot name="footer"></slot>
      </footer>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { watch } from 'vue'

// 定义组件Props类型
interface Props {
  modelValue: boolean // 控制抽屉显示/隐藏
  title?: string // 抽屉标题
  size?: 'md' | 'lg' | 'xl' // 抽屉尺寸
  closeOnClickOutside?: boolean // 点击外部是否关闭
  closeOnEsc?: boolean // 按ESC键是否关闭
}

// 定义组件Emits类型
interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
  (e: 'open'): void
}

// 定义props默认值
const props = withDefaults(defineProps<Props>(), {
  title: '导航菜单',
  size: 'md',
  closeOnClickOutside: true,
  closeOnEsc: true
})

const emit = defineEmits<Emits>()

// 关闭抽屉方法
const closeDrawer = () => {
  emit('update:modelValue', false)
  emit('close')
}

// 打开抽屉方法
const openDrawer = () => {
  emit('update:modelValue', true)
  emit('open')
}

// 监听键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.modelValue && props.closeOnEsc) {
    closeDrawer()
  }
}

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    document.addEventListener('keydown', handleKeydown)
    document.body.style.overflow = 'hidden' // 防止背景滚动
  } else {
    document.removeEventListener('keydown', handleKeydown)
    document.body.style.overflow = ''
  }
})

// 组件卸载时清理事件监听器
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})

// 暴露方法给父组件
defineExpose({
  openDrawer,
  closeDrawer
})
</script>

<style scoped lang="scss">
// 遮罩层淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 优化滚动条样式
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>