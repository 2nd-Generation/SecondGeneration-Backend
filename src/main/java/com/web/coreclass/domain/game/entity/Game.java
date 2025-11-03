package com.web.coreclass.domain.game.entity;

import com.web.coreclass.domain.instructor.entity.InstructorGame;
import com.web.coreclass.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "game")
public class Game extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // "Valorant" "Overwatch 2"

    @Column(name = "game_logo_url")
    private String gameLogoUrl;

    // Game(1) : InstructorGame(N)
    @OneToMany(mappedBy = "game")
    private List<InstructorGame> instructors = new ArrayList<>();
}
