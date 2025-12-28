package com.web.coreclass.domain.googleForm.service;


import com.web.coreclass.domain.googleForm.dto.GoogleFormRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

@Service
public class GoogleFormService {
    private final RestClient restClient = RestClient.create();

    // ⚠️ 실제 사용하시는 구글 폼의 ID로 교체해야 합니다.
    private static final String GOOGLE_FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLSeJ5Jald5tjTEhfZNlQfi7OsaarfgBOXJCr1o2UZwp2KjmhFw/formResponse";

    public void submitToGoogleForm(GoogleFormRequestDto request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        // DTO 데이터를 구글 폼 entry ID와 매핑
        formData.add("entry.389903463", request.getPrivacyAgreement());
        formData.add("entry.404655261", request.getDesiredGame());

        // 발로란트/오버워치 (선택한 종목에 따라 값이 들어감)
        formData.add("entry.1696074802", request.getValorantClass());
        formData.add("entry.1681564620", request.getValorantTier());
        formData.add("entry.1328489994", request.getValorantPosition());

        formData.add("entry.902463495", request.getOverwatchClass());
        formData.add("entry.565147399", request.getOverwatchTier());
        formData.add("entry.1532619185", request.getOverwatchPosition());

        // 인적 사항
        formData.add("entry.1382061334", request.getGameAccount());
        formData.add("entry.1682194881", request.getName());
        formData.add("entry.1700001593", request.getGender());
        formData.add("entry.260228334", request.getBirthDate());
        formData.add("entry.318298886", request.getAddress());
        formData.add("entry.1682681799", request.getPhoneNumber());
        formData.add("entry.1475828191", request.getDiscordId());
        formData.add("entry.1603110817", request.getGuardianName());
        formData.add("entry.76570193", request.getGuardianPhoneNumber());

        String pageHistory = "0"; // 기본 첫 페이지

        if ("VALORANT".equals(request.getDesiredGame())) {
            pageHistory = "0,1,2,4"; // 동의 -> 종목선택 -> 발로란트 -> 정보입력
        } else if ("OVERWATCH".equals(request.getDesiredGame())) {
            pageHistory = "0,1,3,4"; // 동의 -> 종목선택 -> 오버워치 -> 정보입력
        }

        formData.add("pageHistory", pageHistory);
        // 구글 폼으로 POST 전송 (form-urlencoded 방식)
        restClient.post()
                .uri(GOOGLE_FORM_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toBodilessEntity();
    }
}
