package com.web.coreclass.domain.admin.entity;

import com.web.coreclass.global.entity.BaseEntity;
import com.web.coreclass.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
public class Admin extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // 로그인 ID

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호

    @Column(nullable = false)
    private String role; // 역할 (e.g., "ROLE_ADMIN")

    public Admin(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
