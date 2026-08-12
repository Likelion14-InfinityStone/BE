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

@Tag(name = "Medication Scan", description = "약 봉투 이미지 인식 API")
@RestController
@RequestMapping("/api/medication-scans")
@RequiredArgsConstructor
public class MedicationScanController {

    private final MedicationScanService medicationScanService;

    @Operation(
            summary = "약 봉투 인식",
            description = "약 봉투에서 조제·복용 정보를 추출하고 OCR 제품명을 식약처 의약품과 자동 매칭합니다. "
                    + "항목의 mfdsProductCode와 productKoName이 모두 null이면 매칭 실패이므로 의약품 인식 실패 및 재촬영 안내가 필요합니다. "
                    + "복용 정보가 인식되지 않으면 해당 필드는 null로 반환됩니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MedicationScanRes> scan(
            @Parameter(description = "약 봉투 이미지(JPEG 또는 PNG)", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(medicationScanService.scan(file));
    }
}
