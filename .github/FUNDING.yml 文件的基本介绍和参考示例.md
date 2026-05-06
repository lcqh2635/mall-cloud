## FUNDING.yml 是什么？

`FUNDING.yml` 是 GitHub 提供的**赞助配置文件**，配置后 GitHub 会在你的仓库页面显示一个 **"Sponsor"（赞助）按钮**，让用户可以直接支持你的开源项目。

---

## 文件位置

```
urbane-commerce/
└── .github/
    └── FUNDING.yml      # 必须放在 .github 目录下
```

---

## 支持的赞助平台

```yaml
# .github/FUNDING.yml

# GitHub 官方赞助（需申请 GitHub Sponsors）
github: your-github-username

# 爱发电（国内最常用）
custom: https://afdian.com/a/your-username

# 微信/支付宝收款码页面
custom: https://your-website.com/sponsor

# 支持多个自定义链接
custom:
  - https://afdian.com/a/your-username
  - https://your-website.com/donate

# 其他国际平台
patreon: your-patreon-username
open_collective: your-collective-name
ko_fi: your-kofi-username
tidelift: your-tidelift-package
liberapay: your-liberapay-username
issuehunt: your-issuehunt-username
community_bridge: your-project-name
polar: your-polar-username
buy_me_a_coffee: your-buymeacoffee-username
thanks_dev: your-username
```

---

## 实际效果

配置后仓库页面右侧会出现：

```
❤ Sponsor
┌─────────────────────┐
│  Fund this project  │
│  > 爱发电           │
│  > 自定义链接        │
└─────────────────────┘
```

---

## 对于你的项目是否需要？

| 情况 | 建议 |
|------|------|
| 个人练习项目 | ❌ 不需要 |
| 准备开源给社区使用 | ✅ 可以配置 |
| 公司内部项目 | ❌ 不需要 |

> `urbane-commerce` 作为学习/练习项目，暂时**不需要配置** `FUNDING.yml`，等项目成熟准备开源推广时再添加即可。