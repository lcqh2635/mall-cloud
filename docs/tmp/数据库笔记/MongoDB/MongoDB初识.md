MongoDB是一个开源的文档数据库，它采用NoSQL的数据存储方式，以BSON（一种类似于JSON的二进制存储格式）来存储数据。

MongoDB的特点包括：
1. 面向文档存储：数据以文档的形式存储，可以存储非结构化、半结构化、以及结构化数据。
2. 高性能：在处理大量数据时拥有较高的吞吐量和低延迟。
3. 可扩展性：支持集群部署和水平扩展，能够满足大规模数据存储需求。
4. 强大的查询语言：支持丰富的查询操作，包括范围查询、正则表达式查询和地理空间查询等。
5. 高可用性：支持副本集和自动故障转移，保障数据的安全性和可用性。

MongoDB适用于需要高性能、高可用性和可扩展性的业务场景，特别是对数据结构灵活性有要求的场合，如在线游戏、社交网络、大型电子商务平台和物联网应用等。

#### MongoDB添加用户

要进入 MongoDB 控制台并添加用户，您可以按照以下步骤进行操作：

1. 首先，确保已经安装并启动 MongoDB 服务。

2. 打开终端或命令提示符，并使用以下命令进入 MongoDB 控制台：

```bash
mongo
```

3. 进入 MongoDB 控制台后，使用以下命令选择要管理的数据库（在这里我们选择 `admin` 数据库）：

```bash
use admin
```

4. 然后，使用以下命令创建一个新用户并为其指定访问权限：

```bash
db.createUser(
  {
    user: "<用户名>",
    pwd: "<密码>",
    roles: [ { role: "<角色>", db: "<数据库>" } ]
  }
)
```

请将 `<用户名>`、`<密码>`、`<角色>` 和 `<数据库>` 替换为您要创建的用户名、密码、角色和数据库名称。例如，**以下是创建一个名为 "myuser" 的用户，并将其赋予 "readWrite" 角色以访问 "mydatabase" 数据库**的示例：

```bash
db.createUser(
  {
    user: "myuser",
    pwd: "mypassword",
    roles: [ { role: "readWrite", db: "mydatabase" } ]
  }
)

# 创建一个名为 lcqh 的用户，并将其赋予 readWrite 角色以访问 blog 数据库
db.createUser(
  { 
  	user:'lcqh',
  	pwd:'479368',
  	roles:[ { role:'readWrite', db: 'blog'} ]
  }
);

db.createUser(
  { 
  	user:'lcqh',
  	pwd:'479368',
  	roles:[ { role:'userAdminAnyDatabase', db: 'admin'},'readWriteAnyDatabase']
  }
);

# 以上命令将 userAdminAnyDatabase 角色授予了 "admin" 用户。这将使该用户能够在整个数据库实例中管理所有数据库的用户。
```

5. 添加用户后，您可以使用以下命令查看已创建的用户：

```bash
db.getUsers()
```

这将显示已创建的用户的列表。

完成上述步骤后，您已经成功进入 MongoDB 控制台并添加了一个新用户。请根据您的需求设置适当的用户名、密码、角色和数据库。

#### MongoDB数据库权限

在 MongoDB 中，有多种不同的内建角色和权限，您可以根据需要给予用户不同的权限。以下是一些常见的 MongoDB 内建角色及其对应的权限：

1. read：允许用户读取指定数据库的数据。
2. readWrite：允许用户读取和写入指定数据库的数据。
3. dbAdmin：允许用户在指定数据库上执行管理操作，如创建索引、删除集合等。
4. userAdmin：允许用户管理指定数据库的用户，包括创建和删除用户，并为用户分配角色。
5. clusterAdmin：允许用户在整个数据库实例上执行管理操作，包括复制集信息的查看、节点的添加等。
6. backup：允许用户创建和恢复备份。
7. restore：允许用户恢复备份。
8. root：超级用户角色，具有在整个数据库实例上执行任何操作的权限。

这些是一些常见的 MongoDB 内建角色，根据需要您可以为用户分配适当的角色来控制其在数据库中的操作权限。同时，您也可以使用自定义角色和权限来满足特定的需求。

请注意，这里列出的角色和权限是基于 MongoDB 的常见设计，实际权限可能根据您的 MongoDB 版本和配置有所不同。建议在使用特定权限时查阅 MongoDB 官方文档以获得最新的信息。

#### MongoDB数据库权限管理

在 MongoDB 中，常用的数据库权限管理命令包括：

1. 创建用户并授予权限
```bash
use admin
db.createUser(
  {
    user: "username",
    pwd: "password",
    roles: [ { role: "role", db: "database" } ]
  }
)
```

2. 授予额外的角色给用户
```bash
db.grantRolesToUser(
  "username",
  [ { role: "role", db: "database" } ]
)
```

3. 更改用户密码
```bash
use admin
db.changeUserPassword("username", "newPassword")
```

4. 删除用户
```bash
use admin
db.dropUser("username")
```

5. 列出用户
```bash
use admin
db.getUsers()
```

6. 创建角色
```bash
use admin
db.createRole(
   {
     role: "customRole",
     privileges: [ ... ],
     roles: [ ... ]
   }
)
```

7. 删除角色
```bash
use admin
db.dropRole("customRole")
```

这些命令使管理员能够在 MongoDB 中管理用户、角色和其对应的权限，以确保数据库的安全性和合规性。

#### MongoDB创建数据库

在 MongoDB 中，常用的数据库创建命令包括：

1. 创建数据库并切换到该数据库：

```bash
use your_database_name
```

2. 插入一个文档到集合中（自动创建数据库和集合）：

```bash
db.collection_name.insertOne({ key: value })
```

以下是一个完整的创建数据库和集合，并插入文档的示例：

1. 创建一个名为`mydb`的数据库并切换到它：

```bash
use mydb
```

2. 在`mydb`数据库中插入一个名为`users`的集合，并添加一条用户文档：

```bash
db.users.insertOne({ name: "John Doe", age: 30, email: "john.doe@example.com" })
```

这将创建一个名为`mydb`的数据库，并在其中创建一个名为`users`的集合，并插入一条用户文档。

请注意，在 MongoDB 中，数据库和集合是在插入第一个文档时创建的。因此，您可以直接插入文档，而无需显式创建数据库和集合。