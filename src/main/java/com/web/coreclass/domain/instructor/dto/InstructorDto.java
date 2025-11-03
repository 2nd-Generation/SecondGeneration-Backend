package com.web.coreclass.domain.instructor.dto;

import com.web.coreclass.domain.careerHistory.entity.CareerHistory;
import com.web.coreclass.domain.careerHistory.entity.CareerType;
import com.web.coreclass.domain.game.entity.Game;
import com.web.coreclass.domain.instructor.entity.Instructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class InstructorDto {
    /**
     * (C) Create: 강사 생성을 위한 요청 DTO
     * 강사 정보 + 경력 목록 + 담당 게임 이름 목록
     */
    @Getter
    @Setter // Body -> DTO 변환을 위해 필요
    @ToString
    public static class CreateRequest {
        private String name;
        private String profileImgUrl;
        private String currentTitle;
        private List<CareerHistoryRequest> careers;
        private List<String> gameNames; // e.g., ["Valorant", "League of Legends"]

        @Getter
        @Setter
        @ToString
        public static class CareerHistoryRequest {
            private CareerType careerType;
            private String period;
            private String organizationName;
            private String roleTitle;
            private String logoImgUrl;

            // DTO -> Entity 변환 메서드
            public CareerHistory toEntity() {
                CareerHistory history = new CareerHistory();
                history.setCareerType(this.careerType);
                history.setPeriod(this.period);
                history.setOrganizationName(this.organizationName);
                history.setRoleTitle(this.roleTitle);
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
    public static class DetailResponse {
        private Long id;
        private String name;
        private String profileImgUrl;
        private String currentTitle;
        private List<CareerHistoryResponse> careers;
        private List<GameResponse> games;
        private LocalDateTime createdAt;

        // 경력 상세 DTO (Nested)
        @Getter
        @ToString
        public static class CareerHistoryResponse {
            private Long id;
            private CareerType careerType;
            private String period;
            private String organizationName;
            private String roleTitle;
            private String logoImgUrl;

            public CareerHistoryResponse(CareerHistory history) {
                this.id = history.getId();
                this.careerType = history.getCareerType();
                this.period = history.getPeriod();
                this.organizationName = history.getOrganizationName();
                this.roleTitle = history.getRoleTitle();
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
        public DetailResponse(Instructor instructor) {
            this.id = instructor.getId();
            this.name = instructor.getName();
            this.profileImgUrl = instructor.getProfileImgUrl();
            this.currentTitle = instructor.getCurrentTitle();
            this.createdAt = instructor.getCreatedAt();

            // 엔티티 리스트 -> DTO 리스트로 변환
            this.careers = instructor.getCareerHistories().stream()
                    .map(CareerHistoryResponse::new)
                    .collect(Collectors.toList());

            this.games = instructor.getGames().stream()
                    .map(instructorGame -> new GameResponse(instructorGame.getGame()))
                    .collect(Collectors.toList());
        }
    }
}
