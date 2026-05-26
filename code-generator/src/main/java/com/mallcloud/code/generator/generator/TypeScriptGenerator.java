package com.mallcloud.code.generator.generator;

import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.entity.Table;
import com.mybatisflex.codegen.generator.IGenerator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TypeScriptGenerator implements IGenerator {
    protected String templatePath;

    public TypeScriptGenerator() {
        this("/templates/frontend/ts/api.tpl");
    }

    public TypeScriptGenerator(String templatePath) {
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
        if (globalConfig.isEntityGenerateEnable()) {
            log.info("TypeScriptGenerator");
        }
    }
}

// 添加其他产物的生成，例如：DTO、VO、Vue、TypeScript 等等
// https://mybatis-flex.com/zh/others/codegen.html#%E6%B7%BB%E5%8A%A0%E5%85%B6%E4%BB%96%E4%BA%A7%E7%89%A9%E7%9A%84%E7%94%9F%E6%88%90
// 具体实现可以参考 Mybatis Flex 内置的 EntityGenerator 类