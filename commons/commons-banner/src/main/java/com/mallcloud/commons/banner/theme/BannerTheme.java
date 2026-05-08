package com.mallcloud.commons.banner.theme;

import java.io.PrintStream;

/**
 * Banner 主题渲染器接口
 *
 * <p>每种主题实现此接口，负责将 {@link BannerContext} 中的数据
 * 按照自身风格渲染并输出到 {@link PrintStream}。
 *
 * <p>新增主题只需：
 * <ol>
 *   <li>实现本接口</li>
 *   <li>在 {@link BannerThemeFactory} 中注册主题名称</li>
 * </ol>
 *
 * @author mallcloud
 * @see DefaultBannerTheme
 * @see BoxBannerTheme
 * @see ColorfulBannerTheme
 */
public interface BannerTheme {

    /**
     * 执行 Banner 渲染输出
     *
     * @param out        输出流（Spring Boot 传入的 PrintStream）
     * @param context    Banner 数据上下文
     * @param properties Banner 配置属性
     * @param asciiTitle ASCII Art 标题字符串（由外部生成后传入，主题无需关心字体逻辑）
     * @param ansi       ANSI 颜色工具（封装了 ANSI 支持检测和颜色渲染）
     */
    void render(PrintStream out, BannerContext context, BannerProperties properties, String asciiTitle, AnsiHelper ansi);
}