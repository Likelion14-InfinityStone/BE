package com.medipass.server.domain.document.service;

import com.medipass.server.domain.document.entity.Document;
import com.medipass.server.domain.document.repository.DocumentRepository;
import com.medipass.server.domain.document.web.dto.DocumentDownloadRes;
import com.medipass.server.domain.document.web.dto.DocumentMainRes;
import com.medipass.server.domain.document.web.dto.DocumentPreviewRes;
import com.medipass.server.domain.document.web.dto.MedicationDocumentListRes;
import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.medication.exception.MedicationNotFoundException;
import com.medipass.server.domain.medication.repository.MedicationRepository;
import com.medipass.server.domain.regulation.entity.RequirementKind;
import com.medipass.server.domain.trip.entity.ChecklistItem;
import com.medipass.server.domain.trip.repository.ChecklistItemRepository;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;
import com.medipass.server.global.s3.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

// 서류함 조회와 임시 접근 URL 발급을 담당한다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentQueryService {

    private final MedicationRepository medicationRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final DocumentRepository documentRepository;
    private final S3StorageService s3StorageService;

    // 전체 서류 준비 현황과 서류가 필요한 약품 목록을 조회한다.
    public DocumentMainRes getMain(Long userId) {
        List<ChecklistItem> checklistItems = checklistItemRepository
                .findByTripMedication_Trip_User_IdAndRequirementTemplate_KindOrderByTripMedication_Medication_CreatedAtDesc(
                        userId,
                        RequirementKind.UPLOAD
                );

        long registeredCount = checklistItems.stream()
                .filter(item -> item.getDocument() != null)
                .count();

        var medications = new LinkedHashMap<Long, DocumentMainRes.MedicationItem>();
        checklistItems.forEach(item -> {
            Medication medication = item.getTripMedication().getMedication();
            medications.putIfAbsent(
                    medication.getId(),
                    new DocumentMainRes.MedicationItem(
                            medication.getId(),
                            medication.getProduct().getProductKoName()
                    )
            );
        });

        return new DocumentMainRes(
                new DocumentMainRes.Summary(
                        checklistItems.size(),
                        registeredCount,
                        checklistItems.size() - registeredCount
                ),
                List.copyOf(medications.values())
        );
    }

    // 선택한 약의 등록·미등록 서류 목록을 조회한다.
    public MedicationDocumentListRes getByMedication(Long userId, Long medicationId) {
        Medication medication = medicationRepository.findByIdAndUser_Id(medicationId, userId)
                .orElseThrow(MedicationNotFoundException::new);

        List<MedicationDocumentListRes.Item> documents = checklistItemRepository
                .findByTripMedication_Medication_IdAndTripMedication_Medication_User_IdAndRequirementTemplate_Kind(
                        medicationId,
                        userId,
                        RequirementKind.UPLOAD
                )
                .stream()
                .sorted(Comparator
                        .comparing((ChecklistItem item) -> item.getDocument() == null)
                        .thenComparing(ChecklistItem::getId))
                .map(MedicationDocumentListRes.Item::from)
                .toList();

        return new MedicationDocumentListRes(
                medication.getId(),
                medication.getProduct().getProductKoName(),
                documents
        );
    }

    // 서류 정보와 PDF 미리보기 URL을 반환한다.
    public DocumentPreviewRes getPreview(Long userId, Long documentId) {
        Document document = getOwnedDocument(userId, documentId);
        return DocumentPreviewRes.from(
                document,
                s3StorageService.createPreviewUrl(
                        document.getObjectKey(),
                        document.getOriginalFilename()
                )
        );
    }

    // 원본 파일명으로 내려받는 다운로드 URL을 반환한다.
    public DocumentDownloadRes getDownload(Long userId, Long documentId) {
        Document document = getOwnedDocument(userId, documentId);
        return new DocumentDownloadRes(
                document.getId(),
                document.getOriginalFilename(),
                s3StorageService.createDownloadUrl(
                        document.getObjectKey(),
                        document.getOriginalFilename()
                )
        );
    }

    // 사용자 소유 여행에 등록된 서류인지 확인한다.
    private Document getOwnedDocument(Long userId, Long documentId) {
        return documentRepository
                .findByIdAndChecklistItem_TripMedication_Trip_User_Id(documentId, userId)
                .orElseThrow(() -> new BaseException(
                        ErrorResponseCode.NOT_FOUND_RESOURCE,
                        "서류를 찾을 수 없습니다."
                ));
    }
}
