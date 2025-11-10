package com.web.coreclass.global.jwt;


import com.web.coreclass.domain.admin.service.AdminDetailService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    // AdminDetailService 주입 (DB에서 사용자 정보 로드)
    private final AdminDetailService adminDetailService;

    // application.properties에서 시크릿 키와 만료 시간 주입
    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration-ms}")
    private long tokenValidityInMilliseconds;

    private SecretKey key; // JWT 서명에 사용할 키

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_SCHEME = "Bearer ";
    private static final String ROLES_CLAIM = "roles"; // 토큰에 권한 정보를 저장할 클레임 이름

    /**
     * (1) 빈(Bean) 생성 후, 시크릿 키를 SecretKey 객체로 변환
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * (2) 로그인 성공 시 토큰 생성
     * @param authentication (로그인 성공 후 Spring Security가 생성한 인증 객체)
     * @return String (생성된 JWT)
     */
    public String createToken(Authentication authentication) {
        // 1. 사용자의 권한(Role) 목록을 문자열로 변환
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 2. 토큰 만료 시간 설정
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        // 3. JWT 생성
        return Jwts.builder()
                .subject(authentication.getName()) // 사용자 이름(username)
                .claim(ROLES_CLAIM, roles) // 권한 정보
                .issuedAt(new Date()) // 발급 시간
                .expiration(validity) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS512) // 서명 (알고리즘, 키)
                .compact();
    }

    /**
     * (3) HTTP 요청 헤더에서 토큰 추출
     * @return String (추출된 "Bearer "가 제거된 토큰)
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        // "Bearer "로 시작하고, 내용이 존재해야 함
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTHORIZATION_SCHEME)) {
            return bearerToken.substring(AUTHORIZATION_SCHEME.length());
        }
        return null;
    }

    /**
     * (4) 토큰 유효성 검증
     * @return boolean (유효하면 true, 아니면 false)
     */
    public boolean validateToken(String token) {
        try {
            // 토큰 파싱 (서명 검증 포함)
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * (5) 토큰에서 인증(Authentication) 객체 조회
     * @param token (유효성이 검증된 토큰)
     * @return Authentication (Spring Security가 사용할 인증 객체)
     */
    public Authentication getAuthentication(String token) {
        // 1. 토큰에서 Claims(정보) 추출
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // 2. 토큰의 subject(username)을 사용하여 DB에서 UserDetails 조회
        String username = claims.getSubject();
        UserDetails userDetails = adminDetailService.loadUserByUsername(username);

        // 3. UserDetails -> Authentication 객체(UsernamePasswordAuthenticationToken) 생성
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                token,
                userDetails.getAuthorities() // 권한 목록
        );
    }
}
