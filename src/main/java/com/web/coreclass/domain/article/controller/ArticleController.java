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
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * (C) Create: 게시글 생성
     * [POST] /api/article
     */
    @Operation(summary = "공지 생성", description = "공지 카테고리별로 생성")
    @PostMapping
    public ResponseEntity<ArticleDto.ArticleDetailResponse> createArticle(@RequestBody ArticleDto.ArticleCreateRequest request) {
        ArticleDto.ArticleDetailResponse createdArticle = articleService.createArticle(request);
        Long articleId = createdArticle.getId();

        URI location = URI.create("/api/article/" + articleId);

        // 201 Created 응답 + Location 헤더 + 생성된 DTO 본문
        return ResponseEntity.created(location).body(createdArticle);
    }

    /**
     * (R) Read List: 게시글 목록 조회 (카테고리별 필터링)
     * [GET] /api/article?category=NEWS
     * [GET] /api/article (카테고리 없으면 'ALL' 조회)
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
     * (R) Read Detail: 게시글 상세 조회
     * [GET] /api/article/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "공지 상세 조회", description = "공지 Id 값으로 상세 내용 조회")
    public ResponseEntity<ArticleDto.ArticleDetailResponse> getArticleDetail(@PathVariable Long id) {
        ArticleDto.ArticleDetailResponse detail = articleService.getArticleDetails(id);
        return ResponseEntity.ok(detail);
    }

    /**
     * (R) Read Popup List: 팝업 게시글 목록 조회
     * [GET] /api/article/popups
     */
    @GetMapping("/popups")
    @Operation(summary = "팝업 공지 조회", description = "팝업으로 설정된 공지 목록만 우선순위로 정렬하여 조회합니다.")
    public ResponseEntity<List<ArticleDto.ArticleListResponse>> getPopupArticleList() {
        List<ArticleDto.ArticleListResponse> list = articleService.getPopupArticleList();
        return ResponseEntity.ok(list);
    }

    /**
     * (U) Update: 게시글 수정
     * [PUT] /api/article/{id}
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
     * [DELETE] /api/article/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "공지 삭제", description = "공지 Id 값으로삭제")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
