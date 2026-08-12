package com.medipass.server.domain.regulation.web.dto;

import com.medipass.server.domain.regulation.entity.PreparationLevel;
import com.medipass.server.domain.regulation.entity.RequirementKind;

import java.math.BigDecimal;
import java.util.List;

/**
 * 판정 응답, 성분별 판정 결과 목록
 */
public record JudgeRes(
        String country,
        List<IngredientResult> results
) {
    // 성분 1건의 판정 결과
    public record IngredientResult(
            String inn,
            boolean regulated,          // 규제 목록에 있는지
            PreparationLevel level,     // 신호등 (규제 없으면 ALLOWED)
            String categoryCode,        // 원천 분류코드 (규제 있을 때)
            BigDecimal limitMg,         // 반입 허용 한도 (P만, 없으면 null)
            boolean overLimit,          // 소지량이 한도 초과인지 (amountMg 준 경우)
            List<RequirementView> requirements  // 필요 서류 (준비 필요할 때만)
    ) {
    }

    // 필요 서류 1건 (화면 표시용)
    public record RequirementView(
            RequirementKind kind,
            String label,
            String formUrl,
            String description
    ) {
    }
}
