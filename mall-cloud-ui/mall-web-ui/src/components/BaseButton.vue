<script setup lang="ts">
import { computed, withDefaults } from 'vue'

// 定义组件接收的 props 类型
interface Props {
  /** 按钮类型，可选值为 'primary' | 'default' */
  type?: 'primary' | 'default'
  /** 按钮显示文本 */
  text?: string
  /** 是否禁用状态 */
  disabled?: boolean
  /** 是否加载状态 */
  loading?: boolean
  /** 自定义按钮颜色 */
  color?: string
  /** 自定义按钮悬停颜色 */
  hoverColor?: string
  /** 按钮尺寸 */
  size?: 'small' | 'medium' | 'large'
}

// 定义组件发出的事件类型
interface Emits {
  /** 点击事件 */
  (e: 'click', event: MouseEvent): void
}

// 使用 withDefaults 为 props 提供默认值
const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  text: '按钮',
  disabled: false,
  loading: false,
  color: '',
  hoverColor: '',
  size: 'medium'
})

// 定义 emit 函数
const emit = defineEmits<Emits>()

// 计算属性：根据 size 返回对应的类名
const sizeClass = computed(() => {
  return `custom-button--${props.size}`
})

// 点击事件处理函数
const handleClick = (event: MouseEvent) => {
  // 如果处于加载或禁用状态，阻止点击事件
  if (props.loading || props.disabled) {
    event.preventDefault()
    return
  }
  // 触发 click 事件
  emit('click', event)
}
</script>

<template>
  <!-- 组件根元素，添加自定义类名和样式 -->
  <div
      class="custom-button"
      :class="{
      'custom-button--primary': type === 'primary',
      'custom-button--disabled': disabled,
      'custom-button--loading': loading
    }"
      :style="{
      '--button-color': color,
      '--button-hover-color': hoverColor || color
    }"
      @click="handleClick"
  >
    <!-- 加载状态指示器 -->
    <span v-if="loading" class="custom-button__loading">
      <svg class="custom-button__loading-icon" viewBox="0 0 50 50">
        <circle class="custom-button__loading-circle" cx="25" cy="25" r="20" fill="none" />
      </svg>
    </span>

    <!-- 按钮内容插槽，允许自定义内容 -->
    <span class="custom-button__content">
      <slot>
        <!-- 默认插槽内容，如果用户没有提供内容则显示 -->
        {{ text }}
      </slot>
    </span>

    <!-- 右侧图标插槽 -->
    <span v-if="$slots.icon" class="custom-button__icon">
      <slot name="icon"></slot>
    </span>
  </div>
</template>

<style scoped lang="scss">
// 定义变量默认值
.custom-button {
  --button-color: #409eff;
  --button-hover-color: #66b1ff;
  --button-text-color: #fff;
  --button-disabled-color: #c0c4cc;

  // 基础样式
  position: relative;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  border: none;
  border-radius: 4px;
  user-select: none;
  transition: all 0.3s;
  padding: 10px 20px;
  font-size: 14px;
  color: var(--button-text-color);
  background-color: var(--button-color);

  // 不同尺寸样式
  &--small {
    padding: 8px 16px;
    font-size: 12px;
  }

  &--large {
    padding: 12px 24px;
    font-size: 16px;
  }

  // 主要按钮样式
  &--primary {
    background-color: var(--button-color);
    &:hover {
      background-color: var(--button-hover-color);
    }
  }

  // 默认按钮样式
  &:not(.custom-button--primary) {
    background-color: #fff;
    color: #606266;
    border: 1px solid #dcdfe6;
    &:hover {
      color: var(--button-color);
      border-color: #c6e2ff;
      background-color: #ecf5ff;
    }
  }

  // 禁用状态样式
  &--disabled {
    cursor: not-allowed;
    opacity: 0.6;
    &:hover {
      background-color: var(--button-color);
    }
  }

  // 加载状态样式
  &--loading {
    cursor: default;
    opacity: 0.8;
  }

  // 加载动画
  &__loading {
    display: inline-flex;
    margin-right: 8px;
    &-icon {
      width: 1em;
      height: 1em;
      animation: rotate 2s linear infinite;
    }
    &-circle {
      stroke: currentColor;
      stroke-width: 3;
      stroke-linecap: round;
      stroke-dasharray: 90, 150;
      stroke-dashoffset: 0;
      animation: dash 1.5s ease-in-out infinite;
    }
  }

  // 内容区域
  &__content {
    display: inline-flex;
    align-items: center;
  }

  // 图标区域
  &__icon {
    margin-left: 8px;
    display: inline-flex;
    align-items: center;
  }
}

// 旋转动画
@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

// 加载动画
@keyframes dash {
  0% {
    stroke-dasharray: 1, 200;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -40px;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -120px;
  }
}
</style>