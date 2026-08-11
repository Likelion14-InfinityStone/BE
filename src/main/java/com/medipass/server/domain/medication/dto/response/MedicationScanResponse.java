package com.medipass.server.domain.medication.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MedicationScanResponse(
        LocalDate dispensedAt,
        String issuer,
        List<ScannedMedication> medications
) {
}
