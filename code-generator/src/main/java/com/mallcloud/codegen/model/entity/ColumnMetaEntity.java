package com.mallcloud.codegen.model.entity;

import lombok.Data;

/**
 * 字段元数据 —— 包含数据库定义和后期映射信息
 */
@Data
public class ColumnMetaEntity {
    /** 原始字段名（下划线命名） */
    private String columnName;
    /** 数据库类型（如 VARCHAR, INT） */
    private String dbType;
    /** 字段长度 */
    private Integer length;
    /** 小数位 */
    private Integer scale;
    /** 是否主键 */
    private boolean primaryKey;
    /** 是否自增 */
    private boolean autoIncrement;
    /** 是否可为空 */
    private boolean nullable;
    /** 默认值 */
    private String defaultValue;
    /** 字段注释 */
    private String comment;
    /** 映射后的 Java 类型（由 TypeMappingUtils 计算） */
    private String javaType;
    /** 映射后的 Java 属性名（驼峰命名，如 productName） */
    private String fieldName;
    /** 是否在实体类中忽略（用于提取公共字段） */
    private boolean ignored = false;
}
