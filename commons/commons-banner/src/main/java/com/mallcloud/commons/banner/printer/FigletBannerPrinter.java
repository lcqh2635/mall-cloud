package com.mallcloud.commons.banner.printer;

import com.mallcloud.commons.banner.utils.FigletUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FigletBannerPrinter implements Banner {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    @Override
    public void printBanner(@NonNull Environment environment, Class<?> sourceClass, @NonNull PrintStream out) {
        try {
            // 获取服务名（动态！）
            String appName = environment.getProperty("spring.application.name", "Unknown-Service").toUpperCase();

            // ✅ 动态生成 ASCII Art 标题
            String asciiArtTitle = generateAsciiArt(appName);

            // 其他信息
            String serverPort = environment.getProperty("server.port", "8080");
            String profiles = String.join(",", environment.getActiveProfiles());
            String version = environment.getProperty("info.app.version", "1.0.0");
            String springBootVersion = SpringBootVersion.getVersion();
            String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String author = environment.getProperty("custom.banner.author", "unknown");
            String hostAddress = getHostAddress();
            String baseUrl = "https://" + hostAddress + ":" + serverPort;
            String healthUrl = baseUrl + "/actuator/health";
            String swaggerUrl = baseUrl + (isSwaggerV3(environment) ? "/swagger-ui/index.html" : "/swagger-ui.html");

            // 打印横幅
            out.println(GREEN + "===============================================" + RESET);
            out.println(CYAN + asciiArtTitle + RESET); // ✅ 动态 ASCII Art 标题
            out.println(GREEN + "===============================================" + RESET);
            out.println("  服务端口    : " + YELLOW + "🌐 " + serverPort + RESET);
            out.println("  服务IP      : " + CYAN + "📍 " + hostAddress + RESET);
            out.println("  激活环境    : " + GREEN + "🧪 " + (profiles.isEmpty() ? "default" : profiles) + RESET);
            out.println("  服务版本    : " + YELLOW + "🏷️  " + version + RESET);
            out.println("  Spring Boot : " + GREEN + "🌱 " + springBootVersion + RESET);
            out.println("  启动时间    : " + YELLOW + "🕙 " + startTime + RESET);
            out.println("  项目作者    : " + CYAN + "📍 " + author + RESET);
            out.println("  健康检查    : " + CYAN + "🩺 " + healthUrl + RESET);
            out.println("  Swagger文档 : " + GREEN + "📚 " + swaggerUrl + RESET);
            out.println(GREEN + "===============================================" + RESET);
        } catch (Exception e) {
            out.println("⚠️  横幅打印异常: " + e.getMessage());
        }
    }

    // ✅ 核心方法：动态生成 ASCII Art
    private String generateAsciiArt(String text) {
        try {
            // 可选的字体文件下载，参考官网 https://github.com/xero/figlet-fonts/tree/master
            // 或者直接去 ASCII 艺术字生成工具推荐：https://patorjk.com/software/taag/#p=testall 去在线挑选

            // figlet 内置字体 "slant"（推荐），也可换 "standard",
            // return FigletFont.convertOneLine(text); // 默认 slant
            // return FigletFont.convertOneLine(text, "standard");

            // 自定义字体，figlet 工具包默认只提供了两个字体，我不喜欢。可以到 https://github.com/xero/figlet-fonts/tree/master 选择自己喜欢的字体
            InputStream fontStream = getClass().getResourceAsStream("/fonts/ANSI Shadow.flf");
//             InputStream fontStream = getClass().getResourceAsStream("/fonts/ANSI Regular.flf");

            // 使用内置字体 "slant"， "standard"(默认) 两种可选字体，也可以下载并使用自定义字体
            return FigletUtil.convertOneLine(fontStream, text);
        } catch (Exception e) {
            // 降级：如果字体转换失败，返回原始文本
            return "🚀 " + text;
        }
    }

    // 判断是否 Swagger V3
    private boolean isSwaggerV3(Environment environment) {
        return environment.getProperty("springdoc.api-docs.enabled", Boolean.class, false) ||
                environment.containsProperty("springdoc.swagger-ui.path");
    }

    // 获取主机 IP
    private String getHostAddress() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            if (address.isLoopbackAddress()) {
                return String.valueOf(InetAddress.getByName(java.net.NetworkInterface
                        .getNetworkInterfaces().nextElement().getInetAddresses().nextElement().getHostAddress()));
            }
            return address.getHostAddress();
        } catch (Exception _) {
            return "127.0.0.1";
        }
    }

}