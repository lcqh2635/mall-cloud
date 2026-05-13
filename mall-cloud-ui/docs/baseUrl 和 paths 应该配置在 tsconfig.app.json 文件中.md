`baseUrl` 和 `paths` 应该配置在 **`tsconfig.app.json`** 中！

---

## ✅ 正确配置位置

### 📄 tsconfig.app.json

```json
{
  "extends": "./tsconfig.json",
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": [
      "ES2020",
      "DOM",
      "DOM.Iterable"
    ],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    // 👇 路径别名配置（放在这里！）
    "baseUrl": ".",
    "paths": {
      "@/*": [
        "./src/*"
      ],
      "@components/*": [
        "./src/components/*"
      ],
      "@utils/*": [
        "./src/utils/*"
      ],
      "@api/*": [
        "./src/api/*"
      ],
      "@assets/*": [
        "./src/assets/*"
      ]
    }
  },
  "include": [
    "src/**/*.ts",
    "src/**/*.tsx",
    "src/**/*.vue"
  ]
}
```

---

## ⚠️ 重要提醒：Vite 项目需要双配置！

TypeScript 的 `paths` **只影响编译时的类型检查**，不影响运行时。在 Vite 项目中，你**必须同时在 `vite.config.ts` 中配置别名
**，否则会出现：

- ✅ TypeScript 不报错（编译时正常）
- ❌ 浏览器运行时报错 `Failed to resolve import "@/xxx"`（运行时找不到路径）

### 📄 vite.config.ts（同步配置）

```ts
import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {resolve} from 'path'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': resolve(__dirname, './src'),
            '@components': resolve(__dirname, './src/components'),
            '@utils': resolve(__dirname, './src/utils'),
            '@api': resolve(__dirname, './src/api'),
            '@assets': resolve(__dirname, './src/assets')
        }
    }
})
```

> 💡 **TypeScript 配置** → 让 IDE 和编译器认识路径别名  
> **Vite 配置** → 让打包工具和运行时认识路径别名  
> **两者缺一不可！**

---

## 📋 完整配置对照表

| 配置文件                   | 配置项                 | 作用                 |
|------------------------|---------------------|--------------------|
| **tsconfig.app.json**  | `baseUrl` + `paths` | TypeScript 编译时路径解析 |
| **vite.config.ts**     | `resolve.alias`     | Vite 打包时路径解析       |
| **tsconfig.node.json** | ❌ 不需要               | 构建脚本不使用业务代码别名      |

---

## 🎯 常见使用场景

配置完成后，你可以在代码中这样使用：

```ts
// ❌ 相对路径（不推荐）
import Button from '../../../components/Button.vue'
import {formatDate} from '../../../utils/date'

// ✅ 路径别名（推荐）
import Button from '@/components/Button.vue'
import {formatDate} from '@utils/date'
import type {User} from '@/types/user'
import api from '@api/user'
```

---

## 🔧 Vue 项目特殊说明

如果你使用 **Vue 3 + Vite**，还需要确保 `vite.config.ts` 中正确配置：

```ts
import {fileURLToPath, URL} from 'node:url'  // 推荐用这个代替 path.resolve

export default defineConfig({
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }
})
```

> 💡 `fileURLToPath` 是 ESM 模块的标准写法，比 `path.resolve(__dirname, ...)` 更兼容 Vite 的 ESM 环境。

---

## ✅ 验证配置是否生效

1. **TypeScript 检查**：
   ```bash
   npx vue-tsc --noEmit
   # 应该没有 "Cannot find module '@/xxx'" 错误
   ```

2. **Vite 运行检查**：
   ```bash
   npm run dev
   # 浏览器控制台应该没有 "Failed to resolve import" 错误
   ```

3. **IDE 检查**：
    - VS Code 应该能自动补全 `@/` 路径
    - Ctrl/Cmd + 点击路径能跳转到对应文件

---

## 📌 总结

| 问题                      | 答案                          |
|-------------------------|-----------------------------|
| `baseUrl` 和 `paths` 放哪？ | **tsconfig.app.json**       |
| 只配 TypeScript 够吗？       | ❌ 不够，还要配 **vite.config.ts** |
| tsconfig.node.json 需要吗？ | ❌ 不需要，构建脚本不用业务别名            |
| tsconfig.json 需要吗？      | ❌ 不需要，它是项目引用入口              |

如果你需要我帮你生成完整的 `tsconfig.app.json` + `vite.config.ts` 配置模板（针对 `mall-web` 或 `mall-admin`），随时告诉我！
😊