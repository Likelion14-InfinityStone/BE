package com.medipass.server.domain.document.web.controller;

import com.medipass.server.domain.document.entity.DocumentType;
import com.medipass.server.domain.document.service.DocumentService;
import com.medipass.server.domain.document.web.dto.DocumentUploadRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import com.medipass.server.global.response.code.SuccessResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Document", description = "서류 업로드 및 서류함 API")
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "체크리스트 서류 업로드",
            description = "UPLOAD 체크리스트 항목에 영문 처방전 또는 의사 소견서 PDF를 등록합니다."
    )
    @PostMapping(
            value = "/{tripId}/medications/{tripMedicationId}/checklist/{checklistItemId}/document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DocumentUploadRes> upload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tripId,
            @PathVariable Long tripMedicationId,
            @PathVariable Long checklistItemId,
            @Parameter(description = "EN_PRESCRIPTION 또는 DOCTOR_NOTE", required = true)
            @RequestParam DocumentType type,
            @Parameter(description = "최대 10MB PDF 파일", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        DocumentUploadRes response = documentService.upload(
                userDetails.getUser().getId(),
                tripId,
                tripMedicationId,
                checklistItemId,
                type,
                file
        );
        return ApiResponse.success(SuccessResponseCode.SUCCESS_CREATED, response);
    }
}
