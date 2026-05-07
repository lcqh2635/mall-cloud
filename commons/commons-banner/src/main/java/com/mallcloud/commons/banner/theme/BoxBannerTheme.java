package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Box 主题渲染器
 *
 * <p>Unicode 边框风格，使用 Box Drawing 字符绘制边框，
 * 在支持 UTF-8 的终端中视觉层次更清晰，适合本地开发。
 *
 * <p>输出示例：
 * <pre>
 * ╔══════════════════════════════════════════════════╗
 * ║   ADMIN-SERVER ASCII ART                        ║
 * ╠══════════════════════════════════════════════════╣
 * ║  启动时间 │ 2026-05-07 14:01:15  (耗时 3.21 s)  ║
 * ║  进程 PID │ 12345                                ║
 * ║  主机地址 │ 192.168.1.3                          ║
 * ║  ...                                            ║
 * ╚══════════════════════════════════════════════════╝
 * </pre>
 *
 * @author mallcloud
 */
public class BoxBannerTheme implements BannerTheme {
    // ===================== Unicode 边框字符 =====================
    private static final String TL  = "╔"; // 左上角
    private static final String TR  = "╗"; // 右上角
    private static final String BL  = "╚"; // 左下角
    private static final String BR  = "╝"; // 右下角
    private static final String H   = "═"; // 水平线
    private static final String V   = "║"; // 垂直线
    private static final String ML  = "╠"; // 左侧中间连接
    private static final String MR  = "╣"; // 右侧中间连接
    private static final String DIV = "│"; // 内部分隔符

    /** 内容区宽度（不含左右边框字符），需能容纳最长的一行 */
    private static final int BOX_WIDTH = 56;

    @Override
    public void render(PrintStream out, BannerContext ctx, BannerProperties props, String asciiTitle, AnsiHelper ansi) {
        BannerProperties.ColorConfig c = props.getColor();
        BannerProperties.ShowConfig  s = props.getShow();

        // ---------- 收集所有需要展示的行 ----------
        List<String[]> rows = new ArrayList<>(); // 每个元素为 [label, value]

        // 启动时间（含耗时）
        String timeVal = ctx.getStartTime();
        if (s.isCost() && ctx.getStartupCost() != null) {
            timeVal += "  (耗时 " + ctx.getStartupCost() + ")";
        }
        rows.add(new String[]{"启动时间", timeVal});

        if (s.isPid()) {
            rows.add(new String[]{"进程 PID", ctx.getPid()});
        }

        rows.add(new String[]{"主机地址", ctx.getHostAddress()});
        rows.add(new String[]{"服务端口", ctx.getServerPort()});

        if (s.isContextPath() && ctx.getContextPath() != null && !ctx.getContextPath().isEmpty()) {
            rows.add(new String[]{"访问路径", ctx.getContextPath()});
        }

        rows.add(new String[]{"运行环境", ctx.getProfiles()});
        rows.add(new String[]{"应用名称", ctx.getAppName()});

        if (s.isAuthor()) {
            rows.add(new String[]{"项目作者", ctx.getAuthor()});
        }
        if (s.isDescription()) {
            rows.add(new String[]{"服务描述", ctx.getDescription()});
        }

        rows.add(new String[]{"业务版本", ctx.getVersion()});
        rows.add(new String[]{"框架版本", String.format("Java/%-6s  Spring/%-6s  Boot/%s",
                ctx.getJavaVersion(), ctx.getSpringVersion(), ctx.getSpringBootVersion())});

        if (s.isDbUrl() && ctx.getDbUrl() != null && !ctx.getDbUrl().isEmpty()) {
            rows.add(new String[]{"数据库  ", ctx.getDbUrl()});
        }

        rows.add(new String[]{"健康检查", ctx.getHealthUrl()});
        rows.add(new String[]{"接口文档", ctx.getSwaggerUrl()});

        // ---------- 绘制边框 ----------
        String topLine    = TL + H.repeat(BOX_WIDTH) + TR;
        String midLine    = ML + H.repeat(BOX_WIDTH) + MR;
        String bottomLine = BL + H.repeat(BOX_WIDTH) + BR;

        // 顶部边框
        out.println(ansi.colorize(topLine, c.getSeparator()));

        // ASCII Art 标题区（多行标题逐行放入边框内）
        for (String titleLine : asciiTitle.split("\n")) {
            out.println(ansi.colorize(V, c.getSeparator())
                    + ansi.colorize(padRight(" " + titleLine, BOX_WIDTH), c.getTitle())
                    + ansi.colorize(V, c.getSeparator()));
        }

        // 标题与信息区之间的分隔线
        out.println(ansi.colorize(midLine, c.getSeparator()));

        // 信息行
        for (String[] row : rows) {
            String label = row[0];
            String value = row[1];
            // 格式：║  标签  │  值  ║
            String content = String.format("  %-8s %s %-35s", label, DIV, value);
            // 超出宽度截断，保证边框对齐
            if (content.length() > BOX_WIDTH) {
                content = content.substring(0, BOX_WIDTH - 3) + "...";
            }
            out.println(ansi.colorize(V, c.getSeparator())
                    + ansi.colorize(padRight(content, BOX_WIDTH), c.getValue())
                    + ansi.colorize(V, c.getSeparator()));
        }

        // 底部边框
        out.println(ansi.colorize(bottomLine, c.getSeparator()));
    }

    /**
     * 将字符串右侧填充空格到指定长度
     *
     * <p>注意：中文字符占 2 个显示宽度，此处做简单处理，
     * 若标签含中文较多可能导致轻微错位，属已知限制。
     *
     * @param str    原始字符串
     * @param width  目标显示宽度
     * @return 填充后的字符串
     */
    private String padRight(String str, int width) {
        if (str == null) str = "";
        // 估算中文字符数量（每个中文额外占 1 个宽度）
        long chineseCount = str.chars()
                .filter(ch -> ch >= 0x4E00 && ch <= 0x9FFF)
                .count();
        int displayLen = str.length() + (int) chineseCount;
        int padding = Math.max(0, width - displayLen);
        return str + " ".repeat(padding);
    }
}