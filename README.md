# 포트폴리오 블로그 (Spring Boot + Thymeleaf + Vanilla JS)

## 실행 방법
```bash
cd 웹프로젝트
./gradlew bootRun
# 브라우저에서 http://localhost:8080 접속
```

## 개인정보
`src/main/resources/application.yml`
```yaml
blog:
  title: My Portfolio      # 사이트 제목
  author: 홍길동             # 본인 이름
  tagline: 한 줄 소개
  email: you@example.com
  avatar: /img/profile.jpg

github:
  username: octocat        # ★ 본인 깃허브 아이디로 변경
  token: "${GITHUB_TOKEN:}" # 환경변수로 주입 (파일에 직접 넣지 말 것)
```

## 구조
```
src/main/java/com/portfolio/blog/
  controller/BlogController.java   # 모든 페이지 라우팅 (@GetMapping)
  service/PostService.java         # posts/*.md 읽어 글 목록 생성 (마크다운→HTML)
  service/GitHubService.java       # ★ 깃허브 API 호출
  model/Post.java, GitHubRepo.java # 데이터 모델(record)

src/main/resources/
  posts/*.md                       # ★ 블로그 글 (여기에 .md 추가하면 자동 등록)
  templates/
    layout/base.html               # 공통 레이아웃(헤더/사이드바/푸터)
    home.html, post.html, ...       # 각 페이지
  static/css/style.css             # 디자인 (여기 고쳐서 꾸미기)
  static/js/app.js                 # 바닐라 JS
```

## 블로그 글 추가하는 법
`src/main/resources/posts/` 에 `파일명.md` 만들고:
```markdown
---
title: 글 제목
date: 2026-06-15
category: Spring
tags: java, web
summary: 목록에 보일 한 줄 요약
---

여기부터 **마크다운** 본문...
```
파일명이 URL이 됨 (`hello.md` → `/post/hello`).

## 기술 메모
- 마크다운 변환: `commonmark` 라이브러리
- 글은 앱 시작 시 한 번 메모리에 로딩(`@PostConstruct`). 글 추가 후엔 서버 재시작(devtools가 자동 리로드).
- GitHub API 실패 시(네트워크 등) 페이지는 빈 목록으로 정상 동작.
- 설정 파일은 한글 인코딩 문제로 `application.properties` 대신 `application.yml` 사용 (YAML은 UTF-8로 읽힘).
