<template>
  <div class="el-dropdown" ref="dropdownRef">
    <!-- 默认插槽 - 触发元素 -->
    <slot></slot>

    <!-- 下拉菜单插槽 -->
    <transition
        name="el-dropdown"
        @before-enter="beforeEnter"
        @enter="enter"
        @after-enter="afterEnter"
        @before-leave="beforeLeave"
        @leave="leave"
    >
      <div
          v-show="visible"
          class="el-dropdown__popper"
          :class="[placementClass, popperClass]"
      >
        <slot name="dropdown"></slot>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'

interface Props {
  placement?: 'top' | 'top-start' | 'top-end' | 'bottom' | 'bottom-start' | 'bottom-end'
  trigger?: 'hover' | 'click' | 'contextmenu'
  disabled?: boolean
  popperClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  placement: 'bottom',
  trigger: 'hover',
  disabled: false,
  popperClass: ''
})

const visible = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

// 计算菜单位置类
const placementClass = computed(() => {
  return `is-${props.placement}`
})

// 切换菜单显示状态
const toggleMenu = () => {
  if (props.disabled) return
  if (props.trigger === 'click' || props.trigger === 'contextmenu') {
    visible.value = !visible.value
  }
}

// 显示菜单
const showMenu = () => {
  if (props.disabled) return
  visible.value = true
}

// 隐藏菜单
const hideMenu = () => {
  visible.value = false
}

// 点击外部关闭菜单
const handleClickOutside = (event: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    hideMenu()
  }
}

// 右键菜单处理
const handleContextMenu = (event: MouseEvent) => {
  if (props.trigger === 'contextmenu') {
    event.preventDefault()
    showMenu()
  }
}

// 动画钩子
const beforeEnter = (el: HTMLElement) => {
  el.style.opacity = '0'
  el.style.transform = 'scale(0.95) translateY(-10px)'
}

const enter = (el: HTMLElement, done: () => void) => {
  el.animate([
    { opacity: 0, transform: 'scale(0.95) translateY(-10px)' },
    { opacity: 1, transform: 'scale(1) translateY(0)' }
  ], {
    duration: 150,
    easing: 'cubic-bezier(0.4, 0, 0.2, 1)'
  }).onfinish = done
}

const afterEnter = (el: HTMLElement) => {
  el.style.opacity = ''
  el.style.transform = ''
}

const beforeLeave = (el: HTMLElement) => {
  el.style.opacity = '1'
  el.style.transform = 'scale(1) translateY(0)'
}

const leave = (el: HTMLElement, done: () => void) => {
  el.animate([
    { opacity: 1, transform: 'scale(1) translateY(0)' },
    { opacity: 0, transform: 'scale(0.95) translateY(-10px)' }
  ], {
    duration: 150,
    easing: 'cubic-bezier(0.4, 0, 0.2, 1)'
  }).onfinish = done
}

// 设置事件监听
const setupEventListeners = () => {
  if (!dropdownRef.value) return

  if (props.trigger === 'hover') {
    dropdownRef.value.addEventListener('mouseenter', showMenu)
    dropdownRef.value.addEventListener('mouseleave', hideMenu)
  } else if (props.trigger === 'click') {
    dropdownRef.value.addEventListener('click', toggleMenu)
  } else if (props.trigger === 'contextmenu') {
    dropdownRef.value.addEventListener('contextmenu', handleContextMenu)
  }

  document.addEventListener('click', handleClickOutside)
}

const removeEventListeners = () => {
  if (!dropdownRef.value) return

  dropdownRef.value.removeEventListener('mouseenter', showMenu)
  dropdownRef.value.removeEventListener('mouseleave', hideMenu)
  dropdownRef.value.removeEventListener('click', toggleMenu)
  dropdownRef.value.removeEventListener('contextmenu', handleContextMenu)

  document.removeEventListener('click', handleClickOutside)
}

// 监听 trigger 变化
watch(() => props.trigger, (newVal, oldVal) => {
  removeEventListeners()
  setupEventListeners()
})

onMounted(() => {
  setupEventListeners()
})

onBeforeUnmount(() => {
  removeEventListeners()
})
</script>

<style scoped>
.el-dropdown {
  display: inline-block;
  position: relative;
}

.el-dropdown__popper {
  position: absolute;
  z-index: 2000;
  background: var(--el-bg-color-overlay);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  min-width: 120px;
}

.el-dropdown__popper.is-top {
  bottom: 100%;
  margin-bottom: 8px;
}

.el-dropdown__popper.is-top-start {
  bottom: 100%;
  left: 0;
  margin-bottom: 8px;
}

.el-dropdown__popper.is-top-end {
  bottom: 100%;
  right: 0;
  margin-bottom: 8px;
}

.el-dropdown__popper.is-bottom {
  top: 100%;
  margin-top: 8px;
}

.el-dropdown__popper.is-bottom-start {
  top: 100%;
  left: 0;
  margin-top: 8px;
}

.el-dropdown__popper.is-bottom-end {
  top: 100%;
  right: 0;
  margin-top: 8px;
}

.el-dropdown-enter-active,
.el-dropdown-leave-active {
  transition: all 0.15s ease-out;
}
</style>