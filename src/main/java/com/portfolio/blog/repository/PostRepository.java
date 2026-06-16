package com.portfolio.blog.repository;

import com.portfolio.blog.model.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 글(PostEntity) DB 접근.
 * JpaRepository를 상속하면 save(), findAll(), findById(), delete() 등이 자동 제공됨.
 * 메서드 이름 규칙으로 findBySlug 같은 쿼리도 스프링이 자동 구현.
 */
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    Optional<PostEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
