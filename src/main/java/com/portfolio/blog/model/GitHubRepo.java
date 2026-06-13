package com.portfolio.blog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub REST API의 저장소(repository) 응답을 담는 모델.
 * 필요한 필드만 매핑하고 나머지는 무시(@JsonIgnoreProperties).
 *
 * GitHub API 예시: https://api.github.com/users/octocat/repos
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepo(
        String name,
        String description,
        @JsonProperty("html_url") String htmlUrl,
        String language,
        @JsonProperty("stargazers_count") int stars,
        @JsonProperty("forks_count") int forks,
        @JsonProperty("updated_at") String updatedAt,
        boolean fork
) {
}
