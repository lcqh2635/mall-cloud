import type { PluginOption, ViteDevServer } from 'vite';
// Vite 默认内置了 picocolors，用于在终端中打印彩色日志、控制台 ASCII 艺术字输出等
import colors from 'picocolors';

export default function vitePluginVueMonitor (): PluginOption  {
    return {
        // 插件名称
        name: 'ts-start',
        apply: 'serve',
        enforce: 'pre',
        configureServer(server: ViteDevServer) {
            const print = server.printUrls;
            server.printUrls = () => {
                const network = server.resolvedUrls?.network[0];
                const local = server.resolvedUrls?.local[0];
                if (!network && !local) {
                    console.log(colors.red('获取IP地址失败,请检查vite.config.ts文件中server.host配置是否正确!\n'))
                } else {
                    console.info(colors.green(' _________  _                         ______                           \n' +
                        '|  _   _  |(_)                      .\' ____ \\                          \n' +
                        '|_/ | | \\_|__   .--./) .---.  _ .--.| (___ \\_|  .--.   _ .--.   .--./) \n' +
                        '    | |   [  | / /\'`\\;/ /__\\\\[ `/\'`\\]_.____`. / .\'`\\ \\[ `.-. | / /\'`\\; \n' +
                        '   _| |_   | | \\ \\._//| \\__., | |   | \\____) || \\__. | | | | | \\ \\._// \n' +
                        '  |_____| [___].\',__`  \'.__.\'[___]   \\______.\' \'.__.\' [___||__].\',__`  \n' +
                        '  Author: Etc.End                             Email:710962805@qq.com             \n'))
                    print();
                }
            }
        }
    }
}
