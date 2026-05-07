package com.mallcloud.commons.banner.utils;

import lombok.Getter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

/**
 * Figlet 艺术字体工具类
 * 用于将普通文本转换为ASCII艺术字体
 * <p>
 * Figlet 是一种将普通文本转换为大型字母的程序，
 * 字母由文本字符组成，类似于早期电脑和游戏机上的字符艺术。</p>
 *
 * @author urbane
 * @since 1.0.0
 */
@Getter
public class FigletUtil {

    /**
     * 支持的最大字符数
     */
    private static final int MAX_CHARS = 1024;
    /**
     * 硬空格字符，用于替换普通空格
     */
    private final char hardBlank;
    /**
     * 字体高度（行数）
     */
    private final int height;
    /**
     * 不包含下降部分的字体高度
     */
    private final int heightWithoutDescenders;
    /**
     * 最大行长度
     */
    private final int maxLine;
    /**
     * 字符压缩模式
     */
    private final int smushMode;
    /**
     * 字体数据数组
     * 三维数组：[字符编码][行数][列数]
     */
    private final char[][][] font; // [charCode][row][col]
    /**
     * 字体名称
     */
    private final String fontName;

    /**
     * 构造函数，从输入流加载字体文件
     *
     * @param fontStream 字体文件输入流
     * @throws IOException 当读取字体文件出错时抛出
     */
    public FigletUtil(InputStream fontStream) throws IOException {
        if (fontStream == null) {
            throw new IllegalArgumentException("Font stream cannot be null");
        }

        this.font = new char[MAX_CHARS][][];

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new BufferedInputStream(fontStream), StandardCharsets.UTF_8))) {

            // 读取字体文件头部信息
            String headerLine = bufferedReader.readLine();
            if (headerLine == null) {
                throw new IOException("Invalid font file: empty header");
            }
            StringTokenizer tokenizer = new StringTokenizer(headerLine, " ");
            if (tokenizer.countTokens() < 6) {
                throw new IOException("Invalid font header format");
            }

            // 解析字体基本信息
            String signature = tokenizer.nextToken();
            this.hardBlank = signature.charAt(signature.length() - 1);
            this.height = Integer.parseInt(tokenizer.nextToken());
            this.heightWithoutDescenders = Integer.parseInt(tokenizer.nextToken());
            this.maxLine = Integer.parseInt(tokenizer.nextToken());
            this.smushMode = Integer.parseInt(tokenizer.nextToken());

            // 读取注释行数
            int commentLines = Integer.parseInt(tokenizer.nextToken());

            // 读取字体名称
            tokenizer = new StringTokenizer(bufferedReader.readLine(), " ");
            if (tokenizer.hasMoreElements()) {
                this.fontName = tokenizer.nextToken();
            } else {
                this.fontName = "";
            }

            // 跳过注释行
            for (int i = 0; i < commentLines - 1; i++) {
                bufferedReader.readLine();
            }

            // 读取字符数据
            int charCode = 31;
            while (headerLine != null) {
                ++charCode;

                for (int lineIndex = 0; lineIndex < this.height; ++lineIndex) {
                    headerLine = bufferedReader.readLine();
                    if (headerLine != null) {
                        boolean isAbnormalFormat = true;
                        if (lineIndex == 0) {
                            try {
                                String codeTag = headerLine.concat(" ").split(" ")[0];
                                if (codeTag.length() > 2 && "x".equals(codeTag.substring(1, 2))) {
                                    // 十六进制编码
                                    charCode = Integer.parseInt(codeTag.substring(2), 16);
                                } else {
                                    // 十进制编码
                                    charCode = Integer.parseInt(codeTag);
                                }
                            } catch (NumberFormatException var18) {
                                isAbnormalFormat = false;
                            }
                            // 如果是异常格式，读取下一行并恢复字符编码
                            if (isAbnormalFormat) {
                                headerLine = bufferedReader.readLine();
                            }
                        }

                        // 初始化字符数据数组
                        if (lineIndex == 0) {
                            this.font[charCode] = new char[this.height][];
                        }

                        // 计算当前行的有效长度
                        int effectiveLength = headerLine.length() - 1 - (lineIndex == this.height - 1 ? 1 : 0);
                        if (this.height == 1) {
                            ++effectiveLength;
                        }

                        // 分配数组空间
                        this.font[charCode][lineIndex] = new char[effectiveLength];

                        // 处理字符数据，将硬空格替换为空格
                        // 处理字符数据，将硬空格替换为空格
                        for (int charIndex = 0; charIndex < effectiveLength; charIndex++) {
                            char currentChar = headerLine.charAt(charIndex);
                            this.font[charCode][lineIndex][charIndex] =
                                    (currentChar == this.hardBlank) ? ' ' : currentChar;
                        }
                    }
                }
            }
        }
    }

    /**
     * 将普通文本转换为 Figlet 艺术字体
     *
     * @param message 要转换的文本
     * @return 转换后的艺术字体字符串
     */
    public String convert(String message) {
        StringBuilder result = new StringBuilder();

        // 按行处理，逐行构建艺术字体
        for (int lineIndex = 0; lineIndex < this.height; lineIndex++) {
            // 处理每个字符
            for (int charIndex = 0; charIndex < message.length(); charIndex++) {
                String charLine = this.getCharLineString(message.charAt(charIndex), lineIndex);
                if (charLine != null) {
                    result.append(charLine);
                }
            }
            // 每行结束后添加换行符
            result.append('\n');
        }

        return result.toString();
    }

    /**
     * 获取指定字符某一行的字符串表示
     *
     * @param characterCode 字符的 ASCII 码
     * @param lineIndex     行索引
     * @return 指定行的字符串，如果不存在则返回null
     */
    public String getCharLineString(int characterCode, int lineIndex) {
        return this.font[characterCode][lineIndex] == null ? null : new String(this.font[characterCode][lineIndex]);
    }

    /**
     * 使用默认字体(Standard.flf)转换单行文本
     *
     * @param message 要转换的文本
     * @return 转换后的艺术字体字符串
     * @throws IOException 当读取字体文件或转换过程中出错时抛出
     */
    public static String convertOneLine(String message) throws IOException {
        return convertOneLine(FigletUtil.class.getClassLoader().getResourceAsStream("/fonts/Standard.flf"), message);
    }

    /**
     * 使用指定字体流转换单行文本
     *
     * @param fontFileStream 字体文件输入流
     * @param message        要转换的文本
     * @return 转换后的艺术字体字符串
     * @throws IOException 当读取字体文件或转换过程中出错时抛出
     */
    public static String convertOneLine(InputStream fontFileStream, String message) throws IOException {
        return (new FigletUtil(fontFileStream)).convert(message);
    }

    /**
     * 使用指定字体文件转换单行文本
     *
     * @param fontFile 字体文件
     * @param message  要转换的文本
     * @return 转换后的艺术字体字符串
     * @throws IOException 当读取字体文件或转换过程中出错时抛出
     */
    public static String convertOneLine(File fontFile, String message) throws IOException {
        return convertOneLine(new FileInputStream(fontFile), message);
    }

}
