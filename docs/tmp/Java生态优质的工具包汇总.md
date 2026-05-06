Java 生态系统非常丰富，拥有大量优质的第三方工具包（库/框架），广泛应用于开发的各个领域。以下是一些主流且高质量的第三方工具包，按功能分类介绍其作用：

---

### 十二、其他实用工具

| 工具包                                                                       | 作用                                                                                                                        |
|---------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| **[Lombok](https://lombok.com.cn/features/)**                             | 通过注解自动生成 getter/setter/toString 等，减少样板代码。                                                                                 |
| **[MapStruct](https://mapstruct.org/)**       |  自动生成 Bean 映射代码（替代手动 set/get），性能高。                                                                                                                         |
| **[MapStructPlus](https://www.mapstruct.plus/)**                          | 自动生成 Bean 映射代码（替代手动 set/get），性能高。                                                                                         |
| **[Hutool](https://doc.hutool.cn/pages/index/)**                          | 国产工具库，封装了大量常用工具方法（HTTP、加密、日期等），适合国内项目。                                                                                    |
| **[FastExcel](https://fast-excel.github.io/fastexcel/zh-cn/)**            | 快速、简洁、解决大文件内存溢出的 Java 处理 Excel 工具。                                                                                        |
| **[excel-spring-boot-starter](https://www.yuque.com/pig4cloud/excel)**    | 基于 FastExcel 实现的 Spring Boot Starter，用于简化 Excel 的读写操作。                                                                    |
| **[tianai-captcha](http://doc.captcha.tianai.cloud/)**                    | 开源的行为验证码工具，支持多种验证码类型。开源版默认提供了 滑块验证码、旋转验证码、文字点选验证码、滑动还原验证码等。[Hutool 也提供了简单的图形验证码功能，封装了 CaptchaUtil](https://doc.hutool.cn/pages/captcha)                         |
| **[Jeepay](https://doc.jeequan.com/#/integrate/open)**                    | Jeepay计全支付是一套适合企业使用的开源支付系统，提供聚合支付接口，包括交易、退款、转账、分账等。已对接微信，支付宝，云闪付官方接口，以及三方支付和银行的间联通道等。                                     |
| **[IJPay](https://javen205.github.io/IJPay/)**                            | IJPay 让支付触手可及，不依赖任何第三方 MVC 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。                                                          |
| **[oshi](https://www.github-zh.com/projects/3407114-oshi)**               | oshi 是一个获取操作系统和硬件信息的Java库，或获取OS版本、进程、内存、CPU使用、磁盘、分区等信息。[Hutool 提供了Oshi封装-OshiUtil](https://doc.hutool.cn/pages/OshiUtil/) |
| **[JustAuth](https://www.jetbrains.com/)**                                | 一个第三方授权登录的工具类库，它可以让我们脱离繁琐的第三方登录 SDK，让登录变得So easy!                                                                         |
| **[caffeine](https://github.com/ben-manes/caffeine/wiki/Home-zh-CN)**     | Caffeine 是一个基于Java8开发的提供了近乎最佳命中率的高性能的缓存库。                                                                                 |
| **[fastjson2](https://www.github-zh.com/projects/482425877-fastjson2)**   | FastJson2 是一个性能出色的 Java JSON 库。                                                                                           
| **[MyBatis-Plus-Join](https://mybatis-plus-join.github.io/)**  |  MyBatis-Plus 最佳搭档，只做增强不做改变，支持连表查询的mybatis-plus,mybatis-plus风格的连表操作提供left join、right join等操作                                                                                                                        |
| **[Easy-Es](https://www.easy-es.cn/)**                  |  Easy-Es是Mybatis-Plus在ElasticSearch的平替版.                                                                                                                         |

[Github 上最受欢迎的 Java 项目统计网址](https://www.github-zh.com/top/Java)
---

### 总结：推荐组合（现代 Java 项目）

```xml
<!-- 示例：Spring Boot 项目常用依赖 -->
<dependencies>
    <!-- 引入easy-es最新版本的依赖-->
    <dependency>
        <groupId>org.dromara.easy-es</groupId>
        <artifactId>easy-es-boot-starter</artifactId>
        <!--这里Latest Version是指最新版本的依赖,比如3.0.0,可以通过下面的图片获取-->
        <version>3.0.0</version>
    </dependency>
    <!-- 如果有依赖冲突,导致底层es相关依赖非7.17.28,需要参考避坑指南章节文档先排除springboot中内置的es依赖-->

    <dependency>
        <groupId>io.github.linpeilie</groupId>
        <artifactId>mapstruct-plus-spring-boot-starter</artifactId>
        <version>1.5.0</version>
    </dependency>

    <dependency>
        <groupId>com.alibaba.fastjson2</groupId>
        <artifactId>fastjson2</artifactId>
        <version>2.0.58</version>
    </dependency>

    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
        <version>3.2.2</version>
    </dependency>

    <dependency>
        <groupId>com.github.yulichang</groupId>
        <artifactId>mybatis-plus-join-boot-starter</artifactId>
        <version>1.5.4</version>
    </dependency>
</dependencies>
```

---

这些工具包构成了现代 Java 开发生态的核心。选择时应根据项目需求（性能、可维护性、团队熟悉度）进行权衡。建议优先选择社区活跃、文档完善、持续维护的开源项目。