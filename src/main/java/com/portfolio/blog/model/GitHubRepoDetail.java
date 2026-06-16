package com.portfolio.blog.model;

import java.util.List;

/**
 * 프로젝트 상세 페이지(/projects/{repo})에 보여줄 한 저장소의 풍부한 정보.
 * GitHubService가 여러 API 호출 결과를 모아 조립한다.
 *
 * - readmeHtml : README.md(마크다운)를 HTML로 변환한 것  → 텍스트(소개)
 * - languages  : 언어 사용 비율 목록                      → 그래프
 * - 메타데이터(stars/forks/license/날짜 등)               → 표
 * - ogImageUrl : GitHub 미리보기 이미지                   → 그림
 */
public record GitHubRepoDetail(
        String name,
        String description,
        String htmlUrl,
        String homepage,
        String mainLanguage,
        int stars,
        int forks,
        int watchers,
        int openIssues,
        String licenseName,
        String createdAt,
        String pushedAt,
        List<String> topics,
        String readmeHtml,
        List<LanguageStat> languages,
        String ogImageUrl
) {
}
