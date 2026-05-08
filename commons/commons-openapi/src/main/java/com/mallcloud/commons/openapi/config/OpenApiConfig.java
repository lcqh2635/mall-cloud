package com.mallcloud.commons.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 OpenAPI 配置类
 * 功能：
 * - 统一 OpenAPI 配置（如基础信息、安全方案、全局响应等）
 * - 避免重复依赖和配置
 * - 便于团队统一接口文档规范
 * - 各业务服务按需扩展自己的 API 文档内容
 * <p>
 * 注意：
 * - 此类不定义任何具体服务的分组，仅提供“基础能力”
 * - 所有配置会被业务模块继承并覆盖
 */
// https://springdoc.org/#demos
// https://springdoc.org/#migrating-from-springfox
// https://github.com/springdoc/springdoc-openapi-demos/tree/master
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    /**
     * 注册所有公共 DTO 类型，让 OpenAPI 能正确识别其结构
     * 否则在 Swagger UI 中会显示为 "object" 或 "Unknown"
     * <p>
     * 例如：UserBaseInfo、ResponseResult、PageRequest 等
     */
    @Bean
    public GroupedOpenApi commonModels() {
        return GroupedOpenApi.builder()
                .group("Common Models")
                .packagesToScan("io.urbane.commons.dto") // 扫描公共 DTO 包
                .build();
    }

    /**
     * 提供默认的 OpenAPI 配置，业务模块可覆盖或扩展
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI defaultOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("微服务 API 文档")
                        .description("基于 OpenAPI 3.0 规范的统一接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("后端团队")
                                .email("backend@yourcompany.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}