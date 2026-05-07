package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;

/**
 * Colorful 主题渲染器
 *
 * <p>彩色增强版：每行信息使用不同颜色区分，整体更活泼鲜明，
 * 适合个人项目或追求视觉体验的开发环境。
 *
 * <p>颜色分配规则：
 * <ul>
 *   <li>时间 / PID 相关 → 亮黄色（93）</li>
 *   <li>网络 / 地址相关 → 亮青色（96）</li>
 *   <li>环境 / 应用相关 → 亮绿色（92）</li>
 *   <li>版本相关        → 亮蓝色（94）</li>
 *   <li>访问地址        → 亮紫色（95）</li>
 * </ul>
 *
 * @author mallcloud
 */
public class ColorfulBannerTheme implements BannerTheme {

    private static final String SEP = "✦ " + "─".repeat(50) + " ✦";

    // 各类信息对应的 ANSI 高亮色号
    private static final int COLOR_TIME    = 93; // 亮黄
    private static final int COLOR_NETWORK = 96; // 亮青
    private static final int COLOR_APP     = 92; // 亮绿
    private static final int COLOR_VERSION = 94; // 亮蓝
    private static final int COLOR_URL     = 95; // 亮紫
    private static final int COLOR_SEP     = 36; // 青色分隔线

    @Override
    public void render(PrintStream out, BannerContext ctx, BannerProperties props, String asciiTitle, AnsiHelper ansi) {
        BannerProperties.ColorConfig c = props.getColor();
        BannerProperties.ShowConfig  s = props.getShow();

        // ---------- 顶部分隔线与标题 ----------
        out.println(ansi.colorize(SEP, COLOR_SEP));
        out.println(ansi.colorize(asciiTitle, c.getTitle()));
        out.println(ansi.colorize(SEP, COLOR_SEP));

        // ---------- 时间与进程信息（亮黄） ----------
        String timeVal = "🕐 " + ctx.getStartTime();
        if (s.isCost() && ctx.getStartupCost() != null) {
            timeVal += "  ⏱ 耗时 " + ctx.getStartupCost();
        }
        out.println(line("启动时间", timeVal, COLOR_TIME, ansi));

        if (s.isPid()) {
            out.println(line("进程 PID", "🔢 " + ctx.getPid(), COLOR_TIME, ansi));
        }

        // ---------- 网络信息（亮青） ----------
        out.println(line("主机地址", "🌐 " + ctx.getHostAddress(), COLOR_NETWORK, ansi));
        out.println(line("服务端口", "🚀 " + ctx.getServerPort(),  COLOR_NETWORK, ansi));

        if (s.isContextPath() && ctx.getContextPath() != null && !ctx.getContextPath().isEmpty()) {
            out.println(line("访问路径", "🔗 " + ctx.getContextPath(), COLOR_NETWORK, ansi));
        }

        // ---------- 应用信息（亮绿） ----------
        out.println(line("运行环境", "⚙  " + ctx.getProfiles(), COLOR_APP, ansi));
        out.println(line("应用名称", "📦 " + ctx.getAppName(),  COLOR_APP, ansi));

        if (s.isAuthor()) {
            out.println(line("项目作者", "👤 " + ctx.getAuthor(), COLOR_APP, ansi));
        }
        if (s.isDescription()) {
            out.println(line("服务描述", "📝 " + ctx.getDescription(), COLOR_APP, ansi));
        }

        // ---------- 版本信息（亮蓝） ----------
        out.println(line("业务版本", "🏷  " + ctx.getVersion(), COLOR_VERSION, ansi));
        out.println(line("框架版本",
                String.format("☕ Java/%-6s  🌿 Spring/%-6s  🍃 Boot/%s",
                        ctx.getJavaVersion(), ctx.getSpringVersion(), ctx.getSpringBootVersion()),
                COLOR_VERSION, ansi));

        // ---------- 数据库（亮蓝，按需） ----------
        if (s.isDbUrl() && ctx.getDbUrl() != null && !ctx.getDbUrl().isEmpty()) {
            out.println(line("数据库  ", "🗄  " + ctx.getDbUrl(), COLOR_VERSION, ansi));
        }

        // ---------- 访问地址（亮紫） ----------
        out.println(line("健康检查", "🔍 " + ctx.getHealthUrl(), COLOR_URL, ansi));
        out.println(line("接口文档", "📖 " + ctx.getSwaggerUrl(), COLOR_URL, ansi));

        // ---------- 底部分隔线 ----------
        out.println(ansi.colorize(SEP, COLOR_SEP));
    }

    /**
     * 构建带颜色的信息行
     *
     * @param label     标签
     * @param value     值
     * @param colorCode 该行使用的 ANSI 色号
     * @param ansi      ANSI 工具
     * @return 格式化后的完整行
     */
    private String line(String label, String value, int colorCode, AnsiHelper ansi) {
        return ansi.colorize(String.format("  %-8s : %s", label, value), colorCode);
    }
}