package com.medipass.server.domain.regulation.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * 판정 요청, 도착 국가 + 성분 목록
 * 성분(inn)은 식약처 쪽에서 넘겨받는 값, amountMg 는 소지량(mg, 선택)
 */
public record JudgeReq(

        @NotBlank(message = "국가 코드는 필수입니다")
        String country,

        @NotEmpty(message = "성분 목록은 비어 있을 수 없습니다")
        @Valid
        List<Ingredient> ingredients
) {
    public record Ingredient(

            @NotBlank(message = "성분명은 필수입니다")
            String inn,

            @PositiveOrZero(message = "소지량은 0 이상이어야 합니다")
            BigDecimal amountMg // 선택 (null 허용)
    ) {
    }
}
