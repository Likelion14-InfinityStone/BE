package com.medipass.server.domain.medication.web.controller;

import com.medipass.server.domain.medication.service.MedicationService;
import com.medipass.server.domain.medication.web.dto.MedicationCreateReq;
import com.medipass.server.domain.medication.web.dto.MedicationCreateRes;
import com.medipass.server.domain.medication.web.dto.MedicationCardPageRes;
import com.medipass.server.domain.medication.web.dto.MedicationListRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import com.medipass.server.global.response.code.SuccessResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Medication", description = "의약품 및 복약카드 관리 API")
@Validated
@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @Operation(
            summary = "OCR 확인 결과로 의약품 등록",
            description = "스캔 API가 반환한 식약처 제품정보와 사용자가 확인·수정한 복용 정보를 최종 의약품으로 저장합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MedicationCreateRes> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MedicationCreateReq request
    ) {
        MedicationCreateRes response = medicationService.create(
                userDetails.getUser().getId(),
                request
        );
        return ApiResponse.success(SuccessResponseCode.SUCCESS_CREATED, response);
    }

    @Operation(
            summary = "복약카드 목록 조회",
            description = "사이드바에 표시할 로그인 사용자의 복약카드 ID와 한글 약 이름을 최근 등록순으로 조회합니다."
    )
    @GetMapping
    public ApiResponse<MedicationListRes> getAll(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MedicationListRes response = medicationService.getAll(userDetails.getUser().getId());
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "홈 화면 복약카드 조회",
            description = "사용자 닉네임과 복약카드의 앞면·한글 뒷면 정보를 최근 등록순으로 조회합니다. 복약카드가 없으면 빈 목록을 반환합니다."
    )
    @GetMapping("/cards")
    public ApiResponse<MedicationCardPageRes> getCards(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "3") @Min(1) @Max(20) int size
    ) {
        MedicationCardPageRes response = medicationService.getCards(
                userDetails.getUser().getId(),
                page,
                size
        );
        return ApiResponse.success(response);
    }
}
