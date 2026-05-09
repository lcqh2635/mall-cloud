package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;

/**
 * Box 主题渲染器
 *
 * <p>Unicode 闭合边框风格，委托 {@link BoxRenderer} 完成实际绘制。
 * 本类只负责数据组装，不关心宽度计算和填充细节。
 *
 * <p>原左侧对齐 Bug 根因：
 * 之前 padRight() 只粗略估算 CJK 基本区汉字数量，
 * 全角标点、Emoji、URL 中的特殊字符等均未计入，
 * 导致不同行的填充空格数不一致，视觉上左侧 ║ 出现错位。
 * 修复方案：所有宽度计算统一交由 {@link BoxRenderer#charWidth(int)} 精确处理。
 *
 * @author mallcloud
 * @see BoxRenderer
 */
public class BoxBannerTheme implements BannerTheme {

    @Override
    public void render(PrintStream out, BannerContext ctx, BannerProperties props, String asciiTitle, AnsiHelper ansi) {

        BannerProperties.ColorConfig c = props.getColor();
        BannerProperties.ShowConfig  s = props.getShow();

        // 构建 BoxRenderer，传入三种颜色：边框色、标题色、内容色
        BoxRenderer box = new BoxRenderer(ansi, c.getSeparator(), c.getTitle(), c.getValue());

        // ---------- 标题区 ----------
        // title() 内部会自动在标题与信息区之间插入分隔线
        box.title(asciiTitle);

        // ---------- 启动信息 ----------
        String timeVal = ctx.getStartTime();
        if (s.isCost() && ctx.getStartupCost() != null) {
            timeVal += "  (耗时 " + ctx.getStartupCost() + ")";
        }
        box.row("🌐 启动时间", timeVal);

        if (s.isPid()) {
            box.row("🔢 进程编号", ctx.getPid());
        }

        // ---------- 网络信息 ----------
        box.row("🌐 主机地址", ctx.getHostAddress());
        box.row("🚀 服务端口", ctx.getServerPort());

        if (s.isContextPath() && ctx.getContextPath() != null && !ctx.getContextPath().isEmpty()) {
            box.row("访问路径", ctx.getContextPath());
        }

        // ---------- 应用信息 ----------
        box.row("运行环境", ctx.getProfiles());
        box.row("应用名称", ctx.getAppName());

        if (s.isAuthor()) {
            box.row("项目作者", ctx.getAuthor());
        }
        if (s.isDescription()) {
            box.row("服务描述", ctx.getDescription());
        }

        // ---------- 版本信息 ----------
        box.row("业务版本", ctx.getVersion());
        box.row("框架版本", String.format("Java/%-6s  Spring/%-6s  Boot/%s",
                ctx.getJavaVersion(), ctx.getSpringVersion(), ctx.getSpringBootVersion()));

        if (s.isDbUrl() && ctx.getDbUrl() != null && !ctx.getDbUrl().isEmpty()) {
            box.row("数据库址", ctx.getDbUrl());
        }

        // ---------- 访问地址（用分隔线与上方区分） ----------
        box.separator();
        box.row("健康检查", ctx.getHealthUrl());
        box.row("接口文档", ctx.getSwaggerUrl());

        // ---------- 执行渲染 ----------
        box.render(out);
    }
}