package com.medipass.server.domain.trip.web.controller;

import com.medipass.server.domain.medication.web.dto.MedicationListRes;
import com.medipass.server.domain.trip.service.TripService;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeReq;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeRes;
import com.medipass.server.domain.trip.web.dto.TripChecklogRes;
import com.medipass.server.domain.trip.web.dto.TripCreateReq;
import com.medipass.server.domain.trip.web.dto.TripCreateRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import com.medipass.server.global.response.code.SuccessResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    // 저장 전 미리보기 — 선택한 약을 도착국 기준으로 판정만 (저장 X)
    @PostMapping("/analyze")
    public ApiResponse<TripAnalyzeRes> analyze(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TripAnalyzeReq request
    ) {
        return ApiResponse.success(tripService.analyze(userDetails.getUser().getId(), request));
    }

    // 저장 — analyze 결과를 그대로 실어 여행 생성 (재판정 X)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TripCreateRes> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TripCreateReq request
    ) {
        TripCreateRes response = tripService.createTrip(userDetails.getUser().getId(), request);
        return ApiResponse.success(SuccessResponseCode.SUCCESS_CREATED, response);
    }

    // 체크로그함 — 국가 칩(최신순) + 선택 국가(기본=최신)의 여행 목록
    @GetMapping("/checklog")
    public ApiResponse<TripChecklogRes> checklog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "country", required = false) String country
    ) {
        return ApiResponse.success(tripService.checklog(userDetails.getUser().getId(), country));
    }

    // 여행 등록 중 가져갈 약 선택 — 내 복약카드 목록
    @GetMapping("/medications")
    public ApiResponse<MedicationListRes> selectableMedications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(tripService.selectableMedications(userDetails.getUser().getId()));
    }
}
