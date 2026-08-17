package com.medipass.server.domain.sos.web.controller;

import com.medipass.server.domain.sos.service.SosScriptService;
import com.medipass.server.domain.sos.service.SosService;
import com.medipass.server.domain.sos.web.dto.SosContactReq;
import com.medipass.server.domain.sos.web.dto.SosContactsRes;
import com.medipass.server.domain.sos.web.dto.SosScriptReq;
import com.medipass.server.domain.sos.web.dto.SosScriptRes;
import com.medipass.server.global.jwt.CustomUserDetails;
import com.medipass.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sos")
public class SosController {

    private final SosService sosService;
    private final SosScriptService sosScriptService;

    // 결과 화면 연락처 — 현지 신고번호 / 최근접 재외공관 / 의료기관 안내센터
    // 현위치를 바디로 받아야 해서 조회지만 POST 다
    @PostMapping("/contacts")
    public ApiResponse<SosContactsRes> contacts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SosContactReq request
    ) {
        return ApiResponse.success(sosService.getContacts(userDetails.getUser().getId(), request));
    }

    // 현지어 설명문 — 프론트가 만든 한국어 원문을 도착국 언어로 번역
    @PostMapping("/scripts")
    public ApiResponse<SosScriptRes> script(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SosScriptReq request
    ) {
        return ApiResponse.success(sosScriptService.translate(userDetails.getUser().getId(), request));
    }
}
