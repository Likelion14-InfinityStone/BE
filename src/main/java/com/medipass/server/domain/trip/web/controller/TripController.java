package com.medipass.server.domain.trip.web.controller;

import com.medipass.server.domain.trip.service.TripService;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeReq;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeRes;
import com.medipass.server.domain.trip.web.dto.TripCreateReq;
import com.medipass.server.domain.trip.web.dto.TripCreateRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import com.medipass.server.global.response.code.SuccessResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}
