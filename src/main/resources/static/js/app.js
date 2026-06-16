// ===== 바닐라 JS (필수 조건) =====
// 여기에 클라이언트 측 동작을 추가하세요. 아래는 기본 예시 두 가지.

document.addEventListener('DOMContentLoaded', () => {

    // 1) 검색창이 비어있으면 제출 막기
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            const input = searchForm.querySelector('input[name="q"]');
            if (!input.value.trim()) {
                e.preventDefault();
                input.focus();
            }
        });
    }

    // 2) 현재 페이지에 해당하는 내비게이션 메뉴 강조
    const path = window.location.pathname;
    document.querySelectorAll('.nav a').forEach(link => {
        const href = link.getAttribute('href');
        if (href === path || (href !== '/' && path.startsWith(href))) {
            link.style.color = 'var(--primary)';
        }
    });

    // 3) 다크모드 토글
    const themeToggle = document.getElementById('theme-toggle');

    // 현재 테마에 맞는 아이콘(🌙/☀️) 표시
    function updateThemeIcon() {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        if (themeToggle) themeToggle.textContent = isDark ? '☀️' : '🌙';
    }
    updateThemeIcon();

    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            const next = isDark ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next); // 테마 적용
            localStorage.setItem('theme', next);                       // 선택 기억(새로고침해도 유지)
            updateThemeIcon();
        });
    }
});
