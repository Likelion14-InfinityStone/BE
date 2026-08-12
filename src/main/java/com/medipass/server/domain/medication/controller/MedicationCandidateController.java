package com.medipass.server.domain.medication.controller;

import com.medipass.server.domain.medication.dto.response.MedicationCandidateSearchResponse;
import com.medipass.server.domain.medication.service.MedicationCandidateService;
import com.medipass.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Medication", description = "OCR 의약품 자동 매칭 API")
@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationCandidateController {

    private final MedicationCandidateService medicationCandidateService;

    /*
     * 기본 OCR 등록 흐름에서는 MedicationScanService가 식약처 자동 매칭을 내부 수행하므로
     * 프론트가 이 API를 별도로 호출할 필요가 없다.
     * 자동 매칭 실패 또는 향후 사용자가 제품명을 수정하는 기능이 추가될 때,
     * 수정된 제품명으로 식약처 제품을 다시 매칭하기 위한 확장용 엔드포인트로 유지한다.
     */
    @Operation(
            summary = "제품명으로 식약처 의약품 재매칭",
            description = "OCR 자동 매칭 실패 또는 사용자가 제품명을 수정한 경우 식약처 의약품을 다시 조회하고, 검색 결과의 첫 번째 제품을 매칭합니다."
    )
    @GetMapping("/candidates")
    public ApiResponse<MedicationCandidateSearchResponse> searchCandidates(
            @Parameter(
                    description = "식약처에서 검색할 제품명",
                    example = "콘서타"
            )
            @RequestParam("name") String name
    ) {
        return ApiResponse.success(medicationCandidateService.search(name));
    }
}
