package com.mallcloud.code.generator.config;

import com.mallcloud.code.generator.generator.DtoGenerator;
import com.mallcloud.code.generator.generator.TypeScriptGenerator;
import com.mallcloud.code.generator.generator.VoGenerator;
import com.mallcloud.code.generator.generator.VueGenerator;
import com.mallcloud.code.generator.properties.CodegenProperties;
import com.mallcloud.commons.mybatis.entity.BaseEntity;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.*;
import com.mybatisflex.codegen.dialect.IDialect;
import com.mybatisflex.codegen.generator.GeneratorFactory;
import com.mybatisflex.codegen.template.impl.EnjoyTemplate;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.service.IService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

// https://mybatis-flex.com/zh/others/codegen.html
// https://developer.aliyun.com/article/1460051
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class CodegenConfig {

    private final CodegenProperties properties;

    static {
        GeneratorFactory.registerGenerator("dto", new DtoGenerator());
        GeneratorFactory.registerGenerator("vo", new VoGenerator());
        GeneratorFactory.registerGenerator("vue", new VueGenerator());
        GeneratorFactory.registerGenerator("ts", new TypeScriptGenerator());
    }

    void codeGenerator() {
        var dataSourceConfig = properties.getDataSourceConfig();
        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourceConfig.getUrl());
        dataSource.setUsername(dataSourceConfig.getUsername());
        dataSource.setPassword(dataSourceConfig.getPassword());

        // 创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig, IDialect.POSTGRESQL);

        //生成代码
        generator.generate();
    }

    public GlobalConfig createGlobalConfigUseStyle() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 包配置
        var packageConfig = properties.getPackageConfig();
        globalConfig.getPackageConfig()
                // 文件输出目录，默认如下
                .setSourceDir(System.getProperty("user.dir") + "/src/main/java")
                .setBasePackage(packageConfig.getBasePackage());

        // 代码注释配置
        var javadocConfig = properties.getJavadocConfig();
        globalConfig.getJavadocConfig()
                .setAuthor(javadocConfig.getAuthor())
                .setSince(javadocConfig.getSince());

        // 策略配置
        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        var strategyConfig = properties.getStrategyConfig();
        globalConfig.getStrategyConfig()
                // 生成哪个 schema 下的表
                .setGenerateSchema(strategyConfig.getGenerateSchema())
                // 生成哪些表
                .setGenerateTable(String.valueOf(strategyConfig.getGenerateTables()))
                // 数据库表前缀
                .setTablePrefix(strategyConfig.getTablePrefix())
                // 逻辑删除字段
                .setLogicDeleteColumn(strategyConfig.getLogicDeleteColumn())
                // 乐观锁的字段名称
                .setVersionColumn(strategyConfig.getVersionColumn())
                // 需要忽略的列，父类 BaseEntity 定义的字段
                .setIgnoreColumns(String.valueOf(strategyConfig.getIgnoreColumns()));

        // 模板配置
        var templateConfig = properties.getTemplateConfig();
        globalConfig.getTemplateConfig()
                .setTemplate(new EnjoyTemplate())
                .setEntity(templateConfig.getEntity())
                .setTableDef(templateConfig.getTableDef())
                .setMapper(templateConfig.getMapper())
                .setMapperXml(templateConfig.getMapperXml())
                .setService(templateConfig.getService())
                .setServiceImpl(templateConfig.getServiceImpl())
                .setController(templateConfig.getController());

        // entity 配置，并启用 Lombok
        globalConfig.getEntityConfig()
                // 1. 基础配置
                .setOverwriteEnable(true) // 注意：迭代开发时建议改为 false，防止覆盖手写的业务代码
                .setWithLombok(true)
                .setJdkVersion(25) // 💡 建议：目前企业主流是 JDK 17 或 21，JDK 25 较新，请确保您的构建工具(Lombok/Maven)已完全兼容
                .setClassSuffix("Entity");
        // 3. 动态父类工厂 (核心逻辑)
        globalConfig.setEntitySuperClassFactory(table -> {
            // 在这里，可以通过 table 来指定对应 SuperClass
            // 返回 null，则表示不需要设置父类
            String tableName = table.getName();

            // 【策略 A：黑名单】关联表、中间表、简单配置表 -> 不需要父类 (return null)
            if (tableName.contains("_rel") ||
                    tableName.contains("_relation") ||
                    tableName.equals("sys_dict") ||
                    tableName.equals("sys_config")) {
                return null;
            }

            // 【策略 B：特征匹配】日志表、流水表 -> 不继承通用Base（或继承专门的 LogEntity）
            // 因为日志表通常只追加不修改，不需要 updateTime 和 is_deleted
            if (tableName.endsWith("_log") || tableName.endsWith("_record") || tableName.endsWith("_trace")) {
                // 或者 return LogBaseEntity.class;
                return null;
            }

            // 【策略 C：特征匹配】树形结构表 -> 继承 TreeBaseEntity
            // 可以通过表名判断，也可以通过表中是否包含 parent_id 字段精准判断
            if (tableName.contains("dept") ||
                    tableName.contains("menu") ||
                    tableName.contains("category") ||
                    table.containsColumn("parent_id")) {
                return TreeBaseEntity.class;
            }

            // 【策略 D：默认兜底】其余 80% 的核心业务表 -> 继承 BaseEntity
            return BaseEntity.class;
        });

        // TableDef 生成配置
        globalConfig.getTableDefConfig()
                .setOverwriteEnable(true)
                .setClassSuffix("Def");
        // Mapper 生成配置
        globalConfig.getMapperConfig()
                .setOverwriteEnable(true)
                .setClassSuffix("Mapper")
                .setMapperAnnotation(true)
                .setSuperClass(BaseMapper.class);
        // MapperXml 生成配置
        globalConfig.getMapperXmlConfig()
                .setOverwriteEnable(true)
                .setFileSuffix("Mapper");
        // Service 生成配置
        globalConfig.getServiceConfig()
                .setOverwriteEnable(true)
                .setClassSuffix("Service")
                .setSuperClass(IService.class);
        // ServiceImpl 生成配置
        globalConfig.getServiceImplConfig()
                .setOverwriteEnable(true)
                .setCacheExample(true)
                .setClassSuffix("ServiceImpl")
                .setSuperClass(ServiceImpl.class);
        // Controller 生成配置
        globalConfig.getControllerConfig()
                .setOverwriteEnable(true)
                .setRestStyle(true)
                .setClassSuffix("Controller");

        // 可以单独配置某个列
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setColumnName("tenant_id");
        columnConfig.setLarge(true);
        columnConfig.setVersion(true);
        globalConfig.getStrategyConfig()
                .setColumnConfig("tb_account", columnConfig);

        return globalConfig;
    }
}
