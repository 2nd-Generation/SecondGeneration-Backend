package com.web.coreclass.domain.article.controller;

import com.web.coreclass.domain.article.dto.ArticleDto;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import com.web.coreclass.domain.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * (C) Create: 게시글 생성
     * [POST] /api/articles
     */
    @PostMapping
    public ResponseEntity<Void> createArticle(@RequestBody ArticleDto.CreateRequest request) {
        Long articleId = articleService.createArticle(request);

        // 생성된 리소스의 URI를 Location 헤더에 담아 201 Created 응답
        URI location = URI.create("/api/articles/" + articleId);
        return ResponseEntity.created(location).build();
    }

    /**
     * (R) Read List: 게시글 목록 조회 (카테고리별 필터링)
     * [GET] /api/articles?category=NEWS
     * [GET] /api/articles (카테고리 없으면 'ALL' 조회)
     */
    @GetMapping
    public ResponseEntity<List<ArticleDto.ListResponse>> getArticleList(
            // 💡 required = false: 파라미터가 없으면 null이 전달됨
            @RequestParam(required = false) ArticleCategory category
    ) {
        // (Service에서 null을 'ALL'로 처리)
        List<ArticleDto.ListResponse> list = articleService.getArticleList(category);
        return ResponseEntity.ok(list);
    }

    /**
     * (U) Update: 게시글 수정
     * [PUT] /api/articles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateArticle(
            @PathVariable Long id,
            @RequestBody ArticleDto.CreateRequest request
    ) {
        articleService.updateArticle(id, request);
        return ResponseEntity.ok().build(); // 200 OK
    }

    /**
     * (D) Delete: 게시글 삭제
     * [DELETE] /api/articles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
