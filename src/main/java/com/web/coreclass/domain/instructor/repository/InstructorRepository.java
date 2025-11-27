package com.web.coreclass.domain.instructor.repository;

import com.web.coreclass.domain.instructor.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    // (R) Read: 상세 조회 시 N+1 문제를 피하기 위해
    // 연관된 careerHistories와 games(와 game)를 "fetch join"으로 한 번에 가져옵니다.
    @Query("SELECT i FROM Instructor i " +
            "LEFT JOIN FETCH i.careerHistories " +
            "LEFT JOIN FETCH i.games ig " +
            "WHERE i.id = :id")
    Optional<Instructor> findInstructorDetailsById(@Param("id") Long id);

    // 록 조회용 N+1 방지 쿼리 (games만 Join)
    @Query("SELECT i FROM Instructor i " +
            "LEFT JOIN FETCH i.games ig")
    List<Instructor> findAllWithGames();
}
