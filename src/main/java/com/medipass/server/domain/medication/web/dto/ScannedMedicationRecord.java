package com.medipass.server.domain.medication.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medipass.server.domain.medication.entity.DoseUnit;

import java.math.BigDecimal;

public record ScannedMedicationRecord(
        @JsonIgnore String ocrProductText,
        String mfdsProductCode,
        String productKoName,
        String productEnName,
        Integer intakesPerDay,
        Integer totalDays,
        BigDecimal dosePerIntake,
        DoseUnit doseUnit
) {
}
