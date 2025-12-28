package com.web.coreclass.domain.googleForm;



import com.web.coreclass.domain.googleForm.dto.GoogleFormRequestDto;
import com.web.coreclass.domain.googleForm.service.GoogleFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/google-form")
@RequiredArgsConstructor
public class GoogleFormController {
    private final GoogleFormService googleFormService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitApplication(@RequestBody GoogleFormRequestDto request) {
        googleFormService.submitToGoogleForm(request);
        return ResponseEntity.ok("지원서가 성공적으로 접수되었습니다.");
    }
}
