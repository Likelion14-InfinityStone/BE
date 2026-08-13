package com.medipass.server.domain.medication.web.dto;

public record MedicationCandidateRecord(
        String mfdsProductCode,
        String productKoName,
        String productEnName
) {
}
