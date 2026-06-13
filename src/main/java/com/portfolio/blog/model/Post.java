package com.portfolio.blog.model;

import java.time.LocalDate;
import java.util.List;

/**
 * 블로그 글 한 편을 표현하는 모델.
 * resources/posts/ 안의 마크다운(.md) 파일 1개 = Post 1개.
 *
 * @param slug       URL 식별자 (파일명 기반, 예: "hello-spring")
 * @param title      글 제목
 * @param date       작성일
 * @param category   카테고리 (1개)
 * @param tags       태그 목록 (여러 개)
 * @param summary    목록에 보여줄 요약문
 * @param contentHtml 마크다운 본문을 HTML로 변환한 결과
 */
public record Post(
        String slug,
        String title,
        LocalDate date,
        String category,
        List<String> tags,
        String summary,
        String contentHtml
) {
}
