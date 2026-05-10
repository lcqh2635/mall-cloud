package com.mallcloud.code.generator.config;

import com.mybatisflex.codegen.config.EntityConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.PackageConfig;
import com.mybatisflex.codegen.entity.Table;
import com.mybatisflex.codegen.generator.GeneratorFactory;
import com.mybatisflex.codegen.generator.IGenerator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DtoGenerator implements IGenerator {

    private final String templatePath = "/templates/enjoy/entity.tpl";

    @Override
    public String getTemplatePath() {
        return "";
    }

    @Override
    public void setTemplatePath(String templatePath) {

    }

    @Override
    public void generate(Table table, GlobalConfig globalConfig) {
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


        Map<String, Object> params = new HashMap<>(4);
        params.put("table", table);
        params.put("entityConfig", entityConfig);
        params.put("packageConfig", packageConfig);
        params.put("javadocConfig", globalConfig.getJavadocConfig());

        globalConfig.getTemplateConfig().getTemplate().generate(params, templatePath, entityJavaFile);

        GeneratorFactory.registerGenerator("html", new DtoGenerator());
    }

}
