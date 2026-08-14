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
     * 기본 OCR 등록 흐름에서는 MedicationScanService가 식약처 자동 매칭을 내부 수행하므로
     * 프론트가 이 API를 별도로 호출할 필요가 없다.
     * 자동 매칭 실패 또는 향후 사용자가 제품명을 수정하는 기능이 추가될 때,
     * 수정된 제품명으로 식약처 제품을 다시 매칭하기 위한 확장용 엔드포인트로 유지한다.
     */
    @Operation(
            summary = "제품명으로 식약처 의약품 재매칭",
            description = "향후 제품명 수정 기능에서 식약처 의약품을 재검색하기 위한 확장용 API입니다. "
                    + "검색 결과가 여러 건이면 MVP 정책에 따라 첫 번째 제품을 반환합니다."
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
