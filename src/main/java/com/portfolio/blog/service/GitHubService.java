package com.portfolio.blog.service;

import com.portfolio.blog.model.GitHubRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

/**
 * 깃허브에 올린 리포가 웹에 자동으로 기재되는 기능.
 *
 * GitHub REST API(https://api.github.com/users/{username}/repos)를 호출해
 * 공개 저장소 목록을 가져온다. 토큰 없이도 시간당 60회까지 호출 가능.
 *
 */
@Service
public class GitHubService {

    private final RestClient restClient;
    private final String username;

    public GitHubService(@Value("${github.username}") String username,
                         @Value("${github.token:}") String token) {
        this.username = username;

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");

        // (선택) 개인 액세스 토큰이 있으면 호출 한도가 5000회/시간으로 늘어남
        if (token != null && !token.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }
        this.restClient = builder.build();
    }

    /**
     * 공개 저장소 목록을 최신 업데이트순으로 반환.
     * 네트워크 오류 등으로 실패하면 빈 목록 반환(페이지는 정상 동작).
     */
    public List<GitHubRepo> fetchRepos() {
        try {
            GitHubRepo[] repos = restClient.get()
                    .uri("/users/{username}/repos?sort=updated&per_page=100&type=owner", username)
                    .retrieve()
                    .body(GitHubRepo[].class);

            if (repos == null) return List.of();

            return java.util.Arrays.stream(repos)
                    .filter(r -> !r.fork())                 // 포크한 저장소는 제외
                    .sorted(Comparator.comparing(GitHubRepo::updatedAt).reversed())
                    .toList();
        } catch (Exception e) {
            System.err.println("[GitHubService] 저장소 조회 실패: " + e.getMessage());
            return List.of();
        }
    }

    public String getUsername() {
        return username;
    }
}
