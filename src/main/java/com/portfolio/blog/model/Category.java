package com.portfolio.blog.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 계층형 카테고리 노드 (대분류 > 중분류 > 소분류).
 *
 * - key         : URL/식별용 고유 값 (예: "ctf")
 * - name        : 화면에 보이는 이름 (예: "CTF")
 * - description : 카테고리 페이지 상단에 보여줄 소개 HTML (없으면 빈 문자열)
 * - children    : 하위 카테고리들
 * - postCount   : 이 노드 + 모든 하위 노드에 속한 글 수 (CategoryService가 계산)
 *
 * 트리 구조는 CategoryService 에서 코드로 정의한다.
 */
public class Category {

    private final String key;
    private final String name;
    private final String description;
    private final List<Category> children = new ArrayList<>();
    private long postCount = 0;

    public Category(String key, String name, String description) {
        this.key = key;
        this.name = name;
        this.description = description == null ? "" : description;
    }

    public Category(String key, String name) {
        this(key, name, "");
    }

    /** 하위 카테고리 추가 (체이닝 가능) */
    public Category add(Category... subs) {
        this.children.addAll(Arrays.asList(subs));
        return this;
    }

    // ----- getters (Thymeleaf에서 사용) -----
    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<Category> getChildren() { return children; }
    public long getPostCount() { return postCount; }

    public boolean isHasChildren() { return !children.isEmpty(); }
    public boolean isHasDescription() { return !description.isBlank(); }

    public void setPostCount(long postCount) { this.postCount = postCount; }
}
