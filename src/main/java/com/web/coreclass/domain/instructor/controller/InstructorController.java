package com.web.coreclass.domain.instructor.controller;

import com.web.coreclass.domain.instructor.dto.InstructorDto;
import com.web.coreclass.domain.instructor.service.InstructorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instructors")
public class InstructorController {
    private final InstructorService instructorService;

    /**
     * (C) 강사 생성
     */
    @Operation(summary = "강사 생성", description = "강사 생성")
    @PostMapping
    public ResponseEntity<Void> createInstructor(@RequestBody InstructorDto.InstructorCreateRequest request) {
        Long instructorId = instructorService.createInstructor(request);
        // 생성된 리소스의 URI를 반환 (RESTful)
        return ResponseEntity.created(URI.create("/api/instructors/" + instructorId)).build();
    }

    /**
     * (R) 강사 상세 조회
     */
    @Operation(summary = "강사 상세조회", description = "강사 id 값으로 상세조회")
    @GetMapping("/{id}")
    public ResponseEntity<InstructorDto.InstructorDetailResponse> getInstructorDetails(@PathVariable Long id) {
        InstructorDto.InstructorDetailResponse response = instructorService.getInstructorDetails(id);
        return ResponseEntity.ok(response);
    }

    /**
     * (U) 강사 정보 수정 (간단한 예시)
     */
    @Operation(summary = "강사 정보 수정", description = "강사 id 값으로 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateInstructorInfo(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String currentTitle)
    {
        instructorService.updateInstructorInfo(id, name, currentTitle);
        return ResponseEntity.ok().build();
    }

    /**
     * (D) 강사 삭제
     */
    @Operation(summary = "강사 삭제", description = "강사 id 값으로 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstructor(@PathVariable Long id) {
        instructorService.deleteInstructor(id);
        return ResponseEntity.noContent().build();
    }
}
