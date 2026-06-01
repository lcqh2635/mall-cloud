package com.mallcloud.codegen.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单个模板文件信息 —— 属于某个模板组，描述一个待生成的代码文件
 */
@Data
public class TemplateEntity {
    /** 模板文件名（用于展示） */
    private String name;
    /** 模板文件在 classpath 中的相对路径（如 "springboot-mybatisplus/Entity.java.ftl"） */
    private String path;
    /** 生成后文件的输出路径模板（可包含占位符，如 "{packagePath}/entity/{className}.java"） */
    private String outputPath;
    /** 文件类型分类：entity、mapper、controller、vue 等 */
    private String category;
}