好问题！既然 `urbane-dependencies` 已经完全独立，不继承根 `pom.xml`，那我们重新审视这个问题。

---

## 结论：**不需要放在根 `pom.xml` 的 `<modules>` 中**

---

## 原因分析

放入 `<modules>` 的目的只有一个：**让 Maven 在执行 `mvn install` 时自动构建该模块**。

但现在的情况是：

| 问题 | 分析 |
|------|------|
| BOM 无 `<parent>` | 根 pom 的插件配置、属性它都继承不到 |
| BOM 自己管理版本 | 与根 pom 的 `${revision}` 已经脱离 |
| BOM 职责单一 | 只是版本元数据，不参与业务构建 |

所以 **BOM 更适合单独管理，独立发布**，而不是混在业务模块的构建流程中。

---

## 推荐做法：BOM 单独发布

```bash
# 单独构建并安装 BOM，只需执行一次（或版本变更时执行）
cd urbane-dependencies
mvn clean install -Drevision=1.0.0-SNAPSHOT
```

```bash
# 业务代码正常构建，BOM 已在本地仓库中就绪
cd ..
mvn clean package -Drevision=1.0.0-SNAPSHOT
```

---

## 修正后的根 `pom.xml`

```xml
<modules>
    <!-- ✅ 不包含 urbane-dependencies，BOM 独立管理 -->
    <module>commons</module>
    <module>platform</module>
    <module>infrastructure</module>
    <module>services</module>
</modules>
```

---

## 整体关系梳理

```
urbane-dependencies/         ← 独立项目，单独 install/deploy
    └── pom.xml              ← 无 parent，自己定义 revision

urbane-commerce/             ← 业务主项目
    ├── pom.xml              ← 根 pom，不聚合也不 import BOM
    ├── commons/
    │   └── pom.xml          ← parent=根pom，import urbane-dependencies
    ├── platform/
    │   └── pom.xml          ← parent=根pom，import urbane-dependencies
    └── services/
        └── pom.xml          ← parent=根pom，import urbane-dependencies
```

---

## CI/CD 流水线中的处理

在 Jenkins / GitHub Actions 中，构建顺序应该是：

```yaml
steps:
  # 第一步：先发布 BOM
  - name: Deploy BOM
    run: cd urbane-dependencies && mvn clean deploy -Drevision=${VERSION}

  # 第二步：再构建业务代码
  - name: Build Project
    run: mvn clean package -Drevision=${VERSION}
```

这样既消除了循环警告，职责也更加清晰。