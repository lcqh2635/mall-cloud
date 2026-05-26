<#-- API 接口定义模板：api.ftl -->
<#-- 作者：${author} -->
<#-- 生成时间：${now?string("yyyy-MM-dd HH:mm:ss")} -->
<#-- 数据库表：${table.comment} -->

import request from '@/utils/request';
<#-- 使用正则表达式替换末尾的"表"字 -->
<#-- 先尝试去除末尾的"表"字，如果处理后为空则回退原值 -->
<#assign comment = (table.comment!?replace('表$', '', 'r'))!table.comment!>

/**
* ${table.comment!""} API 接口
* 自动生成于：${now?string("yyyy-MM-dd HH:mm:ss")}
*/
// 分页查询${comment}列表
export const get${entity}Page = (pageNum: number, pageSize: number, query: ${entity}) => {
  return request({
    url: '/${table.entityPath}/page',
    method: 'get',
    params: {
      pageNum,
      pageSize,
      ...query
    }
  });
};

// 获取${comment}列表
export const get${entity}List = () => {
  return request({
    url: '/${table.entityPath}/list',
    method: 'get'
  });
};

// 根据id获取${comment}
export const get${entity}ById = (id: number) => {
  return request({
    url: `/api/${table.entityPath}/<#noparse>${id}</#noparse>`,
    method: 'get'
  });
};

// 创建${comment}
export const create${entity} = (data: any) => {
  return request({
    url: '/${table.entityPath}/save',
    method: 'post',
    data
  });
};

// 更新${comment}
export const update${entity} = (data: any) => {
  return request({
    url: '/${table.entityPath}/update',
    method: 'put',
    data
  });
};

// 删除${comment}
export const delete${entity} = (id: number) => {
  return request({
    url: `/api/${table.entityPath}/<#noparse>${id}</#noparse>`,
    method: 'delete'
  });
};

// ${comment}接口定义
export interface ${entity} {
<#list table.fields as field>
  ${field.propertyName}: ${convertType(field.propertyType)}; // ${field.comment!}
</#list>
}

<#-- 自定义函数：将 Java 数据类型转换为 TypeScript 数据类型 -->
<#function convertType javaType>
  <#if javaType == "String">
    <#return "string">
  <#elseif javaType == "int" || javaType == "Integer" || javaType == "long" || javaType == "Long" || javaType == "float" || javaType == "Float" || javaType == "double" || javaType == "Double">
    <#return "number">
  <#elseif javaType == "boolean" || javaType == "Boolean">
    <#return "boolean">
  <#elseif javaType == "Date">
    <#return "string"> <!-- 或者使用 Date 类型 -->
  <#elseif javaType == "BigDecimal">
    <#return "number"> <!-- 或者使用 string 类型 -->
  <#elseif javaType?starts_with("List<")>
    <#return "${javaType[5..-2]}[]"> <!-- 将 List<T> 转换为 T[] -->
  <#elseif javaType?starts_with("Map<")>
    <#return "{ [key: string]: any }"> <!-- 将 Map<K, V> 转换为 { [key: string]: V } -->
  <#else>
    <#return "any"> <!-- 默认返回 any -->
  </#if>
</#function>