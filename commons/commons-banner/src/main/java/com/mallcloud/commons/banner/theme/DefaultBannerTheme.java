package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;

/**
 * Default 主题渲染器
 *
 * <p>标准风格：带分隔线 + Emoji + 彩色标签，适合本地开发环境。
 *
 * <p>输出示例：
 * <pre>
 * ===============================================
 *  ADMIN-SERVER ASCII ART
 * ===============================================
 *   启动时间 : 🕐 2026-05-07 14:01:15  (耗时 3.21 s)
 *   进程 PID : 🔢 12345
 *   主机地址 : 🌐 192.168.1.3
 *   ...
 * ===============================================
 * </pre>
 *
 * @author mallcloud
 */
public class DefaultBannerTheme implements BannerTheme {

    /** 分隔线字符串 */
    private static final String SEP = "=".repeat(54);

    @Override
    public void render(PrintStream out, BannerContext ctx, BannerProperties props, String asciiTitle, AnsiHelper ansi) {

        BannerProperties.ColorConfig c = props.getColor();
        BannerProperties.ShowConfig  s = props.getShow();

        // ---------- 分隔线与标题 ----------
        out.println(ansi.colorize(SEP, c.getSeparator()));
        out.println(ansi.colorize(asciiTitle, c.getTitle()));
        out.println(ansi.colorize(SEP, c.getSeparator()));

        // ---------- 启动时间（含耗时） ----------
        String timeValue = "🕐 " + ctx.getStartTime();
        if (s.isCost() && ctx.getStartupCost() != null) {
            timeValue += ansi.colorize("  (耗时 " + ctx.getStartupCost() + ")", 90);
        }
        out.println(line("启动时间", timeValue, c, ansi));

        // ---------- PID ----------
        if (s.isPid()) {
            out.println(line("进程 PID", "🔢 " + ctx.getPid(), c, ansi));
        }

        // ---------- 网络信息 ----------
        out.println(line("主机地址", "🌐 " + ctx.getHostAddress(), c, ansi));
        out.println(line("服务端口", "🚀 " + ctx.getServerPort(), c, ansi));

        // ---------- Context Path（按需显示） ----------
        if (s.isContextPath() && ctx.getContextPath() != null && !ctx.getContextPath().isEmpty()) {
            out.println(line("访问路径", "🔗 " + ctx.getContextPath(), c, ansi));
        }

        // ---------- 环境与应用 ----------
        out.println(line("运行环境", "⚙  " + ctx.getProfiles(), c, ansi));
        out.println(line("应用名称", "📦 " + ctx.getAppName(), c, ansi));

        if (s.isAuthor()) {
            out.println(line("项目作者", "👤 " + ctx.getAuthor(), c, ansi));
        }
        if (s.isDescription()) {
            out.println(line("服务描述", "📝 " + ctx.getDescription(), c, ansi));
        }

        // ---------- 版本信息 ----------
        out.println(line("业务版本", "🏷  " + ctx.getVersion(), c, ansi));
        out.println(line("框架版本", String.format("☕ Java/%-6s  🌿 Spring/%-6s  🍃 Boot/%s",
                ctx.getJavaVersion(), ctx.getSpringVersion(), ctx.getSpringBootVersion()), c, ansi));

        // ---------- 数据库地址（按需显示，已脱敏） ----------
        if (s.isDbUrl() && ctx.getDbUrl() != null && !ctx.getDbUrl().isEmpty()) {
            out.println(line("数据库  ", "🗄  " + ctx.getDbUrl(), c, ansi));
        }

        // ---------- 访问地址 ----------
        out.println(line("健康检查", "🔍 " + ctx.getHealthUrl(), c, ansi));
        out.println(line("接口文档", "📖 " + ctx.getSwaggerUrl(), c, ansi));

        // ---------- 结尾分隔线 ----------
        out.println(ansi.colorize(SEP, c.getSeparator()));
    }

    /**
     * 构建一行输出，标签与值分别着色
     *
     * @param label  标签文字
     * @param value  值内容
     * @param c      颜色配置
     * @param ansi   ANSI 工具
     * @return 格式化后的完整行字符串
     */
    private String line(String label, String value, BannerProperties.ColorConfig c, AnsiHelper ansi) {
        // 标签固定 8 字符宽度保证对齐
        String labelPart = ansi.colorize(String.format("  %-8s", label), c.getLabel());
        String valuePart = ansi.colorize(value, c.getValue());
        return labelPart + " : " + valuePart;
    }
}