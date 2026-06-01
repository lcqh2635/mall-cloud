package com.mallcloud.codegen.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 前端提交的代码生成请求
 */
@Data
public class GenerationRequest {
    /** 项目 ID */
    private Long projectId;
    /** 数据源 ID（可能是真实连接或虚拟数据源） */
    private Long datasourceId;
    /** 要生成的表名列表 */
    private List<String> tableNames;
    /** 使用的模板组 ID */
    private Long templateGroupId;
    /** 用户自定义的额外变量（key-value） */
    private Map<String, String> customVars;
}