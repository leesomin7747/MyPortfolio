package com.portfolio.blog.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * DB에 저장되는 글 한 개 (테이블: post).
 * 웹에서 글쓰기로 작성한 글이 여기로 들어간다.
 * (기존 .md 글은 파일에서 읽으므로 이 엔티티와 무관 — 하이브리드 구조)
 *
 * @Entity        : 이 클래스가 DB 테이블과 짝이라는 표시
 * @Id @GeneratedValue : 기본키(id) 자동 증가
 */
@Entity
@Table(name = "post")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;            // URL 식별자

    private String title;
    private LocalDate date;
    private String category;        // 카테고리 key
    private String tags;            // 콤마로 구분 저장 (예: "java, spring")
    private String summary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String contentMarkdown; // 본문(마크다운 원문). 보여줄 때 HTML로 변환

    protected PostEntity() {}        // JPA가 요구하는 기본 생성자

    public PostEntity(String slug, String title, LocalDate date, String category,
                      String tags, String summary, String contentMarkdown) {
        this.slug = slug;
        this.title = title;
        this.date = date;
        this.category = category;
        this.tags = tags;
        this.summary = summary;
        this.contentMarkdown = contentMarkdown;
    }

    // getters
    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public String getTags() { return tags; }
    public String getSummary() { return summary; }
    public String getContentMarkdown() { return contentMarkdown; }
}
