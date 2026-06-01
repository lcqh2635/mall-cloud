package com.mallcloud.codegen.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 支持的数据库类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum DbType {
    MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"),
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s"),
    ORACLE("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@%s:%d:%s"),
    SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%d;databaseName=%s");

    /** JDBC 驱动类名 */
    private final String driverClassName;
    /** JDBC URL 模板，使用 String.format 填充 host, port, dbName */
    private final String urlTemplate;

    /**
     * 根据当前配置生成完整的 JDBC URL
     */
    public String buildUrl(String host, int port, String dbName) {
        return String.format(urlTemplate, host, port, dbName);
    }
}
