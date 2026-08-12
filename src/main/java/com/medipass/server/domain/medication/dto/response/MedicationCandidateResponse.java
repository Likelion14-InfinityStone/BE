package com.medipass.server.domain.medication.dto.response;

public record MedicationCandidateResponse(
        String mfdsProductCode,
        String productKoName,
        String productEnName,
        String ediCode
) {
}
