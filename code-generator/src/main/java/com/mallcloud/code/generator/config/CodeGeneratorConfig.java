package com.mallcloud.code.generator.config;

import com.mallcloud.code.generator.generator.DtoGenerator;
import com.mallcloud.code.generator.generator.TypeScriptGenerator;
import com.mallcloud.code.generator.generator.VoGenerator;
import com.mallcloud.code.generator.generator.VueGenerator;
import com.mallcloud.code.generator.properties.CodeGeneratorProperties;
import com.mallcloud.commons.mybatis.entity.BaseEntity;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.dialect.IDialect;
import com.mybatisflex.codegen.generator.GeneratorFactory;
import com.mybatisflex.codegen.generator.impl.*;
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
public class CodeGeneratorConfig {

    private final CodeGeneratorProperties codeGeneratorProperties;

    static {
        GeneratorFactory.registerGenerator("dto", new DtoGenerator());
        GeneratorFactory.registerGenerator("vo", new VoGenerator());
        GeneratorFactory.registerGenerator("vue", new VueGenerator());
        GeneratorFactory.registerGenerator("ts", new TypeScriptGenerator());
    }

    void codeGenerator() {
        var dataSourceConfig = codeGeneratorProperties.getDataSourceConfig();
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
        var javadocConfig = codeGeneratorProperties.getJavadocConfig();

        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 包配置
        globalConfig.getPackageConfig()
                // 文件输出目录，默认如下
                .setSourceDir(System.getProperty("user.dir") + "/src/main/java")
                .setBasePackage("com.mallcloud");

        // 代码注释配置
        globalConfig.getJavadocConfig()
                .setAuthor(javadocConfig.getAuthor())
                .setSince(javadocConfig.getSince());

        // 策略配置
        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                // 生成哪个 schema 下的表
                .setGenerateSchema("mall-cloud")
                // 数据库表前缀
                .setTablePrefix("t_")
                // 生成哪些表
                .setGenerateTable("t_user", "t_account")
                // 逻辑删除字段
                .setLogicDeleteColumn("is_deleted")
                // 乐观锁的字段名称
                .setVersionColumn("version")
                // 需要忽略的列，父类 BaseEntity 定义的字段
                .setIgnoreColumns("create_time", "update_time");
        // 模板配置
        globalConfig.getTemplateConfig()
                .setTemplate(new EnjoyTemplate())
                .setEntity("templates/entity.java")
                .setMapper("templates/mapper.java")
                .setMapperXml("templates/mapper.xml")
                .setService("templates/service.java")
                .setServiceImpl("templates/serviceImpl.java")
                .setController("templates/controller.java");

        // entity 配置，并启用 Lombok
        globalConfig.getEntityConfig()
                .setOverwriteEnable(true)
                .setWithLombok(true)
                .setJdkVersion(25)
                .setClassSuffix("Entity")
                .setSuperClass(BaseEntity.class);
        globalConfig.setEntitySuperClassFactory(table -> {
            // 在这里，可以通过 table 来指定对应 SuperClass
            // 返回 null，则表示不需要设置父类
            return null;
        });
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
        // TableDef 生成配置
        globalConfig.getTableDefConfig()
                .setOverwriteEnable(true)
                .setClassSuffix("Def");

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
