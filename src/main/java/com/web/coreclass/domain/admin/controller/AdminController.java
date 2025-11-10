package com.web.coreclass.domain.admin.controller;

import com.web.coreclass.domain.admin.dto.AdminDto;
import com.web.coreclass.global.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Auth", description = "관리자 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    /**
     * (C) Admin 로그인 (JWT 토큰 발급)
     */
    @Operation(summary = "관리자 로그인", description = "관리자 아이디/비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<AdminDto.LoginResponse> login(
            @RequestBody AdminDto.LoginRequest loginRequest
    ) {
        // 1. Spring Security의 AuthenticationManager를 사용하여 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. 인증 성공 시, SecurityContext에 인증 정보 저장 (선택적)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. JwtProvider를 사용하여 토큰 생성
        String accessToken = jwtProvider.createToken(authentication);

        // 4. DTO에 담아 토큰 반환
        return ResponseEntity.ok(new AdminDto.LoginResponse(accessToken));
    }
}
