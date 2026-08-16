package com.medipass.server.domain.document.web.dto;

import java.util.List;

public record DocumentMainRes(
        Summary summary,
        List<MedicationItem> medications
) {
    public record Summary(
            long totalCount,
            long registeredCount,
            long unregisteredCount
    ) {
    }

    public record MedicationItem(
            Long medicationId,
            String productKoName
    ) {
    }
}
