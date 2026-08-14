package com.medipass.server.domain.trip.util;

import java.math.BigDecimal;

/**
 * 반입 총량 계산 — 일수 × 하루 복용횟수 × 1회 복용량
 */
public final class DosageCalculator {

    private DosageCalculator() {
    }

    // 총 복용 정수 (복용정보가 없으면 null → 총량 계산 불가)
    public static BigDecimal totalUnits(Integer intakesPerDay, BigDecimal dosePerIntake, int carryDays) {
        if (intakesPerDay == null || dosePerIntake == null) {
            return null;
        }
        return BigDecimal.valueOf(carryDays)
                .multiply(BigDecimal.valueOf(intakesPerDay))
                .multiply(dosePerIntake);
    }
}
