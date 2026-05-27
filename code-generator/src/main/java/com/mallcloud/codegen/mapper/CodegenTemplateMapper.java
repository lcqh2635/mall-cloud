package com.mallcloud.codegen.mapper;

import com.mallcloud.codegen.model.entity.TemplateEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodegenTemplateMapper extends BaseMapper<TemplateEntity> {
}
