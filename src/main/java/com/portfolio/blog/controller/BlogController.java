package com.portfolio.blog.controller;

import com.portfolio.blog.model.Category;
import com.portfolio.blog.model.Post;
import com.portfolio.blog.service.CategoryService;
import com.portfolio.blog.service.GitHubService;
import com.portfolio.blog.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class BlogController {

    private final PostService postService;
    private final GitHubService gitHubService;
    private final CategoryService categoryService;

    private final String siteTitle;
    private final String authorName;
    private final String email;
    private final String tagline;
    private final String avatar;

    public BlogController(PostService postService,
                          GitHubService gitHubService,
                          CategoryService categoryService,
                          @Value("${blog.title}") String siteTitle,
                          @Value("${blog.author}") String authorName,
                          @Value("${blog.email:}") String email,
                          @Value("${blog.tagline:}") String tagline,
                          @Value("${blog.avatar:}") String avatar) {
        this.postService = postService;
        this.gitHubService = gitHubService;
        this.categoryService = categoryService;
        this.siteTitle = siteTitle;
        this.authorName = authorName;
        this.email = email;
        this.tagline = tagline;
        this.avatar = avatar;
    }

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("siteTitle", siteTitle);
        model.addAttribute("authorName", authorName);
        model.addAttribute("email", email);
        model.addAttribute("tagline", tagline);
        model.addAttribute("avatar", avatar);
        model.addAttribute("githubUsername", gitHubService.getUsername());
        model.addAttribute("categoryTree", categoryService.getRoots());
        model.addAttribute("cats", categoryService);
        model.addAttribute("allTags", postService.tagCounts());

        // 오른쪽 사이드바: 최근 글 5개
        model.addAttribute("recentPosts", postService.findAll().stream().limit(5).toList());

        // 오른쪽 사이드바: 인기 태그 상위 10개
        var topTags = new LinkedHashMap<String, Long>();
        postService.tagCounts().entrySet().stream().limit(10)
                .forEach(e -> topTags.put(e.getKey(), e.getValue()));
        model.addAttribute("topTags", topTags);
    }

    private static final int PAGE_SIZE = 10;   // 한 페이지에 보여줄 글 수

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "1") int page, Model model) {
        List<Post> all = postService.findAll();
        int total = all.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));

        // 페이지 번호 범위 보정
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        List<Post> pagePosts = all.subList(from, to);

        model.addAttribute("posts", pagePosts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageTitle", "HOME");
        return "home";
    }

    @GetMapping("/post/{slug}")
    public String post(@PathVariable String slug, Model model) {
        Post post = postService.findBySlug(slug);
        if (post == null) {
            model.addAttribute("pageTitle", "Not Found");
            return "error/404";
        }
        model.addAttribute("post", post);
        model.addAttribute("categoryName", categoryService.nameOf(post.category()));
        model.addAttribute("pageTitle", post.title());
        return "post";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("pageTitle", "CATEGORIES");
        return "categories";
    }

    @GetMapping("/category/{key}")
    public String category(@PathVariable String key, Model model) {
        Category category = categoryService.findByKey(key);
        if (category == null) {
            model.addAttribute("pageTitle", "Not Found");
            return "error/404";
        }
        model.addAttribute("category", category);
        model.addAttribute("breadcrumb", categoryService.breadcrumb(key));
        model.addAttribute("posts", postService.findByCategoryKeys(categoryService.subtreeKeys(key)));
        model.addAttribute("pageTitle", category.getName());
        return "category";
    }

    @GetMapping("/tags")
    public String tags(Model model) {
        model.addAttribute("pageTitle", "TAGS");
        return "tags";
    }

    @GetMapping("/tag/{name}")
    public String tag(@PathVariable String name, Model model) {
        model.addAttribute("posts", postService.findByTag(name));
        model.addAttribute("pageTitle", "Tag: #" + name);
        model.addAttribute("filterLabel", "#" + name);
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "ABOUT");
        return "about";
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("repos", gitHubService.fetchRepos());
        model.addAttribute("pageTitle", "PROJECTS");
        return "projects";
    }

    /** 프로젝트 상세: README·언어비율·메타데이터·미리보기 이미지 */
    @GetMapping("/projects/{repo}")
    public String projectDetail(@PathVariable String repo, Model model) {
        var detail = gitHubService.fetchRepoDetail(repo);
        if (detail == null) {
            model.addAttribute("pageTitle", "Not Found");
            return "error/404";
        }
        model.addAttribute("repo", detail);
        model.addAttribute("pageTitle", detail.name());
        return "project-detail";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Post> results = postService.search(q);
        model.addAttribute("posts", results);
        model.addAttribute("pageTitle", "Search");
        model.addAttribute("filterLabel", "\"" + (q == null ? "" : q) + "\" 검색 결과 " + results.size() + "건");
        return "home";
    }
}
