将本地模块发布到 **Maven Central Repository（中央仓库）** 是一个**严格受控、需人工审核**的过程，**不适用于内部项目或私有组件**。Maven Central 仅接受**开源、通用、有明确用途**的公共库（如 `commons-lang3`、`okhttp`、`lombok` 等）。

> ⚠️ **重要前提**：  
> **你的 `commons-core`、`life-insurance-platform-parent` 等模块属于公司内部业务组件，不应也不允许发布到 Maven Central！**  
> 正确做法是发布到公司私有仓库（如 Nexus、Artifactory）。

---

## ✅ 一、什么情况下才应发布到 Maven Central？
- 你开发了一个**通用开源库**（如工具类、SDK、框架适配器）
- 项目托管在 GitHub/GitLab 等公开平台
- 愿意接受 Sonatype 的合规审查（包括许可证、源码、文档等）
- 希望全球开发者通过 `mvn dependency` 直接使用你的库

> 🔸 **你的情况**：作为银行保险公司研发人员，开发的是**人身保险系统内部模块**，**必须使用私有仓库**，**禁止发布到中央仓库**（涉及代码泄露、安全合规风险）。

---

## 🛑 二、如果你仍要了解发布流程（仅限开源项目）

以下是 **将开源项目发布到 Maven Central 的完整流程**（基于 Sonatype OSSRH）：

### 步骤 1：准备工作
#### (1) 拥有一个**顶级域名**（或使用 `io.github.<用户名>`）
- 例如：`com.yourcompany` → 需你拥有 `yourcompany.com` 域名
- 无域名？可用 GitHub：`io.github.yourusername`

#### (2) 项目必须包含：
- `pom.xml` 符合规范（含 `name`, `description`, `url`, `licenses`, `developers`）
- 开源许可证（如 Apache 2.0、MIT）
- 源码（`-sources.jar`）
- Javadoc（`-javadoc.jar`）
- GPG 签名（所有文件需 `.asc` 签名）

#### (3) 在 Sonatype 创建工单
- 访问：https://issues.sonatype.org/
- 创建 Issue，类型选 **Community Support - Open Source Project Repository Hosting**
- 提供：Group ID、项目地址、GitHub 仓库链接
- 等待人工审核（通常 1-2 个工作日）

> 示例 Group ID 申请：
> - 若 GitHub 用户名为 `john`, 仓库为 `my-utils` → Group ID: `io.github.john`
> - 若拥有 `acme.com` → Group ID: `com.acme`

### 步骤 2：配置 `pom.xml`（关键！）
```xml
<project>
  <groupId>io.github.yourusername</groupId>
  <artifactId>my-awesome-library</artifactId>
  <version>1.0.0</version>

  <!-- 必须字段 -->
  <name>My Awesome Library</name>
  <description>A useful utility for Java developers</description>
  <url>https://github.com/yourusername/my-awesome-library</url>

  <licenses>
    <license>
      <name>The Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>

  <developers>
    <developer>
      <name>Your Name</name>
      <email>you@example.com</email>
      <organization>Your Org</organization>
    </developer>
  </developers>

  <scm>
    <connection>scm:git:git://github.com/yourusername/my-awesome-library.git</connection>
    <developerConnection>scm:git:ssh://github.com/yourusername/my-awesome-library.git</developerConnection>
    <url>https://github.com/yourusername/my-awesome-library</url>
  </scm>

  <distributionManagement>
    <snapshotRepository>
      <id>ossrh</id>
      <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
    </snapshotRepository>
    <repository>
      <id>ossrh</id>
      <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
    </repository>
  </distributionManagement>

  <build>
    <plugins>
      <!-- 1. 生成 sources.jar -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
        <version>3.3.1</version>
        <executions>
          <execution>
            <id>attach-sources</id>
            <goals><goal>jar-no-fork</goal></goals>
          </execution>
        </executions>
      </plugin>

      <!-- 2. 生成 javadoc.jar -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
        <version>3.10.0</version>
        <executions>
          <execution>
            <id>attach-javadocs</id>
            <goals><goal>jar</goal></goals>
          </execution>
        </executions>
      </plugin>

      <!-- 3. GPG 签名 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-gpg-plugin</artifactId>
        <version>3.2.7</version>
        <executions>
          <execution>
            <id>sign-artifacts</id>
            <phase>verify</phase>
            <goals><goal>sign</goal></goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

### 步骤 3：配置本地 `settings.xml`
在 `~/.m2/settings.xml` 中添加 Sonatype 账号和 GPG 密钥：
```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-sonatype-username</username>
      <password>your-sonatype-password</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <activation><activeByDefault>true</activeByDefault></activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>your-gpg-passphrase</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

### 步骤 4：发布流程
```bash
# 1. 安装 GPG（如未安装）
gpg --gen-key  # 生成密钥对，并上传公钥到 keyserver

# 2. 部署到 staging 仓库
mvn clean deploy -P release

# 3. 登录 https://s01.oss.sonatype.org/
#    - 在 Staging Repositories 中找到你的组件
#    - 点击 "Close" → 系统自动校验（Javadoc/源码/签名等）
#    - 校验通过后点击 "Release"
# 4. 等待 10-30 分钟，同步到 Maven Central
```

### 步骤 5：后续版本发布
- 修改 `<version>` 为新版（如 `1.0.1`）
- 重复 `mvn deploy` + Web UI Release 操作

---

## ✅ 三、正确做法：发布到公司私有仓库（推荐！）

### 方案 1：使用 Nexus
```bash
# 配置 settings.xml
<servers>
  <server>
    <id>company-nexus</id>
    <username>deploy-user</username>
    <password>xxx</password>
  </server>
</servers>

# pom.xml 中配置
<distributionManagement>
  <repository>
    <id>company-nexus</id>
    <url>http://nexus.yourcompany.com/repository/maven-releases/</url>
  </repository>
  <snapshotRepository>
    <id>company-nexus</id>
    <url>http://nexus.yourcompany.com/repository/maven-snapshots/</url>
  </snapshotRepository>
</distributionManagement>

# 发布命令
mvn deploy
```

### 方案 2：使用 Git + CI/CD 自动发布
- 在 CI（如 GitLab CI）中配置 `mvn deploy`
- 使用 deploy token 避免密码明文

---

## 🔚 总结

| 场景 | 是否应发布到 Maven Central | 正确做法 |
|------|--------------------------|--------|
| 公司内部模块（如 `commons-core`） | ❌ **绝对禁止** | 发布到 **私有 Nexus/Artifactory** |
| 开源通用库（如工具类、SDK） | ✅ 允许 | 走 Sonatype 流程，严格合规 |
| 个人练手项目 | ⚠️ 不推荐（除非真有价值） | 优先使用 GitHub Packages 或私有仓库 |

> 📌 **对你而言**：  
> 请立即停止考虑将 `life-insurance-platform-*` 发布到中央仓库！  
> 联系 DevOps 团队，将 `commons-core`、`platform-parent` 等发布到公司 **Nexus 仓库**，这才是企业级正确实践。

如需 **私有仓库发布配置模板**（Nexus/Artifactory），我可立即提供。是否需要？