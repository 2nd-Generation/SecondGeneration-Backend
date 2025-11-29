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

    // [추가] 모든 강사의 프로필 이미지와 로고 URL만 조회
    @Query("SELECT i.profileImgUrl FROM Instructor i WHERE i.profileImgUrl IS NOT NULL")
    List<String> findAllProfileImgUrls();

    @Query("SELECT i.sgeaLogoImgUrl FROM Instructor i WHERE i.sgeaLogoImgUrl IS NOT NULL")
    List<String> findAllSgeaLogoImgUrls();

    // (CareerHistory에 있는 로고들도 가져와야 함)
    @Query("SELECT c.logoImgUrl FROM CareerHistory c WHERE c.logoImgUrl IS NOT NULL")
    List<String> findAllCareerLogoImgUrls();
}
