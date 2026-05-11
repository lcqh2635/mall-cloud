### ElasticSearch的作用是什么？

Elasticsearch是一个开源的、分布式的搜索和分析引擎，其作用主要包括以下几个方面：

1. **全文搜索**：Elasticsearch提供了强大的全文搜索功能，可以快速地从大规模数据集中进行文本搜索。它使用倒排索引的技术，将文档的内容进行索引并进行高效的搜索和匹配。

2. **实时数据分析**：Elasticsearch支持实时数据的索引和分析，可以从结构化和非结构化数据中提取有用的信息，并进行复杂的数据聚合、过滤和查询。

3. **数据存储和索引**：Elasticsearch作为一个分布式的 NoSQL 数据库，可以存储和索引大规模的数据。它使用分片和副本的机制，将数据划分为多个分片并在集群中进行复制，以实现高可用性和水平扩展。

4. **日志和事件数据处理**：Elasticsearch在处理日志和事件数据方面非常强大。通过将日志数据发送到Elasticsearch并进行索引，可以实现快速的日志搜索、分析和监控。

5. **全文检索引擎**：Elasticsearch支持复杂的全文搜索功能，包括模糊匹配、近似查询、词义扩展等。它还提供了高亮显示、聚合查询、排序等功能，可以满足各种搜索需求。

6. **分布式架构**：Elasticsearch的分布式架构使其具有高可用性和可伸缩性。它可以将数据分布在多个节点上，并自动处理节点故障和负载均衡的问题。

总的来说，Elasticsearch在搜索、分析和存储大规模数据方面非常强大，被广泛应用于各种场景，如企业搜索、日志分析、电子商务、监控和安全分析等。



### Elasticsearch的核心组件

在Spring Boot整合Elasticsearch中，Elasticsearch的核心组件包括以下几个：

1. **节点（Node）**：Elasticsearch是一个分布式系统，由多个节点组成。每个节点都是一个独立的Elasticsearch实例，负责存储和处理数据。在Spring Boot应用中，可以通过配置文件定义节点的地址和端口，或者使用默认的本地节点。

2. **索引（Index）**：索引是Elasticsearch中存储和组织数据的基本单位。类似于关系数据库中的表，索引包含一组具有相似结构的文档。每个索引都有一个唯一的名称，用于标识和引用。

3. **文档（Document）**：文档是Elasticsearch中的数据单元，类似于关系数据库中的记录。每个文档都是一个JSON格式的数据对象，包含多个字段。文档通过其在索引中的唯一ID进行标识和查询。

4. **映射（Mapping）**：映射定义了索引中文档的结构和属性。它指定了每个字段的数据类型、分词器、索引选项等信息。映射可以通过自动推断或手动定义。

5. **搜索（Search）**：搜索是Elasticsearch的核心功能之一，能够高效地执行复杂的全文搜索和过滤操作。你可以使用Elasticsearch提供的丰富的查询DSL（Domain Specific Language）来构建各种搜索条件。

6. **聚合（Aggregation）**：聚合是Elasticsearch用于统计和分析数据的功能。它能够根据某些条件对文档进行分组、计数、求和、平均值等操作，以提供有关数据的汇总信息。

7. **客户端（Client）**：在Spring Boot中，可以使用Elasticsearch提供的高级Java客户端（如REST Client、Transport Client）或者Spring Data Elasticsearch提供的抽象层来与Elasticsearch进行交互。

这些是Elasticsearch的核心组件，它们一起工作，使得你能够在Spring Boot应用中方便地进行数据存储、搜索和分析等操作。



### Elasticsearch中的常用注解

在 Elasticsearch 中，有几个常用的注解用于定义索引、映射和搜索行为。以下是其中一些常见的注解：

1. **@Document**：用于将一个类标记为 Elasticsearch 文档映射。它包含了索引名、文档类型和其他配置选项。

2. **@Id**：用于将字段标记为文档的唯一标识符（ID），每个文档必须具有唯一的 ID。

3. **@Field**：用于将字段标记为文档中的属性。可以指定字段名称、数据类型、索引选项、分析器等信息。

4. **@MultiField**：多字段注解，用于在一个字段上定义多个不同的分析器和映射规则。

5. **@Mapping**：用于在类级别指定映射规则，允许更灵活地定义字段的映射配置。

6. **@Analyzer**：用于指定自定义分析器，包括字符过滤器、分词器和令牌过滤器。

7. **@Parent**：用于定义父子关系，指定该文档的父文档。

8. **@Routing**：用于指定路由值，用于将文档存储在特定的分片上。

这些注解可以与 Elasticsearch 的 Java 客户端（如 Elasticsearch High-Level REST Client、Spring Data Elasticsearch）一起使用，以简化与 Elasticsearch 的交互。使用这些注解可以更轻松地定义索引结构、映射规则和搜索行为。

请注意，这些注解是针对特定的 Elasticsearch 客户端和框架，不同的客户端可能具有不同的注解名称和用法。具体的注解使用方式应参考相应的文档或库。



### SpringBoot整合ElasticSearch搭建搜索引擎

要在Spring Boot中整合Elasticsearch并搭建搜索引擎，你可以按照以下步骤进行操作：

1. **添加Maven依赖**：在项目的pom.xml文件中添加Elasticsearch和Spring Data Elasticsearch的Maven依赖。例如：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

2. **配置Elasticsearch连接**：在application.properties或application.yml文件中配置Elasticsearch的连接信息。例如：

```yaml
spring:
  elasticsearch:
  	# 连接地址，可以集群设置多个，逗号分隔
    uris: http://localhost:9200
    username: admin
    password: admin
    connection-timeout: 10
```

  关于Elasticsearch的跟多配置项，我们可以直接查看 **ElasticsearchProperties** 配置文件。

3. **定义数据模型**：根据需要定义要存储在Elasticsearch中的数据模型。这些模型类需要使用注解`@Document`、`@Id`等来标识索引和字段。例如：

```java
@Getter
@Setter
@Document(indexName = "book")
public class Book {
    @Id
    @Field(type = FieldType.Text)
    private String id;
    
    @Field(analyzer="ik_max_word")
    private String title;
    
    @Field(analyzer="ik_max_word")
    private String author;
    
    @Field(type = FieldType.Double)
    private Double price;
    
    @Field(type = FieldType.Date,format = DateFormat.basic_date_time)
    private Date createTime;
    
    @Field(type = FieldType.Date,format = DateFormat.basic_date_time)
    private Date updateTime;
}
```

每个字段必须使用 @Field 注解并设置type来指定文档映射到Elasticsearch中的数据类型，Java中的数据类型和Elasticsearch中的不相同。如果不指定 type 会导致数据映射失败。

总而言之，通过 `@Field` 注解中的 `type` 属性，你可以明确指定 Elasticsearch 索引中字段的类型，与 Java 中的字段类型进行映射，并确保数据正确地存储和检索。

4. **创建Elasticsearch操作接口**：创建一个继承自ElasticsearchRepository的接口，用于定义各种与Elasticsearch交互的操作。例如：

```java
@Repository
public interface MyDocumentRepository extends ElasticsearchRepository<MyDocument, String> {
    List<MyDocument> findByTitle(String title);
    // 其他操作方法
}
```

5. **编写服务层代码**：在服务层编写调用ElasticsearchRepository接口的方法，实现数据的增删改查等操作。

6. **启动应用程序**：通过Spring Boot启动你的应用程序，Elasticsearch相关的配置将会被加载，并且你可以使用定义的接口进行搜索引擎的操作了。

这样就完成了在Spring Boot中整合Elasticsearch并搭建搜索引擎的基本步骤。你可以根据自己的需求进一步扩展和优化。







为什么Elasticsearch数据模型字段有类型约束了还需要使用type 来指定





### 怎么构建查询请求？

在Spring Boot中整合Elasticsearch，你可以使用Elasticsearch的高级Java客户端（如REST Client、Transport Client）或者Spring Data Elasticsearch提供的抽象层来创建查询请求。下面我会介绍两种常用的方法：

1. **使用Elasticsearch的Java客户端**：

使用Elasticsearch的Java客户端，你可以直接与Elasticsearch进行交互，创建和执行查询请求。以下是一个示例：

```java
RestHighLevelClient client = new RestHighLevelClient(
        RestClient.builder(new HttpHost("localhost", 9200, "http")));

SearchRequest searchRequest = new SearchRequest("your_index");
SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
sourceBuilder.query(QueryBuilders.matchQuery("field_name", "search_text"));
searchRequest.source(sourceBuilder);

SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
SearchHits hits = searchResponse.getHits();
// 处理搜索结果

client.close();
```

2. **使用Spring Data Elasticsearch**：

Spring Data Elasticsearch提供了更高级的抽象层，简化了与Elasticsearch的交互。你可以定义一个Repository接口，并使用注解声明查询方法。以下是一个示例：

首先，在你的实体类中定义一个索引（@Document）和字段（@Field）：

```java
@Document(indexName = "your_index")
public class YourEntity {
    @Id
    private String id;
    
    @Field(type = FieldType.Text)
    private String field_name;
    
    // 其他字段和方法
}
```

然后，在Repository接口中声明查询方法：

```java
public interface YourEntityRepository extends ElasticsearchRepository<YourEntity, String> {
    List<YourEntity> findByFieldName(String search_text);
}
```

在 `ElasticsearchRepository<YourEntity, String>` 中，**`String` 是指实体类 `YourEntity` 的主键类型**。在 Spring Data Elasticsearch 中，你可以使用不同的类型作为主键，例如 String、Long、UUID 等。

在上述示例中，假设 `YourEntity` 实体类有一个名为 `id` 的字段作为主键，并且该字段的类型是 `String`。因此，在 `ElasticsearchRepository` 中指定 `String` 作为第二个泛型参数，以表示该实体类的主键类型为 `String`。

如果 `YourEntity` 实体类的主键类型是其他类型，你可以将 `String` 替换为相应的类型，例如 `Long`、`UUID` 等。

最后，在你的业务逻辑中使用该Repository：

```java
@Autowired
private YourEntityRepository repository;

List<YourEntity> result = repository.findByFieldName("search_text");
// 处理查询结果
```

这是Spring Boot整合Elasticsearch中创建查询请求的两种常见方法。你可以根据具体需求选择适合的方式来创建和执行查询请求。
