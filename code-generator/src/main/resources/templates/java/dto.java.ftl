package ${package.DTO};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
* <p>
* ${table.comment!}
* </p>
*
* @author ${author}
* @since ${date}
*/
@Getter
@Setter
@Accessors(chain = true)
@Schema(description = "创建${table.comment!}请求参数")
public class ${entity}DTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.keyFlag>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>

    <#if field.comment!?length gt 0>
        <#if entityFieldUseJavaDoc>
    /**
     * ${field.comment}
     */
        </#if>
    </#if>
    @Schema(description = "${field.comment!}")
    private ${field.propertyType} ${field.propertyName};
<#------------  END 字段循环遍历  ---------->
</#list>

}
