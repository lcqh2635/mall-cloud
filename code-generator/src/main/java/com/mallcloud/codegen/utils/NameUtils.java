package com.mallcloud.codegen.utils;

/**
 * 命名风格转换工具
 */
public class NameUtils {

    /**
     * 下划线转驼峰（小驼峰）
     */
    public static String toCamelCase(String input) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : input.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    /**
     * 下划线转类名（大驼峰），并可指定去除前缀
     */
    public static String toClassName(String tableName, String prefixToRemove) {
        String name = tableName;
        if (prefixToRemove != null && !prefixToRemove.isEmpty() && name.startsWith(prefixToRemove)) {
            name = name.substring(prefixToRemove.length());
        }
        String camel = toCamelCase(name);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }
}
