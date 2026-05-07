package com.mallcloud.commons.banner.theme;

import lombok.Getter;
import lombok.Setter;

/**
 * Banner 渲染上下文
 *
 * <p>聚合所有需要在 Banner 中展示的运行时信息，
 * 由 {@code FigletBannerPrinter} 收集后传递给各主题渲染器，
 * 实现数据与展示的解耦。
 *
 * @author mallcloud
 */
@Getter
@Setter
public class BannerContext {
    // ===================== 应用基础信息 =====================
    /** Spring 应用名称（spring.application.name） */
    private String appName;

    /** 业务版本号 */
    private String version;

    /** 项目作者 */
    private String author;

    /** 服务描述 */
    private String description;

    // ===================== 运行时网络信息 =====================
    /** 本机 IPv4 地址 */
    private String hostAddress;

    /** 服务监听端口 */
    private String serverPort;

    /** 访问协议（http / https） */
    private String protocol;

    /** Servlet Context Path，如 /api */
    private String contextPath;

    // ===================== 环境信息 =====================
    /** 激活的 Spring Profile，多个以逗号分隔 */
    private String profiles;

    /** Java 版本 */
    private String javaVersion;

    /** Spring Framework 版本 */
    private String springVersion;

    /** Spring Boot 版本 */
    private String springBootVersion;

    // ===================== 启动信息 =====================
    /** 启动时间，格式：yyyy-MM-dd HH:mm:ss */
    private String startTime;

    /**
     * 启动耗时，格式：x.xx s
     * 若无法获取则为 "N/A"
     */
    private String startupCost;

    /**
     * 进程 PID
     * 通过 {@link ProcessHandle#current()} 获取，Java 9+
     */
    private String pid;

    // ===================== 访问地址 =====================
    /** 健康检查地址 */
    private String healthUrl;

    /** 接口文档地址（Swagger / SpringDoc） */
    private String swaggerUrl;

    /**
     * 数据库连接地址（已脱敏密码）
     * 示例：jdbc:mysql://127.0.0.1:3306/mall_cloud
     */
    private String dbUrl;
}