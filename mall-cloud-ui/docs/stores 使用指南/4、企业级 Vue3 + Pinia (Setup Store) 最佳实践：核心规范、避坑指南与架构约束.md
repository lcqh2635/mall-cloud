以下为您精心整理的 Pinia (Setup Store 风格) 在企业级 Vue3 项目中的**核心使用建议与规范约束**。这些规范是基于大量真实项目踩坑经验总结而来，能有效避免响应式丢失、循环依赖、状态污染等常见架构问题。

---

### 🏆 推荐文档标题
**《企业级 Vue3 + Pinia (Setup Store) 最佳实践：核心规范、避坑指南与架构约束》**

---

### 一、 核心规范与避坑指南

#### 1. ⚠️ 绝对禁止直接解构 Store（响应式丢失陷阱）
这是 Pinia 新手最容易犯的错误。直接从 Store 中解构 `state` 或 `getters` 会**切断其与 Store 实例的响应式连接**。

```typescript
// ❌ 错误做法：解构后，count 变成了普通变量，失去响应式，UI 不会更新
const userStore = useUserStore();
const { token, username } = userStore; 

// ✅ 正确做法：使用 Pinia 提供的 storeToRefs 辅助函数
import { storeToRefs } from 'pinia';

const userStore = useUserStore();
// storeToRefs 只会提取 ref/reactive 属性，保留其响应式特性
// 注意：actions (函数) 不需要也不应该被 storeToRefs 包裹，直接解构即可
const { token, username } = storeToRefs(userStore);
const { login, logout } = userStore; 
```
**规范约束**：在 Code Review 中，任何直接解构 Store 响应式属性的代码都应被标记为 Bug。

#### 2. 🔄 Store 间的依赖：必须在 Action 内部调用（防循环依赖）
当 Store A 需要使用 Store B 的数据或方法时，**绝对不能在顶层导入并调用**，这极易引发模块循环依赖（Circular Dependency），导致运行时 `undefined` 错误。

```typescript
// src/stores/modules/permission.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([]);

  const generateRoutes = async () => {
    // ✅ 正确做法：在 Action 内部按需引入并使用其他 Store
    // 这样能确保在函数执行时，目标 Store 已经被完全初始化
    const userStore = useUserStore(); 
    
    // 安全地读取另一个 Store 的状态
    const roles = userStore.roles;
    
    // 调用另一个 Store 的方法
    // await userStore.fetchUserInfo(); 
    
    // ... 根据 roles 生成路由逻辑
  };

  return { routes, generateRoutes };
});
```
**规范约束**：禁止在 `defineStore` 的顶层作用域调用其他 `useXxxStore()`。

#### 3. 🚫 职责边界：Store 应保持“纯粹”，禁止直接操作 DOM
Store 的唯一职责是**管理状态和业务逻辑**。它不应该知道 DOM 的存在。

```typescript
// ❌ 错误做法：在 Store 中直接操作 DOM
export const useAppStore = defineStore('app', () => {
  const theme = ref('light');
  const setTheme = (val: string) => {
    theme.value = val;
    document.documentElement.classList.add(val); // 🚫 污染了 Store 的职责
  };
  return { theme, setTheme };
});

// ✅ 正确做法：将 DOM 操作抽离到 Composable 或组件中
// composables/useTheme.ts
export function useTheme() {
  const appStore = useAppStore();
  
  watch(() => appStore.theme, (newVal) => {
    document.documentElement.classList.toggle('dark', newVal === 'dark');
  }, { immediate: true });
}

// 然后在 main.ts 或 App.vue 中调用 useTheme() 即可
```
**规范约束**：Store 中不应出现 `document`、`window` (除 localStorage 等纯数据存储外)、`setTimeout` (除非是纯业务延迟逻辑) 等浏览器 API 的直接调用。

#### 4. 💾 持久化插件 (`pinia-plugin-persistedstate`) 的进阶约束
结合您使用的持久化插件，需遵守以下安全与性能规范：

*   **细粒度 `paths` 控制**：永远不要无脑持久化整个 Store。例如，`userInfo` 对象可能很大且包含敏感信息，应仅持久化 `token`，`userInfo` 在应用启动时通过 API 重新拉取，确保数据最新。
*   **避免存储非序列化数据**：默认的 `JSON.stringify` 无法正确处理 `Date`、`Map`、`Set` 或 Class 实例。如果必须存储，请在 `persist` 配置中自定义 `serializer`（如前文所述），或先在 State 中将其转换为基本类型（如时间戳）。
*   **跨标签页同步**：如果项目需要多个标签页共享状态，`pinia-plugin-persistedstate` 默认不会触发其他标签页的响应式更新。如需此功能，需额外监听 `storage` 事件或引入 `@pinia/plugin-shared`。

#### 5. 🛡️ TypeScript 类型安全最佳实践
Setup Store 的最大优势是完美的类型推导。为了保持这一优势：

*   **避免显式标注返回类型**：让 TS 自动推导 `return` 对象的类型，除非有极其复杂的泛型需求。
*   **State 初始化必须赋予明确的初始值**：
    ```typescript
    // ❌ 糟糕的推导：TS 可能会推导为 any 或过于宽泛的类型
    const userInfo = ref(); 
    
    // ✅ 优秀的推导：明确告知 TS 类型，且初始值为 null
    const userInfo = ref<UserVO | null>(null); 
    ```
*   **Action 的返回值也应明确**：如果 Action 返回 Promise，确保其 resolve 的类型是明确的，方便组件中 `await` 后获得智能提示。

#### 6. 🧪 测试友好性 (Unit Testing)
Setup Store 本质上是一个返回对象的纯函数，这使得它极其容易进行单元测试。

```typescript
// tests/unit/stores/user.spec.ts
import { setActivePinia, createPinia } from 'pinia';
import { useUserStore } from '@/stores/modules/user';

describe('User Store', () => {
  beforeEach(() => {
    // 每次测试前创建全新的 Pinia 实例，确保测试隔离
    setActivePinia(createPinia());
  });

  it('should update token after login', async () => {
    const store = useUserStore();
    
    // Mock API 调用 (假设使用了 vitest 的 vi.mock)
    // vi.mocked(loginApi).mockResolvedValue({ token: 'test-token' });

    await store.login({ username: 'admin', password: '123' });
    
    expect(store.token).toBe('test-token');
    expect(store.isLoggedIn).toBe(true);
  });
});
```
**规范约束**：不要在测试之间共享 Store 实例，务必在 `beforeEach` 中使用 `setActivePinia(createPinia())` 重置状态。

---

### 二、 项目级目录与文件约束总结

为了保持团队代码的绝对一致性，建议在项目的 `.eslintrc` 或团队 Wiki 中固化以下规则：

1. **命名规范**：Store 文件名使用 `kebab-case` (如 `user-store.ts` 或 `user.ts`)，但导出的函数名必须使用 `use` 前缀 + `PascalCase` (如 `useUserStore`)。
2. **单一职责**：单个 Store 文件不应超过 300 行。如果超过，说明职责过重，应考虑拆分为更细粒度的 Store (如将 `user` 拆分为 `auth` 和 `profile`)。
3. **统一导出**：所有业务 Store 必须在 `src/stores/index.ts` 中统一 `export`，组件中禁止使用相对路径 (如 `../../stores/modules/user`) 直接导入，必须使用别名 `@/stores`。
4. **禁止在组件外直接调用 `useXxxStore()`**：除了在另一个 Store 的 Action 内部，或者在 Composables 中，不应在普通的 `.ts` 工具文件中调用 Store，这会破坏依赖注入的上下文。

---

### 三、 总结

选择 **Setup Store** 是非常明智的决定，它代表了 Vue 3 生态的未来。只要团队严格遵守 **“使用 `storeToRefs` 解构”、“在 Action 内处理跨 Store 依赖”、“保持 Store 职责纯粹”** 这三条铁律，您的状态管理架构将具备极高的**可维护性、类型安全性和测试友好度**，能够从容应对任何规模的企业级项目。