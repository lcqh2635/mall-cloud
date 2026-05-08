package com.mallcloud.commons.banner;

import com.mallcloud.commons.banner.config.MallCloudBannerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类，用于在满足条件时注册自定义 Banner。
 * 条件：
 *   - 存在 spring.banner.enabled 配置项且值为 true（或未设置，默认 true）
 *   - 应用中引入了 spring-boot 的 Banner 类（即正常 Spring Boot 环境）
 *   - 容器中尚无其他 Banner Bean（避免冲突）
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MallCloudBannerProperties.class)
@ConditionalOnProperty(
    prefix = "mallcloud.banner",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class BannerAutoConfiguration {

    /**
     * 注册自定义 Banner Bean。
     * Spring Boot 启动时会自动检测容器中的 Banner Bean 并使用它。
     */
    /**
     * 注册自定义 Banner Bean
     * 此 Bean 将被 Spring Boot 自动发现并用于替换默认 Banner
     * Spring Boot 在启动时会查找类型为 Banner 的 Bean，若存在则使用它替代默认实现
     *
     * @param properties 从 application-example.yaml 中注入的配置属性对象
     * @return CustomBanner 实例
     */
//    @Bean
//    public Banner customBanner(CustomBannerProperties properties) {
//        // 从配置中获取自定义文本，若为空则使用默认 ASCII 艺术字
//        String customText = properties.getApplicationName();
//        boolean enabled = properties.isEnabled();
//
//        // 创建并返回自定义 Banner 实例
//        return new CustomBanner(customText, enabled);
//    }
}