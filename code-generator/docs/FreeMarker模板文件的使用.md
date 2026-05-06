FreeMarker 模板对象属性和方法
对象	属性/方法	作用	用法
package	package.Entity	获取实体类包名	package ${package.Entity};
package.Mapper	获取 Mapper 包名	package ${package.Mapper};
package.Controller	获取 Controller 包名	package ${package.Controller};
table	table.importPackages	获取导入的包	${table.importPackages}
table.comment	获取表描述	${table.comment!}
table.convert	转换字段	${table.convert}
table.name	获取表名	${table.name}
table.mapperName	获取 Mapper 名称	${table.mapperName}
table.controllerName	获取 Controller 名称	${table.controllerName}
table.fields	获取表字段列表	${table.fields}
superMapperClass	superMapperClass	获取 Mapper 父类	${superMapperClass}
superMapperClassPackage	获取 Mapper 父类包	import ${superMapperClassPackage};
entity	entity	获取实体类名	${entity}
date	date	获取当前日期	${date}
author	author	获取作者	${author}
field	field.comment	获取字段描述	${field.comment}
field.keyFlag	获取字段主键标识	${field.keyFlag}
field.keyIdentityFlag	获取字段自增标识	${field.keyIdentityFlag}
field.name	获取字段名	${field.name}
field.convert	获取字段转换规则	${field.convert}
field.propertyType	获取字段属性类型	${field.propertyType}
field.capitalName	获取字段首字母大写名	${field.capitalName}
field.propertyName	获取字段属性名	${field.propertyName}
FreeMarker 模板控制语句

原文链接：https://blog.csdn.net/m0_69786632/article/details/145192014