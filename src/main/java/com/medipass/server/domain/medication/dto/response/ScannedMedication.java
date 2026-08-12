package com.medipass.server.domain.medication.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public record ScannedMedication(
        @JsonIgnore String ocrProductText,
        String mfdsProductCode,
        String productKoName,
        Integer intakesPerDay,
        Integer totalDays,
        BigDecimal dosePerIntake,
        String doseUnit
) {
}
