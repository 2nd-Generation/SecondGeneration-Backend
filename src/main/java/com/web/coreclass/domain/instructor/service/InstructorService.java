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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final GameRepository gameRepository;

    /**
     * (C) Create: 강사 생성
     */
    public InstructorDto.InstructorDetailResponse createInstructor(InstructorDto.InstructorCreateRequest request) {

        // 1. Instructor 엔티티 생성
        Instructor instructor = new Instructor();
        instructor.setName(request.getName());
        instructor.setNickname(request.getNickname());
        instructor.setProfileImgUrl(request.getProfileImgUrl());
        instructor.setSgeaLogoImgUrl(request.getSgeaLogoImgUrl());
        instructor.setContent(request.getContent());

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

        return new InstructorDto.InstructorDetailResponse(savedInstructor);
    }

    /**
     * (R) Read List: 강사 전체 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InstructorDto.InstructorListResponse> getInstructorList() {

        // N+1 방지를 위해 만든 쿼리 사용
        List<Instructor> instructors = instructorRepository.findAllWithGames();

        // Entity List -> DTO List 변환
        return instructors.stream()
                .map(InstructorDto.InstructorListResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * (R) Read: 강사 상세 조회
     */
    @Transactional(readOnly = true) // 조회 전용 트랜잭션 (성능 최적화)
    public InstructorDto.InstructorDetailResponse getInstructorDetails(Long id) {

        // N+1 방지를 위해 Fetch Join 쿼리 사용
        Instructor instructor = instructorRepository.findInstructorDetailsById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found: " + id));

        // Entity -> DTO 변환 후 반환
        return new InstructorDto.InstructorDetailResponse(instructor);
    }

    /**
     * (U) Update: 강사 전체 정보 덮어쓰기 (PUT)
     * (orphanRemoval = true를 활용하여 기존 자식 엔티티를 삭제하고 새로 추가)
     */
    public void updateInstructor(Long id, InstructorDto.InstructorCreateRequest request) {
        // 1. 기존 강사 조회
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found: " + id));

        // 2. 기본 필드 덮어쓰기 (Dirty Checking)
        instructor.setName(request.getName());
        instructor.setNickname(request.getNickname());
        instructor.setProfileImgUrl(request.getProfileImgUrl());
        instructor.setSgeaLogoImgUrl(request.getSgeaLogoImgUrl());
        instructor.setContent(request.getContent());

        // 3. ⭐️ 연관관계(Collection) 필드 덮어쓰기 ⭐️
        // (orphanRemoval=true 이므로, clear() 시 고아 객체가 되어 DELETE 쿼리 발생)
        instructor.getCareerHistories().clear();
        instructor.getGames().clear();

        // 4. DTO의 새 데이터로 다시 채우기 (CascadeType.ALL로 INSERT 쿼리 발생)
        request.getCareers().forEach(careerDto -> {
            instructor.addCareerHistory(careerDto.toEntity());
        });

        request.getGameNames().forEach(gameName -> {
            Game game = gameRepository.findByName(gameName)
                    .orElseThrow(() -> new RuntimeException("Game not found:" + "Z" + gameName));
                            InstructorGame instructorGame = new InstructorGame();
            instructorGame.setGame(game);
            instructor.addGame(instructorGame);
        });
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
