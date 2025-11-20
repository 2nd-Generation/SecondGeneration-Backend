package com.web.coreclass;

import com.web.coreclass.domain.admin.repository.AdminRepository;
import com.web.coreclass.global.s3.ImageController;
import com.web.coreclass.global.config.SecurityConfig; // ⬅️ 실제 설정 가져오기
import com.web.coreclass.global.jwt.JwtAuthenticationFilter;
import com.web.coreclass.global.jwt.JwtProvider;
import com.web.coreclass.global.s3.S3Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
// 💡 핵심 1: 실제 SecurityConfig와 필터를 가져와서 환경을 똑같이 맞춥니다.
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Uploader s3Uploader;

    // 💡 핵심 2: 필터가 동작할 때 필요한 '재료'만 가짜로 넣어줍니다.
    @MockBean
    private JwtProvider jwtProvider;

    // 메인 앱(CoreclassApplication) 실행 시 필요한 빈들 (오류 방지용)
    @MockBean
    private AdminRepository adminRepository;

    // SecurityConfig가 PasswordEncoder를 빈으로 등록하므로,
    // 여기서는 MockBean을 쓰지 않고 실제 빈을 사용하거나
    // 충돌 방지를 위해 굳이 선언하지 않아도 됩니다. (하지만 명시적 Mocking도 괜찮습니다)
    // 여기서는 SecurityConfig의 빈을 사용하도록 MockBean 생략

    @Test
    @DisplayName("이미지 업로드 테스트: S3에 가지 않고 가짜 URL을 반환한다")
    @WithMockUser(roles = "ADMIN") // 관리자 권한으로 실행
    void uploadImageTest() throws Exception {
        // Given: 가짜 파일 생성
        MockMultipartFile fakeFile = new MockMultipartFile(
                "file",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        // Mocking: s3Uploader가 호출되면 가짜 URL 반환
        given(s3Uploader.upload(any())).willReturn("https://fake-s3-url.com/test.png");

        // When & Then
        mockMvc.perform(multipart("/api/image/upload")
                        .file(fakeFile))
                // 💡 핵심 3: .with(csrf()) 제거 (SecurityConfig에서 이미 껐으므로 불필요)
                // 💡 핵심 4: .contentType(...) 절대 금지 (자동 설정을 방해함)
                .andExpect(status().isOk())
                .andExpect(content().string("https://fake-s3-url.com/test.png"))
                .andDo(print());
    }
}