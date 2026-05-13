# 使用模态框组件的完整指南

下面我将详细介绍如何在 Vue 3 + TypeScript 项目中使用上面提供的模态框组件，包含多种使用场景和代码示例。

## 基础使用

### 1. 注册组件

首先，将组件导入并注册（如果是全局注册）：

```typescript
// main.ts 或全局组件注册文件
import Modal from '@/components/Modal.vue'

const app = createApp(App)
app.component('Modal', Modal) // 全局注册
```

或者在使用页面局部注册：

```vue
<script setup lang="ts">
import Modal from '@/components/Modal.vue'
</script>
```

### 2. 基本用法

```vue
<template>
  <div>
    <button @click="showModal = true">打开模态框</button>
    
    <Modal v-model="showModal" title="基本示例">
      <p>这是模态框的内容</p>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showModal = ref(false)
</script>
```

## 进阶使用示例

### 1. 使用所有功能

```vue
<template>
  <div>
    <button @click="openModal">打开完整功能模态框</button>
    
    <Modal
      v-model="isVisible"
      title="完整功能示例"
      :width="600"
      size="large"
      :blur="true"
      :lock-scroll="true"
      @confirm="handleConfirm"
      @cancel="handleCancel"
      @close="handleClose"
    >
      <template #title>
        <h2 style="color: #f56c6c">自定义标题</h2>
      </template>
      
      <div>
        <p>这里是模态框的主要内容区域</p>
        <p>可以放置任何内容，如表单、图片等</p>
      </div>
      
      <template #footer>
        <button class="custom-button" @click="handleCustomAction">自定义操作</button>
        <button class="custom-confirm" @click="handleConfirm">确定</button>
      </template>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const isVisible = ref(false)

const openModal = () => {
  isVisible.value = true
}

const handleConfirm = () => {
  console.log('确认操作')
  // 可以在这里添加业务逻辑
}

const handleCancel = () => {
  console.log('取消操作')
}

const handleClose = () => {
  console.log('模态框已关闭')
}

const handleCustomAction = () => {
  console.log('自定义按钮点击')
}
</script>

<style scoped>
.custom-button {
  padding: 8px 16px;
  background: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}

.custom-confirm {
  padding: 8px 16px;
  background: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
```

### 2. 与表单结合使用

```vue
<template>
  <div>
    <button @click="showFormModal = true">打开表单模态框</button>
    
    <Modal
      v-model="showFormModal"
      title="用户注册"
      :show-close="false"
      :mask-closable="false"
      @confirm="submitForm"
      :confirm-disabled="!formValid"
    >
      <form @submit.prevent="submitForm">
        <div class="form-group">
          <label>用户名:</label>
          <input v-model="form.username" @input="validateForm" />
        </div>
        
        <div class="form-group">
          <label>密码:</label>
          <input v-model="form.password" type="password" @input="validateForm" />
        </div>
        
        <div class="form-group">
          <label>邮箱:</label>
          <input v-model="form.email" type="email" @input="validateForm" />
        </div>
      </form>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

const showFormModal = ref(false)
const formValid = ref(false)

const form = reactive({
  username: '',
  password: '',
  email: ''
})

const validateForm = () => {
  formValid.value = form.username.length > 0 && 
                   form.password.length >= 6 &&
                   /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)
}

const submitForm = () => {
  if (formValid.value) {
    console.log('提交表单:', form)
    // 这里可以添加API调用
    showFormModal.value = false
  }
}
</script>

<style scoped>
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style>
```

### 3. 异步操作示例

```vue
<template>
  <div>
    <button @click="showAsyncModal = true">打开异步操作模态框</button>
    
    <Modal
      v-model="showAsyncModal"
      title="删除确认"
      :confirm-disabled="isDeleting"
      :show-close="!isDeleting"
      :mask-closable="!isDeleting"
      @confirm="handleDelete"
    >
      <p>确定要删除这项内容吗？此操作不可撤销。</p>
      
      <div v-if="isDeleting" class="loading-message">
        <span class="loading-spinner"></span>
        正在删除...
      </div>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showAsyncModal = ref(false)
const isDeleting = ref(false)

const handleDelete = async () => {
  isDeleting.value = true
  
  try {
    // 模拟异步操作
    await new Promise(resolve => setTimeout(resolve, 1500))
    console.log('删除成功')
    showAsyncModal.value = false
  } catch (error) {
    console.error('删除失败:', error)
  } finally {
    isDeleting.value = false
  }
}
</script>

<style scoped>
.loading-message {
  display: flex;
  align-items: center;
  margin-top: 16px;
  color: #666;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
```

## 组件API参考

### Props

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|------|
| v-model | 控制模态框显示/隐藏 | boolean | false |
| title | 模态框标题 | string | '提示' |
| confirmText | 确认按钮文本 | string | '确认' |
| cancelText | 取消按钮文本 | string | '取消' |
| showHeader | 是否显示头部 | boolean | true |
| showFooter | 是否显示底部 | boolean | true |
| showClose | 是否显示关闭按钮 | boolean | true |
| maskClosable | 点击遮罩层是否可以关闭 | boolean | true |
| confirmDisabled | 是否禁用确认按钮 | boolean | false |
| width | 模态框宽度 | number \| string | 520 |
| size | 模态框尺寸 | 'small' \| 'medium' \| 'large' | 'medium' |
| center | 是否居中显示 | boolean | false |
| blur | 是否开启模糊效果 | boolean | false |
| lockScroll | 是否锁定背景滚动 | boolean | true |

### 事件

| 事件名 | 说明 | 回调参数 |
|------|------|------|
| confirm | 点击确认按钮时触发 | - |
| cancel | 点击取消按钮时触发 | - |
| close | 模态框关闭时触发 | - |
| open | 模态框打开时触发 | - |

### 插槽

| 插槽名 | 说明 |
|------|------|
| default | 模态框主要内容 |
| title | 自定义标题内容 |
| footer | 自定义底部内容 |

## 最佳实践建议

1. **大型项目**中建议将组件放在统一的`components/ui`目录下
2. 通过**provide/inject**可以创建一个模态框管理服务，集中管理多个模态框状态
3. 对于需要频繁使用的模态框，可以基于该组件**二次封装**成业务组件
4. 考虑添加**Teleport**功能，将模态框渲染到body下，避免父组件样式影响
5. 可以扩展**动画效果**，提供多种出现/消失动画选项
6. 在需要**国际化**的项目中，将按钮文本等可配置项通过i18n管理

这个模态框组件设计灵活，可以满足大多数业务场景需求，同时保持了良好的可维护性和可扩展性。