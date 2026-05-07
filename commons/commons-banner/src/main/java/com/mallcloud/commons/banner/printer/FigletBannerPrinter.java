package com.mallcloud.commons.banner.printer;

import com.mallcloud.commons.banner.utils.FigletUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.system.JavaVersion;
import org.springframework.core.SpringVersion;
import org.springframework.core.env.Environment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;

/**
 * 自定义微服务启动 Banner 打印器
 *
 * <p>功能：
 * <ul>
 *   <li>使用 Figlet ASCII Art 渲染应用名称</li>
 *   <li>输出服务基础信息（IP、端口、环境、版本等）</li>
 *   <li>自动适配 Swagger V2 / V3 文档地址</li>
 *   <li>兼容 Windows / CI 等非 ANSI 终端</li>
 * </ul>
 *
 * <p>支持的配置项（application.yml）：
 * <pre>
 * mallcloud:
 *   banner:
 *     version: 1.0.0
 *     author: yourName
 *     description: 服务描述
 *     protocol: http          # 可选 http / https，默认 http
 * </pre>
 *
 * @author mallcloud
 */
public class FigletBannerPrinter implements Banner {

    // ===================== ANSI 颜色常量 =====================
    /** 重置颜色 */
    private static final String RESET  = "\u001B[0m";
    /** 绿色 */
    private static final String GREEN  = "\u001B[32m";
    /** 青色 */
    private static final String CYAN   = "\u001B[36m";
    /** 黄色 */
    private static final String YELLOW = "\u001B[33m";

    // ===================== 配置默认值常量 =====================
    private static final String DEFAULT_PORT        = "8080";
    private static final String DEFAULT_VERSION     = "1.0.0";
    private static final String DEFAULT_AUTHOR      = "unknown";
    private static final String DEFAULT_DESCRIPTION = "unknown";
    private static final String DEFAULT_PROTOCOL    = "http";
    private static final String FALLBACK_IP         = "127.0.0.1";


    // ===================== 字体资源缓存 =====================
    /**
     * 在类加载时一次性读取字体文件字节，避免每次启动重复 IO。
     * 若资源不存在则置为 null，后续降级为纯文本标题。
     */
    private static final byte[] FONT_BYTES;
    static {
        byte[] bytes = null;
        try (InputStream in = FigletBannerPrinter.class.getResourceAsStream("/fonts/ANSI Shadow.flf")) {
            if (in != null) {
                bytes = in.readAllBytes();
            }
        } catch (IOException _) {
            // 字体加载失败不影响应用启动，后续降级处理
        }
        FONT_BYTES = bytes;
    }

    // ===================== ANSI 支持检测 =====================
    /**
     * 判断当前终端是否支持 ANSI 颜色转义码。
     *
     * <p>规则：
     * <ul>
     *   <li>Windows 系统默认不支持（CMD / PowerShell 需额外配置）</li>
     *   <li>非 Windows 且存在 console 对象时支持</li>
     *   <li>CI 环境通常不需要颜色，通过环境变量 NO_COLOR 可强制关闭</li>
     * </ul>
     */
    private static final boolean ANSI_SUPPORTED = detectAnsiSupport();
    private static boolean detectAnsiSupport() {
        // 支持 NO_COLOR 标准规范
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }
        // 优先读取 Spring Boot 官方 ANSI 配置
        // 可在 application.yml 中设置：spring.output.ansi.enabled=always
        String ansiEnabled = System.getProperty("spring.output.ansi.enabled", "detect");
        if ("always".equalsIgnoreCase(ansiEnabled)) {
            return true;
        }
        if ("never".equalsIgnoreCase(ansiEnabled)) {
            return false;
        }
        // detect 模式：Windows 额外判断，非 Windows 直接放行
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String term = System.getenv("TERM");
            return term != null && (term.contains("xterm") || term.contains("color"));
        }
        // 非 Windows（Linux/macOS）统一视为支持，包括 IDE 控制台
        return true;
    }

    // ===================== Banner 主体逻辑 =====================
    @Override
    public void printBanner(@NonNull Environment environment, Class<?> sourceClass, @NonNull PrintStream out) {
        try {
            // ---------- 读取应用配置 ----------
            String appName     = environment.getProperty("spring.application.name", "Unknown-Service");
            String serverPort  = environment.getProperty("server.port", DEFAULT_PORT);
            String version     = environment.getProperty("mallcloud.banner.version", DEFAULT_VERSION);
            String author      = environment.getProperty("mallcloud.banner.author", DEFAULT_AUTHOR);
            String description = environment.getProperty("mallcloud.banner.description", DEFAULT_DESCRIPTION);
            // 协议类型：支持 http / https，由配置决定，不再硬编码
            String protocol    = environment.getProperty("mallcloud.banner.protocol", DEFAULT_PROTOCOL);

            // ---------- 运行时信息 ----------
            String profiles         = String.join(", ", environment.getActiveProfiles());
            JavaVersion javaVersion = JavaVersion.getJavaVersion();
            String springVersion    = SpringVersion.getVersion();
            String springBootVersion= SpringBootVersion.getVersion();
            String startTime        = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // ---------- 网络地址 ----------
            String hostAddress = getHostAddress();
            String baseUrl     = protocol + "://" + hostAddress + ":" + serverPort;
            String healthUrl   = baseUrl + "/actuator/health";
            String swaggerUrl  = buildSwaggerUrl(environment, baseUrl);

            // ---------- ASCII Art 标题 ----------
            String asciiTitle = generateAsciiArt(appName);

            // ---------- 输出 Banner ----------
            String sep = colorize("===============================================", GREEN);
            out.println(sep);
            out.println(colorize(asciiTitle, CYAN));
            out.println(sep);
            out.println(line("启动时间", "🕐 " + startTime,                           YELLOW));
            out.println(line("主机地址", "🌐 " + hostAddress,                          CYAN));
            out.println(line("服务端口", "🚀 " + serverPort,                           YELLOW));
            out.println(line("运行环境", "⚙  " + (profiles.isEmpty() ? "default" : profiles), GREEN));
            out.println(line("应用名称", "📦 " + appName,                              CYAN));
            out.println(line("项目作者", "👤 " + author,                              CYAN));
            out.println(line("服务描述", "📝 " + description,                          CYAN));
            out.println(line("业务版本", "🏷 " + version,                             YELLOW));
            out.println(line("框架版本", String.format("☕ Java/%-6s  🌿 Spring/%-6s  🍃 Spring Boot/%s", javaVersion, springVersion, springBootVersion),                    GREEN));
            out.println(line("健康检查", "🔍 " + healthUrl,                            CYAN));
            out.println(line("接口文档", "📖 " + swaggerUrl,                           GREEN));
            out.println(sep);
        } catch (Exception e) {
            // 任何异常都不应阻止应用启动，仅打印提示信息
            out.println("⚠  Banner 渲染失败: " + e.getMessage());
        }
    }

    // ===================== 私有辅助方法 =====================
    /**
     * 生成 Figlet ASCII Art 标题。
     *
     * <p>优先使用缓存的字体字节渲染，若字体不可用则降级为带前缀的纯文本。
     *
     * @param text 应用名称
     * @return ASCII Art 字符串
     */
    private String generateAsciiArt(String text) {
        if (FONT_BYTES == null || FONT_BYTES.length == 0) {
            // 字体资源缺失，降级为纯文本，保持输出整洁
            return "» " + text;
        }
        try (InputStream fontStream = new ByteArrayInputStream(FONT_BYTES)) {
            return FigletUtil.convertOneLine(fontStream, text);
        } catch (Exception _) {
            return "» " + text;
        }
    }

    /**
     * 构建 Swagger / SpringDoc 文档地址。
     *
     * <p>判断策略（按优先级）：
     * <ol>
     *   <li>优先通过 classpath 检测 springdoc 依赖（最可靠）</li>
     *   <li>其次检查 springdoc.api-docs.enabled 是否为 true</li>
     *   <li>降级使用 SpringFox Swagger2 地址</li>
     * </ol>
     *
     * @param environment Spring 环境
     * @param baseUrl     服务基础 URL
     * @return 文档完整地址
     */
    private String buildSwaggerUrl(Environment environment, String baseUrl) {
        if (isSpringdocPresent(environment)) {
            // SpringDoc (OpenAPI 3) 默认路径
            String uiPath = environment.getProperty(
                    "springdoc.swagger-ui.path", "/swagger-ui/index.html");
            return baseUrl + uiPath;
        }
        // 降级兼容 SpringFox Swagger 2
        return baseUrl + "/swagger-ui.html";
    }

    /**
     * 判断当前应用是否使用了 SpringDoc（OpenAPI 3）。
     *
     * <p>先通过 classpath 检测依赖是否存在（最准确），
     * 再结合配置项二次确认（排除 api-docs.enabled=false 的情况）。
     *
     * @param environment Spring 环境
     * @return true 表示使用 SpringDoc
     */
    private boolean isSpringdocPresent(Environment environment) {
        // 方式一：检测 SpringDoc 核心类是否在 classpath
        boolean onClasspath;
        try {
            Class.forName("org.springdoc.core.models.GroupedOpenApi");
            onClasspath = true;
        } catch (ClassNotFoundException _) {
            onClasspath = false;
        }

        if (!onClasspath) {
            return false;
        }

        // 方式二：排除 api-docs 被显式禁用的情况
        String enabled = environment.getProperty("springdoc.api-docs.enabled", "true");
        return !"false".equalsIgnoreCase(enabled);
    }

    /**
     * 获取本机非回环的 IPv4 地址。
     *
     * <p>获取策略：
     * <ol>
     *   <li>优先使用 {@link InetAddress#getLocalHost()} 返回的地址</li>
     *   <li>若为回环地址，则遍历网卡取第一个活跃的非回环 IPv4 地址</li>
     *   <li>均失败时降级返回 127.0.0.1</li>
     * </ol>
     *
     * @return 本机 IP 地址字符串
     */
    private String getHostAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress() && localHost instanceof Inet4Address) {
                return localHost.getHostAddress();
            }

            // 回退：遍历所有网卡，过滤出第一个合适的 IPv4
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                // 跳过：未启动、回环、虚拟（Docker/VMware 等虚拟网卡）
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 仅取非回环的 IPv4 地址，排除 IPv6
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception _) {
            // 网络接口枚举失败，降级处理
        }
        return FALLBACK_IP;
    }

    /**
     * 构建一行 Banner 信息，自动处理 ANSI 颜色兼容性。
     *
     * @param label 标签（左侧固定宽度）
     * @param value 值内容
     * @param color ANSI 颜色代码
     * @return 格式化后的一行字符串
     */
    private String line(String label, String value, String color) {
        // 固定标签宽度为 8 个字符，使输出对齐
        String formatted = String.format("  %-8s : %s", label, value);
        return colorize(formatted, color);
    }

    /**
     * 为文本包裹 ANSI 颜色代码。
     *
     * <p>若当前终端不支持 ANSI，直接返回原文本，避免 CI 日志出现乱码。
     *
     * @param text  原始文本
     * @param color ANSI 颜色代码
     * @return 带颜色的文本，或原始文本
     */
    private String colorize(String text, String color) {
        if (!ANSI_SUPPORTED) {
            return text;
        }
        return color + text + RESET;
    }

}