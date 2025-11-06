package com.web.coreclass.domain.instructor.dto;

import com.web.coreclass.domain.careerHistory.entity.CareerHistory;
import com.web.coreclass.domain.careerHistory.entity.RoleType;
import com.web.coreclass.domain.game.entity.Game;
import com.web.coreclass.domain.instructor.entity.Instructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InstructorDto {
    /**
     * (C) Create: 강사 생성을 위한 요청 DTO
     * 강사 정보 + 경력 목록 + 담당 게임 이름 목록
     */
    @Getter
    @Setter // Body -> DTO 변환을 위해 필요
    @ToString
    public static class InstructorCreateRequest {
        @Schema(description = "강사 이름", example = "김찬희")
        private String name;

        @Schema(description = "강사 게임 닉네임", example = "Mandu")
        private String nickname;

        @Schema(description = "강사 프로필 이미지 Url", example = "null")
        private String profileImgUrl;

        @Schema(description = "SGEA 로고 이미지 Url", example = "null")
        private String sgeaLogoImgUrl;

        @Schema(description = "강사 소개글", example = "메이저 리그 출신 ...")
        private String content;

        @Schema(description = "강사 커리어")
        private List<CareerHistoryRequest> careers;

        @Schema(description = "게임 이름", example = "Overwatch2, Valorant")
        private List<String> gameNames; // e.g., ["Valorant", "League of Legends"]

        @Getter
        @Setter
        @ToString
        public static class CareerHistoryRequest {
            @Schema(description = "활동 연도", example = "2018-2020")
            private String period;

            @Schema(description = "팀 이름", example = "New York Excelsior")
            private String teamName;

            @Schema(description = "역할", example = "Head Coach")
            private RoleType roleType;

            @Schema(description = "팀 로고 이미지 Url", example = "null")
            private String logoImgUrl;

            // DTO -> Entity 변환 메서드
            public CareerHistory toEntity() {
                CareerHistory history = new CareerHistory();
                history.setPeriod(this.period);
                history.setTeamName(this.teamName);
                history.setRoleType(this.roleType);
                history.setLogoImgUrl(this.logoImgUrl);
                return history;
            }
        }
    }

    /**
     * (R) Read: 강사 상세 정보 응답 DTO
     */
    @Getter
    @ToString
    public static class InstructorDetailResponse {
        private Long id;
        private String name;
        private String nickname;
        private String profileImgUrl;
        private String sgeaLogoImgUrl;
        private String content;
        private Set<CareerHistoryResponse> careers;
        private Set<GameResponse> games;
        private LocalDateTime createdAt;

        // 경력 상세 DTO (Nested)
        @Getter
        @ToString
        public static class CareerHistoryResponse {
            private Long id;
            private String period;
            private String teamName;
            private RoleType roleType;
            private String logoImgUrl;

            public CareerHistoryResponse(CareerHistory history) {
                this.id = history.getId();
                this.period = history.getPeriod();
                this.teamName = history.getTeamName();
                this.roleType = history.getRoleType();
                this.logoImgUrl = history.getLogoImgUrl();
            }
        }

        // 게임 상세 DTO (Nested)
        @Getter
        @ToString
        public static class GameResponse {
            private Long id;
            private String name;
            private String gameLogoUrl;

            public GameResponse(Game game) {
                this.id = game.getId();
                this.name = game.getName();
                this.gameLogoUrl = game.getGameLogoUrl();
            }
        }

        // Entity -> DTO 변환 생성자
        public InstructorDetailResponse(Instructor instructor) {
            this.id = instructor.getId();
            this.name = instructor.getName();
            this.nickname = instructor.getNickname();
            this.profileImgUrl = instructor.getProfileImgUrl();
            this.sgeaLogoImgUrl = instructor.getSgeaLogoImgUrl();
            this.content = instructor.getContent();
            this.createdAt = instructor.getCreatedAt();

            // 엔티티 리스트 -> DTO 리스트로 변환
            this.careers = instructor.getCareerHistories().stream()
                    .map(CareerHistoryResponse::new)
                    .collect(Collectors.toSet());

            this.games = instructor.getGames().stream()
                    .map(instructorGame -> new GameResponse(instructorGame.getGame()))
                    .collect(Collectors.toSet());
        }
    }
    /**
     * (R) List Response: 강사 목록 조회를 위한 DTO
     * (상세 DTO에서 content, careers 등 무거운 필드 제외)
     */
    @Getter
    @ToString
    public static class InstructorListResponse {
        private Long id;
        private String name;
        private String nickname;
        private String profileImgUrl;
        private String sgeaLogoImgUrl;
        // ✅ 게임 목록은 포함 (게임 로고 이미지)
        private Set<InstructorDetailResponse.GameResponse> games;

        // Entity -> DTO 변환 생성자
        public InstructorListResponse(Instructor instructor) {
            this.id = instructor.getId();
            this.name = instructor.getName();
            this.nickname = instructor.getNickname();
            this.profileImgUrl = instructor.getProfileImgUrl();
            this.sgeaLogoImgUrl = instructor.getSgeaLogoImgUrl();

            // ✅ N+1 문제를 피하려면, Service에서 Fetch Join이 필요함
            this.games = instructor.getGames().stream()
                    .map(instructorGame -> new InstructorDetailResponse.GameResponse(instructorGame.getGame()))
                    .collect(Collectors.toSet());
        }
    }
}
