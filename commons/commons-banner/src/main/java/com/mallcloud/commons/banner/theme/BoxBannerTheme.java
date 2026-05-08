package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Box 主题渲染器
 *
 * <p>Unicode 边框风格，使用 Box Drawing 字符绘制完整闭合边框。
 *
 * <p>核心难点：终端字符对齐依赖"显示宽度"而非"字符数量"。
 * ASCII 字符显示宽度为 1，中文/全角字符显示宽度为 2，
 * 必须用 {@link #displayWidth(String)} 精确计算才能保证右边框对齐。
 *
 * <p>输出示例：
 * <pre>
 * ╔══════════════════════════════════════════════════════╗
 * ║  ADMIN-SERVER                                        ║
 * ╠══════════════════════════════════════════════════════╣
 * ║  ADMIN-SERVER                                        ║
 * ║  ADMIN-SERVER                                        ║
 * ║  ADMIN-SERVER                                        ║
 * ╚══════════════════════════════════════════════════════╝
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

    /**
     * 内容区显示宽度（不含左右边框字符 ║ 各占 1 列）。
     *
     * <p>此宽度基于"终端显示列数"计算，与字符串 length() 无关。
     * 例如 "启动时间" 有 4 个汉字，length()=4，但显示宽度=8。
     * 所有填充和截断均以显示宽度为准，才能保证右边框 ║ 垂直对齐。
     */
    private static final int BOX_WIDTH = 62;

    @Override
    public void render(PrintStream out,
                       BannerContext ctx,
                       BannerProperties props,
                       String asciiTitle,
                       AnsiHelper ansi) {

        BannerProperties.ColorConfig c = props.getColor();
        BannerProperties.ShowConfig  s = props.getShow();

        // ---------- 收集所有信息行 [label, value] ----------
        List<String[]> rows = buildRows(ctx, s);

        // ---------- 计算实际需要的盒子宽度 ----------
        // 自动适配内容宽度，避免硬编码宽度不够或过宽
        int contentWidth = calcBoxWidth(rows, asciiTitle);

        // ---------- 生成边框行 ----------
        String topLine    = TL + H.repeat(contentWidth) + TR;
        String midLine    = ML + H.repeat(contentWidth) + MR;
        String bottomLine = BL + H.repeat(contentWidth) + BR;

        // ---------- 顶部边框 ----------
        out.println(ansi.colorize(topLine, c.getSeparator()));

        // ---------- ASCII Art 标题区 ----------
        // asciiTitle 可能包含多行，逐行处理，每行单独做宽度填充
        for (String titleLine : asciiTitle.split("\n")) {
            String padded = padToWidth(" " + titleLine, contentWidth);
            out.println(
                    ansi.colorize(V, c.getSeparator()) +
                            ansi.colorize(padded, c.getTitle()) +
                            ansi.colorize(V, c.getSeparator())
            );
        }

        // ---------- 标题与信息区之间的分隔线 ----------
        out.println(ansi.colorize(midLine, c.getSeparator()));

        // ---------- 信息行 ----------
        for (String[] row : rows) {
            String label   = row[0];
            String value   = row[1];

            // 构造内容：两个前导空格 + 标签 + 空格 + │ + 空格 + 值
            // 注意：label 含中文，必须用 displayWidth 而非 length 来对齐
            String content = "  " + label + " " + DIV + " " + value;

            // 若内容超出盒子宽度则截断（保留 "..." 结尾）
            content = truncateToWidth(content, contentWidth);

            // 右侧填充空格使显示宽度恰好等于 contentWidth
            String padded = padToWidth(content, contentWidth);

            out.println(
                    ansi.colorize(V, c.getSeparator()) +
                            ansi.colorize(padded, c.getValue()) +
                            ansi.colorize(V, c.getSeparator())
            );
        }

        // ---------- 底部边框 ----------
        out.println(ansi.colorize(bottomLine, c.getSeparator()));
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 收集所有需要展示的信息行
     *
     * @param ctx 上下文数据
     * @param s   显示开关配置
     * @return 行列表，每项为 [label, value]
     */
    private List<String[]> buildRows(BannerContext ctx, BannerProperties.ShowConfig s) {
        List<String[]> rows = new ArrayList<>();

        // 启动时间（按需附加耗时）
        String timeVal = ctx.getStartTime();
        if (s.isCost() && ctx.getStartupCost() != null) {
            timeVal += "  (耗时 " + ctx.getStartupCost() + ")";
        }
        rows.add(new String[]{"启动时间", timeVal});

        if (s.isPid()) {
            rows.add(new String[]{"进程编号", ctx.getPid()});
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
        rows.add(new String[]{"框架版本",
                String.format("Java/%-6s  Spring/%-6s  Boot/%s",
                        ctx.getJavaVersion(), ctx.getSpringVersion(), ctx.getSpringBootVersion())});

        if (s.isDbUrl() && ctx.getDbUrl() != null && !ctx.getDbUrl().isEmpty()) {
            rows.add(new String[]{"数据库址", ctx.getDbUrl()});
        }

        rows.add(new String[]{"健康检查", ctx.getHealthUrl()});
        rows.add(new String[]{"接口文档", ctx.getSwaggerUrl()});

        return rows;
    }

    /**
     * 动态计算盒子内容区所需的最小宽度
     *
     * <p>遍历所有行和标题行，取最大显示宽度，再加上左右各 2 列边距，
     * 保证任何内容都不会超出边框，也不会因宽度过大留白太多。
     *
     * @param rows       信息行列表
     * @param asciiTitle ASCII Art 标题字符串（可能多行）
     * @return 内容区显示宽度（已加边距）
     */
    private int calcBoxWidth(List<String[]> rows, String asciiTitle) {
        int max = BOX_WIDTH; // 最小宽度兜底

        // 检查标题各行
        for (String line : asciiTitle.split("\n")) {
            // 标题行前缀 " " 占 1 列
            max = Math.max(max, displayWidth(line) + 1 + 2); // +2 作为右边距
        }

        // 检查信息行："  " + label + " │ " + value
        for (String[] row : rows) {
            // "  " = 2，" │ " = 3，标签与值各按显示宽度计算
            int lineWidth = 2 + displayWidth(row[0]) + 3 + displayWidth(row[1]) + 2;
            max = Math.max(max, lineWidth);
        }

        return max;
    }

    /**
     * 将字符串右侧填充空格，使其显示宽度精确等于 targetWidth
     *
     * <p>这是 Box 主题对齐的核心方法。
     * 终端渲染时，右边框 ║ 的列位置 = 左边框列位置 + 1(║) + contentWidth + 1(║)，
     * 只有每行内容的显示宽度严格等于 contentWidth，右边框才能垂直对齐。
     *
     * @param str         原始字符串
     * @param targetWidth 目标显示宽度
     * @return 填充后的字符串
     */
    private String padToWidth(String str, int targetWidth) {
        if (str == null) str = "";
        int current = displayWidth(str);
        int padding = Math.max(0, targetWidth - current);
        return str + " ".repeat(padding);
    }

    /**
     * 将字符串截断至不超过 maxWidth 的显示宽度，超出部分替换为 "..."
     *
     * <p>截断时同样以显示宽度为准，避免在中文字符中间截断导致宽度计算错误。
     *
     * @param str      原始字符串
     * @param maxWidth 最大允许显示宽度
     * @return 截断后的字符串
     */
    private String truncateToWidth(String str, int maxWidth) {
        if (displayWidth(str) <= maxWidth) {
            return str;
        }
        // 逐字符累加宽度，找到不超过 (maxWidth - 3) 的截断点
        // 预留 3 列给 "..."
        StringBuilder sb = new StringBuilder();
        int accumulated = 0;
        for (int i = 0; i < str.length(); ) {
            int cp = str.codePointAt(i);
            int w  = charDisplayWidth(cp);
            if (accumulated + w > maxWidth - 3) break;
            sb.appendCodePoint(cp);
            accumulated += w;
            i += Character.charCount(cp);
        }
        return sb + "...";
    }

    /**
     * 计算字符串在等宽字体终端中的实际显示宽度（列数）
     *
     * <p>等宽终端（如 iTerm2、IDEA 控制台、Linux 终端）的字符宽度规则：
     * <ul>
     *   <li>ASCII 可打印字符（U+0020~U+007E）：宽度 = 1</li>
     *   <li>中文、日文、韩文（CJK）：宽度 = 2</li>
     *   <li>全角拉丁字母、全角标点（U+FF01~U+FF60）：宽度 = 2</li>
     *   <li>其他宽字符块（如中日韩兼容区）：宽度 = 2</li>
     *   <li>控制字符、零宽字符：宽度 = 0（忽略）</li>
     * </ul>
     *
     * <p>此方法遍历字符串的每个 Unicode 码点（codePoint），
     * 用 {@link #charDisplayWidth(int)} 逐一计算并累加，
     * 正确处理 BMP 之外的增补字符（如 Emoji）。
     *
     * @param str 任意字符串
     * @return 终端显示宽度（列数）
     */
    public static int displayWidth(String str) {
        if (str == null || str.isEmpty()) return 0;
        int width = 0;
        // 使用 codePoints() 而非 chars()，正确处理 Unicode 增补平面字符
        for (int i = 0; i < str.length(); ) {
            int cp = str.codePointAt(i);
            width += charDisplayWidth(cp);
            i += Character.charCount(cp); // 增补字符占两个 char，需跳过 2
        }
        return width;
    }

    /**
     * 计算单个 Unicode 码点的终端显示宽度
     *
     * <p>依据 Unicode East Asian Width 标准（UAX #11）和实际终端行为：
     * <ul>
     *   <li>W（Wide）和 F（Fullwidth）类字符：宽度 = 2</li>
     *   <li>其余可打印字符：宽度 = 1</li>
     *   <li>控制字符 / 不可打印字符：宽度 = 0</li>
     * </ul>
     *
     * @param codePoint Unicode 码点
     * @return 0、1 或 2
     */
    private static int charDisplayWidth(int codePoint) {
        // 控制字符和不可打印字符：宽度为 0
        if (codePoint < 0x20 || codePoint == 0x7F) return 0;
        // ASCII 可打印字符（空格到 ~）：宽度为 1
        if (codePoint < 0x7F) return 1;

        // ── 以下均为宽度 2 的双宽字符范围 ──────────────────────────────────

        // 中日韩统一表意文字基本区（最常用的汉字）
        if (codePoint >= 0x4E00 && codePoint <= 0x9FFF) return 2;
        // 中日韩统一表意文字扩展 A 区
        if (codePoint >= 0x3400 && codePoint <= 0x4DBF) return 2;
        // 中日韩统一表意文字扩展 B 区（增补平面）
        if (codePoint >= 0x20000 && codePoint <= 0x2A6DF) return 2;
        // 中日韩兼容表意文字
        if (codePoint >= 0xF900 && codePoint <= 0xFAFF) return 2;
        // 中日韩符号和标点（书名号《》、间隔号·等）
        if (codePoint >= 0x3000 && codePoint <= 0x303F) return 2;
        // 平假名
        if (codePoint >= 0x3040 && codePoint <= 0x309F) return 2;
        // 片假名
        if (codePoint >= 0x30A0 && codePoint <= 0x30FF) return 2;
        // 全角拉丁字母和全角标点（！～之间）
        if (codePoint >= 0xFF01 && codePoint <= 0xFF60) return 2;
        // 全角人民币符号和全角破折号等
        if (codePoint >= 0xFFE0 && codePoint <= 0xFFE6) return 2;
        // 朝鲜语音节
        if (codePoint >= 0xAC00 && codePoint <= 0xD7AF) return 2;
        // 中日韩兼容区（旧字形、竖排专用等）
        if (codePoint >= 0xFE10 && codePoint <= 0xFE1F) return 2;
        if (codePoint >= 0xFE30 && codePoint <= 0xFE4F) return 2;
        // Emoji 及其他宽字符（杂项符号、表情符号等）
        if (codePoint >= 0x1F300 && codePoint <= 0x1F9FF) return 2;
        if (codePoint >= 0x2600  && codePoint <= 0x26FF)  return 2;
        if (codePoint >= 0x2700  && codePoint <= 0x27BF)  return 2;

        // 其余字符（拉丁扩展、西里尔、阿拉伯等）按宽度 1 处理
        return 1;
    }
}