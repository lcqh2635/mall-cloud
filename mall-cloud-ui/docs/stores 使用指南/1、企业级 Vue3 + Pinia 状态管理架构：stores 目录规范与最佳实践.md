以下为您精心设计的 `src/stores` 目录组织方案。在 Vue3 生态中，**Pinia** 已完全取代 Vuex 成为官方推荐的状态管理库。本方案基于 Pinia 的 **Setup Store (组合式 API)** 风格编写，这是目前最符合 Vue3 心智模型且类型推导最友好的最佳实践。

---

### 🏆 推荐标题
**《企业级 Vue3 + Pinia 状态管理架构：`src/stores` 目录规范与最佳实践》**

---

### 一、 推荐目录结构

```text
src/stores/
├── index.ts                # 统一导出入口 (方便外部集中引用，非强制但推荐)
└── modules/                # 按业务领域划分的 Store 模块 (高内聚低耦合)
    ├── app.ts              # 应用级全局状态 (主题、侧边栏折叠、设备类型等)
    ├── user.ts             # 用户核心状态 (Token、用户信息、角色、登录/登出逻辑)
    └── permission.ts       # 权限状态 (动态路由表、菜单树、按钮权限标识 - 视项目复杂度可选)
```

> 💡 **核心差异提示**：与 Vuex 不同，Pinia **不需要**在 `modules/index.ts` 中进行全局的自动注册。Pinia 的 Store 是**按需实例化**的，直接在组件中 `import` 即可，这天然支持 Tree-shaking，且避免了复杂的循环依赖问题。

---

### 二、 核心文件完整代码（含详细中文注释）

#### 1. `src/stores/modules/user.ts` (用户状态管理 - 核心)
这是项目中最关键的 Store，负责管理身份认证和用户信息。

```typescript
/**
 * 用户状态管理 Store
 * 职责：管理 Token、用户基本信息、角色权限，以及登录/登出等核心业务逻辑
 */
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { login as loginApi, getUserInfo as getUserInfoApi, logout as logoutApi } from '@/api/modules/user';
import type { LoginForm, UserVO } from '@/api/types/user';
import { ResultCode } from '@/api/enums/result-code';
import { useLocalStorage } from '@vueuse/core'; // 推荐使用 @vueuse/core 处理持久化

export const useUserStore = defineStore('user', () => {
  // ================= 1. State (状态) =================
  // 推荐使用 @vueuse/core 的 useLocalStorage 实现 Token 持久化，比手动写 localStorage 更优雅且响应式
  const token = useLocalStorage<string>('app_token', '');
  
  // 用户详细信息 (非持久化，每次刷新需重新获取)
  const userInfo = ref<UserVO | null>(null);
  
  // 用户角色列表
  const roles = ref<string[]>([]);

  // ================= 2. Getters (计算属性) =================
  // 使用 computed 替代 Vuex 的 getters，天然支持 TS 类型推导
  const isLoggedIn = computed(() => !!token.value);
  const username = computed(() => userInfo.value?.username ?? '未知用户');
  const avatar = computed(() => userInfo.value?.avatar ?? '');

  // ================= 3. Actions (业务逻辑) =================
  
  /**
   * 用户登录
   * @param loginForm 登录表单数据
   */
  const login = async (loginForm: LoginForm) => {
    try {
      // 调用 API，由于我们在 request.ts 中做了响应解包，这里直接拿到 data
      const res = await loginApi(loginForm);
      
      // 保存 Token (useLocalStorage 会自动同步到 localStorage 并触发响应式更新)
      token.value = res.token;
      
      // 登录成功后，立即获取用户信息
      await fetchUserInfo();
      
      return true;
    } catch (error) {
      console.error('登录失败:', error);
      return false;
    }
  };

  /**
   * 获取当前用户信息与权限
   */
  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfoApi();
      userInfo.value = res.user;
      roles.value = res.roles || [];
    } catch (error) {
      // 获取用户信息失败，通常意味着 Token 已失效，应触发登出
      await logout();
      throw error;
    }
  };

  /**
   * 用户登出
   */
  const logout = async () => {
    try {
      // 通知后端清除 Token (可选，视后端安全规范而定)
      await logoutApi();
    } finally {
      // 无论后端接口成功与否，前端必须清除本地状态
      resetState();
    }
  };

  /**
   * 重置用户状态 (用于登出或 Token 过期时)
   */
  const resetState = () => {
    token.value = '';
    userInfo.value = null;
    roles.value = [];
    // 可选：清除其他相关的持久化存储
    // localStorage.removeItem('app_theme');
  };

  // ================= 4. 导出 =================
  // 将需要暴露给组件使用的 state, getters, actions 统一返回
  return {
    // State
    token,
    userInfo,
    roles,
    // Getters
    isLoggedIn,
    username,
    avatar,
    // Actions
    login,
    fetchUserInfo,
    logout,
    resetState,
  };
});
```

#### 2. `src/stores/modules/app.ts` (应用级状态管理)
管理与具体业务逻辑无关的全局 UI 状态。

```typescript
/**
 * 应用全局状态管理 Store
 * 职责：管理侧边栏、导航栏、主题模式、设备类型等全局 UI 状态
 */
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useDark, useToggle } from '@vueuse/core';

export const useAppStore = defineStore('app', () => {
  // ================= 1. State =================
  // 侧边栏是否折叠
  const sidebarCollapsed = ref(false);
  // 设备类型 (移动端/桌面端)
  const device = ref<'mobile' | 'desktop'>('desktop');

  // ================= 2. Getters =================
  // 使用 @vueuse/core 的 useDark 自动处理暗黑模式，并持久化到 localStorage
  const isDark = useDark({
    selector: 'html',
    attribute: 'class',
    valueDark: 'dark',
    valueLight: 'light',
  });
  // 切换暗黑模式的函数
  const toggleDark = useToggle(isDark);

  const isMobile = computed(() => device.value === 'mobile');

  // ================= 3. Actions =================
  /**
   * 切换侧边栏折叠状态
   */
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value;
  };

  /**
   * 设置设备类型
   */
  const setDevice = (val: 'mobile' | 'desktop') => {
    device.value = val;
    // 移动端自动折叠侧边栏
    if (val === 'mobile') {
      sidebarCollapsed.value = true;
    }
  };

  return {
    sidebarCollapsed,
    device,
    isDark,
    isMobile,
    toggleSidebar,
    toggleDark,
    setDevice,
  };
});
```

#### 3. `src/stores/index.ts` (统一导出入口)
虽然 Pinia 支持直接从 `modules` 导入，但提供一个统一的入口文件可以让代码更整洁，也方便未来做全局的 Store 拦截或插件注入。

```typescript
/**
 * Stores 统一导出入口
 * 作用：集中管理所有 Store 的导出，避免组件中出现过长的相对路径或混乱的模块导入
 */
export { useUserStore } from './modules/user';
export { useAppStore } from './modules/app';
// export { usePermissionStore } from './modules/permission';

/**
 * 辅助函数：一键重置所有 Store 状态
 * 适用场景：用户登出时，除了重置 user store，可能还需要重置 app store 的某些状态
 */
import { useUserStore } from './modules/user';
import { useAppStore } from './modules/app';

export function resetAllStores() {
  const userStore = useUserStore();
  const appStore = useAppStore();
  
  userStore.resetState();
  // 根据需要重置其他 store，例如：
  // appStore.sidebarCollapsed = false; 
}
```

---

### 三、 为什么这么组织？（设计依据与优势）

| 设计决策 | 解决的问题 | 反面教材 (Vuex 时代或不良实践) |
| :--- | :--- | :--- |
| **全面采用 Setup Store (`() => {}`)** | 与 Vue3 `<script setup>` 心智模型完全一致；可直接在 Store 内部使用 `ref`, `computed`, 甚至其他 `composables` (如 `useLocalStorage`)。 | 使用 Options API (`state`, `getters`, `actions` 对象)，无法直接使用 Composition API，代码割裂感强。 |
| **按业务模块拆分 (`modules/`)** | 高内聚低耦合。`user.ts` 只关心用户，`app.ts` 只关心 UI。多人协作时 Git 冲突概率极低。 | 将所有状态塞进一个巨大的 `store.ts` 或 `index.ts`，单文件超 1000 行，难以维护。 |
| **摒弃全局 `modules/index.ts` 自动注册** | Pinia 是按需实例化的。不需要的 Store 不会被初始化，天然支持 Tree-shaking，减小首屏包体积。 | 像 Vuex 一样写脚本遍历 `modules` 目录并 `registerModule`，增加初始化开销和循环依赖风险。 |
| **State 使用 `ref` 而非 `reactive`** | `ref` 在解构时不会丢失响应式（配合 Pinia 的自动解包特性），且与组件内的变量定义方式保持绝对一致。 | 使用 `reactive` 定义整个 state 对象，解构时需要繁琐的 `storeToRefs`，增加认知负担。 |
| **引入 `@vueuse/core` 处理持久化** | 一行代码 `useLocalStorage` 搞定状态持久化与响应式同步，无需手写 `localStorage.getItem` 和 `watch`。 | 在 Action 中手动调用 `localStorage.setItem`，容易遗漏更新，导致状态与存储不一致。 |

---

### 四、 进阶最佳实践建议

1. **避免在 Store 中直接操作 DOM**：Store 应该保持纯粹的数据和逻辑层。如果需要操作 DOM（如滚动、聚焦），请将其封装为 `composables`，然后在组件或 Store 的 Action 中调用该 composable。
2. **TypeScript 类型推导是 Pinia 的最大优势**：在上述代码中，当你在组件中调用 `userStore.userInfo?.username` 时，IDE 会提供完美的智能提示。请确保你的 API 返回类型 (`UserVO`) 定义准确，Store 的类型推导将自动继承这些准确性。
3. **Store 之间的依赖**：如果一个 Store 需要使用另一个 Store 的数据，**直接在 Action 内部调用**即可，无需在顶层导入。这可以避免循环依赖：
   ```typescript
   // 在 permission.ts 中
   export const usePermissionStore = defineStore('permission', () => {
     const routes = ref([]);
     
     const generateRoutes = async () => {
       // ✅ 正确做法：在 Action 内部按需引入并使用，避免顶层循环依赖
       const userStore = useUserStore(); 
       const roles = userStore.roles;
       // ... 根据 roles 生成路由
     };
     
     return { routes, generateRoutes };
   });
   ```
4. **持久化插件替代方案**：如果不使用 `@vueuse/core`，也可以安装 `pinia-plugin-persistedstate`。在 `main.ts` 中 `app.use(pinia)` 之前注册该插件，然后在 Store 中添加 `persist: true` 配置即可实现自动持久化。

这套 Store 架构充分利用了 Vue3 和 Pinia 的现代特性，兼顾了**开发体验、类型安全、性能优化与长期可维护性**，是目前企业级 Vue3 项目的绝对主流标准。