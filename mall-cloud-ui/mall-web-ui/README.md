# 🚀 tauri-bun-ts — 用 Tauri 2.0 + Bun + TypeScript 构建现代桌面应用

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Tauri](https://img.shields.io/badge/tauri-%5E2.0.0-orange)](https://tauri.app)
[![Bun](https://img.shields.io/badge/bun-%5E1.0.0-green)](https://bun.sh)
[![TypeScript](https://img.shields.io/badge/typescript-%5E5.0.0-blue)](https://www.typescriptlang.org/)

> 使用 **Tauri 2.0**、**Bun** 和 **TypeScript** 构建的现代化、高性能桌面应用程序模板，适用于快速启动跨平台桌面开发。

🔗 项目地址：[https://github.com/lcqh2635/tauri-bun-ts](https://github.com/lcqh2635/tauri-bun-ts)

---

## 🌟 项目简介

`tauri-bun-ts` 是一个基于 **Tauri 2.0** 的桌面应用模板，采用 **Bun** 作为运行时和包管理器，结合 **TypeScript** 实现前后端一体化开发。该项目旨在提供一个轻量、高效、现代化的技术栈组合，帮助开发者快速构建安全、跨平台的桌面应用。

### ✅ 特性亮点

- ✅ **Tauri 2.0**：使用 Rust 构建高性能、低资源占用的桌面应用，支持多平台（Windows/macOS/Linux）
- ✅ **Bun**：极速的 JavaScript/TypeScript 运行时，替代 Node.js + npm/yarn/pnpm，提升开发体验
- ✅ **TypeScript**：强类型支持，提升代码可维护性与开发效率
- ✅ **前端框架和工具**：使用 Vue3、Vite、Vue-Router、Pinia 等前端框架
- ✅ **Rust 后端逻辑**：通过 Tauri API 调用 Rust 函数，实现高性能本地操作
- ✅ **跨平台构建**：一键打包为 Windows (.exe)、macOS (.dmg)、Linux (.deb/.rpm/.AppImage)、Android (.apk)、安装包
- ✅ **开发体验优化**：热重载、快速启动、极简配置

---

## 🛠 技术栈

| 技术                                           | 版本      | 说明 |
|----------------------------------------------|---------|------|
| [Tauri](https://tauri.app)                   | ^2.0.0  | 构建安全、快速的桌面应用框架 |
| [Bun](https://bun.sh)                        | ^1.2.20 | 极速 JS/TS 运行时与包管理器 |
| [TypeScript](https://www.typescriptlang.org/) | ^5.6.2" | 类型安全的 JavaScript 超集 |
| Vue                                       | ^3.5.13 | 前端视图层（可替换为任意框架） |
| Rust                                         | 1.80+   | Tauri 核心后端语言 |

---

## 🚀 快速开始

### 1. 安装依赖

确保你已安装：

- [Rust](https://www.rust-lang.org/tools/install)（用于 Tauri 编译）
- [Bun](https://bun.sh/docs/installation)（推荐使用最新稳定版）

```bash
# 安装项目依赖
bun install
```

> 💡 提示：Bun 兼容 `package.json`，无需额外配置即可运行脚本。

---

### 2. 启动开发服务器

```bash
# 启动桌面应用程序
bun run tauri dev
# 启动 android 安卓应用程序，在运行该命令之前必须先在 android-studio 中运行内嵌手机，否则执行会卡住没反映
bun run tauri android dev
```

这将启动前端开发服务器并自动打开 Tauri 桌面窗口（基于 WebView）。

> 默认端口：`http://localhost:1420`  
> 热重载已启用，修改代码后自动刷新。

---

### 3. 构建并打包为安装程序

```bash
# 构建桌面端应用，不包括移动端
bun run --bun tauri build

# 移动端需要单独构建
bun run --bun tauri android build
```

该命令会：

1. 构建前端资源（输出到 `dist/`）
2. 调用 Tauri 打包工具生成原生应用

生成的应用位于 `src-tauri/target/release/` 目录下。

---

支持平台：
- Windows: `.exe`（NSIS / MSI）
- macOS: `.dmg`
- Linux: `.deb` / `.rpm` / `.AppImage`
- Android: `.apk`
- iOS: `.ipa`

> 配置详见 `tauri.conf.json5`

---

## 🧩 项目结构

```bash
tauri-bun-ts/
├── src/                    # 前端源码（Vue/TS/CSS）
├── public/                 # 静态资源（图标、字体等）
├── src-tauri/              # Tauri 后端代码（Rust）
│   ├── src/                # Rust 主程序与命令
│   ├── tauri.conf.json5    # Tauri 配置文件
│   └── Cargo.toml          # Rust 依赖管理
├── .env                    # 环境变量
├── index.html              # 主页面
├── tsconfig.json           # TypeScript 配置
├── bun.lockb               # Bun 锁定文件
├── package.json            # 脚本与元信息（兼容 Bun）
└── README.md
```

---

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request！

1. Fork 项目
2. 创建特性分支：`git checkout -b feat/new-feature`
3. 提交更改：`git commit -m 'feat: add new feature'`
4. 推送分支：`git push origin feat/new-feature`
5. 提交 PR

请遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范。

---

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源，欢迎自由使用、修改与分发。

---

## 🙏 致谢

- [Tauri](https://tauri.app) - 构建安全桌面应用的未来
- [Bun](https://bun.sh) - 下一代 JS 运行时
- [Rust](https://www.rust-lang.org/) - 高性能系统编程语言
- 所有开源贡献者 ❤️

---

## 📬 联系作者

- GitHub: [@lcqh2635](https://github.com/lcqh2635)
- 邮箱：lcqh2635@gmail.com（谷歌邮箱）

> 如果这个项目对你有帮助，欢迎点个 ⭐ Star 支持！

---