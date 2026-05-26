<#-- TypeScript 类型定义模板：types.ftl -->
<#-- 作者：${author} -->
<#-- 生成时间：${now?string("yyyy-MM-dd HH:mm:ss")} -->
<#-- 数据库表：${table.comment} -->

/**
* ${table.comment!""} 类型定义
* 自动生成于：${now?string("yyyy-MM-dd HH:mm:ss")}
* 来源：数据库表 ${table.name}
*/

export interface ${entity}Type {
<#list table.fields as field>
<#-- 字段注释 -->
    <#if field.comment??>
        /**
        * ${field.comment!""}
        */
    </#if>
    ${field.name}: ${toTsType(field.type)};
</#list>
}

<#-- 如果有逻辑删除字段，添加软删除标记 -->
<#if table.logicDeleteField??>
    export type ${entity}WithDeletedType = ${entity}Type & { ${table.logicDeleteField.name}: number };
</#if>

<#-- 枚举类型（示例：状态） -->
<#-- 你可以根据字段值推断枚举，此处简化处理 -->
<#-- 示例：若字段名为 status，则生成 StatusEnum -->
<#-- 这里只做占位，实际可扩展判断逻辑 -->
<#-- 如需自动推断枚举，请扩展本模板 -->

export type ${entity}Status = 'active' | 'inactive'; // 示例，可按需替换
