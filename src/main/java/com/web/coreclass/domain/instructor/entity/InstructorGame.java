package com.web.coreclass.domain.instructor.entity;

import com.web.coreclass.domain.game.entity.GameType;
import com.web.coreclass.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString(exclude = {"instructor", "game"})
@Table(name = "instructor_game")
public class InstructorGame extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // InstructorGame(N) : Instructor(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    // InstructorGame(N) : Game(1)
    @Enumerated(EnumType.STRING) // DB에 "VALORANT" 문자열로 저장됨
    @Column(name = "game_type", nullable = false)
    private GameType gameType;
}
