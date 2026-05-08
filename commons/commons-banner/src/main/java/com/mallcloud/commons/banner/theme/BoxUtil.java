package com.mallcloud.commons.banner.theme;

/**
 * Box 主题渲染器
 *
 * <p>Unicode 边框风格，使用 Box Drawing 字符绘制完整闭合边框。
 *
 * <p>核心难点：终端字符对齐依赖"显示宽度"而非"字符数量"。
 * ASCII 字符显示宽度为 1，中文/全角字符显示宽度为 2，
 * 必须用 {@link #displayWidth(String)} 精确计算才能保证右边框对齐。
 *
 * <p>输出示例：
 * <pre>
 * ╔══════════════════════════════════════════════════════╗
 * ║  ADMIN-SERVER                                        ║
 * ╠══════════════════════════════════════════════════════╣
 * ║  ADMIN-SERVER                                        ║
 * ║  ADMIN-SERVER                                        ║
 * ║  ADMIN-SERVER                                        ║
 * ╚══════════════════════════════════════════════════════╝
 * </pre>
 *
 * @author mallcloud
 */
public class BoxUtil {

    // ===================== Unicode 边框字符 =====================
    private static final String TL  = "╔"; // 左上角
    private static final String TR  = "╗"; // 右上角
    private static final String BL  = "╚"; // 左下角
    private static final String BR  = "╝"; // 右下角
    private static final String H   = "═"; // 水平线
    private static final String V   = "║"; // 垂直线
    private static final String ML  = "╠"; // 左侧中间连接
    private static final String MR  = "╣"; // 右侧中间连接
    private static final String DIV = "│"; // 内部分隔符

}
