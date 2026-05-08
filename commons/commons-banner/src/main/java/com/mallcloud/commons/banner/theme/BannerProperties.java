package com.mallcloud.commons.banner.theme;

import com.mallcloud.commons.banner.config.MallCloudBannerProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * Banner 配置属性类
 *
 * <p>所有配置项均以 {@code mallcloud.banner} 为前缀，支持在 application.yml 中灵活配置。
 *
 * <p>完整配置示例：
 * <pre>
 * mallcloud:
 *   banner:
 *     version: 2.1.0
 *     author: 龙茶清欢
 *     description: 管理服务
 *     protocol: http
 *     theme: default          # default / minimal / box / colorful
 *     font-path: /fonts/ANSI Shadow.flf
 *     show:
 *       pid: true
 *       cost: true
 *       author: true
 *       description: true
 *       context-path: false
 *       db-url: false
 *     color:
 *       title: 36
 *       separator: 32
 *       label: 32
 *       value: 33
 * </pre>
 *
 * @author mallcloud
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = BannerProperties.PREFIX)
public class BannerProperties {

    public static final String PREFIX = "mallcloud.banner";

    // ===================== 基础信息 =====================
    /** 业务版本号，默认 1.0.0 */
    private String version = "1.0.0";

    /** 项目作者 */
    private String author = "unknown";

    /** 服务描述 */
    private String description = "unknown";

    /**
     * 访问协议，可选 http / https
     * 生产环境若走 Nginx 反代，建议设置为 https
     */
    private String protocol = "http";

    /**
     * Figlet 字体文件路径（classpath 内）
     * 可替换为其他 .flf 字体，字体库参考：https://github.com/xero/figlet-fonts
     */
    private String fontPath = "/fonts/ANSI Shadow.flf";

    /**
     * 显示主题
     * <ul>
     *   <li>{@code default}  — 标准带分隔线风格（默认）</li>
     *   <li>{@code minimal}  — 纯文本单行，适合 CI / 日志采集</li>
     *   <li>{@code box}      — Unicode 边框风格，终端体验更佳</li>
     *   <li>{@code colorful} — 彩色增强版，每行使用不同颜色</li>
     * </ul>
     */
    private String theme = "default";

    // ===================== 嵌套配置 =====================
    /** 控制各信息行是否显示 */
    @NestedConfigurationProperty
    private ShowConfig show = new ShowConfig();

    /** 自定义 ANSI 颜色（色号参考标准 ANSI 256 色） */
    @NestedConfigurationProperty
    private ColorConfig color = new ColorConfig();

    // ===================== 内部类：显示控制 =====================
    /**
     * 控制 Banner 各信息行的显示开关
     */
    @Setter
    @Getter
    public static class ShowConfig {

        /** 是否显示进程 PID，默认开启 */
        private boolean pid = true;

        /** 是否显示启动耗时，默认开启 */
        private boolean cost = true;

        /** 是否显示项目作者，默认开启 */
        private boolean author = true;

        /** 是否显示服务描述，默认开启 */
        private boolean description = true;

        /**
         * 是否显示 Context Path
         * 仅在配置了 server.servlet.context-path 时有意义，默认关闭
         */
        private boolean contextPath = false;

        /**
         * 是否显示数据库连接地址
         * 注意：开启时会自动脱敏密码部分，但生产环境仍需谨慎
         */
        private boolean dbUrl = false;
    }

    // ===================== 内部类：颜色配置 =====================
    /**
     * 自定义 ANSI 颜色配置
     *
     * <p>颜色值为标准 ANSI 色号：
     * <pre>
     * 30=黑  31=红  32=绿  33=黄  34=蓝  35=紫  36=青  37=白
     * 90-97 为对应的高亮版本
     * </pre>
     */
    @Setter
    @Getter
    public static class ColorConfig {

        /** ASCII Art 标题颜色，默认青色(36) */
        private int title = 36;

        /** 分隔线颜色，默认绿色(32) */
        private int separator = 32;

        /** 标签颜色，默认绿色(32) */
        private int label = 32;

        /** 值颜色，默认黄色(33) */
        private int value = 33;
    }
}