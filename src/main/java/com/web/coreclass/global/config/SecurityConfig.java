package com.web.coreclass.global.config;

import com.web.coreclass.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JwtAuthenticationFilter 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ⬇️ CSRF, HTTP Basic 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // ⬇️ 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // ⬇️ 세션 관리 정책: STATELESS (무상태)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ⬇️ 예외 처리 핸들러
                .exceptionHandling(exceptions -> exceptions
                        // (1) 인증 실패 시 (예: 토큰 없음, 로그인 실패) -> 401 Unauthorized
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        // (2) 인가 실패 시 (예: 권한 없음, .hasRole("ADMIN") 실패) -> 403 Forbidden
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.setStatus(HttpStatus.FORBIDDEN.value())
                        )
                )

                // ⬇️ 요청별 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // --- Swagger UI 접근 허용 ---
                        .requestMatchers(
                                "/Swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/json/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // --- ⬇️ Admin 로그인 API 허용 ---
                        .requestMatchers(HttpMethod.POST, "/api/admin/login").permitAll()

                        // --- Article (공지) API 권한 설정 ---
                        .requestMatchers(HttpMethod.GET, "/api/article").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/article").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/article/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/article/**").hasRole("ADMIN")

                        // --- Instructor (강사) API 권한 설정 ---
                        .requestMatchers(HttpMethod.GET, "/api/instructor").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/instructor/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/instructor").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/instructor/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/instructor/**").hasRole("ADMIN")

                        // --- 그 외 모든 요청 ---
                        .anyRequest().authenticated()
                )

                // ⬇️ JWT 필터 추가 (UsernamePasswordAuthenticationFilter 전에 실행)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager Bean 노출 (AdminController에서 사용)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
