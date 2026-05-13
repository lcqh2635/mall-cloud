<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

// 定义组件props类型
interface Props {
  position?: 'left' | 'center' | 'right'
}

const props = withDefaults(defineProps<Props>(), {
  position: 'left'
})

// 菜单可见状态
const isMenuVisible = ref(false)
// 容器DOM引用
const containerRef = ref<HTMLElement | null>(null)

// 根据位置计算菜单类名
const menuClasses = computed(() => {
  return {
    'left-0 origin-top-left': props.position === 'left',
    'left-1/2 -translate-x-1/2 origin-top': props.position === 'center',
    'right-0 origin-top-right': props.position === 'right',
    'scale-100 opacity-100': isMenuVisible.value,
    'scale-0 opacity-0': !isMenuVisible.value
  }
})

// 切换菜单显示状态
const toggleMenu = () => {
  isMenuVisible.value = !isMenuVisible.value
}

// 关闭菜单
const closeMenu = () => {
  isMenuVisible.value = false
}

// 点击外部关闭菜单的处理函数
const handleClickOutside = (event: MouseEvent) => {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    closeMenu()
  }
}

// 组件挂载时添加全局点击事件监听
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载前移除事件监听
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <!-- 头像下拉菜单容器 -->
  <div
      class="relative"
      :class="{
      'ml-5': position === 'left',
      'mr-5': position === 'right'
    }"
      ref="containerRef"
  >
    <!-- 头像按钮 -->
    <div
        class="w-10 h-10 rounded-full bg-blue-500 shadow-md overflow-hidden cursor-pointer hover:scale-105 transition-transform"
        @click.stop="toggleMenu"
    >
      <img
          src="https://randomuser.me/api/portraits/men/32.jpg"
          alt="用户头像"
          class="w-full h-full object-cover"
      >
    </div>

    <!-- 下拉菜单 -->
    <ul
        class="absolute top-12 w-48 bg-white/95 backdrop-blur-md rounded-xl shadow-xl overflow-hidden transition-all duration-300 ease-out"
        :class="menuClasses"
        v-show="isMenuVisible"
    >
      <slot>
        <!-- 默认菜单项 -->
        <li class="px-4 py-3 text-sm text-gray-700 hover:bg-gray-100 flex items-center gap-2">
          <span>👤</span>
          <span>个人中心</span>
        </li>
        <li class="px-4 py-3 text-sm text-gray-700 hover:bg-gray-100">设置</li>
        <li class="px-4 py-3 text-sm text-gray-700 hover:bg-gray-100">退出登录</li>
      </slot>
    </ul>
  </div>
</template>