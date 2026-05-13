# Vue3 自定义插件的存放目录规范

在 Vue3 项目中，自定义插件的存放位置取决于插件的类型和使用场景，以下是详细的目录结构建议：

## 推荐目录结构

### 1. **全局插件（推荐位置）**
```
src/
├── plugins/               # 专用于Vue全局插件
│   ├── i18n.ts            # 国际化插件
│   ├── directives.ts      # 自定义指令集合
│   └── components.ts      # 全局组件注册
```

### 2. **功能型插件**
```
src/
├── features/             # 按功能划分的插件
│   ├── auth/             # 认证相关插件
│   │   └── plugin.ts    
│   └── analytics/        # 数据分析插件
│       └── plugin.ts
```

### 3. **第三方插件封装**
```
src/
├── libs/                 # 对第三方库的Vue插件封装
│   ├── axios-plugin.ts   # axios封装插件
│   └── echarts-plugin.ts # echarts封装插件
```

## 不同类型插件的存放建议

| 插件类型 | 推荐位置 | 示例 |
|---------|----------|------|
| **全局功能插件** | `src/plugins/` | 国际化、全局样式、错误处理 |
| **UI组件插件** | `src/plugins/components/` | 全局按钮、弹窗等组件注册 |
| **指令插件** | `src/plugins/directives/` | 权限指令、拖拽指令 |
| **服务封装插件** | `src/libs/` | API服务、WebSocket服务 |
| **功能模块插件** | `src/features/*/plugin.ts` | 支付模块、用户模块 |

## 最佳实践示例

### 1. 全局插件定义

```typescript
// src/plugins/i18n.ts
import { createI18n } from 'vue-i18n'
import messages from '@/locales'

export default {
  install: (app, options) => {
    const i18n = createI18n({
      locale: 'zh-CN',
      messages
    })
    app.use(i18n)
  }
}
```

### 2. 在 main.ts 中使用

```typescript
// src/main.ts
import { createApp } from 'vue'
import App from './App.vue'
import i18n from '@/plugins/i18n'
import directives from '@/plugins/directives'

const app = createApp(App)

// 注册插件
app.use(i18n)
app.use(directives)

app.mount('#app')
```

### 3. 功能模块插件示例

```typescript
// src/features/auth/plugin.ts
export default {
  install: (app, options) => {
    // 注册全局认证方法
    app.config.globalProperties.$auth = {
      login: () => { /*...*/ },
      logout: () => { /*...*/ }
    }
    
    // 注册权限指令
    app.directive('permission', {
      mounted(el, binding) {
        // 权限检查逻辑
      }
    })
  }
}
```

## 与 Vite 插件的区别

注意区分两种插件：
- **Vue 插件**：`app.use()` 注册的，放在 `src/plugins/`
- **Vite 插件**：构建工具插件，放在项目根目录 `/plugins/`

```
项目根目录/
├── plugins/               # Vite插件目录
│   └── console-art.ts     # 构建时插件
├── src/
│   ├── plugins/           # Vue插件目录
│   │   └── i18n.ts        # 运行时插件
```

## 自动加载插件的优化方案

可以创建插件加载器自动注册插件：

```typescript
// src/plugins/index.ts
import type { App } from 'vue'

const plugins = import.meta.glob('./*.ts')

export default {
  install: (app: App) => {
    Object.values(plugins).forEach(async (plugin) => {
      const module = await plugin()
      app.use(module.default)
    })
  }
}
```

然后在 main.ts 中：
```typescript
import plugins from '@/plugins'
app.use(plugins)
```

## 总结建议

1. **简单项目**：使用 `src/plugins/` 集中管理
2. **复杂项目**：按功能拆分到不同目录
3. **插件分类**：
    - 全局基础插件 → `src/plugins/`
    - 业务功能插件 → `src/features/*/plugin.ts`
    - 第三方封装 → `src/libs/`
4. **命名规范**：使用 `*.plugin.ts` 后缀明确标识（可选）

这样的结构既保持了清晰度，也方便团队协作和维护。