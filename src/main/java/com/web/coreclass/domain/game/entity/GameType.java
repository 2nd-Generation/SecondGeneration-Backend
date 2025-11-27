package com.web.coreclass.domain.game.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameType {
    OVERWATCH_2("Overwatch 2", "https://second-generation-upload-bucket.s3.ap-northeast-2.amazonaws.com/overwatch2_logo.png"),
    VALORANT("Valorant", "https://second-generation-upload-bucket.s3.ap-northeast-2.amazonaws.com/valorant_logo.png");

    private final String name;      // DB 저장용 이름 or 화면 표시용
    private final String logoUrl;   // 로고 이미지 URL

    // 문자열(String)로 Enum을 찾는 메서드 추가
    public static GameType fromName(String name) {
        for (GameType type : values()) {
            // 대소문자 무시하고 띄어쓰기 제거 후 비교 등 유연하게 처리 가능
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 게임입니다: " + name);
    }
}