package com.web.coreclass.domain.careerHistory.entity;

import com.web.coreclass.domain.instructor.entity.Instructor;
import com.web.coreclass.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "career_history")
public class CareerHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CareerHistory(N) : Instructor(1)
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @Column(nullable = false)
    private String period; // "2018", "2022-2023"

    @Column(name = "team_name", nullable = false)
    private String teamName; // "SkyFoxes"

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType; // "Head Coach"

    @Column(name = "logo_img_url")
    private String logoImgUrl;
}
