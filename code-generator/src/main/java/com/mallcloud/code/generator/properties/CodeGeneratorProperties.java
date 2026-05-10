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
@ConfigurationProperties(prefix = "mall-cloud.generator")
public class CodeGeneratorProperties {

    @NestedConfigurationProperty
    private DataSourceConfig dataSourceConfig = new DataSourceConfig();

    @NestedConfigurationProperty
    private GlobalConfig globalConfig = new GlobalConfig();

    @NestedConfigurationProperty
    private JavadocConfig javadocConfig = new JavadocConfig();

    @NestedConfigurationProperty
    private PackageConfig packageConfig = new PackageConfig();

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

        /**
         * JDBC 驱动类全限定名（必填）
         * MySQL：com.mysql.cj.jdbc.Driver
         * PostgreSQL：org.postgresql.Driver
         * Oracle：oracle.jdbc.OracleDriver
         * SQL Server：com.microsoft.sqlserver.jdbc.SQLServerDriver
         * 默认值：com.mysql.cj.jdbc.Driver
         */
        private String driverClassName = "com.mysql.cj.jdbc.Driver";

        /**
         * 数据库类型（用于 MyBatis-Plus 生成器自动识别 SQL 语法）
         * 可选值：MYSQL、POSTGRESQL、ORACLE、SQLSERVER、DB2、H2
         * 注意：必须与 driverClassName 和 URL 保持一致
         * 默认值：MYSQL
         */
        private DbType dbType = DbType.MYSQL;

        /**
         * 数据库 Schema 名称（仅适用于 PostgreSQL、Oracle 等支持 Schema 的数据库）
         * 例如：public、myapp_schema
         * MySQL 无需设置，留空即可
         * 默认值：null
         */
        private String schema;

        /**
         * 数据库目录名称（Catalog，适用于 Oracle、SQL Server）
         * 在 MySQL 中通常为数据库名，与 url 中的 dbname 一致
         * 默认值：null
         */
        private String catalog;

        /**
         * 是否启用 SSL 连接（适用于生产环境加密通信）
         * true：强制使用 SSL；false：禁用 SSL（开发环境常用）
         * 默认值：false
         */
        private Boolean useSSL;

        /**
         * 连接超时时间（毫秒），连接数据库的最大等待时间
         * 超时后抛出异常，避免长时间阻塞
         * 默认值：0（无限等待）
         */
        private Integer connectTimeout;

        /**
         * Socket 读写超时时间（毫秒），网络请求最大等待时间
         * 避免因网络延迟导致生成器卡死
         * 默认值：0（无限等待）
         */
        private Integer socketTimeout;

        @Getter
        public enum DbType {
            MYSQL,
            POSTGRESQL,
            ORACLE,
            SQLSERVER,
            DB2,
            H2
        }
    }
}
