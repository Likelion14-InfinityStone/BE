package com.medipass.server.domain.trip.web.dto;

import com.medipass.server.domain.regulation.entity.PreparationLevel;
import com.medipass.server.domain.regulation.entity.RequirementKind;

import java.util.List;

/**
 * 약 판정(분석) 응답 — 약별 신호등·통제여부·필요서류
 * 저장 시 이 결과(신호등 + 서류 templateId)를 그대로 돌려받아 저장한다
 */
public record TripAnalyzeRes(
        List<MedicationResult> medications
) {
    public record MedicationResult(
            Long medicationId,
            String productKoName,
            PreparationLevel preparationLevel, // 신호등
            boolean regulated,           // 통제 성분 포함 여부
            String categoryCode,         // 분류 코드 (N/P/SRM)
            String categoryName,         // 분류 표시명 (마약류/향정신성의약품/각성제 원료)
            String quantityCondition,    // 수량 조건 (예: "최대 2160mg · 30일분")
            List<RequirementItem> requirements // 필요 서류 (준비 필요할 때만)
    ) {
    }

    public record RequirementItem(
            Long templateId,             // 저장 때 이 id 로 체크리스트 생성
            RequirementKind kind,
            String label,
            String formUrl,
            String description
    ) {
    }
}
