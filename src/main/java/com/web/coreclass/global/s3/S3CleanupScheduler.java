package com.web.coreclass.global.s3;

import com.web.coreclass.domain.article.repository.ArticleRepository;
import com.web.coreclass.domain.game.entity.GameType;
import com.web.coreclass.domain.instructor.repository.InstructorRepository;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3CleanupScheduler {

    private final S3Client s3Client; // AWS SDK Client (목록 조회용)
    private final S3Template s3Template; // Spring Cloud S3 (삭제용)
    private final InstructorRepository instructorRepository;
    private final ArticleRepository articleRepository;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // 매일 새벽 4시에 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupOrphanImages() {
        log.info("🧹 [S3 고아 파일 청소] 시작합니다...");

        // 1. DB에 등록된 '사용 중인' 이미지 파일명 다 모으기
        Set<String> validFileNames = new HashSet<>();

        // (1) 강사 관련 이미지
        validFileNames.addAll(extractFileNames(instructorRepository.findAllProfileImgUrls()));
        validFileNames.addAll(extractFileNames(instructorRepository.findAllSgeaLogoImgUrls()));
        validFileNames.addAll(extractFileNames(instructorRepository.findAllCareerLogoImgUrls()));

        // (2) 공지사항 관련 이미지 (썸네일)
        validFileNames.addAll(extractFileNames(articleRepository.findAllThumbnailUrls()));

        // (3) 공지사항 본문(Markdown)에 포함된 이미지 파싱
        List<String> contents = articleRepository.findAllContents();
        for (String content : contents) {
            validFileNames.addAll(extractUrlsFromMarkdown(content));
        }

        // (4) Enum(GameType)에 하드코딩된 이미지도 보호해야 함!
        for (GameType game : GameType.values()) {
            validFileNames.add(extractFileNameFromUrl(game.getLogoUrl()));
        }

        log.info("✅ DB에서 확인된 사용 중인 파일 개수: {}개", validFileNames.size());

        // 2. S3에 있는 모든 파일 목록 조회 및 비교
        int deletedCount = 0;
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).build();
        ListObjectsV2Response result;

        do {
            result = s3Client.listObjectsV2(request);

            for (S3Object s3Object : result.contents()) {
                String key = s3Object.key(); // S3 파일명 (예: uuid_image.png)

                // (A) DB 목록에 없고
                // (B) 생성된 지 24시간이 지난 파일만 삭제 (방금 업로드 중인 파일 보호)
                if (!validFileNames.contains(key) && isOlderThan24Hours(s3Object.lastModified())) {
                    try {
                        log.info("🗑️ 고아 파일 발견 및 삭제: {}", key);
                        s3Template.deleteObject(bucket, key);
                        deletedCount++;
                    } catch (Exception e) {
                        log.error("삭제 실패: {}", key, e);
                    }
                }
            }
            // 다음 페이지가 있으면 계속 조회
            request = request.toBuilder().continuationToken(result.nextContinuationToken()).build();
        } while (result.isTruncated());

        log.info("✨ [S3 고아 파일 청소] 완료. 총 {}개 파일 삭제됨.", deletedCount);
    }

    // --- Helper Methods ---

    // 1. URL 리스트에서 파일명만 추출 (예: https://.../abc.png -> abc.png)
    private Set<String> extractFileNames(List<String> urls) {
        Set<String> fileNames = new HashSet<>();
        for (String url : urls) {
            fileNames.add(extractFileNameFromUrl(url));
        }
        return fileNames;
    }

    // 2. 단일 URL에서 파일명 추출
    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        try {
            // URL 디코딩 (한글 파일명 대비)
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
            return decodedUrl.substring(decodedUrl.lastIndexOf("/") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    // 3. 마크다운 본문에서 이미지 URL 추출 (정규식)
    private Set<String> extractUrlsFromMarkdown(String content) {
        Set<String> fileNames = new HashSet<>();
        // 마크다운 이미지 패턴: ![...](URL) 또는 <img src="URL">
        // 간단하게 http로 시작해서 괄호나 따옴표로 끝나는 패턴을 잡습니다.
        Pattern pattern = Pattern.compile("https://[^\\s)\"]+");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String url = matcher.group();
            fileNames.add(extractFileNameFromUrl(url));
        }
        return fileNames;
    }

    // 4. 24시간 지났는지 확인
    private boolean isOlderThan24Hours(Instant lastModified) {
        return lastModified.isBefore(Instant.now().minus(1, ChronoUnit.DAYS));
    }
}
