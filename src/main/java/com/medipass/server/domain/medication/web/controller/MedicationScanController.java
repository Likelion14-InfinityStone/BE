package com.medipass.server.domain.medication.web.controller;

import com.medipass.server.domain.medication.service.MedicationScanService;
import com.medipass.server.domain.medication.web.dto.MedicationScanRes;
import com.medipass.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Medication", description = "의약품 스캔·검색·등록 API")
@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationScanController {

    private final MedicationScanService medicationScanService;

    @Operation(
            summary = "약 봉투 인식",
            description = "약 봉투에서 조제·복용 정보를 추출하고 OCR 제품명을 식약처 의약품과 자동 매칭합니다. "
                    + "일부 제품의 식약처 매칭에 실패해도 인식된 결과를 반환하며, 해당 항목의 제품정보는 null입니다. "
                    + "프론트에서는 사용자가 제품명을 직접 입력하고 후보 검색을 통해 제품을 선택하도록 안내합니다. "
                    + "이미지에서 약품명을 하나도 추출하지 못한 경우에만 OCR_422를 반환합니다."
    )
    @PostMapping(value = "/scans", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MedicationScanRes> scan(
            @Parameter(description = "약 봉투 이미지(JPEG 또는 PNG)", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(medicationScanService.scan(file));
    }
}
