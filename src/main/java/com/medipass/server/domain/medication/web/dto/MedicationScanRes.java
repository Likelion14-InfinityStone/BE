package com.medipass.server.domain.medication.web.dto;

import java.time.LocalDate;
import java.util.List;

public record MedicationScanRes(
        LocalDate dispensedAt,
        String issuer,
        List<ScannedMedicationRecord> medications
) {
}
