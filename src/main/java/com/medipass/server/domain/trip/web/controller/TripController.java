package com.medipass.server.domain.trip.web.controller;

import com.medipass.server.domain.medication.web.dto.MedicationListRes;
import com.medipass.server.domain.trip.service.TripService;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeReq;
import com.medipass.server.domain.trip.web.dto.TripAnalyzeRes;
import com.medipass.server.domain.trip.web.dto.TripChecklogRes;
import com.medipass.server.domain.trip.web.dto.TripCreateReq;
import com.medipass.server.domain.trip.web.dto.TripCreateRes;
import com.medipass.server.domain.trip.web.dto.ChecklistDoneUpdateReq;
import com.medipass.server.domain.trip.web.dto.TripDetailRes;
import com.medipass.server.domain.trip.web.dto.TripMedicationChecklistRes;
import com.medipass.server.domain.trip.web.dto.TripMedicationDestinationRes;
import com.medipass.server.domain.trip.web.dto.TripTitleUpdateReq;
import com.medipass.server.domain.trip.web.dto.TripTitleUpdateRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import com.medipass.server.global.response.code.SuccessResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 여행 상세 — 여행 티켓 눌렀을 때, 카드 + 담긴 약(신호등) 목록
    @GetMapping("/{tripId}")
    public ApiResponse<TripDetailRes> detail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tripId
    ) {
        return ApiResponse.success(tripService.getTrip(userDetails.getUser().getId(), tripId));
    }

    // 여행 이름 수정 — 같은 사용자 안에서 이름 겹치면 409
    @PatchMapping("/{tripId}/title")
    public ApiResponse<TripTitleUpdateRes> updateTitle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tripId,
            @Valid @RequestBody TripTitleUpdateReq request
    ) {
        return ApiResponse.success(tripService.updateTitle(userDetails.getUser().getId(), tripId, request.title()));
    }

    // 약 상세 - 목적지 규정 — 헤더(제품·성분·함량) + 판정 스냅샷 + 필요 서류
    @GetMapping("/{tripId}/medications/{tripMedicationId}/destination")
    public ApiResponse<TripMedicationDestinationRes> destinationRules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tripId,
            @PathVariable Long tripMedicationId
    ) {
        return ApiResponse.success(tripService.getDestinationRules(
                userDetails.getUser().getId(), tripId, tripMedicationId));
    }

    // 약 상세 - 체크리스트 조회 (진행률 + 서류 항목)
    @GetMapping("/{tripId}/medications/{tripMedicationId}/checklist")
    public ApiResponse<TripMedicationChecklistRes> checklist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tripId,
            @PathVariable Long tripMedicationId
    ) {
        return ApiResponse.success(tripService.getChecklist(
                userDetails.getUser().getId(), tripId, tripMedicationId));
    }

    // 약 상세 - 체크리스트 항목 체크/해제
    @PatchMapping("/{tripId}/medications/{tripMedicationId}/checklist/{checklistItemId}")
    public ApiResponse<TripMedicationChecklistRes> updateChecklistDone(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tripId,
            @PathVariable Long tripMedicationId,
            @PathVariable Long checklistItemId,
            @Valid @RequestBody ChecklistDoneUpdateReq request
    ) {
        return ApiResponse.success(tripService.updateChecklistDone(
                userDetails.getUser().getId(), tripId, tripMedicationId, checklistItemId, request.done()));
    }
}
