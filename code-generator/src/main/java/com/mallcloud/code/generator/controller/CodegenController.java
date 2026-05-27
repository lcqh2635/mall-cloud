package com.mallcloud.code.generator.controller;

import com.mallcloud.code.generator.model.CodegenRequest;
import com.mallcloud.code.generator.service.CodegenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/codegen")
@RequiredArgsConstructor
public class CodegenController {

    private final CodegenService codeGenService;

    @PostMapping("/execute")
    public String executeCodeGen(@RequestBody CodegenRequest request) {
        try {
            codeGenService.generate(
                    request.getDbUrl(),
                    request.getDbUser(),
                    request.getDbPwd(),
                    request.getTableNames(),
                    request.getBasePackage()
            );
            return "代码生成任务已提交，请查看服务端目录: " + request.getOutputDir();
        } catch (Exception e) {
            return "生成失败: " + e.getMessage();
        }
    }

    /**
     * 动态生成代码并以 ZIP 格式下载
     *
     * @param request  前端传递的生成参数（数据库连接、表名、包名等）
     * @param response HTTP 响应对象，用于输出文件流
     */
    @PostMapping("/download-zip")
    public void downloadCodeZip(@RequestBody CodegenRequest request, HttpServletResponse response) {
        // 1. 动态生成 ZIP 文件名，例如：my-project-code-20260527.zip
        String fileName = request.getProjectName() + "-code-" + System.currentTimeMillis() + ".zip";

        try {
            // 2. 设置响应头，告知浏览器这是一个附件下载，并指定 MIME 类型为 ZIP
            response.setContentType("application/zip");
            response.setCharacterEncoding("utf-8");

            // 3. 对文件名进行 URL 编码，防止中文文件名在浏览器中出现乱码
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=utf-8''" + encodedFileName);

            // 4. 禁用浏览器缓存，确保每次点击都生成最新代码
            response.setHeader("Cache-Control", "no-store, no-cache");

            // 5. 调用核心引擎，将生成的 ZIP 数据直接写入 HTTP 响应输出流
            codeGenService.generateAndDownloadZip(request, response.getOutputStream());

        } catch (IOException e) {
            log.error("代码生成 ZIP 下载失败，IO 异常: {}", e.getMessage(), e);
            // 注意：此时响应头可能已经发送，无法再返回 JSON 错误信息，只能记录日志
        } catch (Exception e) {
            log.error("代码生成 ZIP 下载失败，业务异常: {}", e.getMessage(), e);
            try {
                // 如果发生业务异常（如数据库连不上），尝试重置响应并返回 JSON 错误提示
                response.reset();
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"代码生成失败: " + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("返回错误 JSON 失败", ex);
            }
        }
    }
}