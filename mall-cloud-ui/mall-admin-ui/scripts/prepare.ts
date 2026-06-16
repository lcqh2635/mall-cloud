// scripts/prepare.ts
import { existsSync } from 'fs'
import { execSync } from 'child_process'

function prepare() {
    // 1. 检查是否是 Git 仓库
    if (existsSync('.git')) {
        try {
            // 2. 执行 lefthook install，并继承终端输出
            execSync('lefthook install', { stdio: 'inherit' })
            console.log('🚀 Git Hooks 已生效')
        } catch (error) {
            console.error('❌ Lefthook 安装失败，请检查配置')
        }
    } else {
        // 3. 优雅降级提示
        console.log('⚠️ 未检测到 Git 仓库，跳过 Lefthook 安装')
    }
}

prepare()