package com.mallcloud.codegen;

import com.mallcloud.codegen.properties.CodegenProperties;
import com.mallcloud.commons.mybatis.entity.BaseEntity;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.dialect.IDialect;
import com.mybatisflex.codegen.template.impl.EnjoyTemplate;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.service.IService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CodegenApplicationTest {

    private CodegenProperties properties;

    @Test
    void CodegenTest() {
        // 配置数据源
        var dataSourceConfig = properties.getDataSourceConfig();
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

    public static GlobalConfig createGlobalConfigUseStyle() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 代码注释配置
        globalConfig.getJavadocConfig()
                .setAuthor("Fan")
                .setSince("2023-01-01");
        // 包配置
        globalConfig.getPackageConfig()
                .setBasePackage("com.test");
        // 策略配置
        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setTablePrefix("tb_")
                .setGenerateTable("tb_account", "tb_account_session");
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
                .setWithLombok(true)
                .setJdkVersion(25)
                .setClassPrefix("My")
                .setClassSuffix("Entity")
                .setSuperClass(BaseEntity.class);
        globalConfig.setEntitySuperClassFactory(table -> {
            // 在这里，可以通过 table 来指定对应 SuperClass
            // 返回 null，则表示不需要设置父类
            return null;
        });
        // Mapper 生成配置
        globalConfig.getMapperConfig()
                .setClassPrefix("My")
                .setClassSuffix("Mapper")
                .setSuperClass(BaseMapper.class);
        // MapperXml 生成配置
        globalConfig.getMapperXmlConfig()
                .setFilePrefix("My")
                .setFileSuffix("Mapper");
        // Service 生成配置
        globalConfig.getServiceConfig()
                .setClassPrefix("My")
                .setClassSuffix("Service")
                .setSuperClass(IService.class);
        // ServiceImpl 生成配置
        globalConfig.getServiceImplConfig()
                .setClassPrefix("My")
                .setClassSuffix("ServiceImpl")
                .setSuperClass(ServiceImpl.class);
        // Controller 生成配置
        globalConfig.getControllerConfig()
                .setClassPrefix("My")
                .setClassSuffix("Controller");
        // TableDef 生成配置
        globalConfig.getTableDefConfig()
                .setClassPrefix("My")
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
