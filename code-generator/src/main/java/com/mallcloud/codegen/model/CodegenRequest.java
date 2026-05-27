package com.mallcloud.codegen.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * 代码生成请求参数 DTO
 */
@Getter
@Setter
public class CodegenRequest {
    /** 项目名称，用于生成 ZIP 文件名和基础包名 */
    private String projectName;

    /** 数据库 JDBC URL */
    private String dbUrl;
    /** 数据库用户名 */
    private String dbUser;
    /** 数据库密码 */
    private String dbPwd;

    /** 需要生成的表名列表 */
    private Set<String> tableNames;
    /** 基础包路径，如 com.yourcompany.project */
    private String basePackage;
}
