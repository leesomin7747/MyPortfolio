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
        const icon = themeToggle ? themeToggle.querySelector('.material-symbols-outlined') : null;
        // 다크면 '해'(라이트로 전환), 라이트면 '달'(다크로 전환)
        if (icon) icon.textContent = isDark ? 'light_mode' : 'dark_mode';
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

    // 4) 테마 색상(강조색) 지정
    const DEFAULT_HUE = 212;   // 기본 파랑(#0969da)의 색조
    const colorToggle = document.getElementById('color-toggle');
    const colorPanel  = document.getElementById('color-panel');
    const colorSlider = document.getElementById('color-slider');
    const colorValue  = document.getElementById('color-value');
    const colorReset  = document.getElementById('color-reset');

    // 색조(h)를 --primary 변수에 적용 + 슬라이더/숫자 동기화
    function applyHue(h) {
        document.documentElement.style.setProperty('--primary', 'hsl(' + h + ', 70%, 45%)');
        // 헤더·사이드바용 옅은 틴트(투명도 높임)
        document.documentElement.style.setProperty('--accent-tint', 'hsla(' + h + ', 70%, 50%, 0.12)');
        if (colorSlider) colorSlider.value = h;
        if (colorValue) colorValue.textContent = h;
    }

    // 저장된 색조 불러오기
    const savedHue = localStorage.getItem('accentHue');
    if (savedHue) applyHue(savedHue);

    // 🎨 버튼: 패널 열기/닫기
    if (colorToggle) {
        colorToggle.addEventListener('click', (e) => {
            e.stopPropagation();
            colorPanel.hidden = !colorPanel.hidden;
        });
    }
    // 슬라이더: 움직일 때마다 색 적용 + 저장
    if (colorSlider) {
        colorSlider.addEventListener('input', () => {
            applyHue(colorSlider.value);
            localStorage.setItem('accentHue', colorSlider.value);
        });
    }
    // ↺ 기본값 복귀
    if (colorReset) {
        colorReset.addEventListener('click', () => {
            localStorage.removeItem('accentHue');
            document.documentElement.style.removeProperty('--primary');     // 스타일시트 기본값으로
            document.documentElement.style.removeProperty('--accent-tint'); // 틴트 제거
            colorSlider.value = DEFAULT_HUE;
            colorValue.textContent = DEFAULT_HUE;
        });
    }
    // 패널 바깥 클릭 시 닫기
    document.addEventListener('click', (e) => {
        if (colorPanel && !colorPanel.hidden && !e.target.closest('.color-picker-wrap')) {
            colorPanel.hidden = true;
        }
    });
});
