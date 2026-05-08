package com.mallcloud.commons.banner.autoconfigure;

import com.mallcloud.commons.banner.BannerAutoConfiguration;
import com.mallcloud.commons.banner.theme.FigletBannerPrinter;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Banner 注入器 —— EnvironmentPostProcessor 实现（推荐方案）
 *
 * <p>相比 {@link 'ApplicationContextInitializer'}，此方案更可靠：
 * <ul>
 *   <li>{@link EnvironmentPostProcessor} 在 Environment 准备完毕后、
 *       ApplicationContext 创建之前被调用，此时可直接拿到 {@link SpringApplication} 引用，
 *       无需反射，更加安全稳定</li>
 *   <li>这也是 Spring Boot 官方推荐用于修改启动行为的扩展点</li>
 * </ul>
 *
 * <p>执行时序：
 * <pre>
 * SpringApplication.run()
 *   └─ prepareEnvironment()
 *        └─ EnvironmentPostProcessorApplicationListener
 *             └─ 触发所有 EnvironmentPostProcessor
 *                  └─ BannerEnvironmentPostProcessor.postProcessEnvironment()
 *                       └─ springApplication.setBanner(new FigletBannerPrinter())
 *   └─ printBanner()    ← Banner 在这里使用我们注入的实现打印
 *   └─ createApplicationContext()
 * </pre>
 *
 * <p>注册方式（二选一，同时写两个可兼容 Spring Boot 2.x 和 3.x）：
 * <pre>
 * # META-INF/spring.factories（Spring Boot 2.x）
 * org.springframework.boot.env.EnvironmentPostProcessor=\
 *   com.mallcloud.commons.banner.autoconfigure.BannerEnvironmentPostProcessor
 *
 * # META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor
 * # （Spring Boot 3.x，内容只写类名即可）
 * com.mallcloud.commons.banner.autoconfigure.BannerEnvironmentPostProcessor
 * </pre>
 *
 * @author mallcloud
 * @see BannerAutoConfiguration
 */
@Order(Ordered.LOWEST_PRECEDENCE - 10) // 靠后执行，确保 Environment 配置已完整加载
public class BannerEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        // ── 开关检查 ──────────────────────────────────────────────────────────

        // 检查自定义开关：mallcloud.banner.enabled=false 时跳过
        String enabled = environment.getProperty("mallcloud.banner.enabled", "true");
        if ("false".equalsIgnoreCase(enabled)) {
            return;
        }

        // 检查 Spring Boot 官方开关：spring.main.banner-mode=off 时跳过
        // （尊重用户已有的 Banner 关闭配置，不强制覆盖）
        String bannerMode = environment.getProperty("spring.main.banner-mode", "console");
        if ("off".equalsIgnoreCase(bannerMode)) {
            return;
        }

        // ── 注入自定义 Banner ─────────────────────────────────────────────────

        // 此处可以直接调用 application.setBanner()，无需任何反射！
        // 因为 EnvironmentPostProcessor 的方法签名里直接传入了 SpringApplication 实例。
        // 这正是此方案比 ApplicationContextInitializer 更推荐的核心原因。
        application.setBanner(new FigletBannerPrinter());
    }
}