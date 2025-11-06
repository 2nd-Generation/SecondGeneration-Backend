package com.web.coreclass.domain.instructor.entity;

import com.web.coreclass.domain.careerHistory.entity.CareerHistory;
import com.web.coreclass.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "instructor")
public class Instructor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // "서재원"

    @Column(nullable = false)
    private String nickname; // "Rexi"

    @Column(name = "profile_img_url")
    private String profileImgUrl;

    @Column(name = "current_title")
    private String currentTitle; // "Head/Coach"

    @Column(name = "sgea_logo_img_url")
    private String sgeaLogoImgUrl;

    @Column(name = "content")
    private String content;

    private LocalDateTime inactiveAt; // Soft Delete 용



    // Instructor(1) : InstructorGame(N)
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CareerHistory> careerHistories = new HashSet<>();

    // Instructor(1) : CareerHistory(N)
    // 'mappedBy'는 CareerHistory 엔티티에 있는 Instructor 필드명(instructor)을 가리킵니다.
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InstructorGame> games = new HashSet<>(); // 수정 4: ArrayList -> HashSet

    // --- 연관관계 편의 메서드 --- //
    public void addCareerHistory(CareerHistory history) {
        this.careerHistories.add(history);
        history.setInstructor(this); // 양방향 연관관계 설정
    }

    public void addGame(InstructorGame game) {
        this.games.add(game);
        game.setInstructor(this); // 양방향 연관관계 설정
    }
}
