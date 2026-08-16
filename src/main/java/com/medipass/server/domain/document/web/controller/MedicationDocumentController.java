package com.medipass.server.domain.document.web.controller;

import com.medipass.server.domain.document.service.DocumentService;
import com.medipass.server.domain.document.web.dto.MedicationDocumentListRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Document", description = "서류 업로드 및 서류함 API")
@RestController
@RequestMapping("/api/medications/{medicationId}/documents")
@RequiredArgsConstructor
public class MedicationDocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "약품별 서류 목록 조회",
            description = "선택한 약의 등록·미등록 서류 목록을 조회합니다. 날짜는 서류 등록일 기준입니다."
    )
    @GetMapping
    public ApiResponse<MedicationDocumentListRes> getByMedication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long medicationId
    ) {
        return ApiResponse.success(
                documentService.getByMedication(
                        userDetails.getUser().getId(),
                        medicationId
                )
        );
    }
}
