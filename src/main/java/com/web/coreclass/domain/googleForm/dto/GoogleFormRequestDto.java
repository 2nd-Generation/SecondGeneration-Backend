package com.web.coreclass.domain.googleForm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleFormRequestDto {
    @Schema(description = "개인정보 동의", example = "동의")
    private String privacyAgreement;      // entry.389903463
    @Schema(description = "수강 희망 게임", example = "VALORANT, OVERWATCH")
    private String desiredGame;           // entry.404655261

    // 발로란트 관련
    @Schema(description = "수강 희망 반", example = "1:1 프리미엄 집중반 주1회, 1:1 프리미엄 집중반 주2회, 1:5 팀 그룹 취미반, 1:5 팀 그룹 성장반, 1:5 팀 그룹 프로반")
    private String valorantClass;         // entry.1696074802
    @Schema(description = "현재 티어", example = "레디언트 (Radiant), 불멸 (Immortal), 초월자 (Ascendant), 다이아몬드 (Diamond), 플래티넘 (Platinum), 골드 (Gold), 실버 (Silver), 브론즈 (Bronze), 아이언 (Iron), 언랭 (Unranked)")
    private String valorantTier;          // entry.1681564620
    @Schema(description = "주 포지션", example = "타격대 (Duelist), 척후대 (Initiator), 감시자 (Sentinel), 전략가 (Controller), 올라운더 (All-Rounder)")
    private String valorantPosition;      // entry.1328489994

    // 오버워치 관련
    @Schema(description = "수강 희망 반", example = "1:1 프리미엄 집중반 주1회, 1:1 프리미엄 집중반 주2회, 1:5 팀 그룹 취미반, 1:5 팀 그룹 성장반, 1:5 팀 그룹 프로반")
    private String overwatchClass;        // entry.902463495
    @Schema(description = "현재 티어", example = "랭커 (TOP500), 챔피언 (Champion), 그랜드 마스터(Grand master), 마스터(Master), 다이아몬드(Diamond), 플레티넘(Platinum), 골드(Gold), 실버(Silver), 브론즈(Bronze), 언랭(Unranked)")
    private String overwatchTier;         // entry.565147399
    @Schema(description = "주 포지션", example = "탱커, 딜러, 힐러")
    private String overwatchPosition;     // entry.1532619185

    // 지원자 정보
    @Schema(description = "본인 명의 계정", example = "SGEA#31281")
    private String gameAccount;           // entry.1382061334
    @Schema(description = "이름", example = "홍길동")
    private String name;                  // entry.1682194881
    @Schema(description = "성별", example = "남자, 여자")
    private String gender;                // entry.1700001593
    @Schema(description = "생년월일", example = "2025-01-01")
    private String birthDate;             // entry.260228334
    @Schema(description = "거주 지역 (간략히)", example = "서울특별시")
    private String address;               // entry.318298886
    @Schema(description = "학생 핸드폰 번호", example = "010-0000-0000")
    private String phoneNumber;           // entry.1682681799
    @Schema(description = "디스코드 아이디", example = "sgea_admin")
    private String discordId;             // entry.1475828191
    @Schema(description = "보호자 성함", example = "미성년자인 경우 꼭 정보를 기입해주세요")
    private String guardianName;          // entry.1603110817
    @Schema(description = "보호자 핸드폰 번호", example = "010-0000-0000")
    private String guardianPhoneNumber;    // entry.76570193
    @Schema(description = "SGEA를 어떻게 알게되셨나요?", example = "지인 추천, 인터넷 검색(네이버 / 구글), SGEA 인스타그램, 타 인스타그램, 기타(자유롭게 작성)")
    private String knowSgeaPath;
}
