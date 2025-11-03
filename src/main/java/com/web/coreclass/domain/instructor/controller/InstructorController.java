package com.web.coreclass.domain.instructor.controller;

import com.web.coreclass.domain.instructor.dto.InstructorDto;
import com.web.coreclass.domain.instructor.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instructor")
public class InstructorController {
    private final InstructorService instructorService;

    /**
     * (C) 강사 생성
     */
    @PostMapping
    public ResponseEntity<Void> createInstructor(@RequestBody InstructorDto.CreateRequest request) {
        Long instructorId = instructorService.createInstructor(request);
        // 생성된 리소스의 URI를 반환 (RESTful)
        return ResponseEntity.created(URI.create("/api/instructors/" + instructorId)).build();
    }

    /**
     * (R) 강사 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstructorDto.DetailResponse> getInstructorDetails(@PathVariable Long id) {
        InstructorDto.DetailResponse response = instructorService.getInstructorDetails(id);
        return ResponseEntity.ok(response);
    }

    /**
     * (U) 강사 정보 수정 (간단한 예시)
     */
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstructor(@PathVariable Long id) {
        instructorService.deleteInstructor(id);
        return ResponseEntity.noContent().build();
    }
}
