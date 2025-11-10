package com.web.coreclass.domain.admin.repository;

import com.web.coreclass.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,Long> {
    // 사용자 이름(username)으로 Admin 계정을 찾는 메서드
    Optional<Admin> findByUsername(String username);
}
