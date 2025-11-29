package com.web.coreclass.domain.article.repository;

import com.web.coreclass.domain.article.entity.Article;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 1. 전체 게시글을 최신순(postedAt)으로 조회
    List<Article> findAllByOrderByPostedAtDesc();

    // 2. 특정 카테고리의 게시글을 최신순(postedAt)으로 조회
    List<Article> findAllByCategoryOrderByPostedAtDesc(ArticleCategory category);

    // 3. 팝업으로 지정된 게시글만 (1)우선순위 (2)최신순으로 조회 (필요시 사용)
    List<Article> findAllByIsPopupTrueOrderByPriorityAscPostedAtDesc();

    // [추가] 공지사항 썸네일과 본문(Markdown) 조회
    @Query("SELECT a.thumbnailUrl FROM Article a WHERE a.thumbnailUrl IS NOT NULL")
    List<String> findAllThumbnailUrls();

    @Query("SELECT a.content FROM Article a")
    List<String> findAllContents();
}
