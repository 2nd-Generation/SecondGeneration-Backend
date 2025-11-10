package com.web.coreclass.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class AdminDto {
    /**
     * (C) Admin 로그인 요청 DTO
     */
    @Getter
    @Setter
    public static class LoginRequest {
        @Schema(description = "관리자 아이디", example = "admin")
        private String username;

        @Schema(description = "관리자 비밀번호", example = "admin123")
        private String password;
    }

    /**
     * (R) Admin 로그인 응답 DTO (JWT 토큰)
     */
    @Getter
    @RequiredArgsConstructor // final 필드용 생성자
    public static class LoginResponse {
        @Schema(description = "JWT Access Token")
        private final String accessToken;
    }
}
