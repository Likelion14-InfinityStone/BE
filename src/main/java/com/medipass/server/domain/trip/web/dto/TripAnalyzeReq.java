package com.medipass.server.domain.trip.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 약 판정(분석) 요청 — 저장 전 미리보기용
 * 도착 국가 + 선택한 약들. 저장은 안 하고 판정 결과만 반환한다
 */
public record TripAnalyzeReq(

        @NotBlank(message = "도착 국가 코드는 필수입니다")
        String destinationCountryCode, // JAP (ISO alpha-3)

        @NotEmpty(message = "약을 1개 이상 선택해야 합니다")
        @Valid
        List<Item> medications
) {
    public record Item(

            @NotNull(message = "복약카드 id는 필수입니다")
            Long medicationId,

            @NotNull(message = "일수는 필수입니다")
            @Positive(message = "일수는 1 이상이어야 합니다")
            Integer carryDays
    ) {
    }
}
