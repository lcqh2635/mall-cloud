在 MongoDB 中，常用的数据库操作包括：

1. 创建数据库：可以通过插入第一个文档或使用`db.createCollection()`命令来创建数据库。
2. 删除数据库：使用`db.dropDatabase()`命令删除数据库。
3. 查看数据库列表：使用`show dbs`命令列出所有数据库。
4. 切换数据库：使用`use database_name`命令切换到指定数据库。
5. 创建集合：可以通过插入第一个文档或使用`db.createCollection()`命令来创建集合。
6. 删除集合：使用`db.collection_name.drop()`命令删除集合。
7. 查看集合列表：使用`show collections`命令列出数据库中的所有集合。
8. 插入文档：使用`db.collection_name.insertOne()`或`db.collection_name.insertMany()`命令插入一个或多个文档。
9. 查询文档：使用`db.collection_name.find()`命令查询集合中的文档。
10. 更新文档：使用`db.collection_name.updateOne()`或`db.collection_name.updateMany()`命令更新集合中的文档。
11. 删除文档：使用`db.collection_name.deleteOne()`或`db.collection_name.deleteMany()`命令删除集合中的文档。
12. 索引操作：使用`db.collection_name.createIndex()`命令创建索引，`db.collection_name.getIndexes()`命令查看集合中的索引。
13. 聚合操作：使用`db.collection_name.aggregate()`命令进行数据聚合操作。
14. 批量导入导出数据：使用`mongodump`和`mongorestore`命令导入导出数据。
15. 备份与恢复：使用`mongodump`和`mongorestore`命令进行数据库的备份和恢复。

这些是 MongoDB 中常见的数据库操作，您可以根据需求选择相应的命令进行操作。

#### 使用示例

好的，我会给出一些示例来演示 MongoDB 中常见的数据库操作：

1. 创建数据库：
```bash
use blog-cloud
```
2. 创建集合：

   在 MongoDB 中，集合是自动创建的，当您向其中插入一个文档时，该集合会自动创建。因此，您无需显式地批量创建集合。当您插入第一个文档时，MongoDB 将会为该集合创建并自动分配存储空间，而您只需要确保使用正确的集合名称。
```bash
db.createCollection("t_user")
db.createCollection("t_user")
```
3. 插入文档：
```bash
db.t_user.insertOne({
    name: "John",
    age: 30,
    email: "john@example.com"
})
```
4. 查询文档：
```bash
db.t_user.find()
```
5. 更新文档：
```bash
db.t_user.updateOne(
    { name: "John" },
    { $set: { age: 31 } }
)
```
6. 删除文档：
```bash
db.t_user.deleteOne({ name: "John" })
```
7. 创建索引：
```bash
db.t_user.createIndex({ name: 1 })
```
8. 聚合操作：
```bash
db.t_user.aggregate([
    { $group: { _id: null, total: { $sum: "$age" } } }
])
```
9. 导出数据：
```bash
mongodump --db example_db --collection t_user --out /path/to/dump
```
10. 导入数据：
```bash
mongorestore --db example_db --collection t_user /path/to/dump/example_db/users.bson
```

这些示例涵盖了 MongoDB 中常见的数据库操作。您可以根据需要使用相应的命令进行操作。请注意，示例中的数据库名称、集合名称和路径应根据实际情况进行调整。