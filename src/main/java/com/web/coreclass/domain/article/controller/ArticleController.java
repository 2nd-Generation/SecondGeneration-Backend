package com.web.coreclass.domain.article.controller;

import com.web.coreclass.domain.article.dto.ArticleDto;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import com.web.coreclass.domain.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * (C) Create: 게시글 생성
     * [POST] /api/articles
     */
    @Operation(summary = "공지 생성", description = "공지 카테고리별로 생성")
    @PostMapping
    public ResponseEntity<Void> createArticle(@RequestBody ArticleDto.ArticleCreateRequest request) {
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
    @Operation(summary = "공지 조회", description = "카테고리별로 공지 조회 카테고리가 없으면 모두 조회")
    public ResponseEntity<List<ArticleDto.ArticleListResponse>> getArticleList(
            // 💡 required = false: 파라미터가 없으면 null이 전달됨
            @RequestParam(required = false) ArticleCategory category
    ) {
        // (Service에서 null을 'ALL'로 처리)
        List<ArticleDto.ArticleListResponse> list = articleService.getArticleList(category);
        return ResponseEntity.ok(list);
    }

    /**
     * (U) Update: 게시글 수정
     * [PUT] /api/articles/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "공지 수정", description = "공지 Id 값으로 뉴스 수정")
    public ResponseEntity<Void> updateArticle(
            @PathVariable Long id,
            @RequestBody ArticleDto.ArticleCreateRequest request
    ) {
        articleService.updateArticle(id, request);
        return ResponseEntity.ok().build(); // 200 OK
    }

    /**
     * (D) Delete: 게시글 삭제
     * [DELETE] /api/articles/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "공지 삭제", description = "공지 Id 값으로삭제")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
