package com.mallcloud.codegen.config;

import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Properties;

/**
 * FreeMarker 模板引擎配置
 * 支持从 classpath:templates 和文件系统加载模板（便于在线编辑后实时生效）
 */
@Configuration
public class FreeMarkerConfig {

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        // 从 classpath 下的 templates 目录加载模板文件
        configurer.setTemplateLoaderPath("classpath:/templates/");
        configurer.setDefaultEncoding("UTF-8");

        // 详细配置属性
        Properties settings = new Properties();
        settings.put("default_encoding", "UTF-8");
        settings.put("template_exception_handler", TemplateExceptionHandler.RETHROW_HANDLER);
        settings.put("log_template_exceptions", "false");
        configurer.setFreemarkerSettings(settings);
        return configurer;
    }
}
