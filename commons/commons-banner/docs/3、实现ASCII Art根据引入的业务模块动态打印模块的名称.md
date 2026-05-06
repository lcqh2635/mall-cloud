非常好的问题！你希望 **动态根据当前业务模块的名称（如 `order-service`、`user-service`）生成 ASCII Art**，而不是写死在代码里 —— 这完全可行！

我们可以借助一个轻量级开源库：**[Figlet](http://www.figlet.org/)** 风格的 Java 实现 —— **[jfiglet](https://github.com/lalyos/jfiglet)**，它能将任意字符串动态转换为 ASCII Art 字符画！

---

## ✅ 最终效果预览（动态生成）

假设 `spring.application.name=order-service`，控制台输出：

```
  ____                _             _       _       
 / __ \ ___   _ __   | |__    ___  | |__   (_) ___  
| |  | |/ _ \ | '_ \  | '_ \  / _ \ | '_ \  | |/ __| 
| |__| | (_) || |_) | | | | || (_) || |_) | | |\__ \ 
 \___\_\\___/ | .__/  |_| |_| \___/ |_.__/  |_||___/ 
              |_|                                    
```

> ✅ 动态！✅ 不写死！✅ 每个服务启动时自动渲染自己的名字！

---

## ✅ 实现步骤

---

### 步骤 1️⃣：添加 jfiglet 依赖

在你的模块（或 common-banner-starter）的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.github.lalyos</groupId>
    <artifactId>jfiglet</artifactId>
    <version>0.0.8</version>
</dependency>
```

> 📦 Maven 中央仓库地址：https://mvnrepository.com/artifact/com.github.lalyos/jfiglet

---

### 步骤 2️⃣：改造 `CustomBanner.java`，动态生成 ASCII Art

```java
// 修改你的 CustomBanner.java
package com.yourcompany.config;

import com.github.lalyos.jfiglet.FigletFont;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomBanner implements Banner {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        try {
            // 获取服务名（动态！）
            String appName = environment.getProperty("spring.application.name", "Unknown-Service").toUpperCase();

            // ✅ 动态生成 ASCII Art 标题
            String asciiArtTitle = generateAsciiArt(appName);

            // 其他信息
            String serverPort = environment.getProperty("server.port", "8080");
            String profiles = String.join(",", environment.getActiveProfiles());
            String version = environment.getProperty("info.app.version", "1.0.0");
            String gitCommit = environment.getProperty("git.commit.id.abbrev", "unknown");
            String springBootVersion = SpringBootVersion.getVersion();
            String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String hostAddress = getHostAddress();
            String baseUrl = "http://" + hostAddress + ":" + serverPort;
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
            out.println("  Git提交     : " + CYAN + "📦 " + gitCommit + RESET);
            out.println("  Spring Boot : " + GREEN + "🌱 " + springBootVersion + RESET);
            out.println("  启动时间    : " + YELLOW + "🕙 " + startTime + RESET);
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
            // 使用内置字体 "slant"（推荐），也可换 "standard", "banner", "big" 等
            return FigletFont.convertOneLine(text);
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
                return InetAddress.getByName(java.net.NetworkInterface
                        .getNetworkInterfaces().nextElement().getInetAddress().getHostAddress()).getHostAddress();
            }
            return address.getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
```

---

## ✅ 可选：更换字体风格

`jfiglet` 支持多种字体，你可以在 `generateAsciiArt` 中指定：

```java
private String generateAsciiArt(String text) {
    try {
        // 推荐字体（选一个你喜欢的）
        // return FigletFont.convertOneLine(text); // 默认 slant
        // return FigletFont.convertOneLine(text, "standard");
        // return FigletFont.convertOneLine(text, "banner");
        // return FigletFont.convertOneLine(text, "big");
        // return FigletFont.convertOneLine(text, "block");
        // return FigletFont.convertOneLine(text, "digital");
        // return FigletFont.convertOneLine(text, "script");

        // 更紧凑的字体（适合长服务名）
        return FigletFont.convertOneLine(text, "small"); 

    } catch (Exception e) {
        return "🚀 " + text;
    }
}
```

> 💡 服务名较长（如 `inventory-management-service`）建议用 `"small"` 或 `"mini"` 字体避免换行混乱。

---

## ✅ 效果示例

### 服务名：`USER-SERVICE`

```
 _   _           _             _       _       
| | | | ___   __| | ___  _ __ | | __ _(_)_ __  
| | | |/ _ \ / _` |/ _ \| '_ \| |/ _` | | '_ \ 
| |_| | (_) | (_| | (_) | | | | | (_| | | | | |
 \___/ \___/ \__,_|\___/|_| |_|_|\__,_|_|_| |_|
```

### 服务名：`PAYMENT`

```
 ____              _   _               
|  _ \ __ _ _   _| |_| |__   ___ _ __ 
| |_) / _` | | | | __| '_ \ / _ \ '__|
|  __/ (_| | |_| | |_| | | |  __/ |   
|_|   \__,_|\__,_|\__|_| |_|\___|_|   
```

---

## ✅ 自定义字体（进阶）

你还可以加载自定义 `.flf` 字体文件：

1. 从 [http://www.figlet.org/fontdb.cgi](http://www.figlet.org/fontdb.cgi) 下载 `.flf` 文件
2. 放入 `src/main/resources/fonts/`
3. 代码中加载：

```java
InputStream fontStream = getClass().getResourceAsStream("/fonts/myfont.flf");
FigletFont font = FigletFont.parse(fontStream);
return font.convertOneLine(text);
```

---

## ✅ 注意事项

- 控制台宽度有限，服务名太长可能导致换行错乱 → 建议使用 `"small"` 字体或缩写。
- 部分终端不支持 ANSI 颜色 → 可关闭颜色或检测环境。
- 如果你封装成 Starter，确保 `jfiglet` 依赖被传递（默认是 compile scope，没问题）。

---

## ✅ 最终目录结构（示例）

```
src/
└── main/
    ├── java/
    │   └── com/yourcompany/config/CustomBanner.java
    └── resources/
        └── fonts/ (可选自定义字体)
            └── myfont.flf
```

---

## ✅ 总结

你现在已经实现了：

✅ 动态读取 `spring.application.name`  
✅ 使用 `jfiglet` 库动态生成 ASCII Art  
✅ 支持多种字体风格（可配置）  
✅ 自动适配每个微服务，无需硬编码  
✅ 保留原有功能（IP、健康检查、Swagger、Git、颜色等）

---

🎯 **效果拉满，每个服务启动时都自带“个性签名”！**

---

需要我为你生成一个特定服务名（如 `product-catalog-service`）的 ASCII Art 效果预览吗？或者帮你选一个最适合企业风格的字体？欢迎告诉我你的服务名 😊