package com.web.coreclass.domain.admin.service;

import com.web.coreclass.domain.admin.entity.Admin;
import com.web.coreclass.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDetailService implements UserDetailsService {
    private final AdminRepository adminRepository;

    /**
     * Spring Security가 로그인 요청을 받을 때 호출하는 메서드
     * @param username (로그인 시 입력한 아이디)
     * @return UserDetails (Spring Security가 사용하는 사용자 정보)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. DB에서 username으로 Admin 정보 조회
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + username));

        // 2. Admin 엔티티 -> Spring Security의 UserDetails 객체로 변환
        return new User(
                admin.getUsername(),
                admin.getPassword(),
                // 권한 목록 (e.g., "ROLE_ADMIN")
                List.of(new SimpleGrantedAuthority(admin.getRole()))
        );
    }
}
