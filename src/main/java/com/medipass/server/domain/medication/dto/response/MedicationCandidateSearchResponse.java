package com.medipass.server.domain.medication.dto.response;

public record MedicationCandidateSearchResponse(
        String searchKeyword,
        int totalCount,
        MedicationCandidateResponse matchedProduct
) {
}
