package com.web.coreclass.domain.instructor.service;

import com.web.coreclass.domain.game.entity.GameType;
import com.web.coreclass.domain.instructor.dto.InstructorDto;
import com.web.coreclass.domain.instructor.entity.Instructor;
import com.web.coreclass.domain.instructor.entity.InstructorGame;
import com.web.coreclass.domain.instructor.repository.InstructorRepository;
import com.web.coreclass.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorService {

    private final S3Uploader s3Uploader;
    private final InstructorRepository instructorRepository;

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
            // 💡 DB 조회가 아니라 Enum에서 바로 변환 (에러 걱정 없음)
            GameType gameType = GameType.fromName(gameName);

            InstructorGame instructorGame = new InstructorGame();
            instructorGame.setGameType(gameType); // 💡 setGame -> setGameType

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
            // 💡 DB 조회가 아니라 Enum에서 바로 변환 (에러 걱정 없음)
            GameType gameType = GameType.fromName(gameName);

            InstructorGame instructorGame = new InstructorGame();
            instructorGame.setGameType(gameType); // 💡 setGame -> setGameType

            instructor.addGame(instructorGame);
        });
    }

    /**
     * (D) Delete: 강사 삭제
     */
    public void deleteInstructor(Long id) {
        // 1. 삭제할 강사 정보를 먼저 조회 (이미지 URL을 얻기 위해)
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found: " + id));

        // 2. S3 이미지 삭제 (null 체크는 deleteFile 메서드 안에서 함)
        s3Uploader.deleteFile(instructor.getProfileImgUrl());
        s3Uploader.deleteFile(instructor.getSgeaLogoImgUrl());

        // 3. (심화) 경력(CareerHistory)에 포함된 로고 이미지들도 삭제
        instructor.getCareerHistories().forEach(career -> {
            s3Uploader.deleteFile(career.getLogoImgUrl());
        });

        // 4. DB 데이터 삭제
        instructorRepository.deleteById(id);
    }
}
