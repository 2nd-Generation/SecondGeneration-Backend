package com.web.coreclass;

import com.web.coreclass.domain.careerHistory.entity.RoleType;
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
    private EntityManager em; // 영속성 컨텍스트 관리 (캐시 비우기용)

    @Test
    @DisplayName("강사 생성(C): 경력 및 게임 정보를 포함하여 성공적으로 생성된다.")
    void createInstructorTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 강사 생성(C) 테스트 시작 =====");
        // 1. Career DTO 준비
        var career1 = new InstructorDto.InstructorCreateRequest.CareerHistoryRequest();
        career1.setPeriod("2018");
        career1.setTeamName("SkyFoxes");
        career1.setRoleType(RoleType.PLAYER);

        var career2 = new InstructorDto.InstructorCreateRequest.CareerHistoryRequest();
        career2.setPeriod("2019");
        career2.setTeamName("Eternity Gaming");
        career2.setRoleType(RoleType.HEAD_COACH);


        // 2. Main Request DTO 준비
        var request = new InstructorDto.InstructorCreateRequest();
        request.setName("서재원");
        request.setNickname("Rexi");
        request.setSgeaLogoImgUrl("sgea_logo.png");
        request.setContent("메이저 리그 출신...");
        request.setCareers(List.of(career1, career2));
        request.setGameNames(List.of("Valorant", "Overwatch 2")); // setup에서 저장한 게임 이름

        // sout 대신 log.info() 사용
        // 중괄호 {}를 사용하면 파라미터가 효율적으로 전달됩니다.
        log.info("➡️ 생성 요청 DTO: {}", request); // (DTO에 toString()이 구현되어 있어야 함)

        // --- When (실행) ---
        log.info("🚀 instructorService.createInstructor() 호출");
        InstructorDto.InstructorDetailResponse response = instructorService.createInstructor(request);
        Long instructorId = response.getId();
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
        assertThat(findInstructor.getName()).isEqualTo("서재원");
        assertThat(findInstructor.getNickname()).isEqualTo("Rexi");
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
                .extracting(ig -> ig.getGameType().getName())
                .containsExactlyInAnyOrder("Valorant", "Overwatch 2");

        log.info("===== ✅ 강사 생성(C) 테스트 통과 =====");
    }
    @Test
    @DisplayName("강사 목록 조회(R-List): N+1 없이 모든 강사 목록 DTO를 반환한다.")
    void getInstructorListTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 강사 목록(R-List) 테스트 시작 =====");

        // Given 1: 강사 1 ("Rexi") 생성 (Valorant)
        var career1 = new InstructorDto.InstructorCreateRequest.CareerHistoryRequest();
        career1.setPeriod("2018");
        career1.setTeamName("SkyFoxes");
        career1.setRoleType(RoleType.PLAYER);

        var request1 = new InstructorDto.InstructorCreateRequest();
        request1.setName("서재원");
        request1.setNickname("Rexi");
        request1.setSgeaLogoImgUrl("sgea_logo.png");
        request1.setContent("메이저 리그 출신...");
        request1.setCareers(List.of(career1));
        request1.setGameNames(List.of("Valorant"));
        instructorService.createInstructor(request1); // (반환값 안씀)

        // Given 2: 강사 2 ("Aka") 생성 (LoL, Valorant)
        var career2 = new InstructorDto.InstructorCreateRequest.CareerHistoryRequest();
        career2.setPeriod("2020");
        career2.setTeamName("T1");
        career2.setRoleType(RoleType.COACH);

        var request2 = new InstructorDto.InstructorCreateRequest();
        request2.setName("김아카");
        request2.setNickname("Aka");
        request2.setSgeaLogoImgUrl("sgea_logo2.png");
        request2.setContent("LCK 출신...");
        request2.setCareers(List.of(career2));
        request2.setGameNames(List.of("Overwatch 2", "Valorant")); // 2개 게임
        instructorService.createInstructor(request2); // (반환값 안씀)

        // 💡 중요: 영속성 컨텍스트 초기화 (Fetch Join 쿼리 테스트를 위해)
        em.flush();
        em.clear();
        log.info("🔄 영속성 컨텍스트 초기화. DB에서 다시 조회합니다...");

        // --- When (실행) ---
        log.info("🚀 instructorService.getInstructorList() 호출");
        List<InstructorDto.InstructorListResponse> instructorList = instructorService.getInstructorList();

        // --- Then (검증) ---
        log.info("👀 조회된 DTO 목록: {}", instructorList);

        // 1. 개수 검증
        assertThat(instructorList).hasSize(2);

        // 2. 내용 검증 (DTO에 @ToString이 있다면 로그로 확인 가능)
        // (Set은 순서가 없으므로, 이름만 추출하여 검증)
        assertThat(instructorList)
                .extracting("name") // ListResponse DTO의 'name' 필드
                .containsExactlyInAnyOrder("서재원", "김아카");
        assertThat(instructorList)
                .extracting("nickname") // 닉네임 검증
                .containsExactlyInAnyOrder("Rexi", "Aka");
        // 3. (중요) N+1 방지 검증: games 필드가 올바르게 Join 되었는지 확인
        // "Aka" 강사를 찾아서, 게임 개수가 2개가 맞는지 확인
        InstructorDto.InstructorListResponse aka = instructorList.stream()
                .filter(i -> i.getNickname().equals("Aka")) // 닉네임으로 찾기
                .findFirst()
                .orElseThrow();

        log.info("👀 'Aka 김아카' 강사의 DTO 게임 목록: {}", aka.getGames());
        assertThat(aka.getGames()).hasSize(2);
        assertThat(aka.getGames())
                .extracting("name") // GameResponse DTO의 'name' 필드
                .containsExactlyInAnyOrder("Overwatch 2", "Valorant");

        log.info("===== ✅ 강사 목록(R-List) 테스트 통과 =====");
    }

    @Test
    @DisplayName("강사 상세 조회(R): Fetch Join을 통해 모든 연관 엔티티를 DTO로 변환한다.")
    void getInstructorDetailsTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 강사 조회(R) 테스트 시작 =====");
        // createInstructorTest와 동일한 로직으로 강사 1명 미리 생성
        // (실제로는 이 부분을 공통 메서드로 뽑아내는 것이 좋습니다)
        log.info("➡️ Given: 테스트용 강사 1명 생성 중...");
        var career1 = new InstructorDto.InstructorCreateRequest.CareerHistoryRequest();
        career1.setPeriod("2018");
        career1.setTeamName("SkyFoxes");
        career1.setRoleType(RoleType.PLAYER);

        var request = new InstructorDto.InstructorCreateRequest();
        request.setName("서재원");
        request.setNickname("Rexi");
        request.setSgeaLogoImgUrl("sgea_logo.png");
        request.setContent("메이저 리그 출신...");
        request.setCareers(List.of(career1));
        request.setGameNames(List.of("Valorant"));

        InstructorDto.InstructorDetailResponse response = instructorService.createInstructor(request);
        Long instructorId = response.getId();
        log.info("✅ Given: 테스트용 강사 생성 완료 (ID: {})", instructorId);

        // 1차 캐시(영속성 컨텍스트) 비우기
        // (이걸 안 하면 Service의 Fetch Join 쿼리가 아니라 캐시에서 데이터를 읽어버림)
        em.flush();
        em.clear();
        log.info("🔄 영속성 컨텍스트 초기화.");

        // --- When (실행) ---
        log.info("🚀 instructorService.getInstructorDetails({}) 호출", instructorId);
        InstructorDto.InstructorDetailResponse responseDto = instructorService.getInstructorDetails(instructorId);

        // --- Then (검증) ---
        log.info("👀 조회된 DTO: {}", responseDto); // (DetailResponse DTO에 @ToString 권장)
        log.info("👀 DTO 강사명: {}", responseDto.getName());
        log.info("👀 DTO 경력 수: {}", responseDto.getCareers().size());
        log.info("👀 DTO 게임 수: {}", responseDto.getGames().size());
        assertThat(responseDto.getId()).isEqualTo(instructorId);
        assertThat(responseDto.getName()).isEqualTo("서재원");
        assertThat(responseDto.getNickname()).isEqualTo("Rexi");
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
        log.info("===== ✅ 강사 상세 조회(R) 테스트 통과 =====");
    }

    @Test
    @DisplayName("강사 수정(U): 강사 정보 및 연관관계(경력, 게임)를 덮어쓴다.")
    void updateInstructorTest() {
        // --- Given (준비) ---
        log.info("===== 🏁 강사 수정(U) 테스트 시작 =====");

        // Given 1: "Rexi" 강사 생성 (경력 1개, 게임 1개)
        var originalCareer = new InstructorDto.InstructorCreateRequest.CareerHistoryRequest();
        originalCareer.setPeriod("2018");
        originalCareer.setTeamName("Original Team"); // ⬅️ "Original Team"
        originalCareer.setRoleType(RoleType.PLAYER);

        var createRequest = new InstructorDto.InstructorCreateRequest();
        createRequest.setName("서재원");
        createRequest.setNickname("Rexi");
        createRequest.setContent("수정 전 본문");
        createRequest.setCareers(List.of(originalCareer));
        createRequest.setGameNames(List.of("Valorant")); // ⬅️ "Valorant"

        InstructorDto.InstructorDetailResponse created = instructorService.createInstructor(createRequest);
        Long instructorId = created.getId();

        em.flush();
        em.clear();

        // Given 2: "수정용" DTO 준비 (경력 2개, 게임 1개)
        var updatedCareer1 = new InstructorDto.InstructorCreateRequest.CareerHistoryRequest();
        updatedCareer1.setPeriod("2020");
        updatedCareer1.setTeamName("Updated Team 1"); // ⬅️ "Updated Team 1"
        updatedCareer1.setRoleType(RoleType.COACH);

        var updatedCareer2 = new InstructorDto.InstructorCreateRequest.CareerHistoryRequest();
        updatedCareer2.setPeriod("2022");
        updatedCareer2.setTeamName("Updated Team 2"); // ⬅️ "Updated Team 2"
        updatedCareer2.setRoleType(RoleType.HEAD_COACH);

        var updateRequest = new InstructorDto.InstructorCreateRequest();
        updateRequest.setName("서재원(수정)"); // ⬅️ 이름 수정
        updateRequest.setNickname("Rexi-Updated"); // ⬅️ 닉네임 수정
        updateRequest.setContent("수정 완료 본문");
        updateRequest.setCareers(List.of(updatedCareer1, updatedCareer2)); // ⬅️ 경력 2개로 변경
        updateRequest.setGameNames(List.of("Overwatch 2")); // ⬅️ 게임 변경

        // --- When (실행) ---
        log.info("🚀 instructorService.updateInstructor({}) 호출", instructorId);
        instructorService.updateInstructor(instructorId, updateRequest);

        // --- Then (검증) ---
        em.flush();
        em.clear();

        Instructor updatedInstructor = instructorRepository.findInstructorDetailsById(instructorId)
                .orElseThrow(() -> new AssertionError("수정된 강사를 찾을 수 없습니다."));

        log.info("👀 수정된 강사 조회: {}", updatedInstructor.getName());
        log.info("👀 수정된 강사 경력: {}", updatedInstructor.getCareerHistories());
        log.info("👀 수정된 강사 게임: {}", updatedInstructor.getGames());

        // 1. 기본 필드 검증
        assertThat(updatedInstructor.getName()).isEqualTo("서재원(수정)");
        assertThat(updatedInstructor.getNickname()).isEqualTo("Rexi-Updated");

        // 2. ⭐️ 경력(Collection) 덮어쓰기 검증 ⭐️
        assertThat(updatedInstructor.getCareerHistories()).hasSize(2);
        assertThat(updatedInstructor.getCareerHistories())
                .extracting("teamName")
                .containsExactlyInAnyOrder("Updated Team 1", "Updated Team 2");
        // ➡️ "Original Team"이 삭제되었는지 검증

        // 3. ⭐️ 게임(Collection) 덮어쓰기 검증 ⭐️
        assertThat(updatedInstructor.getGames()).hasSize(1);
        assertThat(updatedInstructor.getGames())
                .extracting(ig -> ig.getGameType().getName())
                .containsExactly("Overwatch 2");
        // ➡️ "Valorant"가 삭제되었는지 검증

        log.info("===== ✅ 강사 수정(U) 테스트 통과 =====");
    }
}


