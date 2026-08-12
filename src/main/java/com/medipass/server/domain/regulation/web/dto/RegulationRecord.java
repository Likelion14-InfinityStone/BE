package com.medipass.server.domain.regulation.web.dto;

import com.medipass.server.domain.regulation.entity.PreparationLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 나라마다 형식이 다른 규제 데이터를 하나의 공통 형식으로 통일
 * 각 나라 Source가 이 모양으로 바꿔주면, 국가 무관하게 동일 처리
 */
public record RegulationRecord(
        String substanceName,
        String categoryCode,
        PreparationLevel preparationLevel,
        BigDecimal limitMg,
        String sourceUrl,
        LocalDate verifiedDate
) {
}
