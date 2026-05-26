package com.mallcloud.code.generator.generator;

import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.entity.Table;
import com.mybatisflex.codegen.generator.IGenerator;

public class VoGenerator implements IGenerator {
    @Override
    public String getTemplatePath() {
        return "";
    }

    @Override
    public void setTemplatePath(String s) {

    }

    @Override
    public void generate(Table table, GlobalConfig globalConfig) {

    }
}

// 添加其他产物的生成，例如：DTO、VO、Vue、TypeScript 等等
// https://mybatis-flex.com/zh/others/codegen.html#%E6%B7%BB%E5%8A%A0%E5%85%B6%E4%BB%96%E4%BA%A7%E7%89%A9%E7%9A%84%E7%94%9F%E6%88%90
// 具体实现可以参考 Mybatis Flex 内置的 EntityGenerator 类