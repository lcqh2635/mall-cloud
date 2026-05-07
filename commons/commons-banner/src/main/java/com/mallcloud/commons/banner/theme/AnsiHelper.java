package com.mallcloud.commons.banner.theme;

import lombok.Getter;

/**
 * ANSI 颜色工具类
 *
 * <p>封装 ANSI 转义码的生成与终端兼容性检测，
 * 所有主题渲染器通过此类统一处理颜色输出，
 * 避免在非 ANSI 终端（如部分 CI 环境）出现乱码。
 *
 * <p>ANSI 色号速查：
 * <pre>
 *  标准色：30=黑 31=红 32=绿 33=黄 34=蓝 35=紫 36=青 37=白
 *  高亮色：90=亮黑 91=亮红 92=亮绿 93=亮黄 94=亮蓝 95=亮紫 96=亮青 97=亮白
 * </pre>
 *
 * @author mallcloud
 */
@Getter
public class AnsiHelper {

    /** ANSI 重置符，恢复终端默认颜色 */
    private static final String RESET = "\u001B[0m";

    /** ANSI 转义前缀模板，%d 填入色号 */
    private static final String ANSI_TEMPLATE = "\u001B[%dm";

    /** 当前环境是否支持 ANSI 颜色输出
     * -- GETTER --
     *  是否支持 ANSI 颜色
     */
    private final boolean ansiSupported;

    /**
     * 构造函数，自动检测当前环境的 ANSI 支持情况
     *
     * @param environment Spring Environment，用于读取 spring.output.ansi.enabled
     */
    public AnsiHelper(org.springframework.core.env.Environment environment) {
        this.ansiSupported = detectAnsiSupport(environment);
    }

    // ===================== 公共 API =====================

    /**
     * 为文本包裹指定 ANSI 色号的颜色代码
     *
     * <p>若当前终端不支持 ANSI，直接返回原始文本，不附加任何转义字符。
     *
     * @param text     原始文本
     * @param colorCode ANSI 色号（如 32 表示绿色）
     * @return 带颜色的文本，或原始文本（不支持 ANSI 时）
     */
    public String colorize(String text, int colorCode) {
        if (!ansiSupported) {
            return text;
        }
        return String.format(ANSI_TEMPLATE, colorCode) + text + RESET;
    }

    /**
     * 快捷方法：绿色文本（色号 32）
     */
    public String green(String text)  { return colorize(text, 32); }

    /**
     * 快捷方法：青色文本（色号 36）
     */
    public String cyan(String text)   { return colorize(text, 36); }

    /**
     * 快捷方法：黄色文本（色号 33）
     */
    public String yellow(String text) { return colorize(text, 33); }

    /**
     * 快捷方法：红色文本（色号 31）
     */
    public String red(String text)    { return colorize(text, 31); }

    /**
     * 快捷方法：蓝色文本（色号 34）
     */
    public String blue(String text)   { return colorize(text, 34); }

    /**
     * 快捷方法：紫色文本（色号 35）
     */
    public String purple(String text) { return colorize(text, 35); }

    /**
     * 快捷方法：亮青色文本（色号 96）
     */
    public String brightCyan(String text) { return colorize(text, 96); }

    // ===================== 私有方法 =====================
    /**
     * 检测当前运行环境是否支持 ANSI 颜色输出
     *
     * <p>检测优先级：
     * <ol>
     *   <li>{@code NO_COLOR} 环境变量（标准规范 no-color.org）→ 强制关闭</li>
     *   <li>{@code spring.output.ansi.enabled=always} → 强制开启</li>
     *   <li>{@code spring.output.ansi.enabled=never}  → 强制关闭</li>
     *   <li>Windows 系统 → 检测 TERM 环境变量</li>
     *   <li>其他系统（Linux/macOS/IDE）→ 默认开启</li>
     * </ol>
     *
     * @param environment Spring 环境
     * @return true 表示支持 ANSI
     */
    private boolean detectAnsiSupport(org.springframework.core.env.Environment environment) {
        // 规则1：遵循 NO_COLOR 标准规范 https://no-color.org/
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }

        // 规则2：读取 Spring Boot 官方 ANSI 配置
        // IDEA 中 System.console() 为 null，因此不能用 console 判断
        // 必须依赖此配置显式控制，推荐在 dev 环境设置 always
        String ansiEnabled = "detect";
        if (environment != null) {
            ansiEnabled = environment.getProperty("spring.output.ansi.enabled", "detect");
        }

        if ("always".equalsIgnoreCase(ansiEnabled)) {
            return true;
        }
        if ("never".equalsIgnoreCase(ansiEnabled)) {
            return false;
        }

        // 规则3：detect 模式 —— Windows 需要额外判断
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // Windows Terminal / ConEmu / Git Bash 等设置了 TERM 变量
            String term = System.getenv("TERM");
            return term != null && (term.contains("xterm") || term.contains("color"));
        }

        // 规则4：Linux / macOS / IDE 控制台统一视为支持
        return true;
    }
}