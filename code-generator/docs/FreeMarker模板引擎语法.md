# FreeMarker 模板引擎有哪些模板语法？他们都有什么用途？

FreeMarker 模板引擎提供了一套丰富的模板语法，允许开发者灵活地生成动态内容。以下是 FreeMarker 的一些主要模板语法元素及其用途，并附有相应的使用示例。

### 1. 变量引用 `${expression}`

**用途**：用于插入表达式的值到输出中。`expression` 可以是简单变量名或更复杂的表达式。

**示例**：

```freemarker
<p>Welcome, ${user.name}!</p>
```

### 2. 指令 `<#directive ...>` 和 `</#directive>`

#### 条件语句 `<#if condition>...</#if>`

**用途**：根据条件执行模板部分。

**示例**：

```freemarker
<#if user.isAdmin>
    <p>Welcome admin!</p>
<#else>
    <p>Welcome user!</p>
</#if>
```

#### 循环 `<#list items as item>...</#list>`

**用途**：遍历序列中的元素。

**示例**：

```freemarker
<ul>
    <#list users as user>
        <li>${user.name}</li>
    </#list>
</ul>
```

#### 宏和函数 `<#macro name>...</#macro>` 和 `<@name ... />`

**用途**：定义可重用的代码块或功能。

**示例**：

```freemarker
<#macro greeting name="World">
    <p>Hello, ${name}!</p>
</#macro>

<@greeting name="Alice"/>
```

#### 包含其他模板 `<#include "path/to/template.ftl">`

**用途**：引入其他 FreeMarker 模板文件。

**示例**：

```freemarker
<#include "/common/header.ftl">
```

#### 导入命名空间 `<#import "path/to/macros.ftl" as m>`

**用途**：导入外部资源中的宏或变量。

**示例**：

```freemarker
<#import "/common/macros.ftl" as m>
<@m.greeting />
```

### 3. 内建函数 `${variable?function}`

**用途**：用于转换或格式化数据。

**示例**：

```freemarker
${date?string("yyyy-MM-dd")}
${number?string("#,##0.00")}
```

### 4. 插值与字符串拼接

**用途**：可以将多个表达式直接放在一起，中间不加分隔符，或者使用加号 `+` 进行拼接。

**示例**：

```freemarker
${"Hello, " + user.name}
```

### 5. 注释 `<#-- This is a comment -->`

**用途**：不会出现在最终输出中的注释。

**示例**：

```freemarker
<#-- This section will not be rendered -->
```

### 6. 设置全局变量 `<#setting name=value>`

**用途**：影响整个模板解析过程的行为。

**示例**：

```freemarker
<#setting number_format="currency">
```

### 7. 错误处理 `<#attempt>...</#attempt>`

**用途**：捕获错误并在模板中优雅地处理它们。

**示例**：

```freemarker
<#attempt>
    ${somePossiblyFailingExpression}
<#recover>
    There was an error.
</#attempt>
```

### 8. 定义和调用用户定义函数

**用途**：创建自定义函数来处理特定逻辑。

**示例**：

```freemarker
<#function greet name="World">
    "Hello, ${name}!"
</#function>

<p>${greet("Alice")}</p>
```

### 9. 使用默认值 `${variable!defaultValue}`

**用途**：当变量为空或未定义时提供一个默认值。

**示例**：

```freemarker
<p>Welcome, ${user.name!"Guest"}!</p>
```

### 10. 字典（Map）访问

**用途**：通过键访问映射中的值。

**示例**：

```freemarker
${user["name"]}
```

### 11. 序列（List/Set）操作

**用途**：对序列进行各种操作，如获取长度、查找等。

**示例**：

```freemarker
${items?size}
${items[0]}
```

### 总结

FreeMarker 提供了多种强大的模板语法元素，从简单的变量插值到复杂的控制结构和数据处理功能。这些语法元素使得 FreeMarker 成为一个非常灵活且易于使用的模板引擎，适用于生成 HTML 页面、电子邮件、配置文件等各种文本格式的输出。通过结合使用这些语法元素，你可以构建出复杂而高效的模板系统。