要使用最新的 MyBatis-Plus Generator 搭配 Spring Boot Starter Freemarker 实现代码生成，以下是基于最新实践的综合配置步骤和注意事项：

---

### 一、依赖配置
1. **核心依赖**  
   在 `pom.xml` 中引入以下依赖（版本以 MyBatis-Plus 3.5.2 和 Spring Boot 3.x 为例）：
   ```xml
   <!-- MyBatis-Plus 核心依赖 -->
   <dependency>
       <groupId>com.baomidou</groupId>
       <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
       <version>3.5.7</version>
   </dependency>
   
   <!-- 代码生成器依赖 -->
   <dependency>
       <groupId>com.baomidou</groupId>
       <artifactId>mybatis-plus-generator</artifactId>
       <version>3.5.10.1</version>
   </dependency>
   
   <!-- Freemarker 模板引擎 -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-freemarker</artifactId>
   </dependency>
   
   <!-- MySQL 驱动 -->
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <scope>runtime</scope>
   </dependency>
   
   <!-- Lombok（可选，但推荐） -->
   <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok</artifactId>
       <optional>true</optional>
   </dependency>
   ```
   **说明**：
    - `mybatis-plus-generator` 需要与 `mybatis-plus-boot-starter` 版本一致。
    - Spring Boot 3.x 需确保依赖版本兼容，如 `spring-boot-starter-freemarker` 需适配 Spring Boot 主版本。

---

### 二、代码生成器配置
2. **生成器类示例**  
   使用 `FastAutoGenerator`（推荐）简化配置，生成实体类、Mapper、Service、Controller 等代码：
   ```java
   public class CodeGenerator {
       public static void main(String[] args) {
           FastAutoGenerator.create(
                   "jdbc:mysql://localhost:3306/your_db?useSSL=false&serverTimezone=Asia/Shanghai",
                   "root",
                   "password"
               )
               .globalConfig(builder -> {
                   builder.author("YourName")         // 作者
                          .outputDir(System.getProperty("user.dir") + "/src/main/java") // 输出目录
                          .fileOverride()             // 覆盖已生成文件
                          .enableSwagger()            // 开启 Swagger（需额外依赖）
                          .disableOpenDir();          // 生成后不打开目录
               })
               .packageConfig(builder -> {
                   builder.parent("com.example")      // 父包名
                          .moduleName("system")       // 模块名
                          .entity("entity")           // 实体类包名
                          .mapper("mapper")           // Mapper 接口包名
                          .service("service")         // Service 包名
                          .controller("controller");  // Controller 包名
               })
               .strategyConfig(builder -> {
                   builder.addInclude("user", "role")  // 需生成的表名
                          .addTablePrefix("t_")       // 过滤表前缀
                          .entityBuilder()
                              .enableLombok()          // 启用 Lombok
                              .enableChainModel()      // 链式模型
                          .controllerBuilder()
                              .enableRestStyle();      // REST 风格 Controller
               })
               .templateEngine(new FreemarkerTemplateEngine()) // 使用 FreeMarker 引擎模板
               .execute();
       }
   }
   ```
   **关键点**：
    - `templateEngine(new FreemarkerTemplateEngine())` 指定模板引擎为 Freemarker。
    - `enableSwagger()` 需额外引入 `springfox-swagger2` 依赖。

---

### 三、高级配置
3. **自定义模板路径**  
   若需覆盖默认模板，可在 `resources/templates` 下创建自定义模板文件（如 `entity.java.ftl`），并在生成器中配置：
   ```java
   .templateConfig(builder -> {
       builder.entity("/templates/entity.java.ftl")  // 自定义实体模板
              .controller("/templates/controller.java.ftl");
   })
   ```
   **注意**：模板文件需与 Freemarker 语法兼容。

4. **多模块支持**  
   若项目为多模块结构，可通过 `outputDir` 指定生成路径到特定模块：
   ```java
   .globalConfig(builder -> {
       builder.outputDir("模块路径/src/main/java");
   })
   ```

---

### 四、运行与验证
5. **执行生成器**  
   直接运行 `main` 方法，代码将生成到指定目录。生成后需检查以下内容：
    - 实体类是否包含 Lombok 注解（如 `@Data`）。
    - Mapper XML 文件是否生成在 `resources/mapper` 目录。
    - Controller 是否启用 `@RestController` 注解。

6. **解决常见问题**
    - **依赖缺失**：若生成代码后报错，检查是否遗漏 Lombok、Swagger 或 Web 依赖。
    - **Mapper 扫描失败**：在启动类添加 `@MapperScan("com.example.mapper")` 或为 Mapper 接口添加 `@Mapper` 注解。

---

### 五、版本兼容性建议
- **MyBatis-Plus 3.5.2** 适配 Spring Boot 2.7.x 和 3.x，需确保 JDK 版本 ≥ 17（Spring Boot 3.x 要求）。
- 若需降级版本，可参考 `mybatis-plus-generator 3.4.1` 与 `spring-boot-starter-freemarker 2.7.x` 的组合。

---

通过以上步骤，可快速实现基于 MyBatis-Plus Generator 和 Freemarker 的代码自动化生成，显著提升开发效率。具体配置可根据项目需求调整模板和策略参数。