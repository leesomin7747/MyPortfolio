---
title: Building This Portfolio Blog with Spring Boot
date: 2026-06-09
category: personal-projects
tags: java, spring, thymeleaf
summary: 스프링부트 + 타임리프로 만든 첫 번째 포트폴리오 블로그 글입니다.
---

## 안녕하세요 👋

이 글은 **마크다운(.md)** 파일로 작성되어 commonmark 라이브러리로 HTML로 변환됩니다.

`src/main/resources/posts/` 폴더에 `.md` 파일을 추가하면 자동으로 블로그 글이 됩니다.

### 글 작성 방법

1. `posts/` 폴더에 `파일이름.md` 생성
2. 맨 위 `---` 사이에 메타데이터 작성 (title, date, category, tags, summary)
3. 그 아래 본문을 마크다운으로 작성
4. 서버 재시작 (또는 devtools 자동 리로드)

### 코드 블록도 됩니다

```java
@GetMapping("/")
public String home(Model model) {
    model.addAttribute("posts", postService.findAll());
    return "home";
}
```

> 인용문, **굵게**, *기울임*, [링크](https://spring.io) 모두 지원합니다.
