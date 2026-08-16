package com.medipass.server.domain.document.service;

import com.medipass.server.domain.document.entity.Document;
import com.medipass.server.domain.document.entity.DocumentType;
import com.medipass.server.domain.document.exception.DocumentErrorCode;
import com.medipass.server.domain.document.exception.DocumentException;
import com.medipass.server.domain.document.repository.DocumentRepository;
import com.medipass.server.domain.document.web.dto.DocumentUploadRes;
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
import java.util.Locale;
import java.util.Set;

// 서류 업로드·삭제 등 데이터 변경 작업을 담당한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCommandService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String PDF_EXTENSION = "pdf";
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final Set<DocumentType> UPLOADABLE_TYPES = Set.of(
            DocumentType.EN_PRESCRIPTION,
            DocumentType.DOCTOR_NOTE
    );

    private final TripRepository tripRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final DocumentRepository documentRepository;
    private final S3StorageService s3StorageService;

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

        // S3 업로드 후 서류 정보와 체크리스트 완료 상태를 저장한다.
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

    // 업로드 가능한 체크리스트·서류 종류인지 확인한다.
    private void validateUploadTarget(ChecklistItem checklistItem, DocumentType type) {
        if (checklistItem.getRequirementTemplate().getKind() != RequirementKind.UPLOAD
                || !UPLOADABLE_TYPES.contains(type)) {
            throw new BaseException(
                    ErrorResponseCode.BAD_REQUEST,
                    "해당 체크리스트 항목에는 서류를 업로드할 수 없습니다."
            );
        }
        if (checklistItem.getDocument() != null
                || documentRepository.existsByChecklistItem_Id(checklistItem.getId())) {
            throw new DocumentException(DocumentErrorCode.ALREADY_EXISTS);
        }
    }

    // 파일 크기, 확장자, Content-Type, PDF 시그니처를 검증한다.
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

    // 경로 문자를 제거하고 저장 가능한 파일명인지 확인한다.
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

    // 파일 내용이 PDF 시그니처(%PDF-)로 시작하는지 확인한다.
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

    // DB 저장 실패 시 먼저 업로드된 S3 객체를 정리한다.
    private void cleanupUploadedObject(String objectKey) {
        try {
            s3StorageService.delete(objectKey);
        } catch (RuntimeException cleanupException) {
            log.error("서류 저장 실패 후 S3 객체 정리에 실패했습니다. 객체 키={}",
                    objectKey, cleanupException);
        }
    }
}
