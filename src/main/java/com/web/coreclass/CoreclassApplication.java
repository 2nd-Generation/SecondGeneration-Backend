package com.web.coreclass;

import com.web.coreclass.domain.admin.entity.Admin;
import com.web.coreclass.domain.admin.repository.AdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
//@EnableJpaAuditing
@Slf4j
public class CoreclassApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreclassApplication.class, args);
	}
	// ⬇️ 애플리케이션 시작 시 최초 어드민 계정 생성을 위한 CommandLineRunner
	@Bean
	public CommandLineRunner initAdminUser(
			AdminRepository adminRepository,
			PasswordEncoder passwordEncoder
	) {
		return args -> {
			String adminUsername = "admin";
			// 1. "admin" 계정이 이미 DB에 있는지 확인
			if (adminRepository.findByUsername(adminUsername).isEmpty()) {
				// 2. 없으면, "admin123" 비밀번호를 암호화하여 생성
				String adminPassword = passwordEncoder.encode("admin1234!");
				String adminRole = "ROLE_ADMIN"; // "ROLE_" 접두사가 중요합니다.

				Admin defaultAdmin = new Admin(adminUsername, adminPassword, adminRole);
				adminRepository.save(defaultAdmin);

				log.info("✅ 기본 관리자 계정 생성 완료 (ID: {}, PW: admin1234!)", adminUsername);
			} else {
				log.info("✅ 'admin' 관리자 계정이 이미 존재합니다.");
			}
		};
	}
}
