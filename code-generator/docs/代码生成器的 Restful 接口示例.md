以下是基于 Spring Boot 和 MyBatis-Plus Generator 实现的 Restful 接口代码生成器完整示例，支持通过前端传递参数动态生成代码：

---

### 一、接口设计

#### 1. 请求参数实体类
```java
@Data
public class GenConfigRequest {
    // 数据库配置
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    
    // 包结构配置
    private String parentPackage = "com.example";
    private String moduleName = "demo";
    
    // 生成策略
    private List<String> tables;       // 要生成的表名列表
    private String tablePrefix = "";   // 表前缀过滤
    private boolean lombokEnabled = true;
    private boolean swaggerEnabled = true;
    
    // 输出配置
    private String outputDir = "/tmp/gencode"; // 临时生成目录
}
```

#### 2. 接口定义
```java
@RestController
@RequestMapping("/api/code-generator")
public class CodeGeneratorController {

    @Autowired
    private CodeGeneratorService codeGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<Resource> generateCode(@RequestBody GenConfigRequest config) 
        throws IOException {
        
        // 生成代码到临时目录
        codeGeneratorService.generate(config);
        
        // 将生成的代码打包为 ZIP
        File zipFile = codeGeneratorService.packageToZip(config.getOutputDir());
        
        // 返回 ZIP 文件流
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated-code.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(zipFile));
    }
}
```

---

### 二、核心服务实现

#### 1. 代码生成服务类
```java
@Service
public class CodeGeneratorService {

    public void generate(GenConfigRequest config) {
        // 动态配置生成器
        FastAutoGenerator.create(config.getDbUrl(), config.getDbUsername(), config.getDbPassword())
                .globalConfig(builder -> {
                    builder.author(System.getProperty("user.name"))
                            .outputDir(config.getOutputDir())
                            .disableOpenDir();
                    
                    if (config.isSwaggerEnabled()) {
                        builder.enableSwagger();
                    }
                })
                .packageConfig(builder -> {
                    builder.parent(config.getParentPackage())
                            .moduleName(config.getModuleName())
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .controller("controller");
                })
                .strategyConfig(builder -> {
                    builder.addInclude(config.getTables())
                            .addTablePrefix(config.getTablePrefix())
                            .entityBuilder()
                                .enableLombok(config.isLombokEnabled())
                                .enableChainModel()
                            .controllerBuilder()
                                .enableRestStyle();
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    public File packageToZip(String sourceDir) throws IOException {
        String zipPath = sourceDir + ".zip";
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath));
             FileInputStream fis = new FileInputStream(sourceDir)) {
            
            FileUtils.zipDirectory(new File(sourceDir), zos, "");
            return new File(zipPath);
        }
    }
}
```

#### 2. 文件压缩工具类
```java
public class FileUtils {

    public static void zipDirectory(File sourceDir, ZipOutputStream zos, String parentPath) throws IOException {
        for (File file : sourceDir.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, zos, parentPath + file.getName() + "/");
            } else {
                ZipEntry zipEntry = new ZipEntry(parentPath + file.getName());
                zos.putNextEntry(zipEntry);
                Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        }
    }
}
```

---

### 三、前端调用示例

#### 1. 请求格式
```json
POST /api/code-generator/generate
Content-Type: application/json

{
  "dbUrl": "jdbc:mysql://localhost:3306/test_db?useSSL=false",
  "dbUsername": "root",
  "dbPassword": "123456",
  "tables": ["user", "order"],
  "parentPackage": "com.myproject",
  "moduleName": "system"
}
```

#### 2. 响应处理
前端接收到 ZIP 文件流后，可通过以下方式处理下载：
```javascript
axios.post('/api/code-generator/generate', config, { responseType: 'blob' })
  .then(response => {
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'generated-code.zip');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  });
```

---

### 四、高级功能扩展

#### 1. 异步生成支持
```java
@PostMapping("/async-generate")
public ResponseEntity<String> asyncGenerate(@RequestBody GenConfigRequest config) {
    CompletableFuture.runAsync(() -> {
        codeGeneratorService.generate(config);
        codeGeneratorService.notifyFrontend(config.getTaskId()); // 通过WebSocket通知前端
    });
    return ResponseEntity.accepted().body("任务已提交，ID: " + taskId);
}
```

#### 2. 生成结果缓存
```java
// 使用 Redis 缓存 ZIP 文件路径
@Cacheable(value = "gencode", key = "#config.hashCode()")
public String cacheGeneratedCode(GenConfigRequest config) {
    return generateAndZip(config);
}
```

#### 3. 参数校验增强
```java
@Data
@Validated
public class GenConfigRequest {
    @NotBlank(message = "数据库URL不能为空")
    private String dbUrl;
    
    @Size(min = 1, message = "至少选择一个表")
    private List<String> tables;
}
```

---

### 五、注意事项

1. **安全防护**
    - 对数据库连接信息进行加密传输
    - 限制生成目录防止路径穿越攻击
   ```java
   if (config.getOutputDir().contains("..")) {
       throw new IllegalArgumentException("非法路径");
   }
   ```

2. **资源清理**  
   添加定时任务清理临时文件：
   ```java
   @Scheduled(cron = "0 0 3 * * ?")
   public void cleanTempFiles() {
       FileUtils.deleteDirectory(new File("/tmp/gencode"));
   }
   ```

3. **性能优化**
    - 使用内存文件系统（如 JimFS）替代磁盘操作
    - 对相同参数的生成请求返回缓存结果

---

通过此方案，可实现从前端页面动态配置并触发代码生成，生成的代码通过 ZIP 包下载，满足企业级代码生成需求。可根据实际项目需求扩展模板管理、历史记录等功能。