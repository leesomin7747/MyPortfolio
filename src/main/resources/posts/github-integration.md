---
title: Auto-syncing GitHub Repositories
date: 2026-06-08
category: personal-projects
tags: github, api, java
summary: GitHub REST API로 내 공개 저장소를 PROJECTS 페이지에 자동으로 표시합니다.
---

## 깃허브 연동은 어떻게 동작하나요?

`GitHubService`가 GitHub REST API를 호출해 내 공개 저장소 목록을 가져옵니다.

```
GET https://api.github.com/users/{username}/repos?sort=updated
```

`application.properties`에서 `github.username`만 본인 아이디로 바꾸면
상단 메뉴의 **PROJECTS** 페이지에 저장소가 자동으로 나타납니다.

토큰 없이도 시간당 60회까지 호출되며, 개인 액세스 토큰을 넣으면 5000회로 늘어납니다.
