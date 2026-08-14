package com.medipass.server.domain.trip.web.dto;

import com.medipass.server.domain.regulation.entity.PreparationLevel;

import java.util.List;

/**
 * 여행 생성 응답, 저장된 여행 + 약별 판정 신호등
 */
public record TripCreateRes(
        Long tripId,
        String title,
        List<MedicationResult> medications
) {
    // 약별 판정 결과
    public record MedicationResult(
            Long tripMedicationId,
            Long medicationId,
            String productKoName,
            PreparationLevel preparationLevel  // 신호등 스냅샷
    ) {
    }
}
