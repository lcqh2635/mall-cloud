非常感谢您的反馈！既然您的项目明确使用了 **`pinia`** 搭配 **`pinia-plugin-persistedstate`**，那么状态管理的架构和代码实现就应该**完全围绕该插件的特性**来深度优化，摒弃之前混用 `@vueuse/core` 的方案。

`pinia-plugin-persistedstate` 的核心优势在于：**声明式持久化、细粒度字段控制（`paths`）、以及开箱即用的序列化支持**。

以下是为您量身定制的优化版架构方案。

---

### 🏆 推荐标题
**《企业级 Vue3 + Pinia 状态管理架构：基于 pinia-plugin-persistedstate 的目录规范与最佳实践》**

---

### 一、 优化后的目录结构

```text
src/stores/
├── index.ts                # Pinia 实例创建、插件注册与统一导出
└── modules/                # 按业务领域高内聚拆分的 Store 模块
    ├── app.ts              # 应用级 UI 状态 (主题、布局等，支持持久化)
    ├── user.ts             # 用户核心状态 (Token 持久化，敏感信息隔离)
    └── dict.ts             # (可选) 全局字典/配置缓存 (展示 sessionStorage 用法)
```

> 💡 **架构说明**：Pinia 官方推荐**按需导入** Store，因此不需要像 Vuex 那样写复杂的自动注册脚本。`index.ts` 的主要职责是**初始化 Pinia 并注入持久化插件**，同时作为统一的导出入口，保持外部引用的整洁。

---

### 二、 核心文件完整代码（含详细中文注释）

#### 1. `src/stores/index.ts` (Pinia 实例与插件注册)
```typescript
/**
 * Pinia 状态管理入口文件
 * 职责：创建 Pinia 实例、注册持久化插件、统一导出所有 Store
 */
import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';

// 1. 创建 Pinia 实例
const pinia = createPinia();

// 2. 注册持久化插件
// 该插件会自动拦截 Store 的变化，并根据配置将其序列化后存入 storage
pinia.use(piniaPluginPersistedstate);

// 3. 统一导出 (方便在 main.ts 中 app.use(pinia))
export default pinia;

// 4. 统一导出业务 Store，简化外部组件的 import 路径
export { useUserStore } from './modules/user';
export { useAppStore } from './modules/app';
export { useDictStore } from './modules/dict';
```

#### 2. `src/stores/modules/user.ts` (用户状态 - 核心持久化场景)
```typescript
/**
 * 用户状态管理 Store
 * 职责：管理 Token、用户信息、角色，以及登录/登出逻辑
 * 持久化策略：仅持久化 token，用户信息每次刷新重新获取，确保数据最新且避免存储超限
 */
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { login as loginApi, getUserInfo as getUserInfoApi, logout as logoutApi } from '@/api/modules/user';
import type { LoginForm, UserVO } from '@/api/types/user';

export const useUserStore = defineStore('user', () => {
  // ================= 1. State (状态) =================
  // Token 需要持久化，防止页面刷新后丢失登录态
  const token = ref<string>('');
  
  // 用户详细信息 (不持久化，刷新后通过 fetchUserInfo 重新拉取)
  const userInfo = ref<UserVO | null>(null);
  
  // 用户角色列表 (不持久化，随 userInfo 一起获取)
  const roles = ref<string[]>([]);

  // ================= 2. Getters (计算属性) =================
  const isLoggedIn = computed(() => !!token.value);
  const username = computed(() => userInfo.value?.username ?? '未知用户');
  const avatar = computed(() => userInfo.value?.avatar ?? '');

  // ================= 3. Actions (业务逻辑) =================
  
  /**
   * 用户登录
   */
  const login = async (loginForm: LoginForm) => {
    try {
      const res = await loginApi(loginForm);
      // 赋值后，pinia-plugin-persistedstate 会自动将其持久化到 localStorage
      token.value = res.token;
      await fetchUserInfo();
      return true;
    } catch (error) {
      console.error('登录失败:', error);
      return false;
    }
  };

  /**
   * 获取当前用户信息
   */
  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfoApi();
      userInfo.value = res.user;
      roles.value = res.roles || [];
    } catch (error) {
      // 获取失败通常意味着 Token 失效，触发登出清理
      await logout();
      throw error;
    }
  };

  /**
   * 用户登出
   */
  const logout = async () => {
    try {
      await logoutApi();
    } finally {
      // 无论接口成功与否，必须清空本地状态
      // pinia-plugin-persistedstate 会自动监听到 state 变化并清除 storage 中的对应数据
      resetState();
    }
  };

  /**
   * 重置用户状态
   */
  const resetState = () => {
    token.value = '';
    userInfo.value = null;
    roles.value = [];
  };

  // ================= 4. 持久化配置 (核心) =================
  // 返回 state, getters, actions 以及 persist 配置
  return {
    token,
    userInfo,
    roles,
    isLoggedIn,
    username,
    avatar,
    login,
    fetchUserInfo,
    logout,
    resetState,
  };
}, {
  // ⚠️ pinia-plugin-persistedstate 专属配置
  persist: {
    // 自定义 storage 中的 key 名称，避免与其他项目或模块冲突
    key: 'app-user-store',
    // 指定需要持久化的 state 字段。未列出的字段 (如 userInfo, roles) 刷新后将重置为初始值
    paths: ['token'],
    // 可选：自定义存储介质，默认为 localStorage。此处保持默认即可
    // storage: localStorage, 
  }
});
```

#### 3. `src/stores/modules/app.ts` (应用 UI 状态 - 多 Storage 场景)
```typescript
/**
 * 应用全局 UI 状态管理 Store
 * 职责：管理主题模式、侧边栏状态等
 * 持久化策略：全部持久化，但使用不同的 storage 介质（如敏感/临时配置用 sessionStorage）
 */
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAppStore = defineStore('app', () => {
  // ================= 1. State =================
  // 侧边栏是否折叠
  const sidebarCollapsed = ref(false);
  // 主题模式: 'light' | 'dark'
  const theme = ref<'light' | 'dark'>('light');
  // 临时会话标识 (例如：当前正在编辑的草稿 ID)
  const draftSessionId = ref<string>('');

  // ================= 2. Getters =================
  const isDark = computed(() => theme.value === 'dark');

  // ================= 3. Actions =================
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value;
  };

  const setTheme = (val: 'light' | 'dark') => {
    theme.value = val;
    // 同步更新 DOM 上的 class，触发 CSS 变量切换
    document.documentElement.classList.toggle('dark', val === 'dark');
  };

  const setDraftSession = (id: string) => {
    draftSessionId.value = id;
  };

  return {
    sidebarCollapsed,
    theme,
    draftSessionId,
    isDark,
    toggleSidebar,
    setTheme,
    setDraftSession,
  };
}, {
  persist: [
    {
      // 策略 1：常规 UI 配置，持久化到 localStorage，长期有效
      key: 'app-ui-config',
      paths: ['sidebarCollapsed', 'theme'],
      storage: localStorage,
    },
    {
      // 策略 2：临时会话数据，持久化到 sessionStorage，关闭浏览器标签页即失效
      key: 'app-draft-session',
      paths: ['draftSessionId'],
      storage: sessionStorage,
    }
  ]
});
```

---

### 三、 为什么这么组织？（设计依据与优势）

| 设计决策 | 解决的问题 | 反面教材 (不良实践) |
| :--- | :--- | :--- |
| **全面采用 Setup Store + `persist` 配置对象** | 声明式持久化。只需在导出时配置 `paths`，插件自动处理序列化、反序列化和 `watch` 监听，代码零样板。 | 在 Action 中手动写 `localStorage.setItem`，或在组件中 `watch` store 变化手动存储，极易遗漏导致状态不同步。 |
| **细粒度 `paths` 控制** | 安全且高效。例如 `user` store 中只持久化 `token`，不持久化庞大的 `userInfo` 对象，既节省 storage 空间，又确保刷新后拉取的是最新用户数据。 | 整个 store 对象无脑持久化，导致敏感数据泄露风险，或刷新后展示了过期的用户信息。 |
| **支持多 Storage 策略 (`storage` 字段)** | 灵活应对不同业务场景。如 `app.ts` 所示，长期配置用 `localStorage`，临时草稿用 `sessionStorage`，在一个 Store 内通过数组配置即可完美隔离。 | 为了使用 `sessionStorage` 而强行拆分出另一个庞大的 Store，增加维护成本。 |
| **模块化拆分 (`modules/`)** | 职责单一。`user` 只管认证，`app` 只管 UI。多人协作时 Git 冲突概率极低，且符合 Tree-shaking 原则。 | 将所有状态塞进一个 `store.ts`，单文件超 500 行，难以阅读和维护。 |

---

### 四、 进阶最佳实践建议 (针对 `pinia-plugin-persistedstate`)

1. **处理复杂对象的持久化 (序列化问题)**：
   如果您的 state 中包含 `Date` 对象、`Map`、`Set` 或自定义 Class 实例，默认的 `JSON.stringify` 会导致它们变成普通字符串或空对象。
   **解决方案**：在 `persist` 配置中使用 `serializer`：
   ```typescript
   import { defineStore } from 'pinia';
   
   export const useConfigStore = defineStore('config', () => {
     const lastLoginTime = ref(new Date());
     return { lastLoginTime };
   }, {
     persist: {
       serializer: {
         // 自定义序列化：将 Date 转为时间戳字符串
         serialize: (value) => JSON.stringify(value, (key, val) => {
           if (val instanceof Date) return { __type: 'Date', value: val.getTime() };
           return val;
         }),
         // 自定义反序列化：将时间戳字符串还原为 Date 对象
         deserialize: (value) => JSON.parse(value, (key, val) => {
           if (val && val.__type === 'Date') return new Date(val.value);
           return val;
         }),
       },
     },
   });
   ```

2. **全局默认配置**：
   如果您的项目 90% 的 store 都需要持久化到 `localStorage`，可以在 `src/stores/index.ts` 中配置全局默认值，避免在每个文件中重复书写：
   ```typescript
   import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
   
   pinia.use(
     piniaPluginPersistedstate({
       // 全局默认配置
       storage: localStorage,
       // 可以自定义 key 的生成逻辑，例如自动加上项目前缀
       key: (id) => `__my_project__${id}`,
     })
   );
   ```
   *(注：局部 Store 中的 `persist` 配置会覆盖全局默认配置)*

3. **清理过期数据**：
   `pinia-plugin-persistedstate` 本身不带 TTL (过期时间) 功能。如果需要 Token 过期自动清理，最佳实践是在 **Axios 响应拦截器** 或 **路由守卫** 中检测到 401 时，主动调用 `userStore.resetState()`，插件会自动同步清除 localStorage 中的数据。

这套方案完全契合您使用的技术栈，将 `pinia-plugin-persistedstate` 的声明式、细粒度控制优势发挥到了极致，是目前企业级 Vue3 项目中兼顾**开发效率、安全性与可维护性**的最优解。