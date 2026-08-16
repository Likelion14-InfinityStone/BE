package com.medipass.server.domain.document.web.controller;

import com.medipass.server.domain.document.service.DocumentService;
import com.medipass.server.domain.document.web.dto.DocumentMainRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Document", description = "서류 업로드 및 서류함 API")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentCabinetController {

    private final DocumentService documentService;

    @Operation(
            summary = "서류함 메인 조회",
            description = "내 여행의 서류 준비 현황과 서류 체크리스트가 있는 약품 목록을 조회합니다."
    )
    @GetMapping
    public ApiResponse<DocumentMainRes> getMain(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                documentService.getMain(userDetails.getUser().getId())
        );
    }
}
