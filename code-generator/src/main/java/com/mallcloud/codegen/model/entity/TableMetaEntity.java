package com.mallcloud.codegen.model.entity;

import lombok.Data;

import java.util.List;

/**
 * 表元数据 —— 统一描述物理表或虚拟表的结构
 * 由 MetaDataService 解析后填充，供模板引擎使用
 */
@Data
public class TableMetaEntity {
    /** 原始表名（数据库中的名称） */
    private String tableName;
    /** 表注释 */
    private String comment;
    /** 所属数据源ID */
    private Long datasourceId;
    /** 字段列表 */
    private List<ColumnMetaEntity> columns;
    /** 主键字段列表（多主键情况极少，但支持） */
    private List<ColumnMetaEntity> primaryKeys;
    /** 索引信息（可选） */
    private List<ColumnMetaEntity> indexes;
}
