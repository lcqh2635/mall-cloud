package ${package.Controller};

import ${package.Entity}.${entity};
import ${package.Service}.${table.serviceName};
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
<#-- 使用正则表达式替换末尾的"表"字 -->
<#-- 先尝试去除末尾的"表"字，如果处理后为空则回退原值 -->
<#assign comment = (table.comment!?replace('表$', '', 'r'))!table.comment!>

/**
 * <p>
 * ${comment} 前端控制器
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@RequiredArgsConstructor
@Tag(name = "${comment}管理接口") <#-- 示例：用户管理接口 -->
@RestController
@RequestMapping("<#if package.ModuleName?? && package.ModuleName != "">/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
public class ${table.controllerName} {

    private final ${table.serviceName} ${table.serviceName?uncap_first};

    /**
     * 分页查询${comment}列表
     *
     * @param pageNum  当前页码，默认值：1
     * @param pageSize 每页记录数，默认值：10
     * @param ${entity?uncap_first} 包含查询条件的${comment}实体
     * @return 分页数据（包含总数和当前页数据）
     */
    @Operation(summary = "分页查询", description = "分页获取${comment}列表")
    @GetMapping("/page")
    public ResponseEntity<Page<${entity}VO>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "${comment}实体", required = false) @ParameterObject ${entity} ${entity?uncap_first}) {
        return ResponseEntity.ok(${table.serviceName?uncap_first}.page(new Page<>(pageNum, pageSize)));
    }

    /**
     * 根据ID查询${comment}详情
     *
     * @param id 记录主键ID
     * @return 完整的${comment}实体信息
     */
    @Operation(summary = "根据ID查询", description = "通过主键ID获取${comment}详情")
    @GetMapping("/find/{id}")
    public ResponseEntity<${entity}VO> getById(
            @Parameter(description = "${comment}ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(${table.serviceName?uncap_first}.getById(id));
    }

    /**
     * 新增${comment}
     *
     * @param ${entity?uncap_first} 包含完整字段的${comment}实体
     * @return 操作结果（true表示成功）
     */
    @Operation(summary = "新增记录", description = "创建新的${comment}")
    @PostMapping("/insert")
    public ResponseEntity<Boolean> create(
            @Parameter(description = "${comment}实体", required = true) @RequestBody ${entity} ${entity?uncap_first}) {
        return ResponseEntity.ok(${table.serviceName?uncap_first}.save(${entity?uncap_first}));
    }

    /**
     * 修改${comment}
     *
     * @param ${entity?uncap_first} 包含更新字段的${comment}实体
     * @return 操作结果（true表示成功）
     */
    @Operation(summary = "更新记录", description = "根据ID修改${comment}")
    @PutMapping("/update")
    public ResponseEntity<Boolean> update(
            @Parameter(description = "${comment}实体", required = true) @Valid @RequestBody ${entity} ${entity?uncap_first}) {
        return ResponseEntity.ok(${table.serviceName?uncap_first}.updateById(${entity?uncap_first}));
    }

    /**
     * 删除${comment}
     *
     * @param ids 要删除的记录主键ID列表
     * @return 操作结果（true表示成功）
     */
    @Operation(summary = "删除记录", description = "根据ID删除${comment}")
    @DeleteMapping("/delete")
    public ResponseEntity<Boolean> delete(
            @Parameter(description = "${comment}ID", required = true) @RequestBody Long[] ids) {
        return ResponseEntity.ok(${table.serviceName?uncap_first}.removeByIds(ids));
    }

    /**
     * 根据条件查询${comment}列表
     *
     * @param ${entity?uncap_first} 包含查询条件的${comment}实体
     * @return 匹配条件的${comment}列表（非分页）
     */
    @Operation(summary = "条件查询", description = "根据动态条件查询${comment}列表")
    @GetMapping("/list")
    public ResponseEntity<List<${entity}VO>> list(
            @ParameterObject ${entity} ${entity?uncap_first}) {
        return ResponseEntity.ok(${table.serviceName?uncap_first}.list(${entity?uncap_first}));
    }

    /**
     * 导出${comment}数据到Excel
     *
     * @param query 可选的查询条件（若为空则导出全部数据）
     * @return 包含Excel文件的流响应
     */
    @Operation(summary = "数据导出", description = "导出${comment}数据到Excel文件")
    @GetMapping("/export")
    public ResponseEntity<Resource> exportData(
            @Parameter(description = "导出条件") @ParameterObject ${entity} query) {

        // 获取要导出的数据列表
        List<${entity}> dataList = ${table.serviceName?uncap_first}.list(query);

        // 生成Excel文件（需要实现导出逻辑）
        Resource excelFile = ${table.serviceName?uncap_first}.exportToExcel(dataList);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${comment}数据导出.xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);
    }

    /**
     * 从Excel文件导入${comment}数据
     *
     * @param file 上传的Excel文件（支持xlsx格式）
     * @return 导入结果（成功数量/失败数量）
     */
    @Operation(summary = "数据导入", description = "从Excel文件批量导入${comment}数据")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importData(
            @Parameter(description = "Excel文件", required = true) @RequestPart("file") MultipartFile file) {

        // 执行导入操作
        ImportResult result = ${table.serviceName?uncap_first}.importFromExcel(file);

        return ResponseEntity.ok(result);
    }

}
