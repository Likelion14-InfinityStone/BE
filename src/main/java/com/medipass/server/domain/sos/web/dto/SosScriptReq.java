package com.medipass.server.domain.sos.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 현지어 설명문 번역 요청
 * 원문은 프론트가 상황별 템플릿으로 완성해서 보낸다 (서버는 템플릿을 갖고 있지 않다)
 */
public record SosScriptReq(

        @NotNull(message = "여행을 선택해주세요")
        Long tripId, // 도착 국가의 언어로 번역한다

        @NotBlank(message = "번역할 문장을 입력해주세요")
        @Size(max = 2000, message = "문장은 2000자 이내여야 합니다")
        String text
) {
}
