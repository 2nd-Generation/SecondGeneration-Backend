package com.web.coreclass.domain.instructor.controller;

import com.web.coreclass.domain.instructor.dto.InstructorDto;
import com.web.coreclass.domain.instructor.service.InstructorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instructor")
public class InstructorController {
    private final InstructorService instructorService;

    /**
     * (C) 강사 생성
     */
    @Operation(summary = "강사 생성", description = "강사 생성")
    @PostMapping
    public ResponseEntity<InstructorDto.InstructorDetailResponse> createInstructor(@RequestBody InstructorDto.InstructorCreateRequest request) {
        InstructorDto.InstructorDetailResponse createdInstructor = instructorService.createInstructor(request);
        Long instructorId = createdInstructor.getId();

        URI location = URI.create("/api/instructor/" + instructorId);
        // 201 Created 응답 + Location 헤더 + 생성된 DTO 본문
        return ResponseEntity.created(location).body(createdInstructor);
    }

    /**
     * (R) 강사 전체 목록 조회
     */
    @Operation(summary = "강사 전체 목록 조회", description = "모든 강사 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<InstructorDto.InstructorListResponse>> getInstructorList() {
        List<InstructorDto.InstructorListResponse> list = instructorService.getInstructorList();
        return ResponseEntity.ok(list);
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
            @RequestParam String nickname
    )
    {
        instructorService.updateInstructorInfo(id, name, nickname);
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
