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

import java.util.List;

/**
 * 블로그 전체 페이지 라우팅을 담당하는 컨트롤러.
 * 각 메서드가 화면(템플릿) 하나에 대응한다.
 */
@Controller
public class BlogController {

    private final PostService postService;
    private final GitHubService gitHubService;
    private final CategoryService categoryService;

    private final String siteTitle;
    private final String authorName;

    public BlogController(PostService postService,
                          GitHubService gitHubService,
                          CategoryService categoryService,
                          @Value("${blog.title}") String siteTitle,
                          @Value("${blog.author}") String authorName) {
        this.postService = postService;
        this.gitHubService = gitHubService;
        this.categoryService = categoryService;
        this.siteTitle = siteTitle;
        this.authorName = authorName;
    }

    /**
     * 모든 페이지에 공통으로 들어가는 사이드바/헤더용 데이터.
     * (@ModelAttribute 메서드는 이 컨트롤러의 모든 핸들러 실행 전에 자동 호출됨)
     */
    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("siteTitle", siteTitle);
        model.addAttribute("authorName", authorName);
        model.addAttribute("githubUsername", gitHubService.getUsername());
        model.addAttribute("categoryTree", categoryService.getRoots());   // 계층형 카테고리 트리
        model.addAttribute("cats", categoryService);                      // 템플릿에서 cats.nameOf(key) 사용
        model.addAttribute("allTags", postService.tagCounts());
    }

    /** HOME: 전체 글 목록 */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("posts", postService.findAll());
        model.addAttribute("pageTitle", "HOME");
        return "home";
    }

    /** 글 상세 */
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

    /** 카테고리 전체 목록 */
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("pageTitle", "CATEGORIES");
        return "categories";
    }

    /** 특정 카테고리(계층형): 소개글 + 하위 카테고리 + 해당 노드와 하위 전체의 글 */
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

    /** 태그 클라우드 */
    @GetMapping("/tags")
    public String tags(Model model) {
        model.addAttribute("pageTitle", "TAGS");
        return "tags";
    }

    /** 특정 태그의 글 목록 */
    @GetMapping("/tag/{name}")
    public String tag(@PathVariable String name, Model model) {
        model.addAttribute("posts", postService.findByTag(name));
        model.addAttribute("pageTitle", "Tag: #" + name);
        model.addAttribute("filterLabel", "#" + name);
        return "home";
    }

    /** ABOUT */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "ABOUT");
        return "about";
    }

    /** ⭐ PROJECTS: 깃허브 저장소 자동 표시 */
    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("repos", gitHubService.fetchRepos());
        model.addAttribute("pageTitle", "PROJECTS");
        return "projects";
    }

    /** 검색 */
    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Post> results = postService.search(q);
        model.addAttribute("posts", results);
        model.addAttribute("pageTitle", "Search");
        model.addAttribute("filterLabel", "\"" + (q == null ? "" : q) + "\" 검색 결과 " + results.size() + "건");
        return "home";
    }
}
