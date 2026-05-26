package com.mallcloud.code.generator.properties;

import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.JavadocConfig;
import com.mybatisflex.codegen.config.PackageConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "code-generator")
public class CodeGeneratorProperties {

    @NestedConfigurationProperty
    private DataSourceConfig dataSourceConfig = new DataSourceConfig();

    @NestedConfigurationProperty
    private GlobalConfig globalConfig = new GlobalConfig();

    @NestedConfigurationProperty
    private PackageConfig packageConfig = new PackageConfig();

    @NestedConfigurationProperty
    private JavadocConfig javadocConfig = new JavadocConfig();

    /**
     * 自定义包装类：映射 MyBatis-Plus 的 DataSourceConfig
     * 用于配置数据库连接信息，支持 IDE 智能提示和配置校验
     *
     * @author your-name
     * &#064;date  2024-07-05
     */
    @Getter
    @Setter
    public static class DataSourceConfig {

        /**
         * 数据库 JDBC 连接 URL（必填）
         * 示例：jdbc:mysql://localhost:3306/test_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
         * 注意：不同数据库格式不同，如 PostgreSQL 使用：jdbc:postgresql://host:port/dbname
         * 默认值：无（必须显式配置）
         */
        private String url;

        /**
         * 数据库用户名（必填）
         * 示例：root、postgres、admin
         * 默认值：无（必须显式配置）
         */
        private String username;

        /**
         * 数据库密码（必填）
         * 建议在生产环境使用密钥管理服务（如 Vault）加密存储
         * 默认值：无（必须显式配置）
         */
        private String password;
    }
}
