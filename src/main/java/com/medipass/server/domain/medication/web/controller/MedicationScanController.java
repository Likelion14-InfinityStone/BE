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
                    + "식약처 제품정보(mfdsProductCode, productKoName, productEnName) 또는 복용 단위(doseUnit)를 "
                    + "확인하지 못하면 OCR_422를 반환하므로 재촬영 안내가 필요합니다. "
                    + "사용자는 조제일자·발행기관·복용 횟수·복용 일수·1회 복용량만 수정할 수 있습니다."
    )
    @PostMapping(value = "/scans", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MedicationScanRes> scan(
            @Parameter(description = "약 봉투 이미지(JPEG 또는 PNG)", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(medicationScanService.scan(file));
    }
}
