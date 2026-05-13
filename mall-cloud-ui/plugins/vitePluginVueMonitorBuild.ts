import type { ConfigEnv, PluginOption, UserConfig } from 'vite';
// Vite 默认内置了 picocolors，用于在终端中打印彩色日志、控制台 ASCII 艺术字输出等
import colors from 'picocolors';

export default function vitePluginVueMonitor (): PluginOption  {
    let outDir:string = 'dist'
    return {
        name: 'ts-build',
        apply: 'build', // 值可以是 build 或 serve 亦可以是一个函数，指明它们仅在 build 或 serve 模式时调用
        // normal(默认值)第二批配执行的插件，会在vite的build阶段之前被执行，可以根据配置判断是否需要处理当前文件的代码。
        // pre首批被执行的插件，会在@rollup/plugin-alias插件执行之后执行。
        // post会在vite的build阶段之后被执行，进行代码构建方面的工作(minimize、代码分析...)。
        enforce: 'pre',
        config(config: UserConfig, configEnv:ConfigEnv) {
            if (configEnv.command === 'build') {
                outDir = config.build?.outDir || 'dist';
            }
        },
        closeBundle() {
            console.log(colors.green(
                '                          _ooOoo_                               \n' +
                '                         o8888888o                              \n' +
                '                         88" . "88                              \n' +
                '                         (| ^_^ |)                              \n' +
                '                         O\\  =  /O                              \n' +
                '                      ____/`---\'\\____                           \n' +
                '                    .\'  \\\\|     |  `.                         \n' +
                '                   /  \\\\|||  :  |||//  \\                        \n' +
                '                  /  _||||| -:- |||||-  \\                       \n' +
                '                  |   | \\\\\\  -  / |   |                       \n' +
                '                  | \\_|  \'\'\\---/\'\'  |   |                       \n' +
                '                  \\  .-\\__  `-`  ___/-. /                       \n' +
                '                ___`. .\'  /--.--\\  `. . ___                     \n' +
                '              ."" \'<  `.___\\_<|>_/___.\'  >\'"".                  \n' +
                '            | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |                 \n' +
                '            \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /                 \n' +
                '      ========`-.____`-.___\\_____/___.-`____.-\'========         \n' +
                '                           `=---=\'                              \n' +
                '      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^        \n' +
                '            佛祖保佑       永不宕机     永无BUG                    \n' +
                '           TigerSong       Etc.End      BuildEnd                  \n'))
        }
    }
}
