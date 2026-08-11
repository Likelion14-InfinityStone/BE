package com.medipass.server.domain.medication.dto.response;

import java.math.BigDecimal;

public record ScannedMedication(
        String ocrProductText,
        Integer intakesPerDay,
        Integer totalDays,
        BigDecimal dosePerIntake,
        String doseUnit
) {
}
