package com.web.coreclass.domain.googleForm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleFormRequestDto {
    private String privacyAgreement;      // entry.389903463
    private String desiredGame;           // entry.404655261

    // 발로란트 관련
    private String valorantClass;         // entry.1696074802
    private String valorantTier;          // entry.1681564620
    private String valorantPosition;      // entry.1328489994

    // 오버워치 관련
    private String overwatchClass;        // entry.902463495
    private String overwatchTier;         // entry.565147399
    private String overwatchPosition;     // entry.1532619185

    // 지원자 정보
    private String gameAccount;           // entry.1382061334
    private String name;                  // entry.1682194881
    private String gender;                // entry.1700001593
    private String birthDate;             // entry.260228334
    private String address;               // entry.318298886
    private String phoneNumber;           // entry.1682681799
    private String discordId;             // entry.1475828191
    private String guardianName;          // entry.1603110817
    private String guardianPhoneNumber;    // entry.76570193
}
