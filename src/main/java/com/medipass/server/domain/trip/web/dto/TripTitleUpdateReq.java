package com.medipass.server.domain.trip.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 여행 이름 수정 요청 — 새 제목 (같은 사용자 안에서 중복 불가)
 */
public record TripTitleUpdateReq(

        @NotBlank(message = "여행 제목은 필수입니다")
        String title
) {
}
