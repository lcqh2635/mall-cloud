package com.mallcloud.codegen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成器属性配置 —— 支持在 application.yml 中直接定义生成任务
 * 
 * 使用示例：
 * codegen:
 *   enabled: true
 *   project:
 *     package-name: com.example.demo
 *     author: ZhangSan
 *     remove-prefix: t_
 *   datasource:
 *     type: virtual
 *     tables:
 *       - name: t_user
 *         comment: 用户表
 *         columns:
 *           - columnName: id
 *             dbType: BIGINT
 *             comment: 主键
 *             primaryKey: true
 *             autoIncrement: true
 *             nullable: false
 *           - columnName: user_name
 *             dbType: VARCHAR
 *             length: 50
 *             comment: 用户名
 *   template-group: springboot-mybatisplus
 *   output:
 *     type: zip   # zip 或 local
 *     path: ./generated   # 当 type 为 local 时指定输出目录
 */
@Data
@Component
@ConfigurationProperties(prefix = "codegen")
public class CodegenProperties {

    /** 是否在启动时自动执行生成任务 */
    private boolean enabled = false;

    /** 项目配置 */
    private ProjectConfig project = new ProjectConfig();

    /** 数据源配置（可配置真实连接或虚拟表） */
    private DatasourceConfig datasource = new DatasourceConfig();

    /** 模板组名称（对应 classpath:templates/ 下的子目录名） */
    private String templateGroup = "springboot-mybatisplus";

    /** 输出配置 */
    private OutputConfig output = new OutputConfig();

    /**
     * 项目配置内部类
     */
    @Data
    public static class ProjectConfig {
        /** 包名 */
        private String packageName;
        /** 作者 */
        private String author;
        /** 表前缀去除 */
        private String removePrefix;
    }

    /**
     * 数据源配置内部类
     */
    @Data
    public static class DatasourceConfig {
        /** 类型：jdbc 或 virtual */
        private String type = "virtual";

        // JDBC 真实连接参数（当 type=jdbc 时生效）
        private String dbType;
        private String host;
        private Integer port;
        private String username;
        private String password;
        private String dbName;

        /** 虚拟表定义（当 type=virtual 时生效） */
        private List<VirtualTable> tables = new ArrayList<>();

        @Data
        public static class VirtualTable {
            private String name;        // 表名
            private String comment;     // 表注释
            private List<ColumnDef> columns = new ArrayList<>();

            @Data
            public static class ColumnDef {
                private String columnName;
                private String dbType;
                private Integer length;
                private Integer scale;
                private boolean primaryKey;
                private boolean autoIncrement;
                private boolean nullable = true;
                private String defaultValue;
                private String comment;
            }
        }
    }

    /**
     * 输出配置
     */
    @Data
    public static class OutputConfig {
        /** 输出方式：zip（打包下载）或 local（直接写入磁盘） */
        private String type = "zip";
        /** 当 type 为 local 时，指定输出根目录 */
        private String path = "./generated";
    }
}