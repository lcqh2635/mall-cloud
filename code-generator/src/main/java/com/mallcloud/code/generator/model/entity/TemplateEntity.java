package com.mallcloud.code.generator.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("codegen_template")
public class TemplateEntity {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String name;
    private String templateType; // entity, mapper, service, etc.
    private String content;
    private Boolean isDefault;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}