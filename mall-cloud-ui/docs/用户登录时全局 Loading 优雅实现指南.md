在基于 **Vite + Vue 3 + TypeScript** 的现代前端项目中，实现一个带有平滑过渡动画的全局加载效果，最优雅、最符合现代工程化标准的做法是：**使用 Vue 3 原生的 `<Teleport>` + `<Transition>` 结合 Pinia 状态管理**。

下面我将为你详细梳理可选方案、强烈推荐的最佳实践，并提供包含“高级感”视觉设计的完整代码实现。

---

### 一、 可选方案对比

| 方案 | 实现方式 | 优点 | 缺点 | 适用场景 |
| :--- | :--- | :--- | :--- | :--- |
| **方案 A：原生 Teleport + Pinia (🏆 推荐)** | 自定义组件，通过 `<Teleport to="body">` 挂载，`<Transition>` 控制动画，Pinia 管理状态。 | **零外部依赖**，完全可控，TS 类型完美，可轻松定制高级视觉效果（如毛玻璃）。 | 需要手写约 50 行代码（一劳永逸）。 | **绝大多数现代 Vue 3 项目**，尤其是追求轻量化和高度定制化的项目。 |
| **方案 B：传统 UI 库的 Service** | 使用 Element Plus / Naive UI 的 `Loading.service({ fullscreen: true })`。 | 一行代码，开箱即用。 | 为了一个 Loading 引入庞大 UI 库不划算；样式定制受限；与 Tailwind CSS 生态融合度差。 | 已经重度依赖该 UI 库的传统后台管理系统。 |
| **方案 C：第三方 Loading 插件** | 安装如 `vue-loading-overlay` 等 npm 包。 | 无需自己写组件。 | 增加项目依赖体积；API 可能过时；定制动画效果困难。 | 快速原型开发，不关心包体积的项目。 |
| **方案 D：顶部进度条 (NProgress)** | 拦截路由或 Axios，在页面顶部显示细长进度条。 | **用户体验最佳**，非侵入式，不阻断用户视觉。 | 严格来说不是“全局遮罩”，无法阻止用户疯狂点击按钮。 | 页面跳转、大型数据拉取时的**最佳辅助方案**。 |

---

### 二、 🏆 强烈推荐方案：Teleport + Transition + Pinia

#### 推荐理由：
1. **符合 Vue 3 最佳实践**：`<Teleport>` 完美解决了全局组件层级（z-index）被父组件 `overflow: hidden` 或 `transform` 截断的痛点，直接挂载到 `<body>`。
2. **极致的性能与轻量**：无需任何第三方库，Vite 编译后体积几乎为 0。
3. **高级感视觉定制**：结合 Tailwind CSS，可以轻松实现**背景模糊 (backdrop-blur)**、**平滑淡入淡出**，告别生硬的闪现。
4. **并发安全**：通过 Pinia 的计数器机制，可以完美处理多个接口同时请求时，Loading 不会提前消失的问题。

---

### 三、 详细实现步骤 (附完整代码)

#### 第 1 步：创建 Pinia 状态管理 (支持并发计数)
创建一个 `src/store/loading.ts`。使用计数器 `count` 而不是简单的布尔值，可以防止多个异步请求并发时，第一个请求结束就错误地关闭了全局 Loading。

```typescript
// src/store/loading.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useLoadingStore = defineStore('loading', () => {
  const count = ref(0);
  const isLoading = ref(false);

  const show = () => {
    count.value++;
    isLoading.value = true;
  };

  const hide = () => {
    count.value--;
    if (count.value <= 0) {
      count.value = 0;
      isLoading.value = false;
    }
  };

  // 强制重置（用于异常情况兜底）
  const reset = () => {
    count.value = 0;
    isLoading.value = false;
  };

  return { isLoading, show, hide, reset };
});
```

#### 第 2 步：创建全局 Loading 组件 (带高级感动画)
创建 `src/components/GlobalLoading.vue`。这里使用 Tailwind CSS 实现**毛玻璃遮罩**和**平滑过渡**。

```vue
<!-- src/components/GlobalLoading.vue -->
<script setup lang="ts">
import { useLoadingStore } from '@/store/loading';

const loadingStore = useLoadingStore();
</script>

<template>
  <!-- Teleport 确保组件挂载到 body，不受父级 z-index 或 overflow 影响 -->
  <Teleport to="body">
    <!-- Transition 提供平滑的进入/离开动画 -->
    <Transition name="fade-loading">
      <div v-if="loadingStore.isLoading" class="global-loading-overlay">
        <!-- 毛玻璃背景遮罩 -->
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" />
        
        <!-- 居中加载动画容器 -->
        <div class="relative z-10 flex flex-col items-center justify-center gap-4">
          <!-- 自定义高级感 Spinner (使用 Tailwind animate-spin) -->
          <div class="relative h-12 w-12">
            <div class="absolute inset-0 rounded-full border-4 border-white/20"></div>
            <div class="absolute inset-0 rounded-full border-4 border-t-blue-500 border-r-transparent border-b-transparent border-l-transparent animate-spin"></div>
          </div>
          <span class="text-sm font-medium text-white/90 tracking-wide">处理中...</span>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 定义过渡动画类 */
.fade-loading-enter-active,
.fade-loading-leave-active {
  transition: opacity 0.3s ease, backdrop-filter 0.3s ease;
}

.fade-loading-enter-from,
.fade-loading-leave-to {
  opacity: 0;
  backdrop-filter: blur(0px); /* 离开时模糊效果也平滑消失 */
}

/* 确保遮罩层拥有极高的 z-index */
.global-loading-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
```

#### 第 3 步：在 `App.vue` 中全局注册
只需引入一次，它就会监听 Pinia 的状态并自动工作。

```vue
<!-- src/App.vue -->
<script setup lang="ts">
import GlobalLoading from '@/components/GlobalLoading.vue';
// 其他路由视图...
</script>

<template>
  <RouterView />
  <!-- 全局 Loading 组件 -->
  <GlobalLoading />
</template>
```

#### 第 4 步：在登录逻辑中优雅地调用
在登录页面，使用 `try...finally` 确保无论请求成功还是失败，Loading 都会被关闭。

```vue
<!-- src/views/Login.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import { useLoadingStore } from '@/store/loading';
import { loginApi } from '@/api/auth'; // 假设的 API

const loadingStore = useLoadingStore();
const username = ref('');
const password = ref('');

const handleLogin = async () => {
  try {
    loadingStore.show(); // 开启全局 Loading
    
    // 模拟网络请求
    await loginApi({ username: username.value, password: password.value });
    
    // 登录成功逻辑，如跳转路由
    // router.push('/dashboard');
    
  } catch (error) {
    console.error('登录失败', error);
    // 这里可以触发一个全局的 Toast 错误提示
  } finally {
    loadingStore.hide(); // 无论成功失败，必须关闭 Loading
  }
};
</script>

<template>
  <form @submit.prevent="handleLogin" class="space-y-4">
    <input v-model="username" placeholder="用户名" class="border p-2 rounded" />
    <input v-model="password" type="password" placeholder="密码" class="border p-2 rounded" />
    
    <!-- 按钮也可以加上局部 loading 状态，双重保险 -->
    <button 
      type="submit" 
      :disabled="loadingStore.isLoading"
      class="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
    >
      {{ loadingStore.isLoading ? '登录中...' : '登 录' }}
    </button>
  </form>
</template>
```

---

### 四、 💡 进阶工程化封装 (可选但强烈推荐)

为了避免在每个 API 调用处都手动写 `loadingStore.show()` 和 `hide()`，你可以封装一个 Composable（组合式函数），让代码更干净：

```typescript
// src/composables/useGlobalLoading.ts
import { useLoadingStore } from '@/store/loading';

export function useGlobalLoading() {
  const loadingStore = useLoadingStore();

  // 包装一个异步函数，自动处理 loading 的开关
  const withLoading = async <T>(fn: () => Promise<T>): Promise<T> => {
    try {
      loadingStore.show();
      return await fn();
    } finally {
      loadingStore.hide();
    }
  };

  return { withLoading };
}
```

**在登录组件中使用封装后的方法：**
```typescript
const handleLogin = async () => {
  await withLoading(async () => {
    await loginApi({ username: username.value, password: password.value });
    router.push('/dashboard');
  });
};
```
这样，你的业务代码中再也看不到 `show/hide` 的样板代码，极其优雅。

---

### 五、 现代 UX 设计的额外建议

虽然“全局遮罩 Loading”很常见，但在现代高级 Web 应用中，**全屏阻断式 Loading 正在被逐渐弱化**，因为它会打断用户的视觉流。

对于登录场景，更推荐的 **“组合拳” UX 方案**是：
1. **放弃全屏遮罩**，改为在**登录按钮内部**显示一个小型的旋转 Spinner，并将按钮设为 `disabled` 状态（防止重复提交）。
2. 配合 **NProgress** (`nprogress` npm 包)，在请求开始时在页面顶部显示一条极细的进度条，请求结束时平滑消失。
3. 只有在**极其耗时**的操作（如上传大文件、导出大型报表）时，才启用上述的**全局毛玻璃 Loading 遮罩**。

如果你希望登录体验达到极致，建议将上述的 `GlobalLoading` 组件保留用于全局重型操作，而在登录按钮上单独做局部 Loading 状态，这样既保证了系统的健壮性，又提供了最细腻的用户体验。