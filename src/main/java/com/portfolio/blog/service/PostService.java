package com.portfolio.blog.service;

import com.portfolio.blog.model.Post;
import jakarta.annotation.PostConstruct;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * resources/posts/*.md 파일들을 읽어 Post 목록으로 만들어 메모리에 보관하는 서비스.
 *
 * 마크다운 파일 형식 (맨 위 --- 사이가 "front matter" 메타데이터):
 * ---
 * title: 글 제목
 * date: 2026-06-01
 * category: Spring
 * tags: java, spring, web
 * summary: 한 줄 요약
 * ---
 * (여기부터 마크다운 본문...)
 */
@Service
public class PostService {

    private final Parser markdownParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    // 앱 시작 시 한 번 로딩해 캐싱 (slug -> Post)
    private final Map<String, Post> postsBySlug = new LinkedHashMap<>();

    @PostConstruct
    public void loadPosts() {
        try {
            // classpath의 posts 폴더 안 모든 .md 파일 찾기
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/posts/*.md");

            List<Post> parsed = new ArrayList<>();
            for (Resource resource : resources) {
                Post post = parse(resource);
                if (post != null) {
                    parsed.add(post);
                }
            }
            // 최신 날짜순 정렬
            parsed.sort(Comparator.comparing(Post::date).reversed());
            for (Post p : parsed) {
                postsBySlug.put(p.slug(), p);
            }
            System.out.println("[PostService] 글 " + postsBySlug.size() + "개 로딩 완료");
        } catch (Exception e) {
            System.err.println("[PostService] 글 로딩 실패: " + e.getMessage());
        }
    }

    /** 하나의 .md 리소스를 Post로 파싱 */
    private Post parse(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String filename = resource.getFilename();              // 예: hello-spring.md
            String slug = filename != null ? filename.replace(".md", "") : UUID.randomUUID().toString();

            Map<String, String> meta = new HashMap<>();
            StringBuilder body = new StringBuilder();

            String line;
            boolean inFrontMatter = false;
            boolean frontMatterDone = false;

            while ((line = reader.readLine()) != null) {
                if (!frontMatterDone && line.trim().equals("---")) {
                    if (!inFrontMatter) {
                        inFrontMatter = true;          // front matter 시작
                    } else {
                        inFrontMatter = false;         // front matter 끝
                        frontMatterDone = true;
                    }
                    continue;
                }
                if (inFrontMatter) {
                    int idx = line.indexOf(':');
                    if (idx > 0) {
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        meta.put(key, value);
                    }
                } else {
                    body.append(line).append('\n');
                }
            }

            // 메타데이터 추출 (없으면 기본값)
            String title = meta.getOrDefault("title", slug);
            LocalDate date = parseDate(meta.get("date"));
            String category = meta.getOrDefault("category", "Uncategorized");
            String summary = meta.getOrDefault("summary", "");
            List<String> tags = parseTags(meta.get("tags"));

            // 마크다운 본문 -> HTML
            String contentHtml = htmlRenderer.render(markdownParser.parse(body.toString()));

            return new Post(slug, title, date, category, tags, summary, contentHtml);
        } catch (Exception e) {
            System.err.println("[PostService] 파싱 실패(" + resource.getFilename() + "): " + e.getMessage());
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return value != null ? LocalDate.parse(value.trim()) : LocalDate.now();
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private List<String> parseTags(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // ---------- 조회 메서드들 ----------

    public List<Post> findAll() {
        return new ArrayList<>(postsBySlug.values());
    }

    public Post findBySlug(String slug) {
        return postsBySlug.get(slug);
    }

    public List<Post> findByCategory(String category) {
        return postsBySlug.values().stream()
                .filter(p -> p.category().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /** 여러 카테고리 key 중 하나에 속하는 글 (계층형 카테고리: 자기+하위 전체) */
    public List<Post> findByCategoryKeys(Set<String> keys) {
        return postsBySlug.values().stream()
                .filter(p -> keys.contains(p.category()))
                .collect(Collectors.toList());
    }

    public List<Post> findByTag(String tag) {
        return postsBySlug.values().stream()
                .filter(p -> p.tags().stream().anyMatch(t -> t.equalsIgnoreCase(tag)))
                .collect(Collectors.toList());
    }

    /** 카테고리별 글 개수 (사이드바용) */
    public Map<String, Long> categoryCounts() {
        return postsBySlug.values().stream()
                .collect(Collectors.groupingBy(Post::category, LinkedHashMap::new, Collectors.counting()));
    }

    /** 전체 태그 개수 (태그 클라우드용) */
    public Map<String, Long> tagCounts() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Post p : postsBySlug.values()) {
            for (String tag : p.tags()) {
                result.merge(tag, 1L, Long::sum);
            }
        }
        return result;
    }

    /** 제목/요약/카테고리/태그에 키워드가 포함된 글 검색 */
    public List<Post> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        String k = keyword.toLowerCase();
        return postsBySlug.values().stream()
                .filter(p -> p.title().toLowerCase().contains(k)
                        || p.summary().toLowerCase().contains(k)
                        || p.category().toLowerCase().contains(k)
                        || p.tags().stream().anyMatch(t -> t.toLowerCase().contains(k)))
                .collect(Collectors.toList());
    }
}
