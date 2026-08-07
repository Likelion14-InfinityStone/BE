package com.medipass.server.domain.medication.controller;

import com.medipass.server.domain.medication.service.MedicationScanService;
import com.medipass.server.global.ocr.dto.OcrResult;
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
            summary = "약 봉투 OCR",
            description = "카메라로 촬영하거나 갤러리에서 선택한 약 봉투 이미지의 텍스트를 추출합니다."
    )
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<OcrResult> extract(
            @Parameter(description = "약 봉투 이미지(JPEG 또는 PNG)", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(medicationScanService.extract(file));
    }
}
