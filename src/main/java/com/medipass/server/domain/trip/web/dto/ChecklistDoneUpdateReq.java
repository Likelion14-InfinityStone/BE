package com.medipass.server.domain.trip.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 체크리스트 항목 체크(완료) 토글 — 체크/해제를 명시적으로 지정
 */
public record ChecklistDoneUpdateReq(

        @NotNull(message = "완료 여부는 필수입니다")
        Boolean done
) {
}
