package com.portfolio.blog.service;

import com.portfolio.blog.model.Category;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 블로그의 계층형 카테고리(대분류>중분류>소분류)를 정의하고 관리하는 서비스.
 *
 * ▶ 카테고리 구조와 소개글을 바꾸고 싶으면 이 파일의 buildTree() 만 수정하면 됨.
 * ▶ 각 글(.md)의 front-matter `category:` 에는 아래 노드의 key(영문) 를 적는다.
 *    예) category: ctf  /  category: personal-projects
 *    (화면에 보이는 이름은 한글, URL/식별용 key 는 영문)
 */
@Service
public class CategoryService {

    private final PostService postService;

    private List<Category> roots;                 // 최상위 카테고리들
    private final Map<String, Category> byKey = new LinkedHashMap<>();        // key -> 노드
    private final Map<String, Category> parentOf = new HashMap<>();           // key -> 부모 노드

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
    //  카테고리 트리 정의 (여기만 고치면 구조/소개글이 바뀜)
    //  new Category("영문key", "한글 표시이름", "소개 HTML")
    // ───────────────────────────────────────────────────────────────
    private List<Category> buildTree() {

        // ===== 1. 목표 =====
        Category goals = new Category("goals", "목표",
                "<p>제가 지향하는 커리어 방향과 이를 위한 단계별 목표입니다.</p>")
            .add(
                new Category("security-consultant", "보안 컨설턴트",
                    "<p>저의 목표는 조직의 보안 위험을 진단하고 현실적인 방어책을 설계하는 "
                    + "<strong>보안 컨설턴트</strong>가 되는 것입니다. 정보보호 전공과 탄탄한 개발 경험을 "
                    + "바탕으로, 공격자 관점의 보안 인사이트와 실제 엔지니어링을 잇는 컨설턴트를 지향합니다.</p>"),
                new Category("career-goals", "단기 · 장기 목표",
                    "<h3>단기 목표 (1년)</h3><ul>"
                    + "<li>정보보안기사 취득</li>"
                    + "<li>CTF 3회 이상 입상 및 풀이(write-up) 공개</li>"
                    + "<li>버그바운티로 유효 취약점 1건 이상 제보</li>"
                    + "<li>보안 분야 인턴십 또는 학부연구생 수행</li>"
                    + "</ul><h3>장기 목표 (3~5년)</h3><ul>"
                    + "<li>보안 컨설팅 / 레드팀 직무 진입</li>"
                    + "<li>풀스택 개발 위에 웹 · AI 보안 전문성을 더한 T자형 역량 구축</li>"
                    + "<li>OSCP 취득, CVE · 오픈소스 보안 도구 기여</li>"
                    + "</ul><p><em>(프로필 기반 추천 목표 — 직접 다듬으세요.)</em></p>")
            );

        // ===== 2. 학업 · 전공역량 =====
        Category academics = new Category("academics", "학업 · 전공역량",
                "<p>정보보호 전공자로서의 수업, 연구, 학술 성과입니다.</p>")
            .add(
                new Category("grades", "성적",
                    "<p>전체 평점: <strong>X.X / 4.5</strong> · 전공 평점: <strong>X.X / 4.5</strong>. "
                    + "주요 수강 과목(암호학, 자료구조, 컴퓨터네트워크, 운영체제, 시스템보안 등)을 적어보세요.</p>"),
                new Category("undergrad-researcher", "학부 연구생",
                    "<p>소속 연구실 / 지도교수 / 연구 주제와 기여한 내용을 적습니다.</p>"),
                new Category("internship", "인턴십",
                    "<p>회사, 기간, 역할, 성과를 적습니다.</p>"),
                new Category("papers-reports", "논문 · 보고서",
                    "<p>학술 논문, 기술 보고서, 또는 수업 프로젝트 중 강조할 만한 것을 정리합니다.</p>")
            );

        // ===== 3. 자격증 · 기술 스택 =====
        Category certs = new Category("certs-stack", "자격증 · 기술 스택",
                "<p>보유 자격증과 다루는 도구 · 언어입니다.</p>")
            .add(
                new Category("certifications", "자격증",
                    "<ul>"
                    + "<li>정보보안기사 <em>(목표 / 준비 중)</em></li>"
                    + "<li>정보처리기사</li>"
                    + "<li>OCP MySQL / SQLD</li>"
                    + "<li>TOEIC ___</li>"
                    + "<li>HSK ___</li>"
                    + "<li>컴퓨터활용능력 (ITQ / 컴활)</li>"
                    + "</ul><p><em>점수와 취득일을 채워 넣으세요.</em></p>"),
                new Category("tools", "기술 도구",
                    "<p>사용하는 보안 · 개발 도구:</p><ul>"
                    + "<li><strong>보안:</strong> Burp Suite, Wireshark, Ghidra, IDA, nmap, Metasploit</li>"
                    + "<li><strong>개발 · 협업:</strong> Git · GitHub, Docker, Linux, Postman</li>"
                    + "<li><strong>에디터:</strong> IntelliJ IDEA, VS Code</li>"
                    + "<li><strong>백엔드:</strong> MySQL, Supabase</li>"
                    + "</ul>"),
                new Category("languages-frameworks", "언어 · 프레임워크",
                    "<ul>"
                    + "<li><strong>Java</strong> · Spring Boot</li>"
                    + "<li><strong>Python</strong> · Django</li>"
                    + "<li><strong>JavaScript / TypeScript</strong></li>"
                    + "<li><strong>C</strong></li>"
                    + "<li>HTML · CSS · Thymeleaf</li>"
                    + "<li>SQL (MySQL) · Supabase</li>"
                    + "</ul>")
            );

        // ===== 4. 실전 경험 · 프로젝트 =====
        Category experience = new Category("experience", "실전 경험 · 프로젝트",
                "<p>보안과 개발 양쪽의 실전 경험입니다.</p>")
            .add(
                new Category("exp-security", "보안",
                    "<p>공격 · 방어 보안 활동.</p>")
                    .add(
                        new Category("ctf", "CTF",
                            "<p>CTF 대회 참가와 문제 풀이(write-up).</p>"),
                        new Category("sec-competitions", "공모전 · 해커톤",
                            "<p>보안 분야 공모전과 해커톤.</p>"),
                        new Category("bug-bounty", "버그바운티 · 취약점 분석",
                            "<p>취약점 제보, 취약점 연구 및 분석 보고서.</p>")
                    ),
                new Category("exp-dev", "개발",
                    "<p>풀스택 전반의 소프트웨어 프로젝트.</p>")
                    .add(
                        new Category("personal-projects", "개인 프로젝트",
                            "<p>혼자 만든 프로젝트 — 이 포트폴리오 블로그 포함.</p>"),
                        new Category("team-projects", "팀 프로젝트",
                            "<p>협업 프로젝트와 맡은 역할.</p>"),
                        new Category("dev-competitions", "공모전 · 해커톤",
                            "<p>개발 분야 공모전과 해커톤.</p>")
                    )
            );

        // ===== 5. 커뮤니티 · 대외활동 =====
        Category community = new Category("community", "커뮤니티 · 대외활동",
                "<p>동아리, 프로그램, 함께 성장하는 사람들.</p>")
            .add(
                new Category("clubs", "동아리",
                    "<p>교내외 동아리 활동.</p>")
                    .add(
                        new Category("likelion", "멋쟁이사자처럼",
                            "<p>대학 IT 동아리 — 웹 개발 교육과 팀 프로젝트.</p>"),
                        new Category("write-up", "Write-Up",
                            "<p>보안 스터디 그룹: 문제 풀이와 연구 write-up 공유.</p>")
                    ),
                new Category("external-activities", "대외활동",
                    "<p>외부 교육 프로그램 및 활동.</p>")
                    .add(
                        new Category("knights-frontier", "나이츠프런티어",
                            "<p>활동 설명 — 역할, 기간, 성과.</p>"),
                        new Category("whitehat-school", "화이트햇스쿨",
                            "<p>공격 보안 교육 프로그램 — 배운 것과 만든 것.</p>"),
                        new Category("bob", "BoB (차세대 보안 리더)",
                            "<p>국내 대표 차세대 보안 리더 양성 프로그램. 트랙, 프로젝트, 멘토를 적습니다.</p>"),
                        new Category("cj-unit", "CJ Unit",
                            "<p>활동 설명 — 역할, 기간, 성과.</p>")
                    ),
                new Category("networking", "네트워킹",
                    "<p>참여하는 컨퍼런스, 밋업, 커뮤니티(보안 컨퍼런스, 스터디, 개발자 밋업 등). "
                    + "네트워킹은 컨설팅의 핵심 — 쌓아가는 인맥을 정리하세요.</p>")
            );

        // ===== 6. Next Step =====
        Category nextStep = new Category("next-step", "Next Step",
                "<p>앞으로의 방향과 실행 계획.</p>")
            .add(
                new Category("roadmap", "로드맵",
                    "<p>향후 1~3년의 자격증 · 프로그램 · 프로젝트 타임라인.</p>"),
                new Category("reflection", "성찰",
                    "<p>솔직한 성찰: 살릴 강점과 메울 약점. "
                    + "(예: 보안 전문성 심화, 실전 컨설팅 경험 확보, 보고서 작성 · 고객 커뮤니케이션 강화.)</p>"),
                new Category("required-competencies", "필요 역량",
                    "<p>보안 컨설턴트에게 필요한, 지금 키우고 있는 역량: "
                    + "위험 평가, 위협 모델링, 명확한 기술 문서 작성, 이해관계자 커뮤니케이션.</p>")
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

    /** 노드의 표시 이름 (글 목록에서 카테고리 이름 보여줄 때 사용). 없으면 key 그대로. */
    public String nameOf(String key) {
        Category c = byKey.get(key);
        return c != null ? c.getName() : key;
    }

    /** 해당 노드와 모든 하위 노드의 key 집합 (글 필터링용) */
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

    /** 상위→현재 경로 (breadcrumb용) */
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
