package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 盒子边框渲染工具类
 *
 * <p>提供统一的 Unicode 闭合边框绘制能力，供所有 {@link BannerTheme} 实现复用。
 *
 * <p><b>核心设计原则：</b>
 * <ul>
 *   <li>宽度计算以"终端可见字符的显示列数"为准，而非字符串的 {@code length()}</li>
 *   <li>ANSI 转义码（颜色控制序列）在终端中不占显示列，计算宽度前必须先剥离</li>
 *   <li>中文、全角字符占 2 列，ASCII 字符占 1 列，控制字符占 0 列</li>
 *   <li>盒子宽度由内容动态决定，无需硬编码</li>
 * </ul>
 *
 * <p><b>行类型说明：</b>
 * <ul>
 *   <li>{@code TITLE}     — ASCII Art 标题行，标题区结束后自动插入分隔线</li>
 *   <li>{@code ROW}       — 普通信息行，格式：{@code ║  label  │  value  ║}</li>
 *   <li>{@code RAW_ROW}   — 原始内容行，内容已由调用方完整着色，BoxRenderer
 *                           只负责加边框和右侧填充，<b>不再插入额外的 │ 分隔符</b></li>
 *   <li>{@code SEPARATOR} — 水平分隔线：{@code ╠═══╣}</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * new BoxRenderer(ansi, borderColor, titleColor, contentColor)
 *     .title(asciiTitle)
 *     .row("启动时间", "2026-05-09 09:40:36")   // 普通行，BoxRenderer 负责着 contentColor
 *     .rawRow("🌐 已着色内容字符串")             // 原始行，调用方自行控制颜色
 *     .separator()
 *     .row("健康检查", "http://...")
 *     .render(out);
 * </pre>
 *
 * @author mallcloud
 */
public class BoxRenderer {

    // ===================== Unicode 边框字符 =====================
    private static final String TL  = "╔"; // 左上角
    private static final String TR  = "╗"; // 右上角
    private static final String BL  = "╚"; // 左下角
    private static final String BR  = "╝"; // 右下角
    private static final String H   = "═"; // 水平线
    private static final String V   = "║"; // 垂直线
    private static final String ML  = "╠"; // 左侧中间连接符
    private static final String MR  = "╣"; // 右侧中间连接符
    private static final String DIV = "│"; // 标签与值之间的分隔符（仅 ROW 类型使用）

    /** 盒子内容区最小显示宽度，防止内容过少时盒子过窄 */
    private static final int MIN_WIDTH = 50;
    /** 内容区左右各留的边距列数 */
    private static final int PADDING   = 2;

    // ===================== 行类型常量 =====================
    private static final int TYPE_TITLE     = 0; // ASCII Art 标题行
    private static final int TYPE_ROW       = 1; // 普通信息行（含内部 │ 分隔符）
    private static final int TYPE_RAW_ROW   = 2; // 原始内容行（无内部 │，内容已由外部完整构造）
    private static final int TYPE_SEPARATOR = 3; // 水平分隔线

    // ===================== 实例字段 =====================

    /** 已添加的所有行，按添加顺序存储 */
    private final List<Line> lines = new ArrayList<>();

    private final AnsiHelper ansi;
    /** 边框和分隔线颜色 ANSI 色号 */
    private final int borderColor;
    /** ASCII Art 标题颜色 ANSI 色号 */
    private final int titleColor;
    /** 普通信息行（TYPE_ROW）的内容颜色 ANSI 色号 */
    private final int contentColor;

    /**
     * 构造盒子渲染器
     *
     * @param ansi         ANSI 颜色工具
     * @param borderColor  边框颜色（如 32=绿色）
     * @param titleColor   标题颜色（如 96=亮青色）
     * @param contentColor 普通信息行内容颜色（如 33=黄色）
     */
    public BoxRenderer(AnsiHelper ansi, int borderColor, int titleColor, int contentColor) {
        this.ansi         = ansi;
        this.borderColor  = borderColor;
        this.titleColor   = titleColor;
        this.contentColor = contentColor;
    }

    // ===================== 链式 API =====================

    /**
     * 添加标题区（ASCII Art，支持多行，以 {@code \n} 分隔）
     *
     * <p>标题区末尾会自动插入一条分隔线，将标题与信息区隔开，无需手动调用。
     *
     * @param asciiTitle ASCII Art 字符串
     * @return this（链式调用）
     */
    public BoxRenderer title(String asciiTitle) {
        if (asciiTitle == null || asciiTitle.isBlank()) return this;
        for (String line : asciiTitle.split("\n")) {
            if (!line.isBlank()) {
                // value 存标题行文字，label 不使用
                lines.add(new Line(TYPE_TITLE, "", line.stripTrailing()));
            }
        }
        // 标题区结束后自动插入分隔线
        lines.add(new Line(TYPE_SEPARATOR, "", ""));
        return this;
    }

    /**
     * 添加普通信息行
     *
     * <p>BoxRenderer 负责在内部插入 {@code │} 分隔符，并对整行着 {@code contentColor} 颜色。
     * <p>行格式：{@code ║  label  │  value  [填充]  ║}
     *
     * @param label 标签（支持中文）
     * @param value 值内容
     * @return this（链式调用）
     */
    public BoxRenderer row(String label, String value) {
        lines.add(new Line(TYPE_ROW,
                label == null ? "" : label,
                value == null ? "" : value));
        return this;
    }

    /**
     * 添加原始内容行（Raw Row）
     *
     * <p>与 {@link #row(String, String)} 的区别：
     * <ul>
     *   <li>调用方已将标签、分隔符、值完整拼好并自行控制颜色</li>
     *   <li>BoxRenderer <b>不会</b>再插入 {@code │} 分隔符</li>
     *   <li>BoxRenderer <b>不会</b>再对内容着色</li>
     *   <li>BoxRenderer 只负责：① 加左右边框 ② 计算可见宽度 ③ 右侧填充空格</li>
     * </ul>
     *
     * <p>适用场景：{@link DefaultBannerTheme}、{@link ColorfulBannerTheme} 等
     * 需要对标签和值分别着不同颜色的主题。
     *
     * <p><b>重要：</b>传入的字符串中可以含有 ANSI 转义码，
     * BoxRenderer 会在计算宽度前先调用 {@link #stripAnsi(String)} 剥离转义码，
     * 保证宽度计算只基于可见字符，不受颜色控制序列影响。
     *
     * @param coloredContent 已着色的完整行内容字符串
     * @return this（链式调用）
     */
    public BoxRenderer rawRow(String coloredContent) {
        lines.add(new Line(TYPE_RAW_ROW, "", coloredContent == null ? "" : coloredContent));
        return this;
    }

    /**
     * 在当前位置插入水平分隔线（{@code ╠═══╣}）
     *
     * @return this（链式调用）
     */
    public BoxRenderer separator() {
        lines.add(new Line(TYPE_SEPARATOR, "", ""));
        return this;
    }

    /**
     * 执行渲染，将完整盒子输出到 PrintStream
     *
     * <p>渲染步骤：
     * <ol>
     *   <li>遍历所有行，计算可见宽度最大值，确定盒子内容区宽度</li>
     *   <li>输出顶部边框</li>
     *   <li>逐行输出：边框 + 内容（右侧精确填充空格）+ 边框</li>
     *   <li>输出底部边框</li>
     * </ol>
     *
     * @param out Spring Boot 传入的输出流
     */
    public void render(PrintStream out) {
        // 第一步：确定盒子内容区的显示宽度
        int contentWidth = calcContentWidth();

        // 第二步：构造三种固定边框行
        String top    = TL + H.repeat(contentWidth) + TR;
        String mid    = ML + H.repeat(contentWidth) + MR;
        String bottom = BL + H.repeat(contentWidth) + BR;

        // 第三步：输出顶部边框
        out.println(ansi.colorize(top, borderColor));

        // 第四步：逐行渲染
        for (Line line : lines) {
            switch (line.type) {
                case TYPE_TITLE     -> renderTitleLine(out, line.value, contentWidth);
                case TYPE_ROW       -> renderRowLine(out, line.label, line.value, contentWidth);
                case TYPE_RAW_ROW   -> renderRawRowLine(out, line.value, contentWidth);
                case TYPE_SEPARATOR -> out.println(ansi.colorize(mid, borderColor));
            }
        }

        // 第五步：输出底部边框
        out.println(ansi.colorize(bottom, borderColor));
    }

    // ===================== 私有渲染方法 =====================

    /**
     * 渲染标题行
     * <p>格式：{@code ║ [titleLine 内容] [填充至 contentWidth] ║}
     */
    private void renderTitleLine(PrintStream out, String titleLine, int contentWidth) {
        String content = " " + titleLine; // 标题左侧留一个空格作为视觉间距
        String padded  = padToWidth(content, contentWidth);
        out.println(
                ansi.colorize(V, borderColor) +
                        ansi.colorize(padded, titleColor) +
                        ansi.colorize(V, borderColor)
        );
    }

    /**
     * 渲染普通信息行（含内部 │ 分隔符）
     *
     * <p>格式：{@code ║  label  │  value  [填充]  ║}
     * <p>标签与值均由 BoxRenderer 统一着 {@code contentColor} 颜色。
     */
    private void renderRowLine(PrintStream out, String label, String value, int contentWidth) {
        // 拼接可见内容：左边距 + 标签 + 左边距 + │ + 空格 + 值
        String visibleContent = " ".repeat(PADDING) + label
                + " ".repeat(PADDING) + DIV + " " + value;
        // 超出宽度时截断
        String content = truncateToWidth(visibleContent, contentWidth);
        // 右侧填充空格，使可见宽度精确等于 contentWidth
        String padded  = padToWidth(content, contentWidth);
        out.println(
                ansi.colorize(V, borderColor) +
                        ansi.colorize(padded, contentColor) +
                        ansi.colorize(V, borderColor)
        );
    }

    /**
     * 渲染原始内容行（不插入额外 │，不重复着色）
     *
     * <p>格式：{@code ║  [调用方传入的着色内容]  [填充]  ║}
     *
     * <p>宽度计算关键：传入内容含 ANSI 转义码，必须先用 {@link #stripAnsi(String)}
     * 剥离后再计算可见宽度，否则转义码中的 ASCII 字符会被误计入宽度，
     * 导致填充空格不足，右边框错位。
     */
    private void renderRawRowLine(PrintStream out, String coloredContent, int contentWidth) {
        // 在着色内容左侧加上边距空格（边距本身不着色，保持终端背景色）
        String withPadding = " ".repeat(PADDING) + coloredContent;
        // 计算可见宽度：剥离 ANSI 转义码后再统计显示列数
        int visibleWidth = displayWidth(stripAnsi(withPadding));
        // 右侧填充空格至 contentWidth
        int padding = Math.max(0, contentWidth - visibleWidth);
        String padded = withPadding + " ".repeat(padding);
        out.println(
                ansi.colorize(V, borderColor) +
                        padded +  // 内容已由外部着色，直接输出，不再包裹颜色
                        ansi.colorize(V, borderColor)
        );
    }

    // ===================== 宽度计算 =====================

    /**
     * 计算盒子内容区所需的最小显示宽度
     *
     * <p>遍历所有行，对每种类型分别计算其可见显示宽度，取最大值，
     * 再与 {@link #MIN_WIDTH} 取最大值作为最终宽度。
     *
     * <p><b>RAW_ROW 的处理：</b>必须先剥离 ANSI 转义码再计算宽度，
     * 否则转义序列中的字母和符号会被误计入，导致宽度虚高。
     */
    private int calcContentWidth() {
        int max = MIN_WIDTH;
        for (Line line : lines) {
            int w = switch (line.type) {
                // 标题行：1（左空格） + 标题可见宽度 + PADDING（右边距）
                case TYPE_TITLE ->
                        1 + displayWidth(line.value) + PADDING;
                // 普通行：PADDING + 标签 + PADDING + "│ " + 值 + PADDING
                case TYPE_ROW ->
                        PADDING + displayWidth(line.label)
                                + PADDING + 2  // "│ " 占 2 列
                                + displayWidth(line.value) + PADDING;
                // 原始行：PADDING + 剥离 ANSI 后的可见宽度 + PADDING
                // ↑ 这是修复右边框错位的关键：必须先 stripAnsi 再 displayWidth
                case TYPE_RAW_ROW ->
                        PADDING + displayWidth(stripAnsi(line.value)) + PADDING;
                // 分隔线无内容
                default -> 0;
            };
            max = Math.max(max, w);
        }
        return max;
    }

    // ===================== 字符串处理工具 =====================

    /**
     * 右侧填充空格，使字符串的<b>可见显示宽度</b>精确等于 targetWidth
     *
     * <p>注意：此方法用于不含 ANSI 转义码的纯可见字符串。
     * 含 ANSI 码的字符串请在外部手动计算可见宽度后填充（见 renderRawRowLine）。
     *
     * @param str         原始字符串（不含 ANSI 码）
     * @param targetWidth 目标显示宽度
     * @return 填充后的字符串
     */
    private String padToWidth(String str, int targetWidth) {
        int current = displayWidth(str);
        int padding = Math.max(0, targetWidth - current);
        return str + " ".repeat(padding);
    }

    /**
     * 截断字符串使其可见显示宽度不超过 maxWidth，超出部分替换为 {@code "..."}
     *
     * <p>逐 Unicode 码点处理，避免在多字节字符中间截断。
     * 预留 3 列显示宽度给 {@code "..."}。
     *
     * @param str      原始字符串（不含 ANSI 码）
     * @param maxWidth 最大显示宽度
     * @return 截断后的字符串
     */
    private String truncateToWidth(String str, int maxWidth) {
        if (displayWidth(str) <= maxWidth) return str;
        StringBuilder sb = new StringBuilder();
        int accumulated = 0;
        int i = 0;
        while (i < str.length()) {
            int cp = str.codePointAt(i);
            int w  = charWidth(cp);
            if (accumulated + w > maxWidth - 3) break;
            sb.appendCodePoint(cp);
            accumulated += w;
            i += Character.charCount(cp);
        }
        return sb + "...";
    }

    /**
     * 剥离字符串中所有 ANSI 转义序列，返回纯可见字符串
     *
     * <p>ANSI 转义序列的格式为：{@code ESC [ ... m}（即 {@code \u001B[...m}），
     * 用正则 {@code \u001B\[[0-9;]*m} 匹配并删除。
     * 剥离后的字符串可安全传入 {@link #displayWidth(String)} 进行宽度计算。
     *
     * <p>示例：
     * <pre>
     * 输入："\u001B[96m主机地址\u001B[0m"
     * 输出："主机地址"
     * displayWidth("主机地址") = 8   // 正确
     * displayWidth 原始带码字符串 → 错误（会把 '[','9','6','m' 都计入）
     * </pre>
     *
     * @param str 可能含 ANSI 转义码的字符串
     * @return 剥离转义码后的纯文本字符串
     */
    public static String stripAnsi(String str) {
        if (str == null) return "";
        // 匹配标准 ANSI CSI 颜色/样式序列：ESC [ 数字和分号 m
        return str.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    // ===================== 终端显示宽度计算 =====================

    /**
     * 计算字符串在等宽字体终端中的实际显示宽度（列数）
     *
     * <p><b>调用前提：字符串不含 ANSI 转义码。</b>
     * 若含转义码，请先调用 {@link #stripAnsi(String)} 剥离。
     *
     * <p>宽度规则（East Asian Width，UAX#11）：
     * <ul>
     *   <li>ASCII 可打印字符（U+0020~U+007E）：宽度 = 1</li>
     *   <li>中文、日文、韩文及全角字符：宽度 = 2</li>
     *   <li>Emoji（增补平面宽字符）：宽度 = 2</li>
     *   <li>控制字符 / 零宽字符：宽度 = 0</li>
     * </ul>
     *
     * @param str 纯可见字符串（无 ANSI 码）
     * @return 终端显示列数，null 或空串返回 0
     */
    public static int displayWidth(String str) {
        if (str == null || str.isEmpty()) return 0;
        int width = 0;
        int i = 0;
        while (i < str.length()) {
            int cp = str.codePointAt(i);
            width += charWidth(cp);
            i += Character.charCount(cp); // 增补平面字符占两个 char，需跳过 2
        }
        return width;
    }

    /**
     * 计算单个 Unicode 码点的终端显示宽度
     *
     * @param cp Unicode 码点
     * @return 0（控制字符）、1（半宽）或 2（全宽）
     */
    public static int charWidth(int cp) {
        if (cp < 0x20 || cp == 0x7F) return 0;  // 控制字符
        if (cp < 0x7F) return 1;                  // ASCII 可打印字符

        // ── 全宽字符区间（宽度 = 2）─────────────────────────────────────────
        if (cp >= 0x1100  && cp <= 0x115F)  return 2; // 韩语字母
        if (cp == 0x2329 || cp == 0x232A)   return 2; // 左右角括号
        if (cp >= 0x2E80  && cp <= 0x303E)  return 2; // CJK 部首、符号和标点
        if (cp >= 0x3041  && cp <= 0x33FF)  return 2; // 平假名、片假名、CJK 兼容
        if (cp >= 0x3400  && cp <= 0x4DBF)  return 2; // CJK 扩展 A
        if (cp >= 0x4E00  && cp <= 0x9FFF)  return 2; // CJK 基本区（常用汉字）
        if (cp >= 0xA000  && cp <= 0xA4CF)  return 2; // 彝文
        if (cp >= 0xA960  && cp <= 0xA97F)  return 2; // 韩语字母扩展 A
        if (cp >= 0xAC00  && cp <= 0xD7FF)  return 2; // 朝鲜文音节
        if (cp >= 0xF900  && cp <= 0xFAFF)  return 2; // CJK 兼容表意文字
        if (cp >= 0xFE10  && cp <= 0xFE6F)  return 2; // 竖排形式、全角半角
        if (cp >= 0xFF01  && cp <= 0xFF60)  return 2; // 全角拉丁字母和标点
        if (cp >= 0xFFE0  && cp <= 0xFFE6)  return 2; // 全角货币符号
        if (cp >= 0x1F004 && cp <= 0x1F0CF) return 2; // 麻将、扑克牌符号
        if (cp >= 0x1F300 && cp <= 0x1F9FF) return 2; // 杂项符号、Emoji、旗帜等
        if (cp >= 0x20000 && cp <= 0x2FFFD) return 2; // CJK 扩展 B~F（增补平面）
        if (cp >= 0x30000 && cp <= 0x3FFFD) return 2; // CJK 扩展 G（第三平面）

        return 1; // 其余字符（拉丁、西里尔、阿拉伯等）
    }

    // ===================== 内部数据类 =====================

    /**
     * 行数据封装，区分标题行、信息行、原始行和分隔线
     *
     * @param type  TYPE_TITLE / TYPE_ROW / TYPE_RAW_ROW / TYPE_SEPARATOR
     * @param label 仅 TYPE_ROW 使用
     * @param value TYPE_TITLE/ROW/RAW_ROW 存内容，TYPE_SEPARATOR 为空
     */
        private record Line(int type, String label, String value) {}
}