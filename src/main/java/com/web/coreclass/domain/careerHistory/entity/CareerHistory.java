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

    @Enumerated(EnumType.STRING) // Enum 이름을 DB에 문자열로 저장
    @Column(name = "career_type", nullable = false)
    private CareerType careerType; // PLAYER 또는 COACH

    @Column(nullable = false)
    private String period; // "2018", "2022-2023"

    @Column(name = "organization_name", nullable = false)
    private String organizationName; // "SkyFoxes"

    @Column(name = "role_title")
    private String roleTitle; // "Head Coach", (선수면 null일 수 있음)

    @Column(name = "logo_img_url")
    private String logoImgUrl;


}
