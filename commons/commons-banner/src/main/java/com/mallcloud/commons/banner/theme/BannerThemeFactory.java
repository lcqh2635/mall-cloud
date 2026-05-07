package com.mallcloud.commons.banner.theme;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.*;

/**
 * Banner 主题工厂
 *
 * <p>根据配置的主题名称（{@code mallcloud.banner.theme}）返回对应的渲染器实例。
 * 采用饿汉式预注册，所有主题在工厂初始化时即创建完毕，线程安全且无额外开销。
 *
 * <p>内置主题：
 * <ul>
 *   <li>{@code default}  — 标准带分隔线风格（默认）</li>
 *   <li>{@code minimal}  — 纯文本单行，适合 CI / 日志采集</li>
 *   <li>{@code box}      — Unicode 边框风格</li>
 *   <li>{@code colorful} — 每行不同颜色的彩色增强版</li>
 * </ul>
 *
 * <p>扩展方式：在 {@code static} 块中调用 {@link #register} 即可注册自定义主题。
 *
 * @author mallcloud
 */
public final class BannerThemeFactory {
    /** 主题注册表，key 为小写主题名称 */
    private static final Map<String, BannerTheme> REGISTRY = new HashMap<>();

    /** 未匹配到主题时使用的默认主题名称 */
    private static final String DEFAULT_THEME = "default";

    // 静态块：注册所有内置主题
    static {
        register(DEFAULT_THEME,  new DefaultBannerTheme());
        register("minimal",      new MinimalBannerTheme());
        register("box",          new BoxBannerTheme());
        register("colorful",     new ColorfulBannerTheme());
    }

    /** 工具类，禁止实例化 */
    private BannerThemeFactory() {}

    /**
     * 根据主题名称获取渲染器
     *
     * <p>名称不区分大小写；若未找到对应主题，回退到 default 主题并打印警告。
     *
     * @param themeName 主题名称，如 "default" / "minimal" / "box" / "colorful"
     * @return 对应的 {@link BannerTheme} 实例，永不返回 null
     */
    public static BannerTheme getTheme(String themeName) {
        if (themeName == null || themeName.isBlank()) {
            return REGISTRY.get(DEFAULT_THEME);
        }

        String key = themeName.trim().toLowerCase();
        BannerTheme theme = REGISTRY.get(key);

        if (theme == null) {
            // 未知主题名称，打印警告并降级
            err.printf("[Banner] 未知主题 '%s'，已回退到 default 主题。" +
                    "可选值：default / minimal / box / colorful%n", themeName);
            return REGISTRY.get(DEFAULT_THEME);
        }

        return theme;
    }

    /**
     * 注册自定义主题（供外部扩展使用）
     *
     * <p>可在应用启动前调用此方法注入自定义渲染器：
     * <pre>
     * BannerThemeFactory.register("my-theme", new MyBannerTheme());
     * </pre>
     *
     * @param name  主题名称（会被转为小写存储）
     * @param theme 主题渲染器实例
     */
    public static void register(String name, BannerTheme theme) {
        if (name == null || name.isBlank() || theme == null) {
            throw new IllegalArgumentException("主题名称和渲染器实例均不能为空");
        }
        REGISTRY.put(name.trim().toLowerCase(), theme);
    }

    /**
     * 获取所有已注册的主题名称（用于配置提示或诊断）
     *
     * @return 主题名称集合的字符串，如 "[default, minimal, box, colorful]"
     */
    public static String availableThemes() {
        return REGISTRY.keySet().toString();
    }
}