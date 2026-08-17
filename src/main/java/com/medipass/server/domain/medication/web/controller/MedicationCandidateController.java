package com.medipass.server.domain.medication.web.controller;

import com.medipass.server.domain.medication.service.MedicationCandidateService;
import com.medipass.server.domain.medication.web.dto.MedicationCandidateSearchRes;
import com.medipass.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Medication", description = "의약품 스캔·검색·등록 API")
@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationCandidateController {

    private final MedicationCandidateService medicationCandidateService;

    /*
     * OCR 등록 흐름에서는 MedicationScanService가 식약처 자동 매칭을 내부 수행한다.
     * 직접 입력 흐름에서는 사용자가 입력한 제품명으로 이 API를 호출한 뒤,
     * 반환된 후보 중 정확한 함량·제형의 제품을 선택한다.
     */
    @Operation(
            summary = "제품명으로 식약처 의약품 후보 검색",
            description = "직접 입력한 제품명으로 식약처 의약품을 검색하고, 정확한 제품을 선택할 수 있도록 후보 목록을 반환합니다."
    )
    @GetMapping("/candidates")
    public ApiResponse<MedicationCandidateSearchRes> searchCandidates(
            @Parameter(
                    description = "식약처에서 검색할 제품명",
                    example = "콘서타"
            )
            @RequestParam("name") String name
    ) {
        return ApiResponse.success(medicationCandidateService.search(name));
    }
}
