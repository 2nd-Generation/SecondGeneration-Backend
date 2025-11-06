package com.web.coreclass;

import com.web.coreclass.domain.article.dto.ArticleDto;
import com.web.coreclass.domain.article.entity.Article;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import com.web.coreclass.domain.article.repository.ArticleRepository;
import com.web.coreclass.domain.article.service.ArticleService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 후 DB 롤백
@Slf4j
public class ArticleServiceTest {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private EntityManager em;

    // (참고: @Autowired private MarkdownService markdownService;
    //  ArticleService가 주입받으므로, 여기선 따로 주입받지 않아도
    //  실제 MarkdownService 빈(Bean)이 동작합니다.)
    @Test
    @DisplayName("게시글 생성(C): 마크다운 본문을 포함하여 성공적으로 생성된다.")
    void createArticleTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 게시글 생성(C) 테스트 시작 =====");
        String rawMarkdown = "# SGEA 아카데미\n* 신규 수강생 30% 할인";
        LocalDate today = LocalDate.now();

        var request = new ArticleDto.ArticleCreateRequest();
        request.setCategory(ArticleCategory.EVENT);
        request.setTitle("테스트 이벤트");
        request.setSubTitle("테스트 서브 타이틀");
        request.setContent(rawMarkdown); // ⬅️ 마크다운 원본
        request.setPostedAt(today);
        request.setStartDate(today);
        request.setEndDate(today.plusDays(7));

        log.info("➡️ 생성 요청 DTO: {}", request);

        // --- When (실행) ---
        ArticleDto.ArticleDetailResponse response = articleService.createArticle(request);
        Long articleId = response.getId();
        log.info("✅ 생성된 게시글 ID: {}", articleId);

        // --- Then (검증) ---
        em.flush(); // 영속성 컨텍스트 -> DB 반영
        em.clear(); // 영속성 컨텍스트 초기화

        // 1. DB에서 직접 조회
        Article findArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new AssertionError("게시글이 DB에 저장되지 않았습니다."));

        log.info("👀 DB 조회된 content(원본): {}", findArticle.getContent());

        // 2. 검증
        assertThat(findArticle.getTitle()).isEqualTo("테스트 이벤트");
        assertThat(findArticle.getCategory()).isEqualTo(ArticleCategory.EVENT);
        assertThat(findArticle.getPostedAt()).isEqualTo(today);
        // 💡 중요: DB에는 마크다운 원본이 저장되었는지 확인
        assertThat(findArticle.getContent()).isEqualTo(rawMarkdown);

        log.info("===== ✅ 게시글 생성(C) 테스트 통과 =====");
    }

    @Test
    @DisplayName("게시글 목록 조회(R): 카테고리별로 content가 빠진 DTO 목록을 반환한다.")
    void getArticleListTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 게시글 목록(R) 테스트 시작 =====");
        LocalDate today = LocalDate.now();

        // (ArticleService 대신 Repository로 직접 저장하여 Given 단순화)
        articleRepository.save(createTestArticle(ArticleCategory.NEWS, "뉴스 1", today.minusDays(1)));
        articleRepository.save(createTestArticle(ArticleCategory.EVENT, "이벤트 1", today));
        articleRepository.save(createTestArticle(ArticleCategory.NEWS, "뉴스 2", today.minusDays(2)));

        // --- When 1: 'ALL' 조회 (null) ---
        log.info("➡️ 1. 'ALL' 카테고리 조회 (postedAt 최신순)");
        List<ArticleDto.ArticleListResponse> allList = articleService.getArticleList(null);

        // --- Then 1: 'ALL' 검증 ---
        log.info("✅ [Test Log] 'ALL' 조회 DTO 목록: {}", allList);
        assertThat(allList).hasSize(3);
        // postedAt 최신순 정렬 검증 (오늘 날짜인 '이벤트 1'이 첫 번째)
        assertThat(allList.get(0).getTitle()).isEqualTo("이벤트 1");
        assertThat(allList.get(1).getTitle()).isEqualTo("뉴스 1");
        // DTO 필드 검증 (ListResponse에는 content가 없어야 함 - DTO 정의로 검증)

        // --- When 2: 'NEWS' 조회 ---
        log.info("➡️ 2. 'NEWS' 카테고리 조회");
        List<ArticleDto.ArticleListResponse> newsList = articleService.getArticleList(ArticleCategory.NEWS);

        // --- Then 2: 'NEWS' 검증 ---
        log.info("✅ [Test Log] 'NEWS' 조회 DTO 목록: {}", newsList);
        assertThat(newsList).hasSize(2);
        assertThat(newsList.get(0).getTitle()).isEqualTo("뉴스 1"); // '뉴스 2'보다 최신

        log.info("===== ✅ 게시글 목록(R) 테스트 통과 =====");
    }

    @Test
    @DisplayName("게시글 상세 조회(R): 마크다운이 HTML로 변환된 DTO를 반환한다.")
    void getArticleDetailsTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 게시글 상세(R) 테스트 시작 =====");
        String rawMarkdown = "# 제목\n* 리스트1\n<script>alert('xss');</script>";
        // 💡 중요: MarkdownService가 변환 및 소독(Sanitize)할 예상 결과
        // <script> 태그는 제거되어야 합니다.
        String expectedHtml = "<h1>제목</h1>\n<ul><li>리스트1</li></ul>\n\n";

        Article article = createTestArticle(ArticleCategory.NEWS, "상세 조회용", LocalDate.now());
        article.setContent(rawMarkdown); // 마크다운 원본 저장
        Article savedArticle = articleRepository.save(article);
        Long articleId = savedArticle.getId();

        em.flush();
        em.clear();

        // --- When (실행) ---
        log.info("➡️ articleService.getArticleDetails({}) 호출", articleId);
        ArticleDto.ArticleDetailResponse responseDto = articleService.getArticleDetails(articleId);

        // --- Then (검증) ---
        log.info("👀 조회된 DTO: {}", responseDto);
        log.info("👀 DTO의 HTML Content: {}", responseDto.getSafeHtmlContent());

        assertThat(responseDto.getTitle()).isEqualTo("상세 조회용");

        // 💡💡💡 핵심 검증 💡💡💡
        // DB의 원본(rawMarkdown)이 아닌,
        // 변환되고 소독된 HTML(expectedHtml)이 DTO에 담겼는지 확인
        assertThat(responseDto.getSafeHtmlContent()).isEqualTo(expectedHtml);

        log.info("===== ✅ 게시글 상세(R) 테스트 통과 =====");
    }

    @Test
    @DisplayName("게시글 수정(U): 게시글 내용을 성공적으로 수정한다.")
    void updateArticleTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 게시글 수정(U) 테스트 시작 =====");
        Article article = articleRepository.save(createTestArticle(ArticleCategory.NEWS, "수정 전", LocalDate.now()));
        Long articleId = article.getId();
        em.flush();
        em.clear();


        // 수정용 DTO 준비
        var updateRequest = new ArticleDto.ArticleCreateRequest();
        updateRequest.setTitle("수정 완료");
        updateRequest.setCategory(ArticleCategory.TEST_UPDATE);
        updateRequest.setContent("수정된 본문");
        updateRequest.setPostedAt(LocalDate.now().plusDays(1)); // (다른 필드도 세팅)
        updateRequest.setSubTitle("수정된 서브타이틀");

        // --- When (실행) ---
        log.info("➡️ articleService.updateArticle({}) 호출", articleId);
        articleService.updateArticle(articleId, updateRequest);

        // --- Then (검증) ---
        em.flush();
        em.clear();
        Article updatedArticle = articleRepository.findById(articleId).get();

        log.info("✅ [Test Log] ID {}번 수정됨. DB 최종 조회 결과: {}", articleId, updatedArticle);
        assertThat(updatedArticle.getTitle()).isEqualTo("수정 완료");
        assertThat(updatedArticle.getCategory()).isEqualTo(ArticleCategory.TEST_UPDATE);
        assertThat(updatedArticle.getContent()).isEqualTo("수정된 본문");
        log.info("===== ✅ 게시글 수정(U) 테스트 통과 =====");
    }

    @Test
    @DisplayName("게시글 삭제(D): 게시글을 성공적으로 삭제한다.")
    void deleteArticleTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 게시글 삭제(D) 테스트 시작 =====");
        Article article = articleRepository.save(createTestArticle(ArticleCategory.NEWS, "삭제 대상", LocalDate.now()));
        Long articleId = article.getId();

        // --- When (실행) ---
        log.info("➡️ articleService.deleteArticle({}) 호출", articleId);
        articleService.deleteArticle(articleId);

        // --- Then (검증) ---
        em.flush();
        em.clear();
        boolean isPresent = articleRepository.findById(articleId).isPresent();
        log.info("✅ [Test Log] ID {}번 삭제 시도. DB 조회 결과 (isPresent): {}", articleId, isPresent);
        assertThat(isPresent).isFalse();
        log.info("===== ✅ 게시글 삭제(D) 테스트 통과 =====");
    }


    // 테스트용 Article 엔티티를 쉽게 만들기 위한 Helper 메서드
    private Article createTestArticle(ArticleCategory category, String title, LocalDate postedAt) {
        Article article = new Article();
        article.setCategory(category);
        article.setTitle(title);
        article.setSubTitle("서브 타이틀");
        article.setContent("기본 본문");
        article.setPostedAt(postedAt);
        return article;
    }



}
