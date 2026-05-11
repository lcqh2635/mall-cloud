参考博文：https://springdoc.cn/spring-boot/data.html#data.nosql.elasticsearch

### ElasticsearchClient、ElasticsearchRestTemplate、RestHighLevelClient的区别

ElasticsearchClient、ElasticsearchRestTemplate和RestHighLevelClient是 Elasticsearch Java 客户端的不同实现，它们在功能和用法上有一些区别。

1. **ElasticsearchClient**：
   - ElasticsearchClient 是 Elasticsearch Java 客户端的基础接口，定义了操作 Elasticsearch 的基本方法。
   - 它提供了最底层的 HTTP 请求发送和响应解析功能，但不提供任何额外的封装或便利方法。
   - 使用 ElasticsearchClient 需要手动处理请求和响应的 JSON 序列化和反序列化。
   - 通常情况下，直接使用 ElasticsearchClient 需要对 Elasticsearch 的 API 有更深入的了解。

2. **ElasticsearchRestTemplate**：
   - ElasticsearchRestTemplate 是 Spring Data Elasticsearch 提供的 Elasticsearch 客户端封装。
   - 它对 ElasticsearchClient 进行了高层次的封装，提供了更简单的 API 和更方便的操作方式。
   - ElasticsearchRestTemplate 提供了诸如索引创建、文档保存、搜索查询等常见操作的方法。它通过内部的 ObjectMapper 处理请求和响应的 JSON 序列化和反序列化。
   - ElasticsearchRestTemplate 还支持使用注解进行对象和索引的映射，并提供了一些高级的查询功能。
   - 使用 ElasticsearchRestTemplate 可以更加便捷地操作 Elasticsearch，尤其在 Spring Boot 项目中更为常用。

3. **RestHighLevelClient**：
   - RestHighLevelClient 是 Elasticsearch 官方提供的 Java 高级客户端。
   - 它基于低级别的 RestClient，提供了更丰富的功能和更友好的 API，适用于高级应用场景。
   - RestHighLevelClient 提供了一系列的方法，可以进行索引、搜索、更新、删除等操作，也支持异步执行。
   - 同样，RestHighLevelClient 通过内部的 ObjectMapper 处理请求和响应的 JSON 序列化和反序列化。
   - 如果需要使用 Elasticsearch 提供的一些高级功能或 API，可以选择使用 RestHighLevelClient。

总结：
- 如果你想要简化 Elasticsearch 的操作并与 Spring 框架集成，可以选择使用 ElasticsearchRestTemplate。
- 如果你需要访问 Elasticsearch 提供的一些高级功能或 API，可以选择使用 RestHighLevelClient。
- 如果你需要更底层的控制，或者对 Elasticsearch 的 API 有深入了解，可以使用 ElasticsearchClient。
根据自己的需求和项目环境，选择适合的 Elasticsearch 客户端。

**对于 Spring Boot 项目整合 Elasticsearch，推荐使用 ElasticsearchRestTemplate。**