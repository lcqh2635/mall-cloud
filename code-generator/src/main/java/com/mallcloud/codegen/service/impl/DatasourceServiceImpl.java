package com.mallcloud.codegen.service.impl;

import com.mallcloud.codegen.mapper.CodegenDatasourceMapper;
import com.mallcloud.codegen.service.DatasourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasourceServiceImpl implements DatasourceService {
    private final CodegenDatasourceMapper datasourceMapper;

    // ================= 数据源管理 =================

    @Transactional
    public void saveDatasource(CodegenDatasource datasource) {
        // 简单演示：密码 Base64 编码存储（生产环境请务必使用 Jasypt 或 AES 加密）
        datasource.setDbPassword(Base64.getEncoder().encodeToString(datasource.getDbPassword().getBytes()));
        datasourceMapper.insert(datasource);
    }

    public void updateDatasourceStatus(Long id, Integer status) {
        CodegenDatasource update = new CodegenDatasource();
        update.setId(id);
        update.setStatus(status);
        datasourceMapper.update(update);
    }

    public CodegenDatasource getActiveDatasource(Long id) {
        CodegenDatasource ds = datasourceMapper.selectOneById(id);
        if (ds == null || ds.getStatus() == 0) {
            throw new RuntimeException("数据源不存在或已被禁用");
        }
        // 解密密码
        ds.setDbPassword(new String(Base64.getDecoder().decode(ds.getDbPassword())));
        return ds;
    }

    // ================= 模板管理 =================

    public void saveTemplate(CodegenTemplate template) {
        templateMapper.insert(template);
    }

    /**
     * 获取指定类型的启用的模板列表
     */
    public List<CodegenTemplate> getTemplatesByType(String type) {
        return templateMapper.selectListByQuery(
                QueryWrapper.create().where(CODEGEN_TEMPLATE.TEMPLATE_TYPE.eq(type))
                        .and(CODEGEN_TEMPLATE.STATUS.eq(1))
        );
    }

    /**
     * 根据用户选择的模板 ID 获取模板内容 Map (Key: templateType, Value: templateContent)
     * 如果用户未指定某个类型的模板，则自动 fallback 到该类型的默认模板
     */
    public Map<String, String> getSelectedTemplates(Map<String, Long> selectedTemplateIds) {
        // 1. 查出所有启用的模板
        List<CodegenTemplate> allActiveTemplates = templateMapper.selectListByQuery(
                QueryWrapper.create().where(CODEGEN_TEMPLATE.STATUS.eq(1))
        );

        // 2. 按类型分组
        Map<String, List<CodegenTemplate>> grouped = allActiveTemplates.stream()
                .collect(Collectors.groupingBy(CodegenTemplate::getTemplateType));

        Map<String, String> resultMap = new java.util.HashMap<>();

        // 3. 遍历所有需要的类型 (entity, mapper, service, service_impl, controller)
        List<String> types = List.of("entity", "mapper", "service", "service_impl", "controller");
        for (String type : types) {
            List<CodegenTemplate> templatesOfType = grouped.getOrDefault(type, List.of());
            if (templatesOfType.isEmpty()) continue;

            // 优先使用用户前端选中的模板 ID
            Long selectedId = selectedTemplateIds.get(type);
            CodegenTemplate target = null;

            if (selectedId != null) {
                target = templatesOfType.stream().filter(t -> t.getId().equals(selectedId)).findFirst().orElse(null);
            }

            // 如果没选中或选中的无效，则 fallback 到 is_default = true 的模板
            if (target == null) {
                target = templatesOfType.stream().filter(CodegenTemplate::getIsDefault).findFirst()
                        .orElse(templatesOfType.get(0)); // 兜底取第一个
            }

            resultMap.put(type, target.getContent());
        }
        return resultMap;
    }
}
