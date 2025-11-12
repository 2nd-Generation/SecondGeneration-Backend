package com.web.coreclass.domain.article.dto;

import com.web.coreclass.domain.article.entity.Article;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
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
    public static class ArticleCreateRequest {
        @Schema(description = "공지사항 카테고리", example = "EVENT")
        private ArticleCategory category;

        @Schema(description = "공지사항 대제목", example = "SGEA 아카데미 오픈 기념 특별 이벤트")
        private String title;

        @Schema(description = "공지사항 소제목", example = "아카데미 런칭 기념 무료 체험 강의 및 장기 등록 할인 혜택")
        private String subTitle;

        @Schema(description = "공지사항 본문", example = "<h1>안녕하세요<h1>")
        private String content; // 마크다운 원본

        @Schema(description = "공지사항 썸네일 이미지 링크", example = "null")
        private String thumbnailUrl;

        @Schema(description = "공지사항 게시일", example = "2025-11-06")
        private LocalDate postedAt;

        @Schema(description = "이벤트 시작일 (무기한 : null)", example = "2025-11-06")
        private LocalDate startDate; // (무기한 : Nullable)

        @Schema(description = "이벤트 종료일 (무기한 : null)", example = "2025-12-06")
        private LocalDate endDate;   // (무기한 : Nullable)

        @Schema(description = "팝업 여부", example = "true")
        private boolean isPopup;

        @Schema(description = "팝업 우선순위 (낮을수록 높음)", example = "1")
        private Integer priority;

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
            article.setPopup(this.isPopup);
            article.setPriority(this.priority != null ? this.priority : 99); // null 방지
            return article;
        }
    }

    /**
     * (R) List Response: 목록 조회를 위한 DTO (첫 번째 이미지)
     * 성능을 위해 무거운 content 필드를 제외합니다.
     */
    @Getter
    @ToString
    public static class ArticleListResponse {
        private Long id;
        private ArticleCategory category;
        private String title;
        private String subTitle;
        private String thumbnailUrl;
        private LocalDate postedAt;
        private boolean isPopup;
        private Integer priority;

        // Entity -> DTO 변환 생성자
        public ArticleListResponse(Article article) {
            this.id = article.getId();
            this.category = article.getCategory();
            this.title = article.getTitle();
            this.subTitle = article.getSubTitle();
            this.thumbnailUrl = article.getThumbnailUrl();
            this.postedAt = article.getPostedAt();
            this.isPopup = article.isPopup();
            this.priority = article.getPriority();
        }
    }

    /**
     * (R) Detail Response: 상세 조회를 위한 DTO (두 번째 이미지)
     * 마크다운이 HTML로 변환된 'safeHtmlContent'를 포함합니다.
     */
    @Getter
    @ToString
    public static class ArticleDetailResponse {
        private Long id;
        private ArticleCategory category;
        private String title;
        private String subTitle;
        private String thumbnailUrl;
        private LocalDate postedAt;
        private LocalDate startDate;
        private LocalDate endDate;
        private String safeHtmlContent; // 마크다운 원본(X) -> 변환된 HTML(O)
        private boolean isPopup;
        private Integer priority;

        // Entity -> DTO 변환 생성자
        // (Service에서 변환된 HTML을 주입받습니다)
        public ArticleDetailResponse(Article article, String safeHtmlContent) {
            this.id = article.getId();
            this.category = article.getCategory();
            this.title = article.getTitle();
            this.subTitle = article.getSubTitle();
            this.thumbnailUrl = article.getThumbnailUrl();
            this.postedAt = article.getPostedAt();
            this.startDate = article.getStartDate();
            this.endDate = article.getEndDate();
            this.safeHtmlContent = safeHtmlContent; // 변환된 HTML 저장
            this.isPopup = article.isPopup();
            this.priority = article.getPriority();
        }
    }
}
