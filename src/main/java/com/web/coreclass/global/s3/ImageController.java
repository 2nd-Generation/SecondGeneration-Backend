package com.web.coreclass.global.s3;

import com.web.coreclass.global.s3.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Image Upload", description = "이미지 업로드 API")
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {
    private final S3Uploader s3Uploader;

    @Operation(summary = "이미지 업로드", description = "이미지 파일을 업로드하고 URL을 반환받습니다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) {
        String imageUrl = s3Uploader.upload(file);
        return ResponseEntity.ok(imageUrl);
    }
}
