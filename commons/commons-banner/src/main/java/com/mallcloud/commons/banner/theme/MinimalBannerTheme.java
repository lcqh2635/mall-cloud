package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;

/**
 * Minimal 主题渲染器
 *
 * <p>极简风格：无分隔线、无 Emoji、纯文本单行输出，
 * 适合 CI/CD 流水线、日志采集平台（ELK / Loki）等对格式敏感的场景。
 *
 * <p>输出示例：
 * <pre>
 * [admin-server] started | port=7777 | env=dev | pid=12345 | cost=3.21s | v2.1.0
 * Health  : http://192.168.1.3:7777/actuator/health
 * SwaggerUI : http://192.168.1.3:7777/swagger-ui/index.html
 * </pre>
 *
 * @author mallcloud
 */
public class MinimalBannerTheme implements BannerTheme {

    @Override
    public void render(PrintStream out, BannerContext ctx, BannerProperties props, String asciiTitle, AnsiHelper ansi) {
        BannerProperties.ShowConfig s = props.getShow();

        // 第一行：核心摘要，所有关键信息压缩到一行，方便 grep
        StringBuilder summary = new StringBuilder();
        summary.append("[").append(ctx.getAppName()).append("] started");
        summary.append(" | port=").append(ctx.getServerPort());
        summary.append(" | env=").append(ctx.getProfiles());

        if (s.isPid()) {
            summary.append(" | pid=").append(ctx.getPid());
        }
        if (s.isCost() && ctx.getStartupCost() != null) {
            summary.append(" | cost=").append(ctx.getStartupCost());
        }

        summary.append(" | v").append(ctx.getVersion());

        // Minimal 模式仍保留绿色高亮主摘要行，便于快速定位
        out.println(ansi.green(summary.toString()));

        // 第二行：健康检查与文档地址（换行避免单行过长）
        out.println("  Health   : " + ctx.getHealthUrl());
        out.println("  SwaggerUI: " + ctx.getSwaggerUrl());

        if (s.isDbUrl() && ctx.getDbUrl() != null && !ctx.getDbUrl().isEmpty()) {
            out.println("  Database : " + ctx.getDbUrl());
        }
    }
}