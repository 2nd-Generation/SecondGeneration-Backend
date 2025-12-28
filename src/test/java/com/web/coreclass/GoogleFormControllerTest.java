package com.web.coreclass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.coreclass.domain.admin.repository.AdminRepository;
import com.web.coreclass.domain.googleForm.GoogleFormController;
import com.web.coreclass.domain.googleForm.dto.GoogleFormRequestDto;
import com.web.coreclass.domain.googleForm.service.GoogleFormService;
import com.web.coreclass.global.config.SecurityConfig;
import com.web.coreclass.global.jwt.JwtAuthenticationFilter;
import com.web.coreclass.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoogleFormController.class)
// 💡 핵심: 실제 SecurityConfig와 필터를 가져와서 permitAll() 설정을 테스트합니다.
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class GoogleFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoogleFormService googleFormService;

    // SecurityConfig 로드 시 필요한 Bean들을 Mocking 합니다.
    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AdminRepository adminRepository;

    @Test
    @DisplayName("구글 폼 제출 테스트: 관리자 권한 없이(permitAll) 성공적으로 호출되어야 한다.")
    void submitGoogleFormSuccessTest() throws Exception {
        // Given: 테스트용 데이터 준비
        GoogleFormRequestDto requestDto = new GoogleFormRequestDto();
        requestDto.setName("테스트 지원자");
        requestDto.setPhoneNumber("010-1234-5678");
        requestDto.setDesiredGame("Valorant");
        requestDto.setPrivacyAgreement("동의함");

        // Service 호출 시 아무런 예외도 던지지 않도록 설정 (void 메서드 Mocking)
        doNothing().when(googleFormService).submitToGoogleForm(any(GoogleFormRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/google-form/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // 💡 SecurityConfig에서 permitAll()로 설정했으므로 200 OK가 나와야 합니다.
                .andExpect(status().isOk())
                .andExpect(content().string("지원서가 성공적으로 접수되었습니다."))
                .andDo(print());

        // Service가 실제로 호출되었는지 검증
        verify(googleFormService).submitToGoogleForm(any(GoogleFormRequestDto.class));
    }
}