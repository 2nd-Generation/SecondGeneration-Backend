package com.web.coreclass.global.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
}