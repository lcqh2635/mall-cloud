package com.mallcloud.codegen.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库类型 → Java 类型映射工具
 * 支持主流数据库类型的转换，可后续扩展或从配置文件加载
 */
public class TypeMappingUtils {

    private static final Map<String, String> TYPE_MAP = new HashMap<>();

    static {
        // 数值类型
        TYPE_MAP.put("TINYINT", "Integer");
        TYPE_MAP.put("SMALLINT", "Integer");
        TYPE_MAP.put("MEDIUMINT", "Integer");
        TYPE_MAP.put("INT", "Integer");
        TYPE_MAP.put("INTEGER", "Integer");
        TYPE_MAP.put("BIGINT", "Long");
        TYPE_MAP.put("FLOAT", "Float");
        TYPE_MAP.put("DOUBLE", "Double");
        TYPE_MAP.put("DECIMAL", "java.math.BigDecimal");
        TYPE_MAP.put("NUMERIC", "java.math.BigDecimal");

        // 字符串类型
        TYPE_MAP.put("CHAR", "String");
        TYPE_MAP.put("VARCHAR", "String");
        TYPE_MAP.put("TINYTEXT", "String");
        TYPE_MAP.put("TEXT", "String");
        TYPE_MAP.put("MEDIUMTEXT", "String");
        TYPE_MAP.put("LONGTEXT", "String");
        TYPE_MAP.put("ENUM", "String");
        TYPE_MAP.put("SET", "String");

        // 日期时间
        TYPE_MAP.put("DATE", "java.time.LocalDate");
        TYPE_MAP.put("TIME", "java.time.LocalTime");
        TYPE_MAP.put("DATETIME", "java.time.LocalDateTime");
        TYPE_MAP.put("TIMESTAMP", "java.time.LocalDateTime");

        // 布尔与二进制
        TYPE_MAP.put("BIT", "Boolean");
        TYPE_MAP.put("BOOLEAN", "Boolean");
        TYPE_MAP.put("BLOB", "byte[]");
        TYPE_MAP.put("LONGBLOB", "byte[]");

        // JSON 类型（MySQL 5.7+ 等）
        TYPE_MAP.put("JSON", "String");
    }

    /**
     * 根据数据库类型获取对应的 Java 类型全名
     * @param dbType 数据库字段类型（大小写不敏感）
     * @return Java 类名，若未映射则默认返回 String
     */
    public static String getJavaType(String dbType) {
        if (dbType == null) return "String";
        // 去除括号及后面的内容，如 "VARCHAR(255)" → "VARCHAR"
        String baseType = dbType.replaceAll("\\(.*\\)", "").toUpperCase();
        return TYPE_MAP.getOrDefault(baseType, "String");
    }
}