package com.web.coreclass.domain.article.repository;

import com.web.coreclass.domain.article.entity.Article;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 1. 전체 게시글을 최신순(postedAt)으로 조회
    List<Article> findAllByOrderByPostedAtDesc();

    // 2. 특정 카테고리의 게시글을 최신순(postedAt)으로 조회
    List<Article> findAllByCategoryOrderByPostedAtDesc(ArticleCategory category);
}
