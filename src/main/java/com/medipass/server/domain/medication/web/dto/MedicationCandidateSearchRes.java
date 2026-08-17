package com.medipass.server.domain.medication.web.dto;

import java.util.List;

public record MedicationCandidateSearchRes(
        String searchKeyword,
        int totalCount,
        List<MedicationCandidateRecord> candidates
) {
}
