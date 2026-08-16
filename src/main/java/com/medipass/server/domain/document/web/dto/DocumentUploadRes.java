package com.medipass.server.domain.document.web.dto;

import com.medipass.server.domain.document.entity.Document;
import com.medipass.server.domain.document.entity.DocumentType;

import java.time.LocalDateTime;

public record DocumentUploadRes(
        Long documentId,
        Long checklistItemId,
        DocumentType type,
        String originalFilename,
        long fileSize,
        LocalDateTime uploadedAt
) {
    public static DocumentUploadRes from(Document document) {
        return new DocumentUploadRes(
                document.getId(),
                document.getChecklistItem().getId(),
                document.getType(),
                document.getOriginalFilename(),
                document.getFileSize(),
                document.getCreatedAt()
        );
    }
}
