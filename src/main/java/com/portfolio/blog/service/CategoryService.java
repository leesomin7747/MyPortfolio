package com.portfolio.blog.service;

import com.portfolio.blog.model.Category;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryService {

    private final PostService postService;

    private List<Category> roots;
    private final Map<String, Category> byKey = new LinkedHashMap<>();
    private final Map<String, Category> parentOf = new HashMap<>();

    public CategoryService(PostService postService) {
        this.postService = postService;
    }

    @PostConstruct
    public void init() {
        this.roots = buildTree();
        index(roots, null);

        Map<String, Long> direct = postService.categoryCounts();
        for (Category root : roots) {
            computeCount(root, direct);
        }
    }

    // ───────────────────────────────────────────────────────────────
    //  카테고리 트리 정의 — key/name 만. 소개 내용은 posts/*.md 에 작성.
    // ───────────────────────────────────────────────────────────────
    private List<Category> buildTree() {

        // ===== 1. 목표 =====
        Category goals = new Category("goals", "목표")
            .add(
                new Category("security-consultant", "보안 컨설턴트"),
                new Category("career-goals", "단기 · 장기 목표")
            );

        // ===== 2. 학업 · 전공역량 =====
        Category academics = new Category("academics", "학업 · 전공역량")
            .add(
                new Category("grades", "성적"),
                new Category("undergrad-researcher", "학부 연구생"),
                new Category("internship", "인턴십"),
                new Category("papers-reports", "논문 · 보고서")
            );

        // ===== 3. 자격증 · 기술 스택 =====
        Category certs = new Category("certs-stack", "자격증 · 기술 스택")
            .add(
                new Category("certifications", "자격증"),
                new Category("tools", "기술 도구"),
                new Category("languages-frameworks", "언어 · 프레임워크")
            );

        // ===== 4. 실전 경험 · 프로젝트 =====
        Category experience = new Category("experience", "실전 경험 · 프로젝트")
            .add(
                new Category("exp-security", "보안")
                    .add(
                        new Category("ctf", "CTF"),
                        new Category("sec-competitions", "공모전 · 해커톤"),
                        new Category("bug-bounty", "버그바운티 · 취약점 분석")
                    ),
                new Category("exp-dev", "개발")
                    .add(
                        new Category("personal-projects", "개인 프로젝트"),
                        new Category("team-projects", "팀 프로젝트"),
                        new Category("dev-competitions", "공모전 · 해커톤")
                    )
            );

        // ===== 5. 커뮤니티 · 대외활동 =====
        Category community = new Category("community", "커뮤니티 · 대외활동")
            .add(
                new Category("clubs", "동아리")
                    .add(
                        new Category("likelion", "멋쟁이사자처럼"),
                        new Category("write-up", "Write-Up")
                    ),
                new Category("external-activities", "대외활동")
                    .add(
                        new Category("knights-frontier", "나이츠프런티어"),
                        new Category("whitehat-school", "화이트햇스쿨"),
                        new Category("bob", "BoB (차세대 보안 리더)"),
                        new Category("cj-unit", "CJ Unit")
                    ),
                new Category("networking", "네트워킹")
            );

        // ===== 6. Next Step =====
        Category nextStep = new Category("next-step", "Next Step")
            .add(
                new Category("roadmap", "로드맵"),
                new Category("reflection", "성찰"),
                new Category("required-competencies", "필요 역량")
            );

        return List.of(goals, academics, certs, experience, community, nextStep);
    }

    // ───────────────────────────────────────────────────────────────
    //  내부 유틸
    // ───────────────────────────────────────────────────────────────
    private void index(List<Category> nodes, Category parent) {
        for (Category c : nodes) {
            byKey.put(c.getKey(), c);
            if (parent != null) parentOf.put(c.getKey(), parent);
            index(c.getChildren(), c);
        }
    }

    private long computeCount(Category c, Map<String, Long> direct) {
        long sum = direct.getOrDefault(c.getKey(), 0L);
        for (Category child : c.getChildren()) {
            sum += computeCount(child, direct);
        }
        c.setPostCount(sum);
        return sum;
    }

    // ───────────────────────────────────────────────────────────────
    //  조회 API
    // ───────────────────────────────────────────────────────────────
    public List<Category> getRoots() { return roots; }

    public Category findByKey(String key) { return byKey.get(key); }

    public String nameOf(String key) {
        Category c = byKey.get(key);
        return c != null ? c.getName() : key;
    }

    public Set<String> subtreeKeys(String key) {
        Category c = byKey.get(key);
        if (c == null) return Set.of();
        Set<String> keys = new HashSet<>();
        collectKeys(c, keys);
        return keys;
    }

    private void collectKeys(Category c, Set<String> acc) {
        acc.add(c.getKey());
        for (Category child : c.getChildren()) collectKeys(child, acc);
    }

    public List<Category> breadcrumb(String key) {
        LinkedList<Category> path = new LinkedList<>();
        Category c = byKey.get(key);
        while (c != null) {
            path.addFirst(c);
            c = parentOf.get(c.getKey());
        }
        return path;
    }
}