package com.medipass.server.domain.trip.web.dto;

import java.time.LocalDate;

/**
 * 약 상세 - 근거 — AI 요약 + 출처(규제 원천)
 * summary 는 최초 조회 때 생성해 trip_medication 에 저장, 출처·확인일은 규제 데이터(TSV)에서
 */
public record TripMedicationBasisRes(
        Long tripMedicationId,
        String summary,        // AI 근거 요약
        String source,         // 출처 이름 (예: 일본 후생노동성), 규제 없으면 null
        String sourceUrl,      // 근거 원문 URL, 규제 없으면 null
        LocalDate verifiedDate // 출처 확인일 (규제 TSV verified_date), 규제 없으면 null
) {
}
