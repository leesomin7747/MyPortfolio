package com.portfolio.blog.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.blog.model.GitHubRepo;
import com.portfolio.blog.model.GitHubRepoDetail;
import com.portfolio.blog.model.LanguageStat;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 깃허브에 올린 리포가 웹에 자동으로 기재되는 기능.
 *
 * GitHub REST API를 호출해
 *  - fetchRepos()       : 저장소 목록(PROJECTS 페이지 카드)
 *  - fetchRepoDetail()  : 저장소 1개의 상세(README·언어비율·메타데이터) → 상세 페이지
 * 토큰 없이도 시간당 60회까지 호출 가능.
 */
@Service
public class GitHubService {

    private final RestClient restClient;
    private final String username;

    // README(마크다운) → HTML 변환기
    private final Parser markdownParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .escapeHtml(false)   // README 안의 뱃지/이미지(HTML 태그)도 렌더 (본인 저장소라 허용)
            .build();

    // 자주 쓰는 언어 색상(없으면 회색). 막대 그래프 색으로 사용.
    private static final Map<String, String> LANG_COLORS = Map.ofEntries(
            Map.entry("Java", "#b07219"), Map.entry("JavaScript", "#f1e05a"),
            Map.entry("TypeScript", "#3178c6"), Map.entry("Python", "#3572A5"),
            Map.entry("HTML", "#e34c26"), Map.entry("CSS", "#563d7c"),
            Map.entry("C", "#555555"), Map.entry("C++", "#f34b7d"),
            Map.entry("Shell", "#89e051"), Map.entry("Dockerfile", "#384d54")
    );

    public GitHubService(@Value("${github.username}") String username,
                         @Value("${github.token:}") String token) {
        this.username = username;

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (token != null && !token.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }
        this.restClient = builder.build();
    }

    /** 저장소 목록 (최신 업데이트순, 포크 제외) */
    public List<GitHubRepo> fetchRepos() {
        try {
            GitHubRepo[] repos = restClient.get()
                    .uri("/users/{username}/repos?sort=updated&per_page=100&type=owner", username)
                    .retrieve()
                    .body(GitHubRepo[].class);
            if (repos == null) return List.of();
            return Arrays.stream(repos)
                    .filter(r -> !r.fork())
                    .sorted(Comparator.comparing(GitHubRepo::updatedAt).reversed())
                    .toList();
        } catch (Exception e) {
            System.err.println("[GitHubService] 저장소 조회 실패: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 저장소 1개의 상세 정보 조립 (메타데이터 + README + 언어비율).
     * 실패하면 null 반환 → 컨트롤러에서 404 처리.
     */
    public GitHubRepoDetail fetchRepoDetail(String repo) {
        try {
            // ① 저장소 메타데이터
            RepoApiResponse meta = restClient.get()
                    .uri("/repos/{owner}/{repo}", username, repo)
                    .retrieve()
                    .body(RepoApiResponse.class);
            if (meta == null) return null;

            // ② README → HTML
            String readmeHtml = fetchReadmeHtml(repo);

            // ③ 언어 비율
            List<LanguageStat> languages = fetchLanguages(repo);

            String ogImage = "https://opengraph.githubassets.com/1/" + username + "/" + repo;
            String licenseName = (meta.license() != null) ? meta.license().name() : null;

            return new GitHubRepoDetail(
                    meta.name(), meta.description(), meta.htmlUrl(), meta.homepage(),
                    meta.language(), meta.stars(), meta.forks(), meta.watchers(), meta.openIssues(),
                    licenseName, meta.createdAt(), meta.pushedAt(),
                    meta.topics() != null ? meta.topics() : List.of(),
                    readmeHtml, languages, ogImage
            );
        } catch (Exception e) {
            System.err.println("[GitHubService] 상세 조회 실패(" + repo + "): " + e.getMessage());
            return null;
        }
    }

    /** README.md를 받아 base64 디코딩 후 마크다운 → HTML */
    private String fetchReadmeHtml(String repo) {
        try {
            ReadmeResponse readme = restClient.get()
                    .uri("/repos/{owner}/{repo}/readme", username, repo)
                    .retrieve()
                    .body(ReadmeResponse.class);
            if (readme == null || readme.content() == null) {
                return "<p class=\"muted\">README가 없습니다.</p>";
            }
            // GitHub README content는 줄바꿈 포함 base64 → MIME 디코더 사용
            byte[] decoded = Base64.getMimeDecoder().decode(readme.content());
            String markdown = new String(decoded, StandardCharsets.UTF_8);
            return htmlRenderer.render(markdownParser.parse(markdown));
        } catch (Exception e) {
            return "<p class=\"muted\">README를 불러오지 못했습니다.</p>";
        }
    }

    /** /languages → 바이트 수를 % 로 환산한 LanguageStat 목록(비율 큰 순) */
    private List<LanguageStat> fetchLanguages(String repo) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Integer> bytesByLang = restClient.get()
                    .uri("/repos/{owner}/{repo}/languages", username, repo)
                    .retrieve()
                    .body(Map.class);
            if (bytesByLang == null || bytesByLang.isEmpty()) return List.of();

            long total = bytesByLang.values().stream().mapToLong(Integer::longValue).sum();
            if (total == 0) return List.of();

            return bytesByLang.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .map(e -> new LanguageStat(
                            e.getKey(),
                            Math.round(e.getValue() * 1000.0 / total) / 10.0,   // 소수 1자리 %
                            LANG_COLORS.getOrDefault(e.getKey(), "#9aa5b1")))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public String getUsername() {
        return username;
    }

    // ───────── GitHub API 응답 매핑용 내부 DTO ─────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RepoApiResponse(
            String name,
            String description,
            @JsonProperty("html_url") String htmlUrl,
            String homepage,
            String language,
            @JsonProperty("stargazers_count") int stars,
            @JsonProperty("forks_count") int forks,
            @JsonProperty("watchers_count") int watchers,
            @JsonProperty("open_issues_count") int openIssues,
            @JsonProperty("created_at") String createdAt,
            @JsonProperty("pushed_at") String pushedAt,
            List<String> topics,
            License license
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record License(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReadmeResponse(String content, String encoding) {}
}
