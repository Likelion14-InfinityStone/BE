package com.medipass.server.domain.document.entity;

import com.medipass.server.domain.trip.entity.ChecklistItem;
import com.medipass.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** S3에 저장된 체크리스트 서류의 메타데이터 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "document",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_document_checklist_item",
                columnNames = "checklist_item_id"
        )
)
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 서류가 완료 처리하는 체크리스트 항목
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_item_id", nullable = false)
    private ChecklistItem checklistItem;

    // 서류함에서 구분할 서류 종류
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private DocumentType type;

    // S3 객체 식별자
    @Column(name = "object_key", nullable = false, unique = true, length = 500)
    private String objectKey;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    public static Document of(
            ChecklistItem checklistItem,
            DocumentType type,
            String objectKey,
            String originalFilename,
            String contentType,
            long fileSize
    ) {
        return Document.builder()
                .checklistItem(checklistItem)
                .type(type)
                .objectKey(objectKey)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .fileSize(fileSize)
                .build();
    }
}
