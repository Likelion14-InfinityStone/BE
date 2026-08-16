package com.medipass.server.domain.document.web.dto;

import com.medipass.server.domain.document.entity.Document;
import com.medipass.server.domain.document.entity.DocumentType;
import com.medipass.server.domain.trip.entity.ChecklistItem;

import java.time.LocalDate;
import java.util.List;

public record MedicationDocumentListRes(
        Long medicationId,
        String productKoName,
        List<Item> documents
) {
    public record Item(
            Long checklistItemId,
            Long documentId,
            DocumentType type,
            String title,
            boolean registered,
            LocalDate registeredOn
    ) {
        public static Item from(ChecklistItem checklistItem) {
            Document document = checklistItem.getDocument();
            return new Item(
                    checklistItem.getId(),
                    document == null ? null : document.getId(),
                    document == null ? null : document.getType(),
                    checklistItem.getRequirementTemplate().getLabel(),
                    document != null,
                    document == null ? null : document.getCreatedAt().toLocalDate()
            );
        }
    }
}
