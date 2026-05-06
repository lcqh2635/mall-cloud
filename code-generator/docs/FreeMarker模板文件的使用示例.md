当然，下面是一个综合性的 FreeMarker 模板文件示例。这个模板展示了一个简单的博客文章页面，包含了头部、侧边栏和主要内容区域。它使用了 FreeMarker 的多种语法元素，如变量插值、条件语句、循环、宏定义等。

### 文件结构

假设你的项目有如下文件结构：

```
src/main/resources/templates/
├── common/
│   ├── header.ftl
│   └── footer.ftl
├── macros.ftl
└── blog-post.ftl
```

### 示例代码

#### `common/header.ftl` (头部模板)

```freemarker
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${blogTitle}</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
<header>
    <h1>${blogTitle}</h1>
    <nav>
        <ul>
            <li><a href="/">Home</a></li>
            <li><a href="/about">About</a></li>
            <li><a href="/contact">Contact</a></li>
        </ul>
    </nav>
</header>
```

#### `common/footer.ftl` (尾部模板)

```freemarker
<footer>
    <p>&copy; ${.now?string("yyyy")} My Blog. All rights reserved.</p>
</footer>
</body>
</html>
```

#### `macros.ftl` (宏定义)

```freemarker
<#-- 定义一个显示文章标签的宏 -->
<#macro showTags tags>
    <div class="tags">
        <strong>Tags:</strong>
        <#list tags as tag>
            <span class="tag">${tag}</span>
        </#list>
    </div>
</#macro>

<#-- 定义一个显示评论列表的宏 -->
<#macro showComments comments>
    <section id="comments">
        <h2>Comments (${comments?size})</h2>
        <#if comments?has_content>
            <ul>
                <#list comments as comment>
                    <li>
                        <strong>${comment.author}</strong>: ${comment.content}
                    </li>
                </#list>
            </ul>
        <#else>
            <p>No comments yet.</p>
        </#if>
    </section>
</#macro>
```

#### `blog-post.ftl` (博客文章页面模板)

```freemarker
<#import "/common/header.ftl" as h />
<#import "/common/footer.ftl" as f />
<#import "/macros.ftl" as m />

${h.header(blogTitle="My Personal Blog")}

<main>
    <article>
        <h2>${post.title}</h2>
        <p>By ${post.author} on ${post.date?string("yyyy-MM-dd")}</p>
        
        <#-- 显示文章内容 -->
        <div class="content">
            ${post.content?no_esc}
        </div>

        <#-- 使用宏显示文章标签 -->
        <@m.showTags post.tags />

        <#-- 如果用户已登录，则显示评论表单 -->
        <#if user??>
            <section id="add-comment">
                <h3>Add a Comment</h3>
                <form action="/add-comment" method="POST">
                    <textarea name="comment"></textarea>
                    <button type="submit">Submit</button>
                </form>
            </section>
        <#else>
            <p>Please <a href="/login">log in</a> to add a comment.</p>
        </#if>

        <#-- 使用宏显示评论列表 -->
        <@m.showComments post.comments />
    </article>

    <aside>
        <h3>Popular Posts</h3>
        <ul>
            <#list popularPosts as popPost>
                <li><a href="${popPost.url}">${popPost.title}</a></li>
            </#list>
        </ul>
    </aside>
</main>

${f.footer()}
```

### 解释

- **头部和尾部**：通过 `<#include>` 和 `<#import>` 指令引入了通用的头部和尾部模板，以保持一致性并减少重复代码。
- **宏定义**：在 `macros.ftl` 中定义了两个宏 `showTags` 和 `showComments`，用于处理文章标签和评论列表的显示。这使得代码更加模块化和可重用。
- **条件语句**：使用 `<#if>` 来检查用户是否登录，并根据情况显示不同的内容。
- **循环**：利用 `<#list>` 指令来遍历文章标签、热门帖子和评论列表。
- **内建函数**：使用 `${post.date?string("yyyy-MM-dd")}` 来格式化日期，以及 `${post.content?no_esc}` 来确保 HTML 内容不会被转义。
- **默认值**：使用 `${user??}` 来检查用户对象是否存在，即用户是否已登录。

通过这种方式，你可以创建一个既美观又功能齐全的博客文章页面，同时保持代码的清晰性和可维护性。这个例子展示了如何结合 FreeMarker 的各种语法元素来构建复杂的 Web 页面。