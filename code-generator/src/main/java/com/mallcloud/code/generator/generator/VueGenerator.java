package com.mallcloud.code.generator.generator;

import com.mybatisflex.codegen.config.EntityConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.PackageConfig;
import com.mybatisflex.codegen.entity.Table;
import com.mybatisflex.codegen.generator.IGenerator;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VueGenerator implements IGenerator {
    private String templatePath;

    public VueGenerator() {
        this("/templates/frontend/ts/api.tpl");
    }

    public VueGenerator(String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public String getTemplatePath() {
        return this.templatePath;
    }

    @Override
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public void generate(Table table, GlobalConfig globalConfig) {
        log.info("VueGenerator");

        if (!globalConfig.isEntityGenerateEnable()) {
            return;
        }

        PackageConfig packageConfig = globalConfig.getPackageConfig();
        EntityConfig entityConfig = globalConfig.getEntityConfig();

        String entityPackagePath = packageConfig.getEntityPackage().replace(".", "/");
        File entityJavaFile = new File(packageConfig.getSourceDir(), entityPackagePath + "/" +
                table.buildEntityClassName() + ".java");

        if (entityJavaFile.exists() && !entityConfig.isOverwriteEnable()) {
            return;
        }

        Map<String, Object> params = HashMap.newHashMap(4);
        params.put("table", table);
        params.put("entityConfig", entityConfig);
        params.put("packageConfig", packageConfig);
        params.put("javadocConfig", globalConfig.getJavadocConfig());

        globalConfig.getTemplateConfig().getTemplate().generate(params, templatePath, entityJavaFile);
    }
}

// 添加其他产物的生成，例如：DTO、VO、Vue、TypeScript 等等
// https://mybatis-flex.com/zh/others/codegen.html#%E6%B7%BB%E5%8A%A0%E5%85%B6%E4%BB%96%E4%BA%A7%E7%89%A9%E7%9A%84%E7%94%9F%E6%88%90
// 具体实现可以参考 Mybatis Flex 内置的 EntityGenerator 类