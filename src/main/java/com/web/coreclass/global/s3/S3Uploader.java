package com.web.coreclass.global.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        try {
            // 1. 파일 이름 중복 방지를 위해 UUID 사용 (예: uuid_originalName.png)
            String originalFileName = file.getOriginalFilename();
            String uuidFileName = UUID.randomUUID() + "_" + originalFileName;

            // 2. S3에 업로드 (InputStream 사용)
            s3Template.upload(bucket, uuidFileName, file.getInputStream());

            // 3. 업로드된 파일의 접근 가능한 URL 반환
            return s3Template.download(bucket, uuidFileName).getURL().toString();

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * S3 파일 삭제 메서드
     * @param fileUrl (DB에 저장된 전체 URL)
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            // 1. 전체 URL에서 파일명(Key)만 추출 (예: "https://.../uuid_image.png" -> "uuid_image.png")
            // split("/")을 해서 마지막 부분만 가져옵니다.
            String[] urlParts = fileUrl.split("/");
            String fileName = urlParts[urlParts.length - 1];

            // 2. 한글 파일명 등을 대비해 디코딩
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

            // 3. S3에서 삭제
            s3Template.deleteObject(bucket, decodedFileName);

        } catch (Exception e) {
            // 삭제 실패해도 에러를 던지지 않고 로그만 남김 (DB 트랜잭션 롤백 방지)
            // 필요하면 @Slf4j 붙이고 log.error("S3 삭제 실패", e);
            System.err.println("S3 파일 삭제 실패: " + e.getMessage());
        }
    }
}