package com.mallcloud.code.generator.config;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.builder.CustomFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.model.ClassAnnotationAttributes;
import org.apache.ibatis.annotations.Mapper;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


// 模板文件中可以获取的所有属性都在 AbstractTemplateEngine 中的 getObjectMap 方法方会的 objectMap 对象中
// 代码生成的入口为 FastAutoGenerator 中的 execute 方法
// 路径为 FastAutoGenerator/execute -> AutoGenerator/execute?templateEngine.init(config).batchOutput().open() 中的 batchOutput -> AbstractTemplateEngine/batchOutput?List<TableInfo> tableInfoList = config.getTableInfoList()
public class Demo {
    public static void demo(String[] args) {
        // TODO 后续改进，通过前端动态配置数据库源，以及通过查询数据库获取模板文件

        // 使用 FastAutoGenerator 快速配置代码生成器
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/blog_cloud?serverTimezone=GMT%2B8", "test_db", "123456")
                // 全局配置提供了对代码生成器整体行为的设置，包括输出目录、作者信息、Kotlin 模式、Swagger 集成、时间类型策略等。
                // 参考 https://baomidou.com/reference/new-code-generator-configuration/#%E5%85%A8%E5%B1%80%E9%85%8D%E7%BD%AE-globalconfig
                .globalConfig(builder -> {
                    builder
                            .disableOpenDir() // 允许自动打开输出目录
                            .author("龙茶清欢") // 设置作者
                            .enableSpringdoc() // 开启 Open API 模式
                            .dateType(DateType.ONLY_DATE) // 设置时间类型策略
                            .commentDate("yyyy-MM-dd") // 设置注释日期
                            .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java"); // 输出目录
                })
                // 包配置用于定义生成代码的包结构，包括父包名、模块名、实体类包名、服务层包名等。
                // 参考 https://baomidou.com/reference/new-code-generator-configuration/#%E5%8C%85%E9%85%8D%E7%BD%AE-packageconfig
                .packageConfig(builder -> {
                    builder
                            .parent("com.blog.cloud") // 设置父包名，在 /src/main/java 目录下创建的包名
                            .moduleName("auth") // 设置父包模块名，在 /src/main/java 目录下创建的包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, Paths.get(System.getProperty("user.dir")) + "/src/main/resources/mapper")) // 设置mapperXml生成路径
                            .entity("model.entity") // 设置实体类包名
                            .mapper("mapper") // 设置 Mapper 接口包名
                            .service("service") // 设置 Service 接口包名
                            .serviceImpl("service.impl") // 设置 Service 实现类包名
                            .xml("mapper"); // 设置 Mapper XML 文件包名
                })
                // 策略配置是 MyBatis-Plus 代码生成器的核心部分，它允许开发者根据项目需求定制代码生成的规则，包括命名模式、表和字段的过滤、以及各个代码模块的生成策略。
                // 参考 https://baomidou.com/reference/new-code-generator-configuration/#%E7%AD%96%E7%95%A5%E9%85%8D%E7%BD%AE-strategyconfig
                .strategyConfig(builder -> {
                    builder.addInclude("t_user", "t_blog") // 设置需要生成的表名
                            .addTablePrefix("t_", "sys_") // 增加过滤表前缀

                            // 1、Entity 策略配置。实体策略配置用于定制实体类的生成规则，包括父类、序列化版本 UID、文件覆盖、字段常量、链式模型、Lombok 模型等。
                            .entityBuilder()
                            .javaTemplate("/templates/java/entity.java") // 不使用默认内置的模板，设置自定义实体类模板
                            .superClass("")
                            // .idType(IdType.ASSIGN_ID)
                            .enableLombok(
                                    new ClassAnnotationAttributes("@Getter", "lombok.Getter"),
                                    new ClassAnnotationAttributes("@Setter", "lombok.Setter")
                            ) // 启用 Lombok
                            .enableChainModel() // 开启链式模型 @Accessors(chain = true)
                            .enableRemoveIsPrefix() // 去掉 Boolean 类型的 is 前缀
                            .enableTableFieldAnnotation() // 启用字段注解
                            .versionColumnName("version")
                            .logicDeleteColumnName("is_deleted")
                            .addTableFills(new Column("create_time", FieldFill.INSERT), new Column("update_time", FieldFill.INSERT_UPDATE))

                            // 2、Controller 策略配置。Controller 策略配置用于定制 Controller 类的生成规则，包括父类、文件覆盖、驼峰转连字符、RestController 注解等。
                            .controllerBuilder()
                            .enableRestStyle() // 开启生成 @RestController 控制器

                            // 3、Service 策略配置。Service 策略配置用于定制 Service 接口和实现类的生成规则，包括父类、文件覆盖、文件名称转换等。
                            .serviceBuilder()
                            .serviceTemplate("/templates/java/service.java") // 设置自定义 Service 模板
                            .enableFileOverride() // 覆盖已生成文件
                            .formatServiceFileName("%sService") // 格式化 service 接口文件名称，将格式设定为 entity名 + Service
//                            .convertServiceFileName(serviceName -> {
//                                String fileName;
//                                if(serviceName.startsWith("I")) {
//                                    fileName = serviceName.substring(1);
//                                }else {
//                                    fileName = serviceName;
//                                }
//                                return fileName + "Service";
//                            })

                            // 4、Mapper 策略配置。Mapper 策略配置用于定制 Mapper 接口和对应的 XML 映射文件的生成规则，包括父类、文件覆盖、Mapper 注解、结果映射、列列表、缓存实现类等。
                            .mapperBuilder()
                            .enableFileOverride() // 覆盖已生成文件
                            .enableBaseResultMap() // 开启 xml 中的 BaseResultMap
                            .enableBaseColumnList() // 开启 BaseColumnList
                            .mapperAnnotation(Mapper.class); // 添加 @Mapper 注解
                })
                // 通过 injectionConfig 方法，你可以灵活地自定义模板路径和输出位置，从而生成与后端代码对应的前端 Vue 和 TypeScript 数据表格代码
                // 注入配置允许开发者自定义代码生成器的行为，包括在输出文件之前执行的逻辑、自定义配置 Map 对象、自定义配置模板文件等。
                // 参考 https://baomidou.com/reference/new-code-generator-configuration/#%E6%B3%A8%E5%85%A5%E9%85%8D%E7%BD%AE-injectionconfig
                .injectionConfig(builder -> {
                    builder.customMap(Collections.singletonMap(
                                    "outputDir", // 自定义输出目录的键
                                    System.getProperty("user.dir") + "/frontend/src" // 前端项目的根目录
                            ))
                            .customFile(Collections.singletonMap("api.ts", "/templates/api.ts.ftl")) // API 文件
                            .customFile(Collections.singletonMap("types.ts", "/templates/types.ts.ftl")) // TypeScript 类型定义文件
                            .customFile(Collections.singletonMap("YourTable.vue", "/templates/table.vue.ftl.ftl")); // Vue 组件文件
                })
                // 模板引擎配置
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute(); // 执行生成
    }


    // 请过 Post 请求从前端获取代码生成定制配置数据
    public static void main(String[] args) {
        // 使用 FastAutoGenerator 快速配置代码生成器
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/blog_cloud?useSSL=false&serverTimezone=Asia/Shanghai",
                        "root",
                        "479368"
                )
                // 全局配置提供了对代码生成器整体行为的设置，包括输出目录、作者信息、Kotlin 模式、Swagger 集成、时间类型策略等。
                // 参考 https://baomidou.com/reference/new-code-generator-configuration/#%E5%85%A8%E5%B1%80%E9%85%8D%E7%BD%AE-globalconfig
                .globalConfig(builder -> {
                    builder.author("龙茶清欢")           // 作者
                            .outputDir(System.getProperty("user.dir") + "/project/backend/src/main/java") // 输出目录
                            // 开启 Springdoc 注解
                            .enableSpringdoc()
                            // 设置时间类型策略，默认值: DateType.TIME_PACK
                            .dateType(DateType.ONLY_DATE) // 设置时间类型策略
                            // 设置注释日期格式	默认值: yyyy-MM-dd
                            .commentDate("yyyy-MM-dd") // 设置注释日期
                            .disableOpenDir();          // 生成后不打开目录
                })
                // 包配置用于定义生成代码的包结构，包括父包名、模块名、实体类包名、服务层包名等。
                // 参考 https://baomidou.com/reference/new-code-generator-configuration/#%E5%8C%85%E9%85%8D%E7%BD%AE-packageconfig
                .packageConfig(builder -> {
                    builder.parent("com.example")       // 父包名
                            .moduleName("system")       // 模块名
                            .entity("model.entity")     // 实体类包名
                            .controller("controller")   // Controller 包名
                            .service("service")         // Service 包名
                            .serviceImpl("service.impl")// Service 实现类包名
                            .mapper("mapper")           // Mapper 接口包名
                            .xml("mapper")              // Mapper XML 包名
                            // 设置 mapper.xml 生成路径
                            .pathInfo(Collections.singletonMap(OutputFile.xml, Paths.get(System.getProperty("user.dir")) + "/project/backend/src/main/resources/mapper"));
                })
                // 策略配置是 MyBatis-Plus 代码生成器的核心部分，它允许开发者根据项目需求定制代码生成的规则，包括命名模式、表和字段的过滤、以及各个代码模块的生成策略。
                // 参考 https://baomidou.com/reference/new-code-generator-configuration/#%E7%AD%96%E7%95%A5%E9%85%8D%E7%BD%AE-strategyconfig
                .strategyConfig(builder -> {
                    builder.addInclude("t_user")  // 需生成的表名
                            .addTablePrefix("t_")       // 过滤表前缀

                            // 实体类策略配置
                            .entityBuilder()
                            // 覆盖已生成文件	默认值: false
                            .enableFileOverride()
                            // 禁用生成 serialVersionUID 默认 true
                            .disableSerialVersionUID()
                            // 启用 Lombok 默认值: false 默认只有 Getter,Setter,自3.5.10后增加 ToString
                            .enableLombok()
                            // 开启链式模型	默认值: false
                            .enableChainModel()
                            // 开启 Boolean 类型字段移除 is 前缀	默认值: false
                            .enableRemoveIsPrefix()
                            // 开启生成实体时生成字段注解	默认值: false
                            .enableTableFieldAnnotation()
                            // 开启 ActiveRecord 模型	默认值: false
                            .enableActiveRecord()
                            // 乐观锁字段名(数据库字段)
                            .versionColumnName("version")
                            // 逻辑删除字段名(数据库字段)
                            .logicDeleteColumnName("deleted")
                            // 数据库表映射到实体的命名策略	默认下划线转驼峰命名: NamingStrategy.underline_to_camel
                            .naming(NamingStrategy.no_change)
                            // 数据库表字段映射到实体的命名策略	默认为 null，未指定按照 naming 执行
                            .columnNaming(NamingStrategy.underline_to_camel)
                            // 设置父类
                            .superClass("com.example.system.model.entity.base.BaseEntity")
                            // 添加父类公共字段
                            .addSuperEntityColumns("id", "created_by", "created_time", "updated_by", "updated_time")
                            // 添加忽略字段
                            .addIgnoreColumns("age")
                            // 添加表字段填充
                            .addTableFills(new Column("create_time", FieldFill.INSERT))
                            .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE))
                            // 全局主键类型
                            .idType(IdType.AUTO)
                            // 格式化文件名称
                            .formatFileName("%s")

                            // Controller 策略配置
                            .controllerBuilder()
                            // 覆盖已生成文件	默认值: false
                            .enableFileOverride()
                            // 开启生成@RestController 控制器	默认值: false
                            .enableRestStyle()
                            .formatFileName("%sController")
                            .template("/templates/java/controller.java")

                            // Service 策略配置
                            .serviceBuilder()
                            .enableFileOverride() // 覆盖已生成文件
                            .superServiceClass(IService.class)
                            .formatServiceFileName("%sService") // 格式化 service 接口文件名称，将格式设定为 entity名 + Service
                            .serviceTemplate("/templates/java/service.java") // 设置自定义 Service 模板
                            .superServiceImplClass(ServiceImpl.class)
                            .formatServiceImplFileName("%sServiceImp")
                            .serviceImplTemplate("/templates/java/serviceImpl.java")

                            // Mapper 策略配置
                            .mapperBuilder()
                            .enableFileOverride()
                            .superClass(BaseMapper.class)
                            // 开启 @Mapper 注解	默认值: false
                            .mapperAnnotation(Mapper.class)
                            // 格式化 Mapper 文件名称
                            .formatMapperFileName("%sMapper")
                            .mapperTemplate("/templates/java/mapper.java")
                            // 设置 Mapper XML 策略配置
                            .enableBaseResultMap()
                            // 启用 BaseColumnList	默认值: false
                            .enableBaseColumnList()
                            // 格式化 XML 实现类文件名称
                            .formatXmlFileName("%sMapper")
                            .mapperXmlTemplate("/templates/xml/mapper.xml");
                })
                // 通过 injectionConfig 方法，你可以灵活地自定义模板路径和输出位置，从而生成与后端代码对应的前端 Vue 和 TypeScript 数据表格代码
                // 注入配置允许开发者自定义代码生成器的行为，包括在输出文件之前执行的逻辑、自定义配置 Map 对象、自定义配置模板文件等。
                // 参考 https://baomidou.com/reference/new-code-generator-configuration/#%E6%B3%A8%E5%85%A5%E9%85%8D%E7%BD%AE-injectionconfig
                .injectionConfig(builder -> {
                    Map<String, Object> customMap = new HashMap<>();
                    customMap.put("baseEntityPath", "customValue");
                    builder
                            .beforeOutputFile((tableInfo, objectMap) -> {
                                System.out.println("准备生成文件: " + tableInfo.getEntityName());
                                // 可以在这里添加自定义逻辑，如修改 objectMap 中的配置
                                if (objectMap.get("package") != null && objectMap.get("package") instanceof Map packageMap) {
                                    String parent = (String) packageMap.get("Parent");
                                    System.out.println("父包名为：" + parent);
                                }
                            })
                            .customMap(customMap) //注入自定义属性，参考 https://baomidou.com/reference/new-code-generator-configuration/#%E8%87%AA%E5%AE%9A%E4%B9%89%E6%A8%A1%E6%9D%BF%E6%94%AF%E6%8C%81-dtovo-%E7%AD%89-%E9%85%8D%E7%BD%AE
                            .customFile(
                                    new CustomFile.Builder()
                                            .enableFileOverride()
                                            .packageName("model.entity.base") // 具有默认的 packageConfig 配置的（parent + moduleName）前缀包名
                                            .fileName("BaseEntity.java") // 文件名称会默认添加表名 EntityName 作为前缀，如果不需要，需要自行处理，可以通过 formatNameFunction 自定义前缀
                                            .formatNameFunction(tableInfo -> "")
                                            .templatePath("templates/java/BaseEntity.java.ftl") //指定生成模板路径
                                            .build()
                            ) // BaseEntity 文件
                            .customFile(
                                    new CustomFile.Builder()
                                            .enableFileOverride()
                                            .packageName("model.dto") // 包名,自3.5.10开始,可通过在package里面获取自定义包全路径,低版本下无法获取,示例:package.entityDTO
                                            .fileName("DTO.java") // 文件名称
                                            .templatePath("templates/java/dto.java.ftl") //指定生成模板路径
                                            .build()
                            ) // DTO 文件
                            .customFile(
                                    new CustomFile.Builder()
                                            .enableFileOverride()
                                            .packageName("model.vo")
                                            .fileName("VO.java") // 文件名称
                                            .templatePath("templates/java/vo.java.ftl") //指定生成模板路径
                                            .build()
                            ) // VO 文件
                            .customFile(
                                    new CustomFile.Builder()
                                            .enableFileOverride()
                                            .packageName("api")
                                            .fileName(".ts")
                                            .formatNameFunction(tableInfo -> tableInfo.getEntityName().toLowerCase())
                                            .filePath(System.getProperty("user.dir") + "/project/frontend/src")
                                            .templatePath("templates/ts/api.ts.ftl") //指定生成模板路径
                                            .build()
                            ) // API 文件
                            .customFile(
                                    new CustomFile.Builder()
                                            .enableFileOverride()
                                            .packageName("types")
                                            .fileName("Type.ts") // 文件名称
                                            .formatNameFunction(tableInfo -> tableInfo.getEntityName().toLowerCase())
                                            .filePath(System.getProperty("user.dir") + "/project/frontend/src")
                                            .templatePath("templates/ts/types.ts.ftl") //指定生成模板路径
                                            .build()
                            ) // TypeScript 类型定义文件
                            .customFile(
                                    new CustomFile.Builder()
                                            .enableFileOverride()
                                            .packageName("views")
                                            .fileName("Table.vue") // 文件名称
                                            .filePath(System.getProperty("user.dir") + "/project/frontend/src")
                                            .templatePath("templates/vue/table.vue.ftl") //指定生成模板路径
                                            .build()
                            ); // Vue 组件文件
                })
                // 模板引擎配置
                .templateEngine(new FreemarkerTemplateEngine())// 使用 Freemarker 引擎模板，默认的是 Velocity 引擎模板
                .execute(); // 执行生成
    }
}
