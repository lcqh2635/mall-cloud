package com.mallcloud.commons.core.constant;

/**
 * 通用常量类
 * 用于定义系统中全局使用的常量值，避免魔法值（magic number/string）
 */
public interface CommonConstant {

    /**
     * 成功状态码
     */
    int SUCCESS_CODE = 200;

    /**
     * 默认分页大小
     */
    int DEFAULT_PAGE_SIZE = 10;

    /**
     * 系统编码：UTF-8
     */
    String CHARSET_UTF8 = "UTF-8";

    /**
     * 日期时间格式（ISO 8601）
     */
    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 业务主键前缀（例如用于生成业务单号）
     */
    String BIZ_ORDER_PREFIX = "INS";
}
