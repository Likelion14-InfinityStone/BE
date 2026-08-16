package com.medipass.server.domain.document.web.controller;

import com.medipass.server.domain.document.service.DocumentCommandService;
import com.medipass.server.domain.document.service.DocumentQueryService;
import com.medipass.server.domain.document.web.dto.DocumentDownloadRes;
import com.medipass.server.domain.document.web.dto.DocumentMainRes;
import com.medipass.server.domain.document.web.dto.DocumentPreviewRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Document", description = "서류 업로드 및 서류함 API")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentCabinetController {

    private final DocumentCommandService documentCommandService;
    private final DocumentQueryService documentQueryService;

    @Operation(
            summary = "서류함 메인 조회",
            description = "내 여행의 서류 준비 현황과 서류 체크리스트가 있는 약품 목록을 조회합니다."
    )
    @GetMapping
    public ApiResponse<DocumentMainRes> getMain(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                documentQueryService.getMain(userDetails.getUser().getId())
        );
    }

    @Operation(
            summary = "서류 보기",
            description = "서류 정보와 PDF를 화면에 표시할 수 있는 임시 미리보기 URL을 조회합니다."
    )
    @GetMapping("/{documentId}")
    public ApiResponse<DocumentPreviewRes> getPreview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long documentId
    ) {
        return ApiResponse.success(
                documentQueryService.getPreview(
                        userDetails.getUser().getId(),
                        documentId
                )
        );
    }

    @Operation(
            summary = "서류 다운로드",
            description = "원본 파일명으로 PDF를 내려받을 수 있는 임시 다운로드 URL을 발급합니다."
    )
    @PostMapping("/{documentId}/download")
    public ApiResponse<DocumentDownloadRes> getDownload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long documentId
    ) {
        return ApiResponse.success(
                documentQueryService.getDownload(
                        userDetails.getUser().getId(),
                        documentId
                )
        );
    }

    @Operation(
            summary = "서류 삭제",
            description = "등록된 서류를 삭제하고 연결된 체크리스트를 미등록 상태로 변경합니다."
    )
    @DeleteMapping("/{documentId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long documentId
    ) {
        documentCommandService.delete(
                userDetails.getUser().getId(),
                documentId
        );
        return ApiResponse.success();
    }
}
