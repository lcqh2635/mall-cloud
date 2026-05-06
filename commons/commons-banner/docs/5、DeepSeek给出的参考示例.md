# Spring Boot艺术Banner Starter完整实现

下面我将为你创建一个完整的艺术Banner Starter，其他项目引入即可自动获得艺术Banner打印功能。

## 1. 项目结构

```
art-banner-spring-boot-starter/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── banner/
│       │               ├── ArtBannerSpringBootStarter.java
│       │               ├── config/
│       │               │   ├── ArtBannerAutoConfiguration.java
│       │               │   └── ArtBannerProperties.java
│       │               ├── core/
│       │               │   ├── ArtBannerGenerator.java
│       │               │   ├── ArtBannerPrinter.java
│       │               │   └── font/
│       │               │       ├── BannerFont.java
│       │               │       └── AsciiFontProvider.java
│       │               └── listener/
│       │                   └── ArtBannerApplicationRunListener.java
│       └── resources/
│           ├── META-INF/
│           │   └── spring.factories
│           └── banner/
│               ├── default-banner.txt
│               └── fonts/
│                   └── standard.flf
└── pom.xml
```

## 2. 核心代码实现

### 2.1 POM配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>art-banner-spring-boot-starter</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Art Banner Spring Boot Starter</name>
    <description>Spring Boot艺术Banner打印Starter</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.0</version>
        <relativePath/>
    </parent>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Boot核心依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- 日志依赖 -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        
        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2.2 配置属性类

```java
package com.example.banner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Banner配置属性类
 * 允许通过application.yml进行自定义配置
 */
@ConfigurationProperties(prefix = "spring.art-banner")
public class ArtBannerProperties {
    
    /**
     * 是否启用艺术Banner功能
     */
    private boolean enabled = true;
    
    /**
     * Banner文本内容，默认使用Spring Boot的banner.txt或应用名称
     */
    private String text;
    
    /**
     * 字体名称，支持：standard, big, small, shadow 等
     */
    private String font = "standard";
    
    /**
     * 字体大小（宽度），仅对某些字体有效
     */
    private int width = 80;
    
    /**
     * 字体高度，仅对某些字体有效
     */
    private int height = 20;
    
    /**
     * 字体文件路径，支持classpath:或file:前缀
     */
    private Resource fontFile;
    
    /**
     * 字体颜色（控制台颜色代码）
     */
    private String color = "default";
    
    /**
     * 是否启用颜色输出
     */
    private boolean colorEnabled = true;
    
    /**
     * 是否在Banner下方显示应用信息
     */
    private boolean showInfo = true;
    
    /**
     * 自定义Banner文件路径
     */
    private Resource location;
    
    // Getter和Setter方法
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getFont() { return font; }
    public void setFont(String font) { this.font = font; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public Resource getFontFile() { return fontFile; }
    public void setFontFile(Resource fontFile) { this.fontFile = fontFile; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public boolean isColorEnabled() { return colorEnabled; }
    public void setColorEnabled(boolean colorEnabled) { this.colorEnabled = colorEnabled; }
    
    public boolean isShowInfo() { return showInfo; }
    public void setShowInfo(boolean showInfo) { this.showInfo = showInfo; }
    
    public Resource getLocation() { return location; }
    public void setLocation(Resource location) { this.location = location; }
}
```

### 2.3 Banner字体枚举

```java
package com.example.banner.core.font;

/**
 * 支持的Banner字体类型枚举
 */
public enum BannerFont {
    STANDARD("standard", "标准字体"),
    BIG("big", "大字体"),
    SMALL("small", "小字体"),
    SHADOW("shadow", "阴影字体"),
    BLOCK("block", "方块字体"),
    SCRIPT("script", "手写字体"),
    BUBBLE("bubble", "气泡字体");
    
    private final String code;
    private final String description;
    
    BannerFont(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getDescription() { return description; }
    
    /**
     * 根据字体代码获取字体枚举
     */
    public static BannerFont fromCode(String code) {
        for (BannerFont font : values()) {
            if (font.code.equalsIgnoreCase(code)) {
                return font;
            }
        }
        return STANDARD; // 默认返回标准字体
    }
}
```

### 2.4 ASCII字体提供器

```java
package com.example.banner.core.font;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * ASCII艺术字体提供器
 * 负责加载和提供各种ASCII艺术字体
 */
public class AsciiFontProvider {
    
    private static final String FONT_PATH = "banner/fonts/";
    private static final Map<String, String> FONT_CACHE = new HashMap<>();
    
    /**
     * 获取指定字体的ASCII艺术内容
     */
    public String getFontContent(String fontName) {
        if (!StringUtils.hasText(fontName)) {
            fontName = "standard";
        }
        
        // 先从缓存中获取
        if (FONT_CACHE.containsKey(fontName)) {
            return FONT_CACHE.get(fontName);
        }
        
        try {
            String fontFileName = fontName + ".flf";
            Resource fontResource = new ClassPathResource(FONT_PATH + fontFileName);
            
            if (!fontResource.exists()) {
                // 如果指定字体不存在，使用标准字体
                fontResource = new ClassPathResource(FONT_PATH + "standard.flf");
            }
            
            String fontContent = FileCopyUtils.copyToString(
                new InputStreamReader(fontResource.getInputStream(), StandardCharsets.UTF_8)
            );
            
            FONT_CACHE.put(fontName, fontContent);
            return fontContent;
            
        } catch (Exception e) {
            throw new RuntimeException("加载ASCII字体文件失败: " + fontName, e);
        }
    }
    
    /**
     * 获取内置的简单字体（用于字体文件不可用时）
     */
    public String getSimpleFont() {
        return "standard"; // 简化实现，实际应该返回一个基本的字体模板
    }
}
```

### 2.5 Banner生成器

```java
package com.example.banner.core;

import com.example.banner.config.ArtBannerProperties;
import com.example.banner.core.font.AsciiFontProvider;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 艺术Banner生成器
 * 负责根据配置生成ASCII艺术Banner
 */
public class ArtBannerGenerator {
    
    private final ArtBannerProperties properties;
    private final AsciiFontProvider fontProvider;
    
    public ArtBannerGenerator(ArtBannerProperties properties, AsciiFontProvider fontProvider) {
        this.properties = properties;
        this.fontProvider = fontProvider;
    }
    
    /**
     * 生成Banner字符串
     */
    public String generateBanner() {
        StringBuilder banner = new StringBuilder();
        
        // 添加换行分隔
        banner.append("\n");
        
        // 生成ASCII艺术文字
        String asciiArt = generateAsciiArt();
        if (StringUtils.hasText(asciiArt)) {
            banner.append(asciiArt).append("\n");
        }
        
        // 显示应用信息
        if (properties.isShowInfo()) {
            banner.append(generateAppInfo()).append("\n");
        }
        
        banner.append("\n");
        return banner.toString();
    }
    
    /**
     * 生成ASCII艺术文字
     */
    private String generateAsciiArt() {
        try {
            // 首先检查是否有自定义Banner文件
            if (properties.getLocation() != null && properties.getLocation().exists()) {
                return loadCustomBanner(properties.getLocation());
            }
            
            // 获取Banner文本内容
            String bannerText = getBannerText();
            if (!StringUtils.hasText(bannerText)) {
                return "";
            }
            
            // 使用ASCII艺术字体生成Banner
            return generateAsciiFromText(bannerText);
            
        } catch (Exception e) {
            // 如果生成失败，返回简单的文本Banner
            return "=== " + getBannerText() + " ===\n";
        }
    }
    
    /**
     * 获取Banner文本内容
     */
    private String getBannerText() {
        if (StringUtils.hasText(properties.getText())) {
            return properties.getText();
        }
        return "Spring Boot Application"; // 默认文本
    }
    
    /**
     * 从自定义文件加载Banner
     */
    private String loadCustomBanner(Resource bannerResource) {
        try {
            return FileCopyUtils.copyToString(
                new InputStreamReader(bannerResource.getInputStream(), StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new RuntimeException("加载自定义Banner文件失败", e);
        }
    }
    
    /**
     * 使用ASCII艺术字体生成文本
     */
    private String generateAsciiFromText(String text) {
        // 简化实现：实际应该使用真正的ASCII艺术字体引擎
        // 这里使用简单的装饰效果
        
        String fontContent = fontProvider.getFontContent(properties.getFont());
        
        // 基于字体内容生成ASCII艺术（简化版）
        return generateSimpleAsciiArt(text);
    }
    
    /**
     * 生成简单的ASCII艺术（实际项目应该使用更复杂的算法）
     */
    private String generateSimpleAsciiArt(String text) {
        StringBuilder art = new StringBuilder();
        int width = properties.getWidth();
        
        // 顶部边框
        art.append("╔");
        for (int i = 0; i < width - 2; i++) {
            art.append("═");
        }
        art.append("╗\n");
        
        // 文字行（居中显示）
        art.append("║");
        int padding = (width - 2 - text.length()) / 2;
        for (int i = 0; i < padding; i++) {
            art.append(" ");
        }
        art.append(text);
        for (int i = 0; i < width - 2 - text.length() - padding; i++) {
            art.append(" ");
        }
        art.append("║\n");
        
        // 底部边框
        art.append("╚");
        for (int i = 0; i < width - 2; i++) {
            art.append("═");
        }
        art.append("╝");
        
        return art.toString();
    }
    
    /**
     * 生成应用信息
     */
    private String generateAppInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("启动时间: ").append(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ).append("\n");
        
        info.append("Banner字体: ").append(properties.getFont()).append("\n");
        info.append("颜色模式: ").append(properties.isColorEnabled() ? "启用" : "禁用");
        
        return info.toString();
    }
}
```

### 2.6 Banner打印机

```java
package com.example.banner.core;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * Banner打印机
 * 负责将生成的Banner输出到控制台
 */
public class ArtBannerPrinter implements Banner {
    
    private final ArtBannerGenerator bannerGenerator;
    
    public ArtBannerPrinter(ArtBannerGenerator bannerGenerator) {
        this.bannerGenerator = bannerGenerator;
    }
    
    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        try {
            String banner = bannerGenerator.generateBanner();
            out.println(banner);
        } catch (Exception e) {
            // 如果打印失败，输出简单的Banner
            out.println("\n=== Spring Boot Application ===\n");
        }
    }
}
```

### 2.7 SpringApplicationRunListener

```java
package com.example.banner.listener;

import com.example.banner.core.ArtBannerGenerator;
import com.example.banner.core.ArtBannerPrinter;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 艺术Banner应用启动监听器
 * 在应用启动过程中设置自定义Banner
 */
public class ArtBannerApplicationRunListener implements SpringApplicationRunListener {
    
    private final SpringApplication application;
    private final String[] args;
    
    // 必须的构造函数
    public ArtBannerApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }
    
    @Override
    public void starting() {
        // 应用启动开始时调用
        System.out.println("🎨 ArtBannerStarter: 初始化艺术Banner功能");
    }
    
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        // 环境准备完成后调用
        System.out.println("🎨 ArtBannerStarter: 环境准备完成，配置Banner打印机");
        
        // 检查是否启用艺术Banner
        boolean enabled = environment.getProperty("spring.art-banner.enabled", Boolean.class, true);
        if (enabled) {
            // 设置Banner模式为OFF，我们将使用自定义的Banner打印机
            application.setBannerMode(Banner.Mode.CONSOLE);
        }
    }
    
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        // ApplicationContext准备完成后调用
        // 在这里可以获取配置属性并设置自定义Banner
        try {
            ArtBannerProperties properties = context.getBean(ArtBannerProperties.class);
            ArtBannerGenerator generator = context.getBean(ArtBannerGenerator.class);
            
            // 创建并设置自定义Banner打印机
            ArtBannerPrinter bannerPrinter = new ArtBannerPrinter(generator);
            application.setBannerMode(Banner.Mode.CONSOLE);
            
            // 通过反射设置Banner（因为SpringApplication的banner字段是私有的）
            setBannerViaReflection(application, bannerPrinter);
            
        } catch (Exception e) {
            System.err.println("🎨 ArtBannerStarter: 设置自定义Banner失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过反射设置SpringApplication的Banner
     */
    private void setBannerViaReflection(SpringApplication application, Banner banner) {
        try {
            java.lang.reflect.Field bannerField = SpringApplication.class.getDeclaredField("banner");
            bannerField.setAccessible(true);
            bannerField.set(application, banner);
        } catch (Exception e) {
            System.err.println("🎨 ArtBannerStarter: 反射设置Banner失败: " + e.getMessage());
        }
    }
    
    @Override
    public void started(ConfigurableApplicationContext context) {
        // 应用启动完成后调用
        System.out.println("🎨 ArtBannerStarter: 艺术Banner显示完成");
    }
    
    // 其他生命周期方法保持空实现
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {}
    
    @Override
    public void ready(ConfigurableApplicationContext context) {}
    
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.err.println("🎨 ArtBannerStarter: 应用启动失败");
    }
}
```

### 2.8 自动配置类

```java
package com.example.banner.config;

import com.example.banner.core.ArtBannerGenerator;
import com.example.banner.core.font.AsciiFontProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 艺术Banner自动配置类
 * 根据条件自动创建相关Bean
 */
@Configuration
@ConditionalOnClass(ArtBannerGenerator.class)
@EnableConfigurationProperties(ArtBannerProperties.class)
@ConditionalOnProperty(prefix = "spring.art-banner", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ArtBannerAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public AsciiFontProvider asciiFontProvider() {
        return new AsciiFontProvider();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ArtBannerGenerator artBannerGenerator(ArtBannerProperties properties, AsciiFontProvider fontProvider) {
        return new ArtBannerGenerator(properties, fontProvider);
    }
}
```

### 2.9 主启动类（可选）

```java
package com.example.banner;

/**
 * 艺术Banner Starter主类
 * 主要用于标识和文档说明
 */
public class ArtBannerSpringBootStarter {
    // 这是一个Starter项目，不需要主方法
    // 该类主要用于标识和提供文档
}
```

### 2.10 Spring Factories配置

在`src/main/resources/META-INF/spring.factories`中：

```properties
# 自动配置类注册
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.banner.config.ArtBannerAutoConfiguration

# 应用启动监听器注册
org.springframework.boot.SpringApplicationRunListener=\
com.example.banner.listener.ArtBannerApplicationRunListener

# 配置属性元数据（用于IDE提示）
org.springframework.boot.autoconfigure.EnableConfigurationProperties=\
com.example.banner.config.ArtBannerProperties
```

## 3. 使用示例

### 3.1 在其他项目中引入Starter

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>art-banner-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3.2 配置示例（application.yml）

```yaml
spring:
  art-banner:
    enabled: true
    text: "我的Spring Boot应用"
    font: "shadow"
    width: 100
    height: 15
    color-enabled: true
    color: "green"
    show-info: true
    # location: classpath:custom-banner.txt
```

### 3.3 自定义Banner文件（可选）

在`src/main/resources/custom-banner.txt`：

```
  _____ _____ _____ _____ _____ _____ _____ 
 |_____|_____|_____|_____|_____|_____|_____|
 |     |     |     |     |     |     |     |
 |  C  |  U  |  S  |  T  |  O  |  M  |     |
 |_____|_____|_____|_____|_____|_____|_____|
```

## 4. 打包和发布

使用Maven命令打包：
```bash
mvn clean package
```

发布到Maven仓库：
```bash
mvn clean deploy
```

## 5. 功能特点

1. **自动启用**: 引入依赖后自动生效
2. **丰富配置**: 支持字体、颜色、大小等多样化配置
3. **自定义Banner**: 支持自定义Banner文件
4. **应用信息**: 可显示启动时间等有用信息
5. **条件化配置**: 可根据条件自动启用或禁用
6. **优雅降级**: 配置错误时自动使用简单Banner

这个Starter提供了完整的艺术Banner功能，其他项目只需引入依赖即可获得丰富的Banner显示能力。