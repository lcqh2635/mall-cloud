以下为您精心整理的 Vue Router 4 在企业级 Vue3 项目中的**核心使用建议与规范约束**。这些规范基于大量真实中后台项目的踩坑经验总结，能有效避免路由死循环、类型丢失、性能瓶颈以及维护灾难。

---

### 🏆 推荐文档标题
**《企业级 Vue3 + Vue Router 最佳实践：核心规范、避坑指南与架构约束》**

---

### 一、 核心规范与避坑指南

#### 1. 🎯 路由跳转：强制优先使用 `name` 而非 `path`
使用路由名称（`name`）进行跳转是更健壮的做法。如果未来后端要求修改 URL 路径结构，只需修改路由配置，所有通过 `name` 跳转的代码无需任何改动。

```typescript
import { useRouter } from 'vue-router';

const router = useRouter();

// ❌ 错误做法：硬编码 path，路径重构时极易遗漏报错
router.push('/user/detail/123?tab=info');

// ✅ 正确做法：使用 name + params/query，享受完整的 TS 类型推导和智能提示
router.push({ 
  name: 'UserDetail', 
  params: { id: '123' }, 
  query: { tab: 'info' } 
});
```

#### 2. ⚡ 组件加载：强制使用动态 `import` (懒加载)
除了极少数必须首屏渲染的核心组件（如 Layout 外壳），所有页面级组件**必须**使用动态导入。这能显著减小首屏 JS 包体积，提升 LCP (最大内容绘制) 性能。

```typescript
// ❌ 错误做法：同步导入，会导致该组件及其依赖被打包进初始 chunk
import UserList from '@/views/user/list.vue';
const routes = [{ path: '/user', component: UserList }];

// ✅ 正确做法：动态导入，Vite 会自动将其拆分为独立的 chunk，按需加载
const routes = [{ 
  path: '/user', 
  component: () => import('@/views/user/list.vue') 
}];
```

#### 3. 🔄 动态路由注入：防死循环的标准范式
在权限控制中动态添加路由 (`addRoute`) 时，最容易引发**无限重定向死循环**。必须严格遵守以下范式：

```typescript
// ✅ 正确做法：使用标志位 + replace 重新导航
let isDynamicRoutesAdded = false;

router.beforeEach(async (to, from, next) => {
  const token = getToken();
  
  if (!token) {
    next('/login');
    return;
  }

  if (!isDynamicRoutesAdded) {
    try {
      // 1. 获取用户权限/角色
      await fetchUserInfo();
      
      // 2. 过滤并动态添加路由
      const accessRoutes = generateRoutes(userRoles);
      accessRoutes.forEach(route => router.addRoute(route));
      
      // 3. 标记已添加
      isDynamicRoutesAdded = true;
      
      // 4. ⚠️ 核心关键：addRoute 是异步生效的，当前导航仍匹配旧路由表。
      // 必须使用 replace: true 重新触发一次导航，让 Router 匹配到新注入的路由，
      // 且 replace 不会在浏览器历史中留下多余的记录。
      next({ ...to, replace: true });
    } catch (error) {
      // 权限获取失败，清除 Token 并跳回登录
      removeToken();
      next(`/login?redirect=${to.path}`);
    }
  } else {
    // 路由已初始化，直接放行
    next();
  }
});
```

#### 4. 🚫 404 兜底路由：必须置于路由表最末尾
Vue Router 是按照**从上到下**的顺序匹配路由的。如果将通配符 `/:pathMatch(.*)*` 放在前面，它会拦截所有后续路由，导致整个应用白屏或全部跳转 404。

```typescript
const routes = [
  { path: '/login', component: () => import('@/views/login.vue') },
  // ... 其他业务路由
  
  // ✅ 正确做法：永远放在数组的最后一个
  { 
    path: '/:pathMatch(.*)*', 
    name: 'NotFound', 
    component: () => import('@/views/error/404.vue') 
  }
];
```

#### 5. 🛑 `useRoute` / `useRouter` 的使用限制
这两个 Composables **只能在 `<script setup>` 或普通的 `setup()` 函数中调用**。它们依赖于 Vue 的当前组件实例上下文。

```typescript
// ❌ 错误做法：在普通的 .ts 工具文件或 Store 顶层直接调用
import { useRoute } from 'vue-router';
const route = useRoute(); // 🚫 报错：Cannot call useRoute outside of setup()

// ✅ 正确做法：将其封装在函数内，在组件内部调用时传入，或在 Composable 内部调用
export function useCurrentPath() {
  const route = useRoute(); // ✅ 在 composable 内部调用是安全的
  return computed(() => route.path);
}
```

---

### 二、 TypeScript 类型安全最佳实践

Vue Router 4 提供了极佳的 TS 支持，但需要手动扩展 `RouteMeta` 接口，才能消除 `meta` 属性上的 `any` 类型，获得完美的智能提示。

在 `src/types/vue-router.d.ts` (或任意全局 `.d.ts` 文件) 中添加：

```typescript
/**
 * 扩展 Vue Router 的 RouteMeta 接口
 * 使得在定义路由和读取 meta 时，拥有严格的类型检查
 */
import 'vue-router';

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题 (用于 document.title 或侧边栏菜单显示) */
    title?: string;
    /** 菜单图标标识 */
    icon?: string;
    /** 允许访问的角色标识数组 */
    roles?: string[];
    /** 是否在侧边栏菜单中隐藏 (如登录页、404页) */
    hidden?: boolean;
    /** 是否开启 keep-alive 缓存 */
    keepAlive?: boolean;
    /** 是否固定在顶部多标签页 (TagsView) */
    affix?: boolean;
    /** 是否要求必须登录 (通常由守卫统一处理，但显式声明更清晰) */
    requiresAuth?: boolean;
  }
}
```
**收益**：在组件中编写 `route.meta.title` 时，IDE 会自动提示所有可选字段，且拼写错误会立即被 TS 编译器标红。

---

### 三、 路由守卫 (Guards) 架构约束

#### 1. 职责单一，拒绝“上帝守卫”
不要在 `router/index.ts` 中写一个长达 200 行的 `beforeEach`。应按职责拆分为独立文件，并在入口统一注册（如前文 `guards/` 目录结构所示）。

#### 2. 守卫执行顺序与 Token 校验逻辑
标准的权限守卫逻辑链条应严格遵循以下顺序，避免逻辑漏洞：
1. **白名单校验**：如果是 `/login` 等白名单路径，直接 `next()`。
2. **无 Token 拦截**：如果没有 Token 且不在白名单，重定向到 `/login?redirect=xxx`。
3. **有 Token 但未初始化路由**：拉取用户信息 -> 动态 `addRoute` -> `next({ ...to, replace: true })`。
4. **有 Token 且已初始化路由**：直接 `next()`。

#### 3. 避免在守卫中执行耗时同步操作
路由守卫会阻塞导航。不要在 `beforeEach` 中执行大量的同步计算或复杂的 DOM 操作。如果需要初始化大量数据，应放在 `main.ts` 的 `app.mount('#app')` 之前，或在进入特定页面前通过 Composable 异步拉取。

---

### 四、 性能与体验优化建议

#### 1. 滚动行为 (`scrollBehavior`)
SPA 应用在路由切换时，默认会保留上一个页面的滚动位置。对于中后台系统，通常期望每次进入新页面时滚动条回到顶部。

```typescript
const router = createRouter({
  // ...
  scrollBehavior(to, from, savedPosition) {
    // 1. 如果使用了浏览器的“前进/后退”按钮，恢复到之前保存的位置
    if (savedPosition) {
      return savedPosition;
    }
    // 2. 如果目标路由包含 hash (如 #section-1)，滚动到该 hash 位置
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth',
      };
    }
    // 3. 默认情况：平滑滚动到页面顶部
    return { top: 0, behavior: 'smooth' };
  }
});
```

#### 2. Keep-Alive 与 Router-View 的配合
如果需要实现页面缓存，必须使用 Vue 3 的 `<RouterView v-slot>` 语法，并结合 `route.meta.keepAlive` 进行动态判断。

```vue
<!-- App.vue 或 Layout 组件中 -->
<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
      <!-- 只有当 meta.keepAlive 为 true 时，才使用 keep-alive 包裹 -->
      <keep-alive :include="cachedViews">
        <component :is="Component" :key="route.fullPath" />
      </keep-alive>
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
// 从全局 Store 或当前路由 meta 中获取需要缓存的组件 name 列表
const cachedViews = computed(() => {
  // 实际项目中通常由 Store 管理一个 tagsView 列表
  return route.meta.keepAlive ? [route.name as string] : [];
});
</script>
```
*注意：被 `keep-alive` 缓存的组件，其 `<script setup>` 中必须显式声明 `name` 选项（在 `<script>` 标签中），否则缓存将失效。*

---

### 五、 项目级团队规范约束总结

建议在项目的 `.eslintrc` 或团队 Wiki 中固化以下规则：

1. **命名规范**：路由的 `name` 必须使用 **PascalCase (大驼峰)**，且必须全局唯一。例如：`UserList`, `OrderDetail`。这有助于与组件的 `name` 保持一致，方便 `keep-alive` 缓存。
2. **路径规范**：路由的 `path` 必须使用 **kebab-case (短横线)** 或以 `/` 开头的绝对路径。例如：`/user-management/user-list`。
3. **禁止硬编码跳转**：Code Review 时，发现 `router.push('/xxx')` 应要求重构为 `router.push({ name: 'Xxx' })`。
4. **路由表模块化**：单个路由配置文件（如 `modules/user.ts`）不应超过 150 行。如果业务线庞大，应按领域进一步拆分。
5. **Meta 属性必填项**：对于需要渲染在侧边栏菜单中的路由，`meta.title` 和 `meta.icon` 应作为强制规范，缺失则应在构建或 CI 阶段给出警告。

---

### 六、 总结

Vue Router 4 的设计已经非常优雅，但在企业级应用中，**“动态路由的死循环防范”、“TypeScript 的严格类型约束”以及“跳转方式的规范化”** 是决定项目长期可维护性的三大基石。严格遵守上述规范，您的路由层将变得坚如磐石，能够轻松支撑复杂的中后台业务迭代。