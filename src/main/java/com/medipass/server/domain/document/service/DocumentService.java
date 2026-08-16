package com.medipass.server.domain.document.service;

import com.medipass.server.domain.document.entity.Document;
import com.medipass.server.domain.document.entity.DocumentType;
import com.medipass.server.domain.document.exception.DocumentErrorCode;
import com.medipass.server.domain.document.exception.DocumentException;
import com.medipass.server.domain.document.repository.DocumentRepository;
import com.medipass.server.domain.document.web.dto.DocumentMainRes;
import com.medipass.server.domain.document.web.dto.DocumentPreviewRes;
import com.medipass.server.domain.document.web.dto.MedicationDocumentListRes;
import com.medipass.server.domain.document.web.dto.DocumentUploadRes;
import com.medipass.server.domain.medication.entity.Medication;
import com.medipass.server.domain.medication.exception.MedicationNotFoundException;
import com.medipass.server.domain.medication.repository.MedicationRepository;
import com.medipass.server.domain.regulation.entity.RequirementKind;
import com.medipass.server.domain.trip.entity.ChecklistItem;
import com.medipass.server.domain.trip.entity.Trip;
import com.medipass.server.domain.trip.exception.ChecklistItemNotFoundException;
import com.medipass.server.domain.trip.exception.TripAccessDeniedException;
import com.medipass.server.domain.trip.exception.TripNotFoundException;
import com.medipass.server.domain.trip.repository.ChecklistItemRepository;
import com.medipass.server.domain.trip.repository.TripRepository;
import com.medipass.server.global.exception.BaseException;
import com.medipass.server.global.response.code.ErrorResponseCode;
import com.medipass.server.global.s3.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String PDF_EXTENSION = "pdf";
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final Set<DocumentType> UPLOADABLE_TYPES = Set.of(
            DocumentType.EN_PRESCRIPTION,
            DocumentType.DOCTOR_NOTE
    );

    private final TripRepository tripRepository;
    private final MedicationRepository medicationRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final DocumentRepository documentRepository;
    private final S3StorageService s3StorageService;

    // 사용자의 서류 준비 현황과 약품 목록을 조회한다.
    @Transactional(readOnly = true)
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
            var medication = item.getTripMedication().getMedication();
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
    @Transactional(readOnly = true)
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

    // 서류 정보와 PDF 미리보기 URL을 조회한다.
    @Transactional(readOnly = true)
    public DocumentPreviewRes getPreview(Long userId, Long documentId) {
        Document document = documentRepository
                .findByIdAndChecklistItem_TripMedication_Trip_User_Id(documentId, userId)
                .orElseThrow(() -> new BaseException(
                        ErrorResponseCode.NOT_FOUND_RESOURCE,
                        "서류를 찾을 수 없습니다."
                ));

        return DocumentPreviewRes.from(
                document,
                s3StorageService.createPreviewUrl(
                        document.getObjectKey(),
                        document.getOriginalFilename()
                )
        );
    }

    // 체크리스트에 PDF 서류를 업로드하고 등록 정보를 저장한다.
    @Transactional
    public DocumentUploadRes upload(
            Long userId,
            Long tripId,
            Long tripMedicationId,
            Long checklistItemId,
            DocumentType type,
            MultipartFile file
    ) {
        validateTripOwner(userId, tripId);
        ChecklistItem checklistItem = checklistItemRepository
                .findByIdAndTripMedication_IdAndTripMedication_Trip_Id(
                        checklistItemId, tripMedicationId, tripId)
                .orElseThrow(ChecklistItemNotFoundException::new);

        validateUploadTarget(checklistItem, type);
        String originalFilename = validateFile(file);

        // 파일을 먼저 업로드한 뒤 서류 정보를 DB에 저장한다.
        S3StorageService.UploadResult uploaded = s3StorageService.upload(file, PDF_EXTENSION);
        Document document = Document.of(
                checklistItem,
                type,
                uploaded.objectKey(),
                originalFilename,
                uploaded.contentType(),
                uploaded.size()
        );
        checklistItem.attachDocument(document);

        try {
            return DocumentUploadRes.from(documentRepository.saveAndFlush(document));
        } catch (DataIntegrityViolationException e) {
            cleanupUploadedObject(uploaded.objectKey());
            throw new DocumentException(DocumentErrorCode.ALREADY_EXISTS);
        } catch (RuntimeException e) {
            cleanupUploadedObject(uploaded.objectKey());
            throw e;
        }
    }

    // 요청한 사용자가 해당 여행의 소유자인지 확인한다.
    private void validateTripOwner(Long userId, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(TripNotFoundException::new);
        if (!trip.getUser().getId().equals(userId)) {
            throw new TripAccessDeniedException();
        }
    }

    // 서류 업로드가 가능한 체크리스트인지 확인한다.
    private void validateUploadTarget(ChecklistItem checklistItem, DocumentType type) {
        if (checklistItem.getRequirementTemplate().getKind() != RequirementKind.UPLOAD) {
            throw uploadNotAllowedException();
        }
        if (!UPLOADABLE_TYPES.contains(type)) {
            throw uploadNotAllowedException();
        }
        if (checklistItem.getDocument() != null
                || documentRepository.existsByChecklistItem_Id(checklistItem.getId())) {
            throw new DocumentException(DocumentErrorCode.ALREADY_EXISTS);
        }
    }

    private BaseException uploadNotAllowedException() {
        return new BaseException(
                ErrorResponseCode.BAD_REQUEST,
                "해당 체크리스트 항목에는 서류를 업로드할 수 없습니다."
        );
    }

    // 파일 크기와 PDF 형식을 검증하고 안전한 파일명을 반환한다.
    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentException(DocumentErrorCode.INVALID_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new DocumentException(DocumentErrorCode.FILE_TOO_LARGE);
        }

        String originalFilename = cleanFilename(file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        boolean validExtension = extension != null
                && PDF_EXTENSION.equals(extension.toLowerCase(Locale.ROOT));
        boolean validContentType = PDF_CONTENT_TYPE.equalsIgnoreCase(file.getContentType());
        if (!validExtension || !validContentType || !hasPdfSignature(file)) {
            throw new DocumentException(DocumentErrorCode.INVALID_FILE);
        }
        return originalFilename;
    }

    private String cleanFilename(String originalFilename) {
        String cleaned = StringUtils.cleanPath(
                originalFilename == null || originalFilename.isBlank()
                        ? "document.pdf"
                        : originalFilename
        );
        String filename = StringUtils.getFilename(cleaned);
        if (filename == null || filename.length() > 255) {
            throw new DocumentException(DocumentErrorCode.INVALID_FILE);
        }
        return filename;
    }

    // 파일 내용이 PDF 서명(%PDF-)으로 시작하는지 확인한다.
    private boolean hasPdfSignature(MultipartFile file) {
        byte[] signature = new byte[5];
        try (InputStream inputStream = file.getInputStream()) {
            return inputStream.read(signature) == signature.length
                    && signature[0] == '%'
                    && signature[1] == 'P'
                    && signature[2] == 'D'
                    && signature[3] == 'F'
                    && signature[4] == '-';
        } catch (IOException e) {
            throw new DocumentException(DocumentErrorCode.INVALID_FILE);
        }
    }

    // DB 저장 실패 시 이미 업로드된 S3 파일을 정리한다.
    private void cleanupUploadedObject(String objectKey) {
        try {
            s3StorageService.delete(objectKey);
        } catch (RuntimeException cleanupException) {
            log.error("서류 저장 실패 후 S3 객체 정리에 실패했습니다. 객체 키={}",
                    objectKey, cleanupException);
        }
    }
}
