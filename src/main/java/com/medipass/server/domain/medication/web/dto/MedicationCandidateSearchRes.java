package com.medipass.server.domain.medication.web.dto;

public record MedicationCandidateSearchRes(
        String searchKeyword,
        int totalCount,
        MedicationCandidateRecord matchedProduct
) {
}
