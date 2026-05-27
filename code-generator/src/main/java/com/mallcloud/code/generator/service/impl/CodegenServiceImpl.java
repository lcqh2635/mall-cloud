package com.mallcloud.code.generator.service.impl;

import com.mallcloud.code.generator.model.CodegenRequest;
import com.mallcloud.code.generator.service.CodegenService;
import com.mybatisflex.codegen.config.GlobalConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodegenServiceImpl implements CodegenService {

    @Override
    public void generate(String dbUrl, String dbUser, String dbPwd, Set<String> tableNames, String basePackage) {

    }

    /**
     * 生成代码并打包为 ZIP 写入输出流
     *
     * @param request      前端请求参数
     * @param outputStream HTTP 响应的输出流
     */
    @Override
    public void generateAndDownloadZip(CodegenRequest request, OutputStream outputStream) {
        Path tempDir = null;

        try {
            // ================= 1. 创建系统临时目录 =================
            // 使用 java.io.tmpdir 创建一个唯一的临时文件夹，避免并发冲突
            tempDir = Files.createTempDirectory("flex-codegen-" + request.getProjectName() + "-");
            log.info("创建临时目录成功: {}", tempDir.toAbsolutePath());

            // ================= 2. 建立数据库连接 =================
            // 使用 try-with-resources 确保数据库连接在使用后自动关闭
            try (Connection connection = DriverManager.getConnection(
                    request.getDbUrl(), request.getDbUser(), request.getDbPwd())) {

                // ================= 3. 构建 MyBatis-Flex 全局配置 =================
                GlobalConfig globalConfig = buildGlobalConfig(request, tempDir.toString(), connection);

                // ================= 4. 执行代码生成 =================
                // 将表名列表转换为数组，初始化生成器并执行
                String[] tables = request.getTableNames().toArray(new String[0]);
                FlexGenerator generator = new FlexGenerator(globalConfig, tables);
                generator.generate();
                log.info("代码生成完毕，准备打包 ZIP...");
            }

            // ================= 5. 将临时目录打包为 ZIP 并输出 =================
            zipDirectory(tempDir, outputStream);
            log.info("ZIP 打包并输出完成！");

        } catch (Exception e) {
            log.error("代码生成或打包过程发生异常", e);
            throw new RuntimeException("代码生成失败: " + e.getMessage(), e);
        } finally {
            // ================= 6. 兜底清理临时目录 =================
            // 无论成功还是失败，必须删除临时目录，防止服务器磁盘被撑爆
            if (tempDir != null) {
                deleteDirectoryQuietly(tempDir);
            }
        }
    }

    /**
     * 构建 MyBatis-Flex 的全局配置
     */
    private GlobalConfig buildGlobalConfig(CodegenRequest request, String outputDir, Connection connection) {
        GlobalConfig globalConfig = new GlobalConfig();

        // 1. 基础生成配置：绑定 PG 连接，指定输出到临时目录
        globalConfig.getGeneratorConfig()
                .setConnection(connection)
                .setOutputDir(outputDir) // 核心：输出到临时目录而不是项目源码目录
                .setAuthor("CodeGen-System");

        // 2. 包配置
        globalConfig.getPackageConfig()
                .setBasePackage(request.getBasePackage())
                .setEntityPackage("entity")
                .setMapperPackage("mapper")
                .setServicePackage("service")
                .setServiceImplPackage("service.impl")
                .setControllerPackage("controller");

        // 3. 实体类策略配置
        globalConfig.getEntityConfig()
                .setWithLombok(true)
                .setWithColumnComment(true)
                .setJdkVersion(21) // 适配 Spring Boot 4
                .setClassSuffix("Entity")
                .setLogicDeleteColumn("is_deleted");

        // 4. 动态父类工厂 (复用之前的最佳实践)
        globalConfig.setEntitySuperClassFactory(this::determineSuperClass);

        // 5. 覆盖配置：在临时目录生成，无所谓覆盖，设为 true 即可
        globalConfig.getGeneratorConfig().setOverwriteEnable(true);

        return globalConfig;
    }

    /**
     * 动态决定父类策略
     */
    private Class<?> determineSuperClass(Table table) {
        String tableName = table.getName();
        if (tableName.contains("_rel") || tableName.contains("_dict")) {
            return null;
        }
        // 默认返回 Object，实际项目中可返回 BaseEntity.class
        return Object.class;
    }

    // ==================== 工具方法区 ====================

    /**
     * 将指定目录打包为 ZIP 并写入输出流
     * 使用 Java NIO 的 Files.walkFileTree 遍历目录树
     */
    private void zipDirectory(Path sourceDir, OutputStream out) throws IOException {
        // 使用 ZipOutputStream 包装输出流，指定 UTF-8 防止中文乱码
        try (ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // 计算相对路径：去除临时目录的绝对前缀，只保留项目内部的包路径结构
                    // 例如：/tmp/flex-codegen-xxx/com/yourcompany/entity/User.java -> com/yourcompany/entity/User.java
                    Path targetFile = sourceDir.relativize(file);

                    // 创建 ZIP 条目
                    zos.putNextEntry(new ZipEntry(targetFile.toString()));
                    // 将文件内容复制到 ZIP 流中
                    Files.copy(file, zos);
                    zos.closeEntry();

                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * 静默删除目录及其所有子文件
     * 使用 Files.walkFileTree 从叶子节点向上删除
     */
    private void deleteDirectoryQuietly(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file); // 删除文件
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir); // 删除空目录
                    return FileVisitResult.CONTINUE;
                }
            });
            log.info("临时目录清理成功: {}", dir.toAbsolutePath());
        } catch (IOException e) {
            // 仅记录警告，不抛出异常，避免掩盖之前的业务异常
            log.warn("清理临时目录失败，可能存在文件占用: {}", dir.toAbsolutePath(), e);
        }
    }
}
