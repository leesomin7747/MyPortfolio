# MyPortfolio
웹프로그래밍 기말 과제 — Spring Boot + Thymeleaf + Vanilla JS

**요구 조건 충족 + 깃허브 자동 연동까지 동작 검증 완료.**

## ✅ 충족한 필수 조건
- [x] **스프링부트** (3.5.14)
- [x] **타임리프** (layout-dialect로 공통 레이아웃)
- [x] **바닐라 JS** (`static/js/app.js`)
- [x] **블로그 형식** (글 목록 / 글 상세 / 카테고리 / 태그 / 검색)
- [x] **⭐ 깃허브 리포 자동 기재** (`/projects` — GitHub REST API 연동)

## 🚀 실행 방법
```bash
cd 웹프로젝트
./gradlew bootRun
# 브라우저에서 http://localhost:8080 접속
```
> JDK 26 + Gradle 9.5.1 기준으로 빌드·실행 확인됨. 처음 실행 시 Gradle 배포본을 받느라 시간이 좀 걸림.

## ⚙️ 가장 먼저 할 일 (본인 정보로 수정)
`src/main/resources/application.properties`
```properties
blog.title=My Portfolio      # 사이트 제목
blog.author=홍길동            # 본인 이름
github.username=octocat      # ★ 본인 깃허브 아이디로 변경 (이거 안 바꾸면 octocat 저장소 나옴)
github.token=${GITHUB_TOKEN:} # ★ 토큰은 파일에 직접 넣지 말 것! 환경변수로 주입 (아래 참고)
```

## 🔐 GitHub 토큰을 환경변수로 안전하게 다루기
**토큰을 `application.properties`에 평문으로 적으면 깃에 올라가 노출됩니다.** 그래서 코드는
`github.token=${GITHUB_TOKEN:}` 로 **환경변수 `GITHUB_TOKEN`** 에서 읽도록 되어 있습니다.
(토큰은 선택사항 — 없어도 공개 저장소는 시간당 60회까지 조회됩니다.)

토큰을 쓰고 싶다면 아래 **셋 중 하나**로 설정하세요:

**① 영구 설정 (zsh 사용자 추천)** — 터미널에서 본인 토큰으로 한 번 실행:
```bash
echo 'export GITHUB_TOKEN=ghp_본인토큰' >> ~/.zshrc && source ~/.zshrc
```

**② 실행할 때만 임시로:**
```bash
GITHUB_TOKEN=ghp_본인토큰 ./gradlew bootRun
```

**③ IntelliJ에서:** Run/Debug Configurations → Environment variables 에
`GITHUB_TOKEN=ghp_본인토큰` 추가.

> ⚠️ 토큰 문자열은 `~/.zshrc`, IntelliJ 설정, 또는 `.gitignore`에 등록된 로컬 파일에만 두세요.
> `application.properties`·소스코드·README 등 **깃에 올라가는 파일에는 절대 넣지 마세요.**
> (`.env`, `secret.properties`, `*.local.properties` 등은 이미 `.gitignore` 처리됨.)

## 📁 구조
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

## ✍️ 블로그 글 추가하는 법
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

## 🎯 가산점용 추가기능 아이디어 (네가 붙일 부분)
- **다크모드 토글** (app.js + CSS 변수 — 이미 변수로 짜둠, 쉬움)
- **글 목록 실시간 검색 필터** (서버 검색 말고 JS로 즉시 필터)
- **GitHub 커밋 잔디/언어 통계 차트** (Chart.js 같은 거 없이 바닐라로)
- **댓글 기능** (글마다 댓글 — DB나 메모리 저장)
- **글 작성 폼** (`@PostMapping` + 마크다운 미리보기)
- **페이지네이션 / 인기글 / 최근글 위젯**
- **반응형 디자인 개선, 애니메이션**

## 🛠 기술 메모
- 마크다운 변환: `commonmark` 라이브러리
- 글은 앱 시작 시 한 번 메모리에 로딩(`@PostConstruct`). 글 추가 후엔 서버 재시작(devtools가 자동 리로드).
- GitHub API 실패 시(네트워크 등) 페이지는 빈 목록으로 정상 동작.
