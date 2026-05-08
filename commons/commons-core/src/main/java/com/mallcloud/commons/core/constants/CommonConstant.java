package com.mallcloud.commons.core.constants;

/**
 * 通用常量类
 * 用于定义系统中全局使用的常量值，避免魔法值（magic number/string）
 */
public final class CommonConstant {

    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 系统编码：UTF-8
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * 日期时间格式（ISO 8601）
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 业务主键前缀（例如用于生成业务单号）
     */
    public static final String BIZ_ORDER_PREFIX = "INS";

    // 私有构造函数，防止实例化
    private CommonConstant() {
        throw new UnsupportedOperationException("常量类不可实例化");
    }
}
