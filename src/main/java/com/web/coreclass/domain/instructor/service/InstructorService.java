package com.web.coreclass.domain.instructor.service;

import com.web.coreclass.domain.game.entity.Game;
import com.web.coreclass.domain.game.repository.GameRepository;
import com.web.coreclass.domain.instructor.dto.InstructorDto;
import com.web.coreclass.domain.instructor.entity.Instructor;
import com.web.coreclass.domain.instructor.entity.InstructorGame;
import com.web.coreclass.domain.instructor.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final GameRepository gameRepository;

    /**
     * (C) Create: 강사 생성
     */
    public Long createInstructor(InstructorDto.CreateRequest request) {

        // 1. Instructor 엔티티 생성
        Instructor instructor = new Instructor();
        instructor.setName(request.getName());
        instructor.setProfileImgUrl(request.getProfileImgUrl());
        instructor.setCurrentTitle(request.getCurrentTitle());

        // 2. CareerHistory 엔티티 생성 및 연관관계 매핑 (Cascade)
        request.getCareers().forEach(careerDto -> {
            instructor.addCareerHistory(careerDto.toEntity()); // 연관관계 편의 메서드 사용
        });

        // 3. Game 엔티티 조회 및 InstructorGame 매핑 (Cascade)
        request.getGameNames().forEach(gameName -> {
            // DB에서 게임 이름으로 Game 엔티티 조회
            Game game = gameRepository.findByName(gameName)
                    .orElseThrow(() -> new RuntimeException("Game not found: " + gameName));

            // InstructorGame 매핑 엔티티 생성
            InstructorGame instructorGame = new InstructorGame();
            instructorGame.setGame(game);

            // 연관관계 편의 메서드 사용
            instructor.addGame(instructorGame);
        });

        // 4. Instructor 저장 (Cascade 설정으로 하위 엔티티들 동시 저장)
        Instructor savedInstructor = instructorRepository.save(instructor);

        return savedInstructor.getId();
    }

    /**
     * (R) Read: 강사 상세 조회
     */
    @Transactional(readOnly = true) // 조회 전용 트랜잭션 (성능 최적화)
    public InstructorDto.DetailResponse getInstructorDetails(Long id) {

        // N+1 방지를 위해 Fetch Join 쿼리 사용
        Instructor instructor = instructorRepository.findInstructorDetailsById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found: " + id));

        // Entity -> DTO 변환 후 반환
        return new InstructorDto.DetailResponse(instructor);
    }

    /**
     * (U) Update: 강사 정보 수정 (예시: 기본 정보만 수정)
     */
    public void updateInstructorInfo(Long id, String name, String currentTitle) {
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found: " + id));

        // @Transactional 안이므로, 엔티티를 수정하면 "Dirty Checking"에 의해 자동 UPDATE 쿼리 발생
        instructor.setName(name);
        instructor.setCurrentTitle(currentTitle);
        // (save()를 호출할 필요 없음)
    }

    /**
     * (D) Delete: 강사 삭제
     */
    public void deleteInstructor(Long id) {
        // CascadeType.ALL, orphanRemoval=true 설정으로
        // 강사 삭제 시 관련 경력, 게임 매핑도 모두 자동 삭제됨
        instructorRepository.deleteById(id);
    }
}
