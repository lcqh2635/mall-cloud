package com.mallcloud.commons.banner.listener;

import com.mallcloud.commons.banner.printer.FigletBannerPrinter;
import com.mallcloud.commons.banner.printer.ImageBannerPrinter;
import com.mallcloud.commons.banner.printer.TextBannerPrinter;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;
import java.time.Instant;

// 可参考 Spring Boot 官方的示例类 EventPublishingRunListener
public class MallCloudSpringApplicationRunListener implements SpringApplicationRunListener, Ordered {

    private final SpringApplication application;
    private final String[] args;
    private Instant startTime;
    private Instant contextPreparedTime;

    /**
     * 必须的构造函数
     * Spring Boot 会通过反射调用这个构造函数
     */
    public MallCloudSpringApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
        System.out.println("CustomSpringApplicationRunListener 构造函数被调用");
    }

    @Override
    public void starting(@NonNull ConfigurableBootstrapContext bootstrapContext) {
        startTime = Instant.now();
        System.out.println("🚀 [阶段1 - starting] 应用启动开始");
        System.out.println("   启动时间: " + startTime);
        System.out.println("   主应用类: " + application.getMainApplicationClass());
        System.out.println("   命令行参数: " + String.join(", ", args));
    }

    @Override
    public void environmentPrepared(@NonNull ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        System.out.println("🌍 [阶段2 - environmentPrepared] 环境准备完成");
        System.out.println("   激活的配置文件: " + String.join(", ", environment.getActiveProfiles()));
        System.out.println("   默认配置文件: " + String.join(", ", environment.getDefaultProfiles()));
        System.out.println("   应用名称: " + environment.getProperty("spring.application.name", "未设置"));
        switch (environment.getProperty("custom.banner.type", "figlet")) {
            case "text":
                new TextBannerPrinter().printBanner(environment, application.getMainApplicationClass(), System.out);
                break;
            case "figlet":
                new FigletBannerPrinter().printBanner(environment, application.getMainApplicationClass(), System.out);
                break;
            case "image":
                new ImageBannerPrinter().printBanner(environment, application.getMainApplicationClass(), System.out);
                break;
        }
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        contextPreparedTime = Instant.now();
        System.out.println("🔧 [阶段3 - contextPrepared] 应用上下文创建完成");
        System.out.println("   上下文ID: " + context.getId());
        System.out.println("   上下文显示名称: " + context.getDisplayName());
        System.out.println("   启动阶段耗时: " +
                Duration.between(startTime, contextPreparedTime).toMillis() + "ms");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        Instant contextLoadedTime = Instant.now();
        System.out.println("📦 [阶段4 - contextLoaded] Bean定义加载完成");
        System.out.println("   Bean定义数量: " + context.getBeanDefinitionCount());
        System.out.println("   上下文准备耗时: " +
                Duration.between(contextPreparedTime, contextLoadedTime).toMillis() + "ms");
    }

    @Override
    public void started(@NonNull ConfigurableApplicationContext context, Duration timeTaken) {
        System.out.println("✅ [阶段5 - started] 应用上下文刷新完成");
        if (timeTaken != null) {
            System.out.println("   启动总耗时: " + timeTaken.toMillis() + "ms");
        }
        System.out.println("   所有Bean定义: " + String.join(", ", context.getBeanDefinitionNames()));
    }

    @Override
    public void ready(@NonNull ConfigurableApplicationContext context, Duration timeTaken) {
        System.out.println("🎉 [阶段6 - ready] 应用完全启动完成");
        if (timeTaken != null) {
            System.out.println("   就绪阶段耗时: " + timeTaken.toMillis() + "ms");
        }
        System.out.println("   总启动时间: " +
                Duration.between(startTime, Instant.now()).toMillis() + "ms");
    }

    @Override
    public void failed(ConfigurableApplicationContext context, @NonNull Throwable exception) {
        SpringApplicationRunListener.super.failed(context, exception);
        System.out.println("❌ [阶段7 - failed] 应用启动失败");
        System.out.println("   失败原因: " + exception.getMessage());
        System.out.println("   失败时间: " + Instant.now());
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
