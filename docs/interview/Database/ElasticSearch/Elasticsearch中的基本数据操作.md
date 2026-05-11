

当使用 Elasticsearch 7.x 及更早的版本时，可以演示索引（Index）、类型（Type）、文档（Document）、字段（Field）、映射（Mapping）和索引分片（Index Shards）的使用。请参考以下示例：

1. 创建索引（Index）：
```
PUT /my_index
```

2. 创建类型（Type）（在 Elasticsearch 7.x 中创建一个类型已经被废弃，仅适用于 Elasticsearch 6.x 及更早的版本）：
```
PUT /my_index/_doc/1
{
  "field1": "value1",
  "field2": "value2"
}
```

3. 获取文档（Document）：
```
GET /my_index/_doc/1
```

4. 更新文档（Document）：
```
POST /my_index/_update/1
{
  "doc": {
    "field2": "new_value"
  }
}
```

5. 搜索文档（Document）：
```
GET /my_index/_search
{
  "query": {
    "match": {
      "field1": "value1"
    }
  }
}
```

6. 创建字段（Field）映射（Mapping）：
```
PUT /my_index/_mapping
{
  "properties": {
    "field1": {
      "type": "keyword"
    },
    "field2": {
      "type": "text"
    }
  }
}
```

7. 查看映射（Mapping）：
```
GET /my_index/_mapping
```

8. 分片（Shard）相关操作，例如获取分片信息：
```
GET /my_index/_settings
GET /my_index/_shard_stores
```

请注意，以上示例基于 Elasticsearch 7.x 及更早版本。在 Elasticsearch 8.x 版本中，类型（Type）的概念已被废弃，并且建议将多类型的索引重新设计为多个单类型索引。
