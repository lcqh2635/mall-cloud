package com.mallcloud.codegen.model.entity;

import com.mallcloud.codegen.model.enums.DatasourceType;
import com.mallcloud.codegen.model.enums.DbType;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据源配置实体 —— 既可以是真实数据库连接，也可以是手动设计的虚拟数据源
 */
@Data
@Table("gen_datasource")
public class DatasourceEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 连接名称（用户自定义） */
    private String name;

    /** 数据库类型：MYSQL、POSTGRESQL 等 */
    private DbType dbType;

    /** 主机地址 */
    private String host;

    /** 端口号 */
    private Integer port;

    /** 用户名 */
    private String username;

    /** 密码（生产环境建议加密存储） */
    private String password;

    /** 数据库名 */
    private String dbName;

    /** 数据源类别：JDBC 真实连接 / VIRTUAL 手动设计 */
    private DatasourceType type;

    /**
     * 虚拟数据源的表结构 JSON（当 type = VIRTUAL 时使用）
     * 存储格式：List<TableMeta> 的 JSON 字符串
     */
    @Column(ignore = true)
    private List<TableMetaEntity> virtualTables;

    /** 连接状态（由定时任务或手动检测后更新，非持久化字段） */
    @Column(ignore = true)
    private Boolean alive;

    /** 最后检测时间 */
    private LocalDateTime lastCheckTime;

    /** 创建时间 */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;
}
