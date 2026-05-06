# SpringBoot3项目怎么使用FreeMarker模板引擎？

在 Spring Boot 3 项目中使用 FreeMarker 模板引擎，你需要进行一些配置，并创建相应的控制器和模板文件。以下是详细的步骤、示例代码以及对 FreeMarker 常用语法的解释。

### 添加依赖

首先，在 `pom.xml` 中添加 `spring-boot-starter-freemarker` 依赖项：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-freemarker</artifactId>
</dependency>
```

### 配置 FreeMarker

Spring Boot 自动配置了 FreeMarker，但你可以在 `application.properties` 或 `application.yml` 中自定义设置。例如：

```properties
# application.properties
spring.freemarker.template-loader-path=classpath:/templates/
spring.freemarker.suffix=.ftl
spring.freemarker.charset=UTF-8
spring.freemarker.request-context-attribute=request
```

或者在 YAML 格式中：

```yaml
# application.yml
spring:
  freemarker:
    template-loader-path: classpath:/templates/
    suffix: .ftl
    charset: UTF-8
    request-context-attribute: request
```

### 创建控制器

接下来，创建一个简单的 Spring MVC 控制器来处理请求并返回视图名称：

```java
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String hello(Model model) {
        model.addAttribute("message", "Hello, World!");
        return "hello"; // 对应于 templates/hello.ftl
    }
}
```

### 创建 FreeMarker 模板

在 `src/main/resources/templates/` 目录下创建 `hello.ftl` 文件：

```html
<!DOCTYPE html>
<html>
<head>
    <title>Hello Page</title>
</head>
<body>
    <h1>${message}</h1>
</body>
</html>
```

### 运行应用程序

启动你的 Spring Boot 应用程序，访问根路径 `/`，你应该会看到显示了 "Hello, World!" 的页面。

### 解释 FreeMarker 常用语法的作用

FreeMarker 使用它自己的模板语言来描述如何组合静态文本和动态内容。这里是一些常用语法的简要说明：

- **变量引用** `${expression}`：插入表达式的值到输出中。`expression` 可以是简单变量名或更复杂的表达式。

  ```html
  <p>Welcome, ${user.name}!</p>
  ```

- **指令** `<#directive ...>`：控制逻辑流程或定义宏等。常见的有：

    - **条件语句** `<#if condition>...</#if>`：根据条件执行模板部分。

      ```freemarker
      <#if user.isAdmin>
          <p>Welcome admin!</p>
      </#if>
      ```

    - **循环** `<#list items as item>...</#list>`：遍历序列中的元素。

      ```freemarker
      <ul>
          <#list users as user>
              <li>${user.name}</li>
          </#list>
      </ul>
      ```

    - **宏和函数** `<#macro name>...</#macro>` 和 `<@name ... />`：定义可重用的代码块或功能。

      ```freemarker
      <#macro greeting name="World">
          <p>Hello, ${name}!</p>
      </#macro>
  
      <@greeting name="Alice"/>
      ```

- **内建函数** `${variable?function}`：用于转换或格式化数据。比如日期格式化 `${date?string("yyyy-MM-dd")}`。

- **插值与字符串拼接**：可以将多个表达式直接放在一起，中间不加分隔符，或者使用加号 `+` 进行拼接。

  ```freemarker
  ${"Hello, " + user.name}
  ```

- **注释** `<#-- This is a comment -->`：不会出现在最终输出中的注释。

- **包含其他模板** `<#include "/common/header.ftl">`：引入其他 FreeMarker 模板文件。

- **导入命名空间** `<#import "/common/macros.ftl" as m>`：允许访问外部资源中的宏或变量。

- **设置全局变量** `<#setting number_format="currency">`：影响整个模板解析过程的行为。

通过这些语法元素，你可以创建灵活且动态的 HTML 页面或其他类型的文本输出。以上只是 FreeMarker 功能的一个概览，实际应用中还有更多高级特性和选项可用。

# FreeMarker 模板中的数据是怎么获取的？

FreeMarker 模板中的数据是通过一个称为“数据模型”（Data Model）的结构传递给模板引擎的。这个数据模型是一个键值对集合，其中键通常是字符串，而值可以是任何 Java 对象，包括但不限于：

- 基本类型（如 `String`, `Integer`）
- 复杂对象（如自定义类实例）
- 集合（如 `List`, `Set`）
- 映射（如 `Map`）

在 FreeMarker 模板中，你使用 `${key}` 或者 `<#assign key=value>` 等方式来引用这些键所对应的值。

### 数据传递的方式

#### 1. **Spring MVC 中的 Model**

当你在一个 Spring Boot 或 Spring MVC 应用程序中使用 FreeMarker 时，通常会在控制器方法中将数据添加到 `Model` 或 `ModelAndView` 对象中。例如：

```java
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleController {

    @GetMapping("/example")
    public String example(Model model) {
        // 添加单个属性
        model.addAttribute("message", "Hello, World!");
        
        // 添加复杂对象
        User user = new User("Alice");
        model.addAttribute("user", user);
        
        // 添加集合
        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
        model.addAttribute("items", items);

        return "example"; // 返回逻辑视图名称，对应于 templates/example.ftl
    }
}
```

在这个例子中，`model.addAttribute()` 方法用于向模型中添加数据，然后返回逻辑视图名称 `"example"`，这会告诉 Spring MVC 使用名为 `example.ftl` 的 FreeMarker 模板，并将模型中的数据传递给它。

#### 2. **直接创建数据模型**

如果你不使用 Spring MVC，而是直接使用 FreeMarker API，你可以手动创建一个数据模型并将其传递给模板处理器。例如：

```java
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FreeMarkerExample {
    public static void main(String[] args) throws IOException, TemplateException {
        // 创建配置实例
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
        cfg.setDefaultEncoding("UTF-8");

        // 加载模板
        Template template = cfg.getTemplate("example.ftl");

        // 创建数据模型
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("message", "Hello, World!");
        dataModel.put("user", new User("Alice"));
        dataModel.put("items", Arrays.asList("Item 1", "Item 2", "Item 3"));

        // 输出到文件或流
        try (FileWriter writer = new FileWriter("output.html")) {
            template.process(dataModel, writer);
        }
    }
}
```

在这个例子中，我们直接创建了一个 `Map<String, Object>` 作为数据模型，并将其与模板一起传递给 `template.process()` 方法进行渲染。

### 数据访问的方式

一旦数据被传递给 FreeMarker 引擎，你就可以在模板中以如下几种方式访问它们：

- **直接引用**：`${variableName}` 直接获取变量的值。

  ```freemarker
  <p>${message}</p>
  ```

- **点符号或方括号语法**：对于复杂对象或映射，可以使用点符号（`.`）或方括号（`[]`）来访问属性或键。

  ```freemarker
  <p>Welcome, ${user.name}!</p>
  <p>Welcome, ${user["name"]}!</p>
  ```

- **循环和条件语句**：对于集合，可以使用 `<#list>` 循环；对于条件判断，可以使用 `<#if>` 指令。

  ```freemarker
  <ul>
      <#list items as item>
          <li>${item}</li>
      </#list>
  </ul>

  <#if user.isAdmin>
      <p>Welcome admin!</p>
  </#if>
  ```

- **内建函数**：使用内置函数来格式化或转换数据，如 `${date?string("yyyy-MM-dd")}`。

### 总结

FreeMarker 模板中的数据来源取决于你的应用程序框架和实现方式。在 Spring MVC 中，数据通常来自控制器中的模型对象；而在其他情况下，你可能需要手动构建数据模型并将其传递给 FreeMarker 引擎。无论哪种方式，FreeMarker 提供了丰富的语法让你能够灵活地操作和展示这些数据。