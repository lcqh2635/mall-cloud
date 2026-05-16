<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch } from 'vue'

// 定义Props类型
interface Props {
  /** 控制模态框显示/隐藏（v-model） */
  modelValue: boolean
  /** 模态框标题 */
  title?: string
  /** 确认按钮文本 */
  confirmText?: string
  /** 取消按钮文本 */
  cancelText?: string
  /** 是否显示头部 */
  showHeader?: boolean
  /** 是否显示底部 */
  showFooter?: boolean
  /** 是否显示关闭按钮 */
  showClose?: boolean
  /** 点击遮罩层是否可以关闭 */
  maskClosable?: boolean
  /** 是否禁用确认按钮 */
  confirmDisabled?: boolean
  /** 模态框宽度（数字或字符串，如400或'50%'） */
  width?: number | string
  /** 模态框尺寸 */
  size?: 'small' | 'medium' | 'large'
  /** 是否居中显示 */
  center?: boolean
  /** 是否开启模糊效果 */
  blur?: boolean
  /** 是否锁定背景滚动 */
  lockScroll?: boolean
}

// 定义Emits类型
interface Emits {
  /** 更新modelValue事件 */
  (e: 'update:modelValue', value: boolean): void
  /** 确认事件 */
  (e: 'confirm'): void
  /** 取消事件 */
  (e: 'cancel'): void
  /** 关闭事件 */
  (e: 'close'): void
  /** 打开事件 */
  (e: 'open'): void
}

// 设置默认Props
const props = withDefaults(defineProps<Props>(), {
  title: '提示',
  confirmText: '确认',
  cancelText: '取消',
  showHeader: true,
  showFooter: true,
  showClose: true,
  maskClosable: true,
  confirmDisabled: false,
  width: 520,
  size: 'medium',
  center: false,
  blur: false,
  lockScroll: true
})

const emit = defineEmits<Emits>()

// 处理ESC键关闭
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.modelValue) {
    handleClose()
  }
}

// 锁定背景滚动
const lockBodyScroll = (shouldLock: boolean) => {
  if (props.lockScroll) {
    document.body.style.overflow = shouldLock ? 'hidden' : ''
  }
}

// 组件挂载时添加事件监听
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

// 组件卸载前移除事件监听
onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
  lockBodyScroll(false)
})

// 监听modelValue变化
watch(() => props.modelValue, (val) => {
  lockBodyScroll(val)
  if (val) {
    emit('open')
  }
})

// 处理遮罩层点击
const handleMaskClick = () => {
  if (props.maskClosable) {
    handleClose()
  }
}

// 处理关闭操作
const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}

// 处理确认操作
const handleConfirm = () => {
  emit('confirm')
  handleClose()
}

// 处理取消操作
const handleCancel = () => {
  emit('cancel')
  handleClose()
}
</script>

<template>
  <!-- 过渡动画包裹 -->
  <transition name="fade">
    <!-- 遮罩层 -->
    <div
        v-show="modelValue"
        class="modal-mask"
        :class="{'modal-mask--blur': blur}"
        @click.self="handleMaskClick"
    >
      <!-- 模态框容器 -->
      <div
          class="modal-container"
          :class="[`modal-container--${size}`, { 'modal-container--center': center }]"
          :style="{ width: typeof width === 'number' ? `${width}px` : width }"
      >
        <!-- 头部 -->
        <header v-if="showHeader" class="modal-header">
          <!-- 标题插槽 -->
          <slot name="title">
            <h3 class="modal-title">{{ title }}</h3>
          </slot>

          <!-- 关闭按钮 -->
          <button
              v-if="showClose"
              class="modal-close"
              @click="handleClose"
          >
            <svg viewBox="0 0 1024 1024" width="16" height="16">
              <path d="M563.8 512l262.5-312.9c4.4-5.2 0.7-13.1-6.1-13.1h-79.8c-4.7 0-9.2 2.1-12.3 5.7L511.6 449.8 295.1 191.7c-3-3.6-7.5-5.7-12.3-5.7H203c-6.8 0-10.5 7.9-6.1 13.1L459.4 512 196.9 824.9c-4.4 5.2-0.7 13.1 6.1 13.1h79.8c4.7 0 9.2-2.1 12.3-5.7l216.5-258.1 216.5 258.1c3 3.6 7.5 5.7 12.3 5.7h79.8c6.8 0 10.5-7.9 6.1-13.1L563.8 512z" />
            </svg>
          </button>
        </header>

        <!-- 内容区域 -->
        <div class="modal-body">
          <!-- 默认插槽 -->
          <slot></slot>
        </div>

        <!-- 底部 -->
        <footer v-if="showFooter" class="modal-footer">
          <!-- 底部插槽 -->
          <slot name="footer">
            <!-- 默认底部按钮 -->
            <button
                class="modal-button modal-button--cancel"
                @click="handleCancel"
            >
              {{ cancelText }}
            </button>
            <button
                class="modal-button modal-button--confirm"
                :disabled="confirmDisabled"
                @click="handleConfirm"
            >
              {{ confirmText }}
            </button>
          </slot>
        </footer>
      </div>
    </div>
  </transition>
</template>

<style scoped lang="scss">
// 定义变量
$modal-zindex: 1000;
$modal-mask-bg: rgba(0, 0, 0, 0.5);
$modal-bg: #fff;
$modal-border-radius: 4px;
$modal-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
$modal-header-padding: 16px 24px;
$modal-body-padding: 24px;
$modal-footer-padding: 12px 24px;
$modal-close-color: #999;
$modal-close-hover-color: #333;
$modal-button-padding: 8px 16px;

// 遮罩层样式
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: $modal-mask-bg;
  z-index: $modal-zindex;
  display: flex;
  transition: opacity 0.3s ease;

  // 模糊效果
  &--blur {
    backdrop-filter: blur(5px);
  }
}

// 模态框容器
.modal-container {
  background: $modal-bg;
  border-radius: $modal-border-radius;
  box-shadow: $modal-box-shadow;
  transition: all 0.3s ease;
  margin: auto;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 48px);
  max-width: calc(100vw - 48px);

  // 不同尺寸
  &--small {
    width: 400px;
  }

  &--medium {
    width: 520px;
  }

  &--large {
    width: 720px;
  }

  // 居中样式
  &--center {
    align-self: center;
  }
}

// 头部样式
.modal-header {
  padding: $modal-header-padding;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
}

// 标题样式
.modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}

// 关闭按钮样式
.modal-close {
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  color: $modal-close-color;
  transition: color 0.3s;

  &:hover {
    color: $modal-close-hover-color;
  }

  svg {
    display: block;
  }
}

// 内容区域样式
.modal-body {
  padding: $modal-body-padding;
  overflow-y: auto;
  flex: 1;
  color: rgba(0, 0, 0, 0.65);
}

// 底部样式
.modal-footer {
  padding: $modal-footer-padding;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}

// 按钮基础样式
.modal-button {
  padding: $modal-button-padding;
  border-radius: $modal-border-radius;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  border: 1px solid transparent;

  // 取消按钮样式
  &--cancel {
    background: #fff;
    border-color: #d9d9d9;
    color: rgba(0, 0, 0, 0.65);
    margin-right: 8px;

    &:hover {
      color: #409eff;
      border-color: #c6e2ff;
      background-color: #ecf5ff;
    }
  }

  // 确认按钮样式
  &--confirm {
    background: #1890ff;
    color: #fff;
    border-color: #1890ff;

    &:hover {
      background: #40a9ff;
      border-color: #40a9ff;
    }

    // 禁用状态
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

// 过渡动画
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
}
</style>