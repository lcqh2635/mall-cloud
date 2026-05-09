package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;

/**
 * Colorful 主题渲染器（含盒子边框版）
 *
 * <p>在原有每行不同颜色的基础上，引入 {@link BoxRenderer} 提供完整闭合边框。
 * 颜色分区规则与原版保持一致：
 * <ul>
 *   <li>启动时间 / 进程 PID → 亮黄色（93）</li>
 *   <li>网络 / 地址信息     → 亮青色（96）</li>
 *   <li>应用 / 环境信息     → 亮绿色（92）</li>
 *   <li>版本信息            → 亮蓝色（94）</li>
 *   <li>访问地址            → 亮紫色（95）</li>
 *   <li>边框和分隔线        → 青色（36）</li>
 * </ul>
 *
 * <p>实现要点：
 * Colorful 主题每行颜色不同，而 {@link BoxRenderer#row(String, String)} 对整行
 * 使用同一颜色。解决方案是将着色后的内容字符串直接传入 row()，
 * ANSI 转义码不占显示宽度，不影响 BoxRenderer 的宽度对齐计算。
 *
 * <p>输出示例：
 * <pre>
 * ╔══════════════════════════════════════════════════════════╗
 * ║  ADMIN-SERVER  ← ASCII Art 大字标题（亮青色）               ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  🕐 启动时间  │  2026-05-09 09:40:36  ⏱ 耗时 3.21 s         ║  ← 亮黄
 * ║  🔢 进程编号  │  623773                                    ║  ← 亮黄
 * ║  🌐 主机地址  │  192.168.1.3                               ║  ← 亮青
 * ║  🚀 服务端口  │  7777                                      ║  ← 亮青
 * ╠══════════════════════════════════════════════════════════╣
 * ║  🔍 健康检查  │  http://192.168.1.3:7777/actuator/health   ║  ← 亮紫
 * ║  📖 接口文档  │  http://192.168.1.3:7777/swagger-ui.html   ║  ← 亮紫
 * ╚══════════════════════════════════════════════════════════╝
 * </pre>
 *
 * @author mallcloud
 * @see BoxRenderer
 */
public class ColorfulBannerTheme implements BannerTheme {

    // ===================== 各区域颜色定义 =====================

    /** 边框和分隔线颜色：青色 */
    private static final int COLOR_BORDER  = 36;
    /** 启动时间 / 进程 PID：亮黄色 */
    private static final int COLOR_TIME    = 93;
    /** 网络 / 地址信息：亮青色 */
    private static final int COLOR_NETWORK = 96;
    /** 应用 / 环境信息：亮绿色 */
    private static final int COLOR_APP     = 92;
    /** 版本信息：亮蓝色 */
    private static final int COLOR_VERSION = 94;
    /** 访问地址：亮紫色 */
    private static final int COLOR_URL     = 95;

    @Override
    public void render(PrintStream out, BannerContext ctx, BannerProperties props, String asciiTitle, AnsiHelper ansi) {

        BannerProperties.ColorConfig c = props.getColor();
        BannerProperties.ShowConfig  s = props.getShow();

        // ── 构建 BoxRenderer ──────────────────────────────────────────────────
        // Colorful 主题每行颜色各异，BoxRenderer 的 contentColor 参数作为兜底色，
        // 实际颜色通过 colorRow() 辅助方法将着色内容字符串直接传入 row() 实现。
        // 边框统一用 COLOR_BORDER（青色），标题用配置的 title 颜色。
        // ─────────────────────────────────────────────────────────────────────
        BoxRenderer box = new BoxRenderer(ansi, COLOR_BORDER, c.getTitle(), COLOR_BORDER);

        // ---------- 标题区（ASCII Art，自动在下方插入分隔线） ----------
        box.title(asciiTitle);

        // ---------- 启动时间（亮黄色） ----------
        String timeVal = ctx.getStartTime();
        if (s.isCost() && ctx.getStartupCost() != null) {
            timeVal += "  ⏱ 耗时 " + ctx.getStartupCost();
        }
        // colorRow：将整行内容着色后传入 BoxRenderer，边框对齐由 BoxRenderer 保证
        colorRow(box, ansi, "🌐 启动时间", timeVal, COLOR_TIME);

        // ---------- 进程编号（亮黄色，按需显示） ----------
        if (s.isPid()) {
            colorRow(box, ansi, "🔢 进程编号", ctx.getPid(), COLOR_TIME);
        }

        // ---------- 网络信息（亮青色） ----------
        colorRow(box, ansi, "🌐 主机地址", ctx.getHostAddress(), COLOR_NETWORK);
        colorRow(box, ansi, "🚀 服务端口", ctx.getServerPort(),  COLOR_NETWORK);

        if (s.isContextPath() && ctx.getContextPath() != null && !ctx.getContextPath().isEmpty()) {
            colorRow(box, ansi, "🔗 访问路径", ctx.getContextPath(), COLOR_NETWORK);
        }

        // ---------- 应用信息（亮绿色） ----------
        colorRow(box, ansi, "🍃 运行环境", ctx.getProfiles(), COLOR_APP);
        colorRow(box, ansi, "📦 应用名称", ctx.getAppName(),  COLOR_APP);

        if (s.isAuthor()) {
            colorRow(box, ansi, "👤 项目作者", ctx.getAuthor(), COLOR_APP);
        }
        if (s.isDescription()) {
            colorRow(box, ansi, "📝 服务描述", ctx.getDescription(), COLOR_APP);
        }

        // ---------- 版本信息（亮蓝色） ----------
        colorRow(box, ansi, "🏷 业务版本", ctx.getVersion(), COLOR_VERSION);
        colorRow(box, ansi, "🍃 框架版本",
                String.format("☕ Java/%-6s  🌿 Spring/%-6s  Boot/%s",
                        ctx.getJavaVersion(), ctx.getSpringVersion(), ctx.getSpringBootVersion()),
                COLOR_VERSION);

        // ---------- 数据库地址（亮蓝色，按需，已脱敏） ----------
        if (s.isDbUrl() && ctx.getDbUrl() != null && !ctx.getDbUrl().isEmpty()) {
            colorRow(box, ansi, "🗄  数据库址", ctx.getDbUrl(), COLOR_VERSION);
        }

        // ---------- 访问地址（亮紫色，用分隔线隔开） ----------
        box.separator(); // 在访问地址上方插入分隔线，形成视觉分区
        colorRow(box, ansi, "🔍 健康检查", ctx.getHealthUrl(), COLOR_URL);
        colorRow(box, ansi, "📖 接口文档", ctx.getSwaggerUrl(), COLOR_URL);

        // ---------- 执行渲染，输出完整盒子 ----------
        box.render(out);
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 将整行内容着色后添加到 BoxRenderer
     *
     * <p>核心思路：
     * {@link BoxRenderer} 计算宽度时调用 {@link BoxRenderer#displayWidth(String)}，
     * 该方法逐码点统计可见字符宽度，ANSI 转义码（如 {@code \u001B[93m}）中的字符
     * 均为 ASCII 控制字符，displayWidth 对其返回 0，因此不会被计入显示宽度，
     * 颜色嵌入字符串内部完全不影响边框的对齐计算。
     *
     * <p>行格式（BoxRenderer 内部构造）：
     * {@code ║  [color]label  │  value[reset]  [padding]  ║}
     *
     * @param box      目标 BoxRenderer 实例
     * @param ansi     ANSI 颜色工具
     * @param label    标签文字（含 Emoji 前缀）
     * @param value    值内容
     * @param colorCode 该行使用的 ANSI 色号
     */
    private void colorRow(BoxRenderer box, AnsiHelper ansi, String label, String value, int colorCode) {
        // 将标签和值整体着色后，作为"value"传入 BoxRenderer
        // label 参数置空，避免 BoxRenderer 在内部再拼一次未着色的标签
        // BoxRenderer 的行格式：║  [label]  │  [value]  ║
        // 传入空 label 后格式变为：║    │  [coloredLabel  │  coloredValue]  ║
        // 但为了让分隔符 │ 只出现一次且位置准确，将标签和值拼为完整着色行传入
        String coloredLine = ansi.colorize(label + "  │  " + value, colorCode);
        // label 传空字符串，BoxRenderer 不会再插入额外的 │ 和左边距标签
        box.row("", coloredLine);
    }
}