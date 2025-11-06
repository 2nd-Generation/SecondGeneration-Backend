package com.web.coreclass.domain.article.service;

import com.web.coreclass.domain.article.dto.ArticleDto;
import com.web.coreclass.domain.article.entity.Article;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import com.web.coreclass.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final MarkdownService markdownService; // ⬅️ 마크다운 변환기 주입

    /**
     * (C) Create: 게시글 생성
     */
    public ArticleDto.ArticleDetailResponse createArticle(ArticleDto.ArticleCreateRequest request) {
        Article article = request.toEntity(); // DTO -> Entity 변환
        Article savedArticle = articleRepository.save(article);

        // 마크다운 변환 로직
        String safeHtml = markdownService.markdownToSafeHtml(savedArticle.getContent());
        // ✅ 생성된 엔티티와 변환된 HTML로 DTO를 만들어 반환
        return new ArticleDto.ArticleDetailResponse(savedArticle, safeHtml);
    }

    /**
     * (R) Read List: 게시글 목록 조회 (카테고리 필터링)
     * (ALL 카테고리 처리는 Controller에서 category=null로 호출)
     */
    @Transactional(readOnly = true) // 조회 전용 (성능 최적화)
    public List<ArticleDto.ArticleListResponse> getArticleList(ArticleCategory category) {

        List<Article> articles;

        if (category == null) { // 'ALL' 선택 시
            articles = articleRepository.findAllByOrderByPostedAtDesc();
        } else { // 'NEWS', 'EVENT' 등 특정 카테고리 선택 시
            articles = articleRepository.findAllByCategoryOrderByPostedAtDesc(category);
        }

        // Entity List -> DTO List 변환
        return articles.stream()
                .map(ArticleDto.ArticleListResponse::new) // ListResponse DTO로 변환
                .collect(Collectors.toList());
    }

    /**
     * (R) Read Detail: 게시글 상세 조회
     * (마크다운 -> HTML 변환 포함)
     */
    @Transactional(readOnly = true)
    public ArticleDto.ArticleDetailResponse getArticleDetails(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. id=" + id));

        // 💡 핵심 로직: 마크다운을 HTML로 변환
        String safeHtml = markdownService.markdownToSafeHtml(article.getContent());

        // DTO 생성자에 엔티티와 변환된 HTML을 함께 전달
        return new ArticleDto.ArticleDetailResponse(article, safeHtml);
    }

    /**
     * (U) Update: 게시글 수정
     * (CreateRequest DTO를 재활용, 또는 별도 UpdateRequest DTO 생성)
     */
    public void updateArticle(Long id, ArticleDto.ArticleCreateRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. id=" + id));

        // Dirty Checking (트랜잭션 내에서 엔티티 수정)
        article.setCategory(request.getCategory());
        article.setTitle(request.getTitle());
        article.setSubTitle(request.getSubTitle());
        article.setContent(request.getContent()); // ⬅️ 마크다운 원본 덮어쓰기
        article.setThumbnailUrl(request.getThumbnailUrl());
        article.setPostedAt(request.getPostedAt());
        article.setStartDate(request.getStartDate());
        article.setEndDate(request.getEndDate());

        // @Transactional 종료 시 자동 UPDATE
    }

    /**
     * (D) Delete: 게시글 삭제
     */
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }
}
