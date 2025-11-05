package com.web.coreclass;

import com.web.coreclass.domain.careerHistory.entity.RoleType;
import com.web.coreclass.domain.game.entity.Game;
import com.web.coreclass.domain.game.repository.GameRepository;
import com.web.coreclass.domain.instructor.dto.InstructorDto;
import com.web.coreclass.domain.instructor.entity.Instructor;
import com.web.coreclass.domain.instructor.repository.InstructorRepository;
import com.web.coreclass.domain.instructor.service.InstructorService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;


@SpringBootTest // 스프링 컨텍스트를 모두 로드 (Service, Repository 빈 사용)
@Transactional
@Slf4j
public class InstructorServiceTest {
    @Autowired
    private InstructorService instructorService;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager em; // 영속성 컨텍스트 관리 (캐시 비우기용)

    // (Given) 각 테스트 실행 전에 게임 데이터를 미리 세팅
    @BeforeEach
    void setup() {
        // 1. "Valorant" 생성
        Game valorant = new Game();
        valorant.setName("Valorant");
        gameRepository.save(valorant);

        // 2. "League of Legends" 생성
        Game lol = new Game();
        lol.setName("League of Legends");
        gameRepository.save(lol);
    }

    @Test
    @DisplayName("강사 생성(C): 경력 및 게임 정보를 포함하여 성공적으로 생성된다.")
    void createInstructorTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 강사 생성(C) 테스트 시작 =====");
        // 1. Career DTO 준비
        var career1 = new InstructorDto.CreateRequest.CareerHistoryRequest();
        career1.setPeriod("2018");
        career1.setTeamName("SkyFoxes");
        career1.setRoleType(RoleType.PLAYER);

        var career2 = new InstructorDto.CreateRequest.CareerHistoryRequest();
        career2.setPeriod("2019");
        career2.setTeamName("Eternity Gaming");
        career2.setRoleType(RoleType.HEAD_COACH);


        // 2. Main Request DTO 준비
        var request = new InstructorDto.CreateRequest();
        request.setName("Rexi 서재원");
        request.setCurrentTitle("Head/Coach");
        request.setSgeaLogoImgUrl("sgea_logo.png");
        request.setContent("메이저 리그 출신...");
        request.setCareers(List.of(career1, career2));
        request.setGameNames(List.of("Valorant", "League of Legends")); // setup에서 저장한 게임 이름

        // sout 대신 log.info() 사용
        // 중괄호 {}를 사용하면 파라미터가 효율적으로 전달됩니다.
        log.info("➡️ 생성 요청 DTO: {}", request); // (DTO에 toString()이 구현되어 있어야 함)

        // --- When (실행) ---
        log.info("🚀 instructorService.createInstructor() 호출");
        Long instructorId = instructorService.createInstructor(request);
        log.info("✅ 생성된 강사 ID: {}", instructorId);

        // --- Then (검증) ---
        // 1. 영속성 컨텍스트 캐시를 비우고 DB에서 직접 다시 조회
        em.flush();
        em.clear();
        log.info("🔄 영속성 컨텍스트 초기화. DB에서 다시 조회합니다...");

        // 2. N+1 방지 Fetch Join 쿼리로 조회 (Service에서 사용한 것과 동일하게 검증)
        Instructor findInstructor = instructorRepository.findInstructorDetailsById(instructorId)
                .orElseThrow(() -> new AssertionError("강사가 DB에 저장되지 않았습니다."));

        log.info("👀 조회된 강사 이름: {}", findInstructor.getName());
        log.info("👀 조회된 강사 SGEA 로고: {}", findInstructor.getSgeaLogoImgUrl());
        log.info("👀 조회된 경력 수: {}", findInstructor.getCareerHistories().size());
        log.info("👀 조회된 게임 수: {}", findInstructor.getGames().size());

        // 3. AssertJ로 검증
        assertThat(findInstructor.getId()).isEqualTo(instructorId);
        assertThat(findInstructor.getName()).isEqualTo("Rexi 서재원");
        assertThat(findInstructor.getSgeaLogoImgUrl()).isEqualTo("sgea_logo.png");
        assertThat(findInstructor.getContent()).isEqualTo("메이저 리그 출신...");

        // 4. 연관관계 검증 (수정된 부분)
        assertThat(findInstructor.getCareerHistories()).hasSize(2);
        // Set은 순서가 없으므로, 'organizationName' 필드만 추출하여 내용 검증
        assertThat(findInstructor.getCareerHistories())
                .extracting("teamName") // CareerHistory에서 organizationName 필드를 추출
                .containsExactlyInAnyOrder("SkyFoxes", "Eternity Gaming"); // 순서 상관없이 이 값들이 있는지 검증

        assertThat(findInstructor.getCareerHistories())
                .extracting("roleType") // ⬅️ 수정
                .containsExactlyInAnyOrder(RoleType.PLAYER, RoleType.HEAD_COACH);

        assertThat(findInstructor.getGames()).hasSize(2);
        // Set에서 InstructorGame을 꺼내고, 다시 Game을 꺼내서 Name을 추출
        assertThat(findInstructor.getGames())
                .extracting(instructorGame -> instructorGame.getGame().getName())
                .containsExactlyInAnyOrder("Valorant", "League of Legends");

        log.info("===== ✅ 강사 생성(C) 테스트 통과 =====");
    }

    @Test
    @DisplayName("강사 조회(R): Fetch Join을 통해 모든 연관 엔티티를 DTO로 변환한다.")
    void getInstructorDetailsTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 강사 조회(R) 테스트 시작 =====");
        // createInstructorTest와 동일한 로직으로 강사 1명 미리 생성
        // (실제로는 이 부분을 공통 메서드로 뽑아내는 것이 좋습니다)
        log.info("➡️ Given: 테스트용 강사 1명 생성 중...");
        var career1 = new InstructorDto.CreateRequest.CareerHistoryRequest();
        career1.setPeriod("2018");
        career1.setTeamName("SkyFoxes");
        career1.setRoleType(RoleType.PLAYER);

        var request = new InstructorDto.CreateRequest();
        request.setName("Rexi 서재원");
        request.setCurrentTitle("Head/Coach");
        request.setSgeaLogoImgUrl("sgea_logo.png");
        request.setContent("메이저 리그 출신...");
        request.setCareers(List.of(career1));
        request.setGameNames(List.of("Valorant"));

        Long instructorId = instructorService.createInstructor(request);
        log.info("✅ Given: 테스트용 강사 생성 완료 (ID: {})", instructorId);

        // 1차 캐시(영속성 컨텍스트) 비우기
        // (이걸 안 하면 Service의 Fetch Join 쿼리가 아니라 캐시에서 데이터를 읽어버림)
        em.flush();
        em.clear();
        log.info("🔄 영속성 컨텍스트 초기화.");

        // --- When (실행) ---
        log.info("🚀 instructorService.getInstructorDetails({}) 호출", instructorId);
        InstructorDto.DetailResponse responseDto = instructorService.getInstructorDetails(instructorId);

        // --- Then (검증) ---
        log.info("👀 조회된 DTO: {}", responseDto); // (DetailResponse DTO에 @ToString 권장)
        log.info("👀 DTO 강사명: {}", responseDto.getName());
        log.info("👀 DTO 경력 수: {}", responseDto.getCareers().size());
        log.info("👀 DTO 게임 수: {}", responseDto.getGames().size());
        assertThat(responseDto.getId()).isEqualTo(instructorId);
        assertThat(responseDto.getName()).isEqualTo("Rexi 서재원");
        assertThat(responseDto.getSgeaLogoImgUrl()).isEqualTo("sgea_logo.png");
        assertThat(responseDto.getContent()).isEqualTo("메이저 리그 출신...");

        // DTO 내부의 리스트 검증
        assertThat(responseDto.getCareers()).hasSize(1);
        assertThat(responseDto.getCareers())
                .extracting("teamName") // ⬅️ 수정
                .containsExactly("SkyFoxes");
        assertThat(responseDto.getCareers())
                .extracting("roleType") // ⬅️ 수정
                .containsExactly(RoleType.PLAYER);

        // ⬇️ 이 부분이 "Game DTO 검증 동일" 코드입니다. ⬇️
        assertThat(responseDto.getGames()).hasSize(1);

        // DTO의 Set<GameResponse>에서 'name' 필드만 추출합니다.
        assertThat(responseDto.getGames())
                .extracting("name") // GameResponse DTO의 'name' 필드
                .containsExactly("Valorant"); // 순서가 1개라 InAnyOrder 대신 Exactly 사용
        log.info("===== ✅ 강사 조회(R) 테스트 통과 =====");
    }
}


