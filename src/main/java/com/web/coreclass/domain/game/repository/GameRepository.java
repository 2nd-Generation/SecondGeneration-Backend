package com.web.coreclass.domain.game.repository;

import com.web.coreclass.domain.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    // (C) Create: 강사 생성 시, 게임 이름으로 기존 Game 엔티티를 찾아오기 위해 사용
    Optional<Game> findByName(String name);
}
