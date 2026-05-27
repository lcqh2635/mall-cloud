package com.mallcloud.codegen.mapper;

import com.mallcloud.codegen.model.entity.DatasourceEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodegenDatasourceMapper extends BaseMapper<DatasourceEntity> {
}
