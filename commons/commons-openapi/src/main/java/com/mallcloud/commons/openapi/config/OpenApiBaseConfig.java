package com.mallcloud.commons.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.List;

/**
 * OpenAPI 基础配置抽象类
 * 功能：
 *   - 提供标准的 OpenAPI 分组创建方法，供业务模块继承使用
 *   - 自动注入全局参数（如 X-User-ID）
 *   - 统一路径前缀、分组命名规范
 *   - 支持自定义服务器地址（用于测试/生产环境）
 * <p>
 * 使用方式（在 order-service 中）：
 *   &#064;Configuration
 *   public class OrderOpenApiConfig extends OpenApiBaseConfig {
 *       &#064;Bean
 *       public GroupedOpenApi orderApi() {
 *           return createGroup("Order Service", "/order/**", "io.urbane.order");
 *       }
 *   }
 */
public abstract class OpenApiBaseConfig {

    /**
     * 创建一个标准的 OpenAPI 分组
     *
     * @param groupName     分组名称（如 "订单服务"），将显示在 Swagger UI 左侧导航栏
     * @param pathPattern   请求路径匹配规则，如 "/order/**"，只扫描此路径下的 Controller
     * @param packageName   包扫描路径，指定包含 Controller 的包名，提高性能
     * @return GroupedOpenApi 实例
     */
    protected GroupedOpenApi createGroup(String groupName, String pathPattern, String packageName) {
        return GroupedOpenApi.builder()
                .group(groupName)
                .pathsToMatch(pathPattern)
                .packagesToScan(packageName)
                .addOpenApiCustomizer(this::addGlobalHeaders)
                .build();
    }

    /**
     * 添加全局请求头参数（所有接口默认携带）
     * 如：X-User-ID、X-Trace-ID 等，由网关注入
     *
     * @param openApi OpenAPI 对象
     */
    private void addGlobalHeaders(OpenAPI openApi) {
        // 添加 X-User-ID 参数（来自网关认证）
        openApi.addServersItem(new Server()
                .url("https://api.urbane.io")
                .description("生产环境"));

        openApi.addServersItem(new Server()
                .url("https://api-stg.urbane.io")
                .description("预发布环境"));

        openApi.addServersItem(new Server()
                .url("http://localhost:8080")
                .description("本地开发环境"));

        openApi.getComponents().getSecuritySchemes().put("bearerAuth",
                new io.swagger.v3.oas.models.security.SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        // 全局 Header：X-User-ID
        openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> operation.addParametersItem(
                new Parameter()
                        .name("X-User-ID")
                        .in("header")
                        .required(false)
                        .description("用户ID，由网关注入，格式：数字ID")
                        .schema(new io.swagger.v3.oas.models.media.StringSchema())
        )));
    }

    /**
     * （可选）设置默认服务器地址
     * 若需动态从配置加载，可在子类重写此方法
     */
    protected List<Server> getDefaultServers() {
        return List.of(
                new Server().url("http://localhost:8080").description("本地开发"),
                new Server().url("https://api.urbane.io").description("生产环境")
        );
    }
}
