package com.web.coreclass.domain.article.dto;

import com.web.coreclass.domain.article.entity.Article;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

public class ArticleDto {
    /**
     * (C) Create: 게시글 생성을 위한 요청 DTO
     * 마크다운 원본(content)을 포함한 모든 정보를 받습니다.
     */
    @Getter
    @Setter // @RequestBody 매핑을 위해
    @ToString
    public static class CreateRequest {
        private ArticleCategory category;
        private String title;
        private String subTitle;
        private String content; // 마크다운 원본
        private String thumbnailUrl;
        private LocalDate postedAt;
        private LocalDate startDate; // (무기한 : Nullable)
        private LocalDate endDate;   // (무기한 : Nullable)

        // DTO -> Entity 변환 메서드
        public Article toEntity() {
            Article article = new Article();
            article.setCategory(this.category);
            article.setTitle(this.title);
            article.setSubTitle(this.subTitle);
            article.setContent(this.content); // 마크다운 원본 저장
            article.setThumbnailUrl(this.thumbnailUrl);
            article.setPostedAt(this.postedAt);
            article.setStartDate(this.startDate);
            article.setEndDate(this.endDate);
            return article;
        }
    }

    /**
     * (R) List Response: 목록 조회를 위한 DTO (첫 번째 이미지)
     * 성능을 위해 무거운 content 필드를 제외합니다.
     */
    @Getter
    @ToString
    public static class ListResponse {
        private Long id;
        private ArticleCategory category;
        private String title;
        private String subTitle;
        private String thumbnailUrl;
        private LocalDate postedAt;

        // Entity -> DTO 변환 생성자
        public ListResponse(Article article) {
            this.id = article.getId();
            this.category = article.getCategory();
            this.title = article.getTitle();
            this.subTitle = article.getSubTitle();
            this.thumbnailUrl = article.getThumbnailUrl();
            this.postedAt = article.getPostedAt();
        }
    }

    /**
     * (R) Detail Response: 상세 조회를 위한 DTO (두 번째 이미지)
     * 마크다운이 HTML로 변환된 'safeHtmlContent'를 포함합니다.
     */
    @Getter
    @ToString
    public static class DetailResponse {
        private Long id;
        private ArticleCategory category;
        private String title;
        private String subTitle;
        private String thumbnailUrl;
        private LocalDate postedAt;
        private LocalDate startDate;
        private LocalDate endDate;
        private String safeHtmlContent; // 마크다운 원본(X) -> 변환된 HTML(O)

        // Entity -> DTO 변환 생성자
        // (Service에서 변환된 HTML을 주입받습니다)
        public DetailResponse(Article article, String safeHtmlContent) {
            this.id = article.getId();
            this.category = article.getCategory();
            this.title = article.getTitle();
            this.subTitle = article.getSubTitle();
            this.thumbnailUrl = article.getThumbnailUrl();
            this.postedAt = article.getPostedAt();
            this.startDate = article.getStartDate();
            this.endDate = article.getEndDate();
            this.safeHtmlContent = safeHtmlContent; // 변환된 HTML 저장
        }
    }
}
