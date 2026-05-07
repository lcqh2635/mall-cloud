package com.mallcloud.commons.banner.theme;

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
import java.util.regex.Pattern;

/**
 * 自定义微服务启动 Banner 打印器（主入口）
 *
 * <p>职责：
 * <ol>
 *   <li>从 Spring {@link Environment} 和运行时收集所有信息，填充 {@link BannerContext}</li>
 *   <li>生成 Figlet ASCII Art 标题</li>
 *   <li>根据 {@link BannerProperties#getTheme()} 选择主题渲染器并委托输出</li>
 * </ol>
 *
 * <p>此类只负责数据收集与流程调度，具体样式输出由各 {@link com.mallcloud.commons.banner.theme.BannerTheme} 实现。
 *
 * <p>使用方式（在 SpringApplication 启动前设置）：
 * <pre>
 * SpringApplication app = new SpringApplication(AdminServerApplication.class);
 * app.setBanner(new FigletBannerPrinter());
 * app.run(args);
 * </pre>
 *
 * @author mallcloud
 * @see BannerProperties 配置项说明
 * @see BannerThemeFactory 主题注册与扩展
 */
public class FigletBannerPrinter implements Banner {

    // ===================== 默认值常量 =====================
    private static final String DEFAULT_PORT     = "8080";
    private static final String DEFAULT_IP       = "127.0.0.1";
    private static final String UNKNOWN          = "unknown";

    // ===================== 启动时间戳 =====================
    /**
     * JVM 启动时间戳（毫秒），用于计算启动耗时。
     * 在类加载时即记录，尽可能接近真实启动时刻。
     */
    private static final long JVM_START_TIME = System.currentTimeMillis();

    // ===================== 字体资源缓存 =====================
    /**
     * 字体文件字节缓存，类加载时一次性读取。
     * 使用 volatile 保证多线程可见性（虽然 Banner 通常单线程，保险起见）。
     *
     * <p>字体可在 application.yml 中通过 mallcloud.banner.font-path 自定义，
     * 但静态缓存加载时尚无配置，因此先加载默认路径，
     * 若配置了自定义路径则在 printBanner 中动态加载。
     */
    private static volatile byte[] defaultFontBytes;
    static {
        defaultFontBytes = loadFontBytes("/fonts/ANSI Shadow.flf");
    }

    // ===================== Banner 主体逻辑 =====================
    @Override
    public void printBanner(@NonNull Environment environment, Class<?> sourceClass, @NonNull PrintStream out) {
        try {
            // ---------- 1. 初始化配置 ----------
            BannerProperties props = buildProperties(environment);

            // ---------- 2. 初始化 ANSI 工具 ----------
            AnsiHelper ansi = new AnsiHelper(environment);

            // ---------- 3. 收集运行时数据 ----------
            BannerContext ctx = buildContext(environment, props);

            // ---------- 4. 生成 ASCII Art 标题 ----------
            String asciiTitle = generateAsciiArt(ctx.getAppName(), props.getFontPath());

            // ---------- 5. 选择主题并渲染输出 ----------
            BannerTheme theme = BannerThemeFactory.getTheme(props.getTheme());
            theme.render(out, ctx, props, asciiTitle, ansi);
        } catch (Exception e) {
            // 任何异常都不应阻止应用启动
            out.println("⚠  Banner 渲染失败: " + e.getMessage());
        }
    }

    // ===================== 数据收集 =====================
    /**
     * 从 Spring {@link Environment} 构建 {@link BannerProperties}。
     *
     * <p>由于 Banner 打印时 Spring 容器尚未完全启动，
     * {@code @ConfigurationProperties} 还未绑定，需手动读取各属性。
     *
     * @param env Spring 环境
     * @return 填充好的配置对象
     */
    private BannerProperties buildProperties(Environment env) {
        BannerProperties props = new BannerProperties();
        props.setVersion(env.getProperty("mallcloud.banner.version", "1.0.0"));
        props.setAuthor(env.getProperty("mallcloud.banner.author", UNKNOWN));
        props.setDescription(env.getProperty("mallcloud.banner.description", UNKNOWN));
        props.setProtocol(env.getProperty("mallcloud.banner.protocol", "http"));
        props.setTheme(env.getProperty("mallcloud.banner.theme", "default"));
        props.setFontPath(env.getProperty("mallcloud.banner.font-path", "/fonts/ANSI Shadow.flf"));

        // 显示开关
        BannerProperties.ShowConfig show = props.getShow();
        show.setPid(env.getProperty("mallcloud.banner.show.pid", Boolean.class, true));
        show.setCost(env.getProperty("mallcloud.banner.show.cost", Boolean.class, true));
        show.setAuthor(env.getProperty("mallcloud.banner.show.author", Boolean.class, true));
        show.setDescription(env.getProperty("mallcloud.banner.show.description", Boolean.class, true));
        show.setContextPath(env.getProperty("mallcloud.banner.show.context-path", Boolean.class, false));
        show.setDbUrl(env.getProperty("mallcloud.banner.show.db-url", Boolean.class, false));

        // 颜色配置
        BannerProperties.ColorConfig color = props.getColor();
        color.setTitle(env.getProperty("mallcloud.banner.color.title", Integer.class, 36));
        color.setSeparator(env.getProperty("mallcloud.banner.color.separator", Integer.class, 32));
        color.setLabel(env.getProperty("mallcloud.banner.color.label", Integer.class, 32));
        color.setValue(env.getProperty("mallcloud.banner.color.value", Integer.class, 33));

        return props;
    }

    /**
     * 收集所有运行时信息，构建 {@link BannerContext}。
     *
     * @param env   Spring 环境
     * @param props Banner 配置
     * @return 填充完整的上下文对象
     */
    private BannerContext buildContext(Environment env, BannerProperties props) {
        BannerContext ctx = new BannerContext();

        // ---------- 基础信息 ----------
        ctx.setAppName(env.getProperty("spring.application.name", "Unknown-Service"));
        ctx.setVersion(props.getVersion());
        ctx.setAuthor(props.getAuthor());
        ctx.setDescription(props.getDescription());
        ctx.setProtocol(props.getProtocol());

        // ---------- 网络信息 ----------
        String host = getHostAddress();
        String port = env.getProperty("server.port", DEFAULT_PORT);
        ctx.setHostAddress(host);
        ctx.setServerPort(port);

        String contextPath = env.getProperty("server.servlet.context-path", "");
        ctx.setContextPath(contextPath);

        // ---------- 环境与版本 ----------
        String profiles = String.join(", ", env.getActiveProfiles());
        ctx.setProfiles(profiles.isEmpty() ? "default" : profiles);
        ctx.setJavaVersion(String.valueOf(JavaVersion.getJavaVersion()));
        ctx.setSpringVersion(SpringVersion.getVersion());
        ctx.setSpringBootVersion(SpringBootVersion.getVersion());

        // ---------- 时间与进程 ----------
        ctx.setStartTime(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ctx.setStartupCost(calcStartupCost());
        ctx.setPid(String.valueOf(ProcessHandle.current().pid()));

        // ---------- 访问地址 ----------
        String baseUrl = props.getProtocol() + "://" + host + ":" + port + contextPath;
        ctx.setHealthUrl(baseUrl + "/actuator/health");
        ctx.setSwaggerUrl(buildSwaggerUrl(env, baseUrl));

        // ---------- 数据库地址（脱敏） ----------
        if (props.getShow().isDbUrl()) {
            ctx.setDbUrl(maskDbUrl(env.getProperty("spring.datasource.url", "")));
        }

        return ctx;
    }

    // ===================== 工具方法 =====================

    /**
     * 生成 Figlet ASCII Art 标题。
     *
     * <p>优先使用自定义字体路径，若与默认路径相同则使用缓存字节，
     * 否则重新读取。任何异常均降级为纯文本标题。
     *
     * @param text     应用名称
     * @param fontPath 字体文件 classpath 路径
     * @return ASCII Art 字符串，或降级的纯文本
     */
    private String generateAsciiArt(String text, String fontPath) {
        byte[] fontBytes;

        if ("/fonts/ANSI Shadow.flf".equals(fontPath)) {
            // 使用静态缓存，避免重复 IO
            fontBytes = defaultFontBytes;
        } else {
            // 自定义字体路径，动态加载
            fontBytes = loadFontBytes(fontPath);
        }

        if (fontBytes == null || fontBytes.length == 0) {
            return "» " + text;
        }

        try (InputStream stream = new ByteArrayInputStream(fontBytes)) {
            return FigletUtil.convertOneLine(stream, text);
        } catch (Exception e) {
            return "» " + text;
        }
    }

    /**
     * 从 classpath 加载字体文件为字节数组。
     *
     * @param path classpath 路径，如 "/fonts/ANSI Shadow.flf"
     * @return 字体字节数组，加载失败则返回 null
     */
    private static byte[] loadFontBytes(String path) {
        try (InputStream in = FigletBannerPrinter.class.getResourceAsStream(path)) {
            return in != null ? in.readAllBytes() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 计算从 JVM 启动到当前时刻的耗时。
     *
     * @return 格式化耗时字符串，如 "3.21 s"
     */
    private String calcStartupCost() {
        long elapsed = System.currentTimeMillis() - JVM_START_TIME;
        return String.format("%.2f s", elapsed / 1000.0);
    }

    /**
     * 获取本机非回环的 IPv4 地址。
     *
     * <p>获取策略：
     * <ol>
     *   <li>优先使用 {@link InetAddress#getLocalHost()} 返回的非回环 IPv4</li>
     *   <li>回退：遍历网卡，跳过未启动、回环、虚拟网卡，取第一个 IPv4</li>
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

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) return DEFAULT_IP;

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // 跳过：未启动的、回环的、虚拟的（Docker / VMware 虚拟网卡）
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
            // 网络接口枚举失败，降级处理
        }
        return DEFAULT_IP;
    }

    /**
     * 构建 Swagger / SpringDoc 接口文档地址。
     *
     * <p>判断逻辑：
     * <ol>
     *   <li>检测 springdoc 核心类是否在 classpath（最可靠）</li>
     *   <li>再确认 api-docs 未被显式禁用</li>
     *   <li>降级使用 Springfox Swagger2 地址</li>
     * </ol>
     *
     * @param env     Spring 环境
     * @param baseUrl 服务基础 URL
     * @return 完整文档 URL
     */
    private String buildSwaggerUrl(Environment env, String baseUrl) {
        boolean springdocOnClasspath;
        try {
            Class.forName("org.springdoc.core.models.GroupedOpenApi");
            springdocOnClasspath = true;
        } catch (ClassNotFoundException e) {
            springdocOnClasspath = false;
        }

        if (springdocOnClasspath) {
            String apiDocsEnabled = env.getProperty("springdoc.api-docs.enabled", "true");
            if (!"false".equalsIgnoreCase(apiDocsEnabled)) {
                String uiPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui/index.html");
                return baseUrl + uiPath;
            }
        }

        return baseUrl + "/swagger-ui.html";
    }

    /**
     * 对数据库连接 URL 进行密码脱敏处理。
     *
     * <p>将 URL 中 {@code password=xxx} 形式的密码替换为 {@code password=***}，
     * 适用于常见的 JDBC URL 格式。
     *
     * <p>示例：
     * <pre>
     * 输入：jdbc:mysql://127.0.0.1:3306/mall?password=secret&amp;useSSL=false
     * 输出：jdbc:mysql://127.0.0.1:3306/mall?password=***&amp;useSSL=false
     * </pre>
     *
     * @param url 原始数据库连接 URL
     * @return 脱敏后的 URL，若为空则返回空字符串
     */
    private String maskDbUrl(String url) {
        if (url == null || url.isBlank()) return "";
        // 匹配 password=任意非&字符，替换密码部分为 ***
        return Pattern.compile("(password=)[^&\\s]+", Pattern.CASE_INSENSITIVE)
                .matcher(url)
                .replaceAll("$1***");
    }
}