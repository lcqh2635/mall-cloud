<#--
  =================================================================================================
  MyBatis-Plus 代码生成器 - Controller 模板 (OpenAPI 3.0 标准版)
  =================================================================================================
  作者：CodeGenWeb
  生成时间：${now?string("yyyy-MM-dd HH:mm:ss")}
  数据库表：${table.comment}（表名：${table.name}）

  说明：
  1. 本模板严格遵循 OpenAPI 3.0 规范，所有接口均使用 io.swagger.v3.oas.annotations 包注解
  2. 响应格式统一使用 MyBatis-Plus 的 R<T> 类，前端可统一处理
  3. 所有接口均提供 Swagger 文档，可通过 http://localhost:8080/swagger-ui.html 查看
  4. 本文件为 Freemarker 模板，生成时会自动替换 ${} 中的变量
  5. 请勿手动修改生成的文件，下次生成将覆盖！
  =================================================================================================
-->

package ${package.Controller};

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.api.R;
import ${package.Entity}.${entity};
import ${package.Service}.${service};
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* ${table.comment!""} 控制器
*
* <p>提供 ${table.comment!""} 模块的完整 RESTful API 接口，遵循 OpenAPI 3.0 规范</p>
* <p>Swagger 文档地址：http://localhost:8080/swagger-ui.html</p>
* <p>接口分组：${table.comment!""}</p>
*
* @author ${author}
* @date ${now?string("yyyy-MM-dd")}
*/
@RestController
@RequestMapping("/api/${table.entityName}")
@Tag(name = "${table.comment!""}", description = "${table.comment!""}的增删改查接口，支持分页、条件查询")
public class ${controller} {

@Autowired
private ${service} ${serviceVar}; // 自动注入 Service 层

// ==================== 1. 获取所有 ${table.comment!""}（不分页） ====================
/**
* 获取所有 ${table.comment!""} 信息（不分页）
*
* <p>此接口用于获取系统中所有 ${table.comment!""} 列表，适用于数据量较小的场景（如下拉框加载）</p>
* <p>响应格式：R<List<${entity}>>，成功时返回 200 + 数据，失败时返回 500 + 错误信息</p>
*
* @return 成功返回包含所有 ${entity} 对象的列表，失败返回错误信息
* @api {GET} /api/${table.entityName}/list 获取所有 ${table.comment!""}
* @apiName ListAll${entity}
* @apiGroup ${table.comment!""}
* @apiSuccessExample {json} 成功响应:
*     HTTP/1.1 200 OK
*     {
*       "code": 200,
*       "msg": "success",
*       "data": [
*         {
*           "id": 1,
*           "username": "zhangsan",
*           "email": "zhangsan@example.com",
*           "create_time": "2024-01-01T10:00:00"
*         }
*       ]
*     }
* @apiErrorExample {json} 失败响应:
*     HTTP/1.1 500 Internal Server Error
*     {
*       "code": 500,
*       "msg": "系统内部错误",
*       "data": null
*     }
*/
@GetMapping("/list")
@Operation(summary = "获取所有 ${table.comment!""} 列表（不分页）",
description = "查询系统中所有 ${table.comment!""} 信息，不进行分页，适用于数据量小的场景（如用户选择器）")
@ApiResponses({
@ApiResponse(responseCode = "200", description = "查询成功",
content = @Content(mediaType = "application/json",
schema = @Schema(implementation = R.class))),
@ApiResponse(responseCode = "500", description = "服务器内部错误")
})
public R<List<${entity}>> list() {
List<${entity}> list = ${serviceVar}.list();
return R.ok(list);
}

// ==================== 2. 分页查询 ${table.comment!""} ====================
/**
* 分页查询 ${table.comment!""} 信息
*
* <p>支持按页码和每页大小进行分页查询，返回总记录数、当前页、总页数等元数据</p>
* <p>推荐用于前端表格展示，提升性能和用户体验</p>
*
* @param current 当前页码，从 1 开始，默认值为 1
* @param size 每页记录数，默认值为 10，最大不超过 100
* @return 分页结果 IPage<${entity}>，包含 records（数据）、total（总数）、current（当前页）、size（每页大小）、pages（总页数）
* @api {GET} /api/${table.entityName}/page 分页查询 ${table.comment!""}
* @apiName Page${entity}
* @apiGroup ${table.comment!""}
* @apiParam {Number} [current=1] 当前页码
* @apiParam {Number} [size=10] 每页数量（最大100）
* @apiSuccessExample {json} 成功响应:
*     HTTP/1.1 200 OK
*     {
*       "code": 200,
*       "msg": "success",
*       "data": {
*         "records": [...],
*         "total": 150,
*         "current": 1,
*         "size": 10,
*         "pages": 15
*       }
*     }
*/
@GetMapping("/page")
@Operation(summary = "分页查询 ${table.comment!""} 列表",
description = "根据页码和每页大小分页查询 ${table.comment!""} 信息，返回包含总记录数、当前页、总页数的完整分页对象")
@Parameters({
@Parameter(name = "current", description = "当前页码，从1开始", example = "1", required = false, schema = @Schema(type = "integer", defaultValue = "1")),
@Parameter(name = "size", description = "每页显示记录数，默认10，最大100", example = "10", required = false, schema = @Schema(type = "integer", defaultValue = "10"))
})
@ApiResponses({
@ApiResponse(responseCode = "200", description = "查询成功",
content = @Content(mediaType = "application/json",
schema = @Schema(implementation = R.class))),
@ApiResponse(responseCode = "400", description = "参数错误（如 size > 100）"),
@ApiResponse(responseCode = "500", description = "服务器内部错误")
})
public R<IPage<${entity}>> page(@RequestParam(defaultValue = "1") Long current,
@RequestParam(defaultValue = "10") Long size) {

// 校验每页数量上限（安全控制）
if (size > 100) {
size = 100L;
}

Page<${entity}> page = new Page<>(current, size);
IPage<${entity}> result = ${serviceVar}.page(page);
return R.ok(result);
}

// ==================== 3. 根据 ID 查询单个 ${table.comment!""} ====================
/**
* 根据 ${table.comment!""} ID 查询单条记录
*
* <p>用于前端详情页、编辑页加载数据</p>
* <p>若 ${table.comment!""} 不存在，返回 404 错误提示</p>
*
* @param id ${table.comment!""} 主键 ID（必填）
* @return 成功返回 ${entity} 对象，失败返回错误信息
* @api {GET} /api/${table.entityName}/{id} 根据ID查询 ${table.comment!""}
* @apiName Get${entity}ById
* @apiGroup ${table.comment!""}
* @apiParam {Number} id ${table.comment!""}ID（必须为正整数）
* @apiSuccessExample {json} 成功响应:
*     HTTP/1.1 200 OK
*     {
*       "code": 200,
*       "msg": "success",
*       "data": {
*         "id": 1,
*         "username": "zhangsan",
*         "email": "zhangsan@example.com"
*       }
*     }
* @apiErrorExample {json} ${table.comment!""} 不存在:
*     HTTP/1.1 404 Not Found
*     {
*       "code": 404,
*       "msg": "未找到该${table.comment!""}",
*       "data": null
*     }
*/
@GetMapping("/{id}")
@Operation(summary = "根据${table.comment!""}ID查询单条记录",
description = "通过主键ID查询单条 ${table.comment!""} 信息，适用于详情页展示或编辑前加载数据")
@Parameters({
@Parameter(name = "id", description = "${table.comment!""}主键ID，必须为正整数", required = true, example = "1", schema = @Schema(type = "integer"))
})
@ApiResponses({
@ApiResponse(responseCode = "200", description = "查询成功",
content = @Content(mediaType = "application/json",
schema = @Schema(implementation = R.class))),
@ApiResponse(responseCode = "404", description = "${table.comment!""}不存在",
content = @Content(mediaType = "application/json",
schema = @Schema(implementation = R.class))),
@ApiResponse(responseCode = "500", description = "服务器内部错误")
})
public R<${entity}> getById(@PathVariable Long id) {
${entity} entity = ${serviceVar}.getById(id);
if (entity == null) {
return R.fail("未找到该${table.comment!""}");
}
return R.ok(entity);
}

// ==================== 4. 新增 ${table.comment!""} ====================
/**
* 新增一个 ${table.comment!""}
*
* <p>前端需传递完整的 ${table.comment!""} 信息（不含id），服务端自动生成主键</p>
* <p>支持字段校验：如 username 不允许为空，email 必须为邮箱格式</p>
*
* @param entity ${table.comment!""} 对象（JSON 格式），包含必要字段，不包含 id
* @return 成功返回 true，失败返回 false
* @api {POST} /api/${table.entityName} 新增 ${table.comment!""}
* @apiName Create${entity}
* @apiGroup ${table.comment!""}
* @apiParam {String} [username] ${table.comment!""}用户名（必填，长度2-20）
* @apiParam {String} [email] ${table.comment!""}邮箱地址（必填，符合邮箱格式）
* @apiParamExample {json} 请求示例:
*     {
*       "username": "lisi",
*       "email": "lisi@example.com"
*     }
* @apiSuccessExample {json} 成功响应:
*     HTTP/1.1 200 OK
*     {
*       "code": 200,
*       "msg": "success",
*       "data": true
*     }
* @apiErrorExample {json} 参数错误:
*     HTTP/1.1 400 Bad Request
*     {
*       "code": 400,
*       "msg": "参数校验失败",
*       "data": null
*     }
*/
@PostMapping
@Operation(summary = "新增 ${table.comment!""}",
description = "创建新 ${table.comment!""}，需传入必要字段，服务端自动生成ID。支持 Spring Validation 校验")
@ApiResponses({
@ApiResponse(responseCode = "200", description = "新增成功",
content = @Content(mediaType = "application/json",
schema = @Schema(implementation = R.class))),
@ApiResponse(responseCode = "400", description = "请求参数校验失败",
content = @Content(mediaType = "application/json",
schema = @Schema(implementation = R.class))),
@ApiResponse(responseCode = "500", description = "服务器内部错误")
})
public R<Boolean> save(@RequestBody ${entity} entity) {
    boolean success = ${serviceVar}.save(entity);
    return success ? R.ok(true) : R.fail("保存失败");
    }

    // ==================== 5. 修改 ${table.comment!""} ====================
    /**
    * 修改 ${table.comment!""} 信息
    *
    * <p>必须携带 ${table.comment!""} ID，服务端根据 ID 更新记录</p>
    * <p>支持部分字段更新，未传字段保持原值</p>
    *
    * @param entity ${table.comment!""} 对象（JSON 格式），必须包含 id 字段
    * @return 成功返回 true，失败返回 false
    * @api {PUT} /api/${table.entityName} 修改 ${table.comment!""}
    * @apiName Update${entity}
    * @apiGroup ${table.comment!""}
    * @apiParam {Number} id ${table.comment!""}ID（必须存在）
    * @apiParam {String} [username] 新用户名（可选）
    * @apiParam {String} [email] 新邮箱（可选）
    * @apiParamExample {json} 请求示例:
    *     {
    *       "id": 1,
    *       "username": "zhangsan_update",
    *       "email": "zhangsan_new@example.com"
    *     }
    * @apiSuccessExample {json} 成功响应:
    *     HTTP/1.1 200 OK
    *     {
    *       "code": 200,
    *       "msg": "success",
    *       "data": true
    *     }
    * @apiErrorExample {json} ${table.comment!""} 不存在:
    *     HTTP/1.1 404 Not Found
    *     {
    *       "code": 404,
    *       "msg": "未找到该${table.comment!""}",
    *       "data": null
    *     }
    */
    @PutMapping
    @Operation(summary = "修改 ${table.comment!""} 信息",
    description = "根据${table.comment!""}ID更新信息，支持部分字段更新。请求体必须包含id字段")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "修改成功",
    content = @Content(mediaType = "application/json",
    schema = @Schema(implementation = R.class))),
    @ApiResponse(responseCode = "404", description = "${table.comment!""}不存在",
    content = @Content(mediaType = "application/json",
    schema = @Schema(implementation = R.class))),
    @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public R<Boolean> update(@RequestBody ${entity} entity) {
        boolean success = ${serviceVar}.updateById(entity);
        return success ? R.ok(true) : R.fail("修改失败");
        }

        // ==================== 6. 删除 ${table.comment!""} ====================
        /**
        * 删除 ${table.comment!""}（物理删除）
        *
        * <p>执行物理删除，不可恢复。建议在生产环境使用逻辑删除（deleted=1）</p>
        *
        * @param id ${table.comment!""} 主键 ID
        * @return 成功返回 true，失败返回 false
        * @api {DELETE} /api/${table.entityName}/{id} 删除 ${table.comment!""}
        * @apiName Delete${entity}
        * @apiGroup ${table.comment!""}
        * @apiParam {Number} id ${table.comment!""}主键ID（必须存在）
        * @apiSuccessExample {json} 成功响应:
        *     HTTP/1.1 200 OK
        *     {
        *       "code": 200,
        *       "msg": "success",
        *       "data": true
        *     }
        * @apiErrorExample {json} ${table.comment!""} 不存在:
        *     HTTP/1.1 404 Not Found
        *     {
        *       "code": 404,
        *       "msg": "未找到该${table.comment!""}",
        *       "data": null
        *     }
        */
        @DeleteMapping("/{id}")
        @Operation(summary = "删除 ${table.comment!""}（物理删除）",
        description = "根据ID物理删除 ${table.comment!""} 记录。生产环境建议使用逻辑删除，避免数据丢失")
        @Parameters({
        @Parameter(name = "id", description = "${table.comment!""}主键ID，必须为正整数", required = true, example = "1", schema = @Schema(type = "integer"))
        })
        @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = R.class))),
        @ApiResponse(responseCode = "404", description = "${table.comment!""}不存在",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = R.class))),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
        })
        public R<Boolean> delete(@PathVariable Long id) {
            boolean success = ${serviceVar}.removeById(id);
            return success ? R.ok(true) : R.fail("删除失败");
            }
            }
