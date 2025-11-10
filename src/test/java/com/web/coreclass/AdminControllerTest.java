package com.web.coreclass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.coreclass.domain.admin.dto.AdminDto;
import com.web.coreclass.domain.admin.entity.Admin;
import com.web.coreclass.domain.admin.repository.AdminRepository;
import com.web.coreclass.domain.admin.service.AdminDetailService;
import com.web.coreclass.domain.article.dto.ArticleDto;
import com.web.coreclass.domain.article.entity.ArticleCategory;
import com.web.coreclass.global.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc 주입을 위해 필요
@Transactional // 테스트 후 DB 롤백
public class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc; // HTTP 요청을 시뮬레이션

    @Autowired
    private ObjectMapper objectMapper; // DTO -> JSON 변환

    // ⬇️ 2. AdminDetailsService를 직접 주입받도록 추가
    @Autowired
    private AdminDetailService adminDetailService;

    // --- ⬇️ 1. 역할 테스트를 위해 Bean 추가 주입 ---
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private JwtProvider jwtProvider;

    // ⬇️ 2. 토큰 해독(Parse)을 위한 시크릿 키 설정 (JwtProvider와 동일하게)
    @Value("${jwt.secret}")
    private String secretString;
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // (참고: admin/admin123 계정은 CoreclassApplication에서 자동 생성됨)

    @Test
    @DisplayName("관리자 로그인 성공 (200 OK)")
    void loginSuccessTest() throws Exception {
        // Given: 로그인 요청 DTO
        AdminDto.LoginRequest loginRequest = new AdminDto.LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin1234!");

        // When & Then
        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists()) // 1. accessToken이 존재하는지
                .andExpect(jsonPath("$.accessToken").isString()) // 2. 문자열인지
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 비밀번호 틀림 (401 Unauthorized)")
    void loginFailTest() throws Exception {
        // Given: 잘못된 비밀번호
        AdminDto.LoginRequest loginRequest = new AdminDto.LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // 401 (인증 실패)
                .andDo(print());
    }

    @Test
    @DisplayName("보안 API 접근 실패 - 토큰 없음 (401 Unauthorized)")
    void securedApiFailNoTokenTest() throws Exception {
        // Given: Article 생성 요청 DTO
        ArticleDto.ArticleCreateRequest articleRequest = new ArticleDto.ArticleCreateRequest();
        articleRequest.setCategory(ArticleCategory.NEWS);
        articleRequest.setTitle("테스트 뉴스");
        articleRequest.setSubTitle("테스트");
        articleRequest.setContent("내용");
        articleRequest.setPostedAt(LocalDate.now());

        // When & Then: Authorization 헤더 없이 API 호출
        mockMvc.perform(post("/api/article") //
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleRequest)))
                .andExpect(status().isUnauthorized()) // 401 (JWT 필터가 거름)
                .andDo(print());
    }

    @Test
    @DisplayName("보안 API 접근 성공 - 유효한 토큰 (201 Created)")
    void securedApiSuccessWithTokenTest() throws Exception {
        // --- 1. 로그인하여 유효한 토큰 획득 ---
        AdminDto.LoginRequest loginRequest = new AdminDto.LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin1234!");

        MvcResult loginResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 본문(JSON)에서 토큰 추출
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        AdminDto.LoginResponse loginResponse = objectMapper.readValue(loginResponseJson, AdminDto.LoginResponse.class);
        String accessToken = loginResponse.getAccessToken();

        // --- 2. 획득한 토큰으로 보안 API 호출 ---

        // Given: Article 생성 요청 DTO
        ArticleDto.ArticleCreateRequest articleRequest = new ArticleDto.ArticleCreateRequest();
        articleRequest.setCategory(ArticleCategory.EVENT);
        articleRequest.setTitle("이벤트 테스트 (보안 통과)");
        articleRequest.setSubTitle("토큰 테스트");
        articleRequest.setContent("# 이벤트");
        articleRequest.setPostedAt(LocalDate.now());

        // When & Then: Authorization 헤더에 토큰을 포함하여 API 호출
        mockMvc.perform(post("/api/article")
                        .header("Authorization", "Bearer " + accessToken) // ⬅️ 획득한 토큰 사용
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleRequest)))
                .andExpect(status().isCreated()) // 201 Created (ArticleController의 반환값)
                .andExpect(jsonPath("$.title").value("이벤트 테스트 (보안 통과)"))
                .andDo(print());
    }
    // ⬇️ 4. Bean 로드 확인용 테스트 메서드 추가
    @Test
    @DisplayName("AdminDetailsService Bean이 정상적으로 로드되는지 확인")
    void adminDetailsServiceLoads() {
        // 이 테스트는 adminDetailsService가 성공적으로 주입되었는지 확인합니다.
        assertThat(adminDetailService).isNotNull();
    }

    @Test
    @DisplayName("DB의 'admin' 계정 역할이 'ROLE_ADMIN'인지 확인")
    void checkAdminRoleInDatabaseTest() {
        // Given: CommandLineRunner가 'admin' 계정을 생성했어야 함
        Admin admin = adminRepository.findByUsername("admin")
                .orElseThrow(() -> new AssertionError("Admin user 'admin' not found in H2 DB"));

        // When & Then
        // 💡 'admin' 계정의 'role' 필드 값이 "ROLE_ADMIN"과 정확히 일치하는지 확인
        assertThat(admin.getRole()).isEqualTo("ROLE_ADMIN");
    }

    // --- ⬇️ 4. [TEST 2] JWT 토큰 클레임 확인 테스트 ---
    @Test
    @DisplayName("발급된 토큰(JWT)의 'roles' 클레임이 'ROLE_ADMIN'인지 확인")
    void checkTokenClaimsTest() throws Exception {
        // --- 1. 로그인하여 유효한 토큰 획득 ---
        AdminDto.LoginRequest loginRequest = new AdminDto.LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin1234!"); // (수정된 비밀번호)

        MvcResult loginResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseJson = loginResult.getResponse().getContentAsString();
        AdminDto.LoginResponse loginResponse = objectMapper.readValue(loginResponseJson, AdminDto.LoginResponse.class);
        String accessToken = loginResponse.getAccessToken();

        assertThat(accessToken).isNotNull(); // 토큰이 발급되었는지 확인

        // --- 2. 획득한 토큰을 해독(Parse) ---
        Claims claims = Jwts.parser()
                .verifyWith(key) // 1번에서 주입받은 키로 검증
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        // --- 3. 토큰 내부의 'roles' 클레임 확인 ---
        // 💡 JwtProvider의 createToken 메서드에서 "roles"라는 이름으로 저장했음
        String rolesClaim = claims.get("roles", String.class);

        // 💡 토큰 안의 역할 값이 "ROLE_ADMIN"과 정확히 일치하는지 확인
        assertThat(rolesClaim).isEqualTo("ROLE_ADMIN");
    }
}
