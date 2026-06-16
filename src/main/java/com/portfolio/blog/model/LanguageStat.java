package com.portfolio.blog.model;

/**
 * 저장소의 언어 사용 비율 한 항목.
 * GitHub /languages API의 바이트 수를 % 로 환산해 막대 그래프에 사용한다.
 *
 * @param name    언어 이름 (예: Java)
 * @param percent 전체 대비 비율 (예: 62.5)
 * @param color   막대 색상 (CSS color)
 */
public record LanguageStat(String name, double percent, String color) {
}
