package com.mallcloud.codegen.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP 文件打包工具 —— 将内存中的文本文件内容直接打包为 ZIP 字节流
 */
public class ZipUtils {

    /**
     * 将一组文本文件打包成 ZIP 输出到指定的输出流
     * @param fileMap   key: 文件在 ZIP 中的相对路径, value: 文件文本内容
     * @param baos      目标字节输出流（调用后需自行关闭）
     */
    public static void zipTextFiles(Map<String, String> fileMap, ByteArrayOutputStream baos) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> entry : fileMap.entrySet()) {
                String filePath = entry.getKey();
                String content = entry.getValue();

                // 创建 ZIP 条目
                ZipEntry zipEntry = new ZipEntry(filePath);
                zos.putNextEntry(zipEntry);

                // 写入文件内容（UTF-8 编码）
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }
            zos.finish();
        }
    }
}