package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;

/**
 * Default 主题渲染器（含盒子边框版）
 *
 * <p>在原有标准风格（Emoji + 彩色标签）基础上，
 * 引入 {@link BoxRenderer} 提供完整闭合的 Unicode 边框，
 * 使整体视觉更加规整、专业。
 *
 * <p>与 BoxBannerTheme 的区别：
 * <ul>
 *   <li>Default：标签前带 Emoji，标签列与值列分别着不同颜色</li>
 *   <li>Box：纯文本标签，整行统一着值颜色，风格更简洁</li>
 * </ul>
 *
 * <p>输出示例：
 * <pre>
 * ╔══════════════════════════════════════════════════════════╗
 * ║  ADMIN-SERVER  ← ASCII Art 大字标题                       ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  启动时间  :  🕐 2026-05-09 09:40:36  (耗时 3.21 s)         ║
 * ║  进程编号  :  🔢 623773                                   ║
 * ║  主机地址  :  🌐 192.168.1.3                              ║
 * ║  服务端口  :  🚀 7777                                     ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  健康检查  :  🔍 http://192.168.1.3:7777/actuator/health  ║
 * ║  接口文档  :  📖 http://192.168.1.3:7777/swagger-ui.html  ║
 * ╚══════════════════════════════════════════════════════════╝
 * </pre>
 *
 * @author mallcloud
 * @see BoxRenderer
 */
public class DefaultBannerTheme implements BannerTheme {

    @Override
    public void render(PrintStream out, BannerContext ctx, BannerProperties props, String asciiTitle, AnsiHelper ansi) {

        BannerProperties.ColorConfig c = props.getColor();
        BannerProperties.ShowConfig  s = props.getShow();

        // ── 构建 BoxRenderer ──────────────────────────────────────────────────
        // Default 主题的颜色语义：
        //   边框色  → 配置的 separator 颜色（默认绿色 32）
        //   标题色  → 配置的 title 颜色（默认亮青色 96）
        //   内容色  → 由每行单独决定（BoxRenderer 的 contentColor 作为兜底）
        // 注意：Default 主题每行颜色不同，所以这里 contentColor 只是占位，
        //       实际颜色通过 BoxRenderer 的 row() 不涉及，
        //       改用自定义的 colorRow() 方法实现逐行着色。
        // ─────────────────────────────────────────────────────────────────────
        // 由于 BoxRenderer.row() 整行使用同一颜色，而 Default 主题需要标签与值
        // 分别着色，因此这里继承 BoxRenderer 并重写行渲染，或直接使用支持
        // 双色行的扩展方式。
        // 最简方案：将"标签:值"整体视为值传入，标签嵌入 ANSI 色码后拼入值中，
        // 这样 BoxRenderer 负责边框和宽度，颜色由我们在值内部控制。
        // ─────────────────────────────────────────────────────────────────────

        // 创建支持双色行的 Default 专用 BoxRenderer（边框色、标题色、内容兜底色）
        DefaultBoxRenderer box = new DefaultBoxRenderer(ansi, c.getSeparator(), c.getTitle());

        // ---------- 标题区（ASCII Art，自动在下方插入分隔线） ----------
        box.title(asciiTitle);

        // ---------- 启动时间（亮黄色，含耗时后缀） ----------
        String timeVal = "🕐 " + ctx.getStartTime();
        if (s.isCost() && ctx.getStartupCost() != null) {
            // 耗时部分用暗灰色（90）与时间主体区分
            timeVal += ansi.colorize("  (耗时 " + ctx.getStartupCost() + ")", 90);
        }
        // 标签色：配置的 label 色；值色：配置的 value 色
        box.row("启动时间", timeVal, c.getLabel(), c.getValue());

        // ---------- 进程 PID（按需显示） ----------
        if (s.isPid()) {
            box.row("进程编号", "🔢 " + ctx.getPid(), c.getLabel(), c.getValue());
        }

        // ---------- 网络信息 ----------
        box.row("主机地址", "🌐 " + ctx.getHostAddress(), c.getLabel(), c.getValue());
        box.row("服务端口", "🚀 " + ctx.getServerPort(),  c.getLabel(), c.getValue());

        if (s.isContextPath() && ctx.getContextPath() != null && !ctx.getContextPath().isEmpty()) {
            box.row("访问路径", "🔗 " + ctx.getContextPath(), c.getLabel(), c.getValue());
        }

        // ---------- 应用信息 ----------
        box.row("运行环境", "⚙  " + ctx.getProfiles(), c.getLabel(), c.getValue());
        box.row("应用名称", "📦 " + ctx.getAppName(),  c.getLabel(), c.getValue());

        if (s.isAuthor()) {
            box.row("项目作者", "👤 " + ctx.getAuthor(), c.getLabel(), c.getValue());
        }
        if (s.isDescription()) {
            box.row("服务描述", "📝 " + ctx.getDescription(), c.getLabel(), c.getValue());
        }

        // ---------- 版本信息 ----------
        box.row("业务版本", "🏷  " + ctx.getVersion(), c.getLabel(), c.getValue());
        box.row("框架版本",
                String.format("☕ Java/%-6s  🌿 Spring/%-6s  🍃 Boot/%s",
                        ctx.getJavaVersion(), ctx.getSpringVersion(), ctx.getSpringBootVersion()),
                c.getLabel(), c.getValue());

        // ---------- 数据库（按需，已脱敏） ----------
        if (s.isDbUrl() && ctx.getDbUrl() != null && !ctx.getDbUrl().isEmpty()) {
            box.row("数据库址", "🗄  " + ctx.getDbUrl(), c.getLabel(), c.getValue());
        }

        // ---------- 访问地址（分隔线隔开，视觉分区更清晰） ----------
        box.separator();
        box.row("健康检查", "🔍 " + ctx.getHealthUrl(), c.getLabel(), c.getValue());
        box.row("接口文档", "📖 " + ctx.getSwaggerUrl(), c.getLabel(), c.getValue());

        // ---------- 执行渲染，输出完整盒子 ----------
        box.render(out);
    }

    // =========================================================================
    // DefaultBoxRenderer —— BoxRenderer 的内部扩展
    //
    // BoxRenderer 的 row() 方法对整行使用同一颜色，
    // Default 主题需要标签与值分别着色（标签用 labelColor，值用 valueColor），
    // 因此通过继承扩展 row() 方法，增加双色行支持。
    // =========================================================================

    /**
     * Default 主题专用的盒子渲染器
     *
     * <p>继承 {@link BoxRenderer}，扩展支持标签与值分别着色的 {@code row()} 重载，
     * 其余边框绘制、宽度计算、截断逻辑全部复用父类实现。
     */
    private static class DefaultBoxRenderer extends BoxRenderer {

        /** ANSI 工具引用，用于在 row() 中对标签和值分别着色 */
        private final AnsiHelper ansi;

        /**
         * @param ansi        ANSI 颜色工具
         * @param borderColor 边框颜色 ANSI 色号
         * @param titleColor  标题区颜色 ANSI 色号
         */
        DefaultBoxRenderer(AnsiHelper ansi, int borderColor, int titleColor) {
            // contentColor 传 0 作为占位，实际颜色由双色 row() 方法控制
            super(ansi, borderColor, titleColor, 0);
            this.ansi = ansi;
        }

        /**
         * 添加一条双色信息行（标签与值分别着色）
         *
         * <p>实现方式：将带颜色的标签拼入"值"字段后，
         * 整体作为一个字符串传给父类 {@code row()}，
         * 父类负责边框对齐，颜色嵌在字符串内部。
         *
         * <p>格式：{@code ║  [labelColor]label[reset]  :  [valueColor]value[reset]  ║}
         *
         * @param label      标签文字（如 "启动时间"）
         * @param value      值内容（如 "🕐 2026-05-09 09:40:36"）
         * @param labelColor 标签 ANSI 色号
         * @param valueColor 值 ANSI 色号
         */
        public void row(String label, String value, int labelColor, int valueColor) {
            // 将"着色标签 + 分隔符 + 着色值"拼为一个整体字符串
            // BoxRenderer 的宽度计算会用 displayWidth() 统计其中可见字符的宽度，
            // ANSI 转义码本身不占显示宽度，因此不影响对齐计算。
            String coloredLabel = ansi.colorize(label, labelColor);
            String coloredValue = ansi.colorize(value, valueColor);
            // 传给父类 row()，label 置空，将完整着色内容放在 value 位置
            // 父类格式：║  [label]  │  [value]  ║
            // 这里利用 label="" 使分隔符 │ 紧贴左侧边距，内容全在 value 侧
            super.row("", coloredLabel + "  :  " + coloredValue);
        }
    }
}