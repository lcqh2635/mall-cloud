MongoDB 中的常见概念包括数据库（Database）、集合（Collection）、文档（Document）、字段（Field）、索引（Index）、嵌套文档（Nested Document）、复制集（Replica Set）、分片集群（Sharded Cluster）等。

下面是 MongoDB 中的常见概念及其与 MySQL 中的相关概念的对应关系：

1. 数据库（Database）：
   - MongoDB：数据库是一个数据存储和管理的容器。
   - MySQL：数据库在 MySQL 中也是一个数据存储和管理的容器，例如，可以有多个数据库，每个数据库包含多个表。

2. 集合（Collection）：
   - MongoDB：集合是一组文档的容器，类似于表。
   - MySQL：集合在 MySQL 中对应于表，是一组行和列的结构化数据集合。

3. 文档（Document）：
   - MongoDB：文档是 MongoDB 中的基本数据单元，类似于行。
   - MySQL：文档在 MySQL 中对应于行，是表中的一条记录。

4. 字段（Field）：
   - MongoDB：字段是文档中包含的键值对，类似于列。
   - MySQL：字段对应于表中的列，包含了特定类型的数据。

5. 索引（Index）：
   - MongoDB：索引用于快速检索文档，提高查询效率。
   - MySQL：索引在 MySQL 中也用于快速检索数据，可以提高查询性能。

6. 嵌套文档（Nested Document）：
   - MongoDB：嵌套文档允许一个文档中嵌套其他文档或数组。
   - MySQL：嵌套文档在 MySQL 中通常以多表关联的方式实现。

7. 复制集（Replica Set）：
   - MongoDB：复制集是一组维护相同数据集合的 MongoDB 服务器实例。
   - MySQL：复制集在 MySQL 中类似于主从复制，用于实现数据的冗余和高可用性。

8. 分片集群（Sharded Cluster）：
   - MongoDB：分片集群允许 MongoDB 横向扩展，分散数据。
   - MySQL：在 MySQL 中，分片集群通常通过分片技术实现数据的横向扩展，把数据分散到不同的服务器。

#### MongoDB操作数据库

在 MongoDB 中，您可以使用以下命令来创建数据库、查看数据库和删除数据库：

1. 创建数据库：
   - 在 MongoDB 中，数据库只有在插入第一个文档时才会被创建，因此您可以直接插入文档来创建数据库。例如，使用以下命令插入一个文档，自动创建名为`your_database_name`的数据库：
     ```bash
     use your_database_name
     db.collection_name.insertOne({ key: value })
     
     use blog
     db.t_user.insertOne({ key: value })
     
     # 插入数据
     db.t_user.insertOne({
         name: "John",
         age: 30,
         email: "john@example.com"
     })
     ```
   - 如果要在客户端创建空数据库，可以使用`db.createCollection()`命令创建一个集合。集合的创建会自动创建包含该集合的数据库。例如，使用以下命令创建一个名为`your_database_name`的空数据库：
     ```bash
     use your_database_name
     db.createCollection("collection_name")
     ```
   
2. 查看数据库：
   - 在 MongoDB 中，使用以下命令可以列出所有数据库：
     ```bash
     show dbs
     ```

3. 删除数据库：
   - 在 MongoDB 中，使用以下命令来删除数据库：
     ```bash
     use your_database_name
     db.dropDatabase()
     ```
     注意：删除数据库操作是不可逆的，请谨慎使用。