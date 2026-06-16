// scripts/check-pm.mjs
const userAgent = process.env.npm_config_user_agent || '';

// 解析出包管理器名称，例如 'npm', 'yarn', 'pnpm', 'bun'
const pm = userAgent.split(' ')[0]?.split('/')[0] || 'unknown';

if (pm !== 'bun') {
    // 使用 ANSI 转义码输出红色和黄色，让警告更醒目
    const red = '\x1b[31m';
    const yellow = '\x1b[33m';
    const reset = '\x1b[0m';

    console.error(`\n${red}🚫 检测到非法包管理器！${reset}`);
    console.error(`${yellow}当前使用的是: ${red}${pm}${reset}`);
    console.error(`${yellow}本项目强制要求使用 Bun，请运行: ${red}bun install${reset}\n`);

    // 强制退出进程，阻断安装
    process.exit(1);
}