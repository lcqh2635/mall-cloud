package com.mallcloud.codegen.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mallcloud.codegen.model.CodegenRequest;
import com.mallcloud.codegen.service.CodegenService;
import com.mallcloud.commons.core.model.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 代码生成控制器
 * <p>提供前端界面触发代码生成的核心 API 接口</p>
 * <p>接收前端配置，执行生成，返回 ZIP 文件下载路径</p>
 * <p>
 * 参考，接口添加作者 <a href="https://doc.xiaominfo.com/docs/features/author">...</a>
 * 有时候在开发接口时,我们希望给该接口添加一个作者,这样前端或者别个团队来对接该接口时,
 * 如果该接口返回的数据或者调用有问题,都能准确找到该人,提升效率. 类 @ApiSupport   方法 @ApiOperationSupport
 *
 * @author 龙茶清欢
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/api/codegen")
@RequiredArgsConstructor
@Tag(name = "代码生成接口", description = "提供代码生成功能的相关接口")
public class CodegenController {

    private final CodegenService codeGenService;

    /**
     * 执行代码生成
     *
     * @param request 生成请求参数（包含数据源ID、表名列表、模板映射、作者）
     * @return 生成结果（成功则返回 ZIP 文件名）
     * &#064;api  {POST} /api/codegen/generate 执行代码生成
     * &#064;apiName  GenerateCode
     * &#064;apiGroup  代码生成
     * &#064;apiParamExample  {json} 请求示例:
     * {
     * "dataSourceId": 1,
     * "tableNames": ["t_user", "t_product"],
     * "templateMap": {
     * "vue-list": "vue-list.vue.ftl",
     * "ts-api": "api.ts.ftl",
     * "ts-types": "types.ts.ftl"
     * },
     * "author": "张三"
     * }
     * &#064;apiSuccessExample  {json} 成功响应:
     * HTTP/1.1 200 OK
     * {
     * "success": true,
     * "message": "生成成功",
     * "zipFileName": "codegen_1712345678.zip"
     * }
     * &#064;apiErrorExample  {json} 失败响应:
     * HTTP/1.1 500 Internal Server Error
     * {
     * "success": false,
     * "message": "生成失败：数据库连接失败"
     * }
     */
    @PostMapping("/generate")
    @ApiOperationSupport(author = "xiaoymin@foxmail.com")
    @Operation(summary = "执行代码生成", description = "根据前端配置，动态生成后端 Java 代码和前端 Vue/TS 代码，并打包为 ZIP")
    public String executeCodeGen(@Valid @RequestBody CodegenRequest request) {
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
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ResponseResult.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "参数校验失败",
                    content = @Content(schema = @Schema(implementation = ResponseResult.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "用户不存在",
                    content = @Content(schema = @Schema(implementation = ResponseResult.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "系统内部错误",
                    content = @Content(schema = @Schema(implementation = ResponseResult.class))
            )
    })
    @Operation(summary = "代码预览", description = "根据文件名下载由代码生成器生成的 ZIP 压缩包")
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

    @Operation(summary = "普通body请求")
    @PostMapping("/body")
    public ResponseEntity<FileResp> body(@RequestBody FileResp fileResp) {
        return ResponseEntity.ok(fileResp);
    }

    @Operation(summary = "普通body请求+Param+Header+Path")
    @Parameters({
            @Parameter(name = "id", description = "文件id", in = ParameterIn.PATH),
            @Parameter(name = "token", description = "请求token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "name", description = "文件名称", required = true, in = ParameterIn.QUERY)
    })
    @PostMapping("/bodyParamHeaderPath/{id}")
    public ResponseEntity<FileResp> bodyParamHeaderPath(@PathVariable("id") String id, @RequestHeader("token") String token, @RequestParam("name") String name, @RequestBody FileResp fileResp) {
        fileResp.setName(fileResp.getName() + ",receiveName:" + name + ",token:" + token + ",pathID:" + id);
        return ResponseEntity.ok(fileResp);
    }

}