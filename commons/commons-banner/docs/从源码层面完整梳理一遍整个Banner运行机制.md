好问题！我们从源码层面完整梳理一遍整个运行机制。流程图中每个节点都可以点击追问细节，下面逐层解释每个阶段的原理。

---

## 一、触发时机：Spring Boot 为什么会调用我们的代码？

Spring Boot 的 `SpringApplication.run()` 在内部有一个固定的启动序列，Banner 的打印发生在 **IoC 容器刷新之前**，这是关键：

```java
// Spring Boot 源码简化版（SpringApplication.java）
public ConfigurableApplicationContext run(String... args) {
    // 1. 准备环境（读取 application.yml）
    ConfigurableEnvironment environment = prepareEnvironment(...);

    // 2. ← 这里打印 Banner，容器还没启动！
    Banner printedBanner = printBanner(environment);

    // 3. 创建 ApplicationContext
    context = createApplicationContext();

    // 4. 刷新容器（@Bean / @Autowired 等在这里处理）
    refreshContext(context);
}
```

打印时机早于容器刷新，意味着 `@Autowired`、`@Value` 等注解全都不可用，这就是为什么 `buildProperties()` 里要手动调用 `environment.getProperty()` 而不是注入配置类。

---

## 二、Banner 接口：Spring Boot 的扩展点

`Banner` 是 Spring Boot 提供的一个单方法接口：

```java
// Spring Boot 源码
@FunctionalInterface
public interface Banner {
    void printBanner(Environment environment, Class<?> sourceClass, PrintStream out);
}
```

默认实现是 `SpringBootBanner`（打印 Spring 字样的那个）。我们通过以下方式替换它：

```java
SpringApplication app = new SpringApplication(AdminServerApplication.class);
app.setBanner(new FigletBannerPrinter()); // 替换默认实现
app.run(args);
```

Spring Boot 内部的 `printBanner()` 逻辑会按优先级查找 Banner 实现：自定义 `banner.txt` 文件 → 自定义 `Banner` 实现 → 默认 `SpringBootBanner`，我们的方式属于第二优先级，直接覆盖。

---

## 三、字体缓存：为什么要用 `static` 块？

```java
// 类加载时执行一次，之后所有调用共享字节数组
private static final byte[] FONT_BYTES;
static {
    FONT_BYTES = loadFontBytes("/fonts/ANSI Shadow.flf");
}
```

Banner 接口每次调用 `printBanner()` 都是同一个实例，字体文件如果放在方法内读取，每次启动都会有磁盘 IO。用 `static` 块在类加载时读入内存后，每次渲染直接用 `new ByteArrayInputStream(FONT_BYTES)` 包装，零磁盘开销。

---

## 四、启动耗时：为什么不用 `ApplicationStartedEvent`？

```java
// 类加载时记录时间戳，越早越准
private static final long JVM_START_TIME = System.currentTimeMillis();

// printBanner 被调用时做差值
long elapsed = System.currentTimeMillis() - JVM_START_TIME;
```

`ApplicationStartedEvent` 在容器刷新完成后才触发，那时已经过了大部分启动时间，用它计算耗时会偏小。我们在类加载时就记录时间戳，`printBanner` 被调用时做差值，得到的是从 JVM 加载我们的类到 Banner 打印这段时间，更接近用户感知的真实启动耗时。

---

## 五、主题模式：策略模式的经典应用

整个主题系统就是一个**策略模式**：

```
BannerTheme（接口）
    ├── DefaultBannerTheme   ← 策略A
    ├── MinimalBannerTheme   ← 策略B
    ├── BoxBannerTheme       ← 策略C
    └── ColorfulBannerTheme  ← 策略D

BannerThemeFactory           ← 策略工厂
FigletBannerPrinter          ← 上下文（Context），持有并调用策略
```

`FigletBannerPrinter` 只负责收集数据，完全不关心怎么渲染，切换主题只需改 yml 里的一个字符串，无需修改任何 Java 代码，符合开闭原则。