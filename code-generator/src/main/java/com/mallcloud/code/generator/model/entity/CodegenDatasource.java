package com.mallcloud.code.generator.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("codegen_datasource")
public class CodegenDatasource {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String name;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword; // 实际生产中请使用 AES 等加密存储
    private String basePackage;
    private String author;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
