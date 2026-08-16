package com.medipass.server.domain.document.web.dto;

import com.medipass.server.domain.document.entity.Document;
import com.medipass.server.domain.document.entity.DocumentType;
import com.medipass.server.domain.medication.entity.Medication;

import java.net.URI;
import java.time.LocalDate;

public record DocumentPreviewRes(
        Long documentId,
        Long medicationId,
        String productKoName,
        DocumentType type,
        String title,
        String originalFilename,
        long fileSize,
        LocalDate registeredOn,
        URI previewUrl
) {
    public static DocumentPreviewRes from(Document document, URI previewUrl) {
        Medication medication = document.getChecklistItem()
                .getTripMedication()
                .getMedication();

        return new DocumentPreviewRes(
                document.getId(),
                medication.getId(),
                medication.getProduct().getProductKoName(),
                document.getType(),
                document.getChecklistItem().getRequirementTemplate().getLabel(),
                document.getOriginalFilename(),
                document.getFileSize(),
                document.getCreatedAt().toLocalDate(),
                previewUrl
        );
    }
}
